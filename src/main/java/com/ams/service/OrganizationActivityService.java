package com.ams.service;

import com.ams.dto.PageResponse;
import com.ams.dto.organization.OrganizationActivityResponse;
import com.ams.entity.Organization;
import com.ams.entity.OrganizationActivity;
import com.ams.entity.User;
import com.ams.enums.OrganizationActivityType;
import com.ams.repository.OrganizationActivityRepository;
import com.ams.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationActivityService implements  IOrganizationActivityService {
    private final OrganizationActivityRepository organizationActivityRepository;
    private final ServiceUtil serviceUtil;

    @Override
    public void logActivity(
            User actor,
            Organization organization,
            String description,
            OrganizationActivityType activityType
    ) {
        OrganizationActivity activity = OrganizationActivity.builder()
                .organization(organization)
                .actor(actor)
                .description(description)
                .activityType(activityType)
                .build();

        organizationActivityRepository.save(activity);
    }

    @Override
    @Transactional
    public PageResponse<OrganizationActivityResponse> getOrganizationActivity(String email, Long organizationId, int page, int size) {
        // Check if the user exists
        User user = serviceUtil.getUser(email);

        // Check if the organization exists
        Organization organization = serviceUtil.getOrganization(organizationId);

        // Check if the user is the owner of the organization
        serviceUtil.isUserOwnerOfOrganization(user,organization);

        // Fetch the organization activities with pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrganizationActivity> organizationActivityPage = organizationActivityRepository
                .findByOrganization(organization, pageable);
        List<OrganizationActivity> organizationActivityList = organizationActivityPage.getContent();

        // If the list is empty, return an empty PageResponse
        if (organizationActivityList.isEmpty()) {
            return new PageResponse<>(
                    List.of(),
                    organizationActivityPage.getNumber(),
                    organizationActivityPage.getSize(),
                    organizationActivityPage.getTotalElements(),
                    organizationActivityPage.getTotalPages(),
                    organizationActivityPage.isLast()
            );
        }

        // Map OrganizationActivity entities to DTOs
        List<OrganizationActivityResponse> content = organizationActivityList.stream()
                .map(activity -> new OrganizationActivityResponse(
                        activity.getId(),
                        activity.getActivityType().name(),
                        serviceUtil.getUserName(user),
                        activity.getDescription(),
                        activity.getCreatedAt()
                ))
                .toList();

        return new PageResponse<>(
                content,
                organizationActivityPage.getNumber(),
                organizationActivityPage.getSize(),
                organizationActivityPage.getTotalElements(),
                organizationActivityPage.getTotalPages(),
                organizationActivityPage.isLast()
        );

    }
}

