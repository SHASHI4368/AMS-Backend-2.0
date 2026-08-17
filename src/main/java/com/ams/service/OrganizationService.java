package com.ams.service;

import com.ams.dto.PageResponse;
import com.ams.dto.organization.OrganizationListResponse;
import com.ams.dto.organization.OrganizationRequest;
import com.ams.dto.organization.OrganizationResponse;
import com.ams.dto.organization.UpdateOrganizationRequest;
import com.ams.entity.Membership;
import com.ams.entity.Organization;
import com.ams.entity.User;
import com.ams.enums.*;
import com.ams.exception.ServiceException;
import com.ams.repository.MembershipRepository;
import com.ams.repository.OrganizationRepository;
import com.ams.repository.UserRepository;
import com.ams.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService implements IOrganizationService {
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final ServiceUtil serviceUtil;

    @Override
    @Transactional
    public OrganizationResponse createOrganization(String email, OrganizationRequest request) {
        User owner = serviceUtil.getUser(email);

        Organization organization = Organization.builder()
                .name(request.name())
                .description(request.description())
                .logoUrl(request.logoUrl())
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        organizationRepository.save(organization);

        Membership membership = Membership.builder()
                .user(owner)
                .organization(organization)
                .status(MembershipStatus.ACTIVE)
                .role(OrganizationRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();

        membershipRepository.save(membership);

        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getDescription(),
                organization.getLogoUrl(),
                organization.getCreatedAt(),
                organization.getMemberCount(),
                OrganizationRole.OWNER.name()
        );
    }

    @Override
    @Transactional
    public List<OrganizationResponse> getMyOrganizations(String email) {
        User user = serviceUtil.getUser(email);

        List<Membership> memberships = membershipRepository
                .findByUserAndStatus(user, MembershipStatus.ACTIVE);

        return memberships.stream()
                .map(membership -> {
                    Organization org = membership.getOrganization();
                    return new OrganizationResponse(
                            org.getId(),
                            org.getName(),
                            org.getDescription(),
                            org.getLogoUrl(),
                            org.getCreatedAt(),
                            org.getMemberCount(),
                            membership.getRole().name()
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public OrganizationResponse getOrganizationById(String email, Long organizationId) {
        User user = serviceUtil.getUser(email);

        Organization organization = serviceUtil.getOrganization(organizationId);

        // Check if the user is a member of the organization
        Membership membership = membershipRepository
                .findByUserAndOrganization(
                        user, organization
                )
                .orElseThrow(() ->
                        new ServiceException("You are not a member of this organization with id: " + organizationId)
                );

        // Check if the membership is active
        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new ServiceException("Your membership in this organization is not active with id: " + organizationId);
        }

        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getDescription(),
                organization.getLogoUrl(),
                organization.getCreatedAt(),
                organization.getMemberCount(),
                membership.getRole().name()
        );
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganization(String email, Long organizationId, UpdateOrganizationRequest request) {
        User user = serviceUtil.getUser(email);

        Organization organization = serviceUtil.getOrganization(organizationId);

        // Check if the user has permission to update the organization
        if(!organization.getOwner().getEmail().equals(email)) {
            throw new ServiceException(
                    "You do not have permission to update this organization with id: " + organizationId
            );
        }

        organization.setName(request.name());
        organization.setDescription(request.description());
        organization.setLogoUrl(request.logoUrl());

        organizationRepository.save(organization);

        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getDescription(),
                organization.getLogoUrl(),
                organization.getCreatedAt(),
                organization.getMemberCount(),
                OrganizationRole.OWNER.name()
        );
    }

    @Override
    @Transactional
    public PageResponse<OrganizationListResponse> getAllOrganizations(String email, int page, int size, String name) {
        // Check if the user exists
        User user = serviceUtil.getUser(email);

        // Fetch all organizations with pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Organization> organizationPage = organizationRepository.findByNameContainingIgnoreCase(name, pageable);
        List<Organization> organizations = organizationPage.getContent();

        // If no organizations found, return empty response
        if (organizations.isEmpty()) {
            return new PageResponse<>(
                    List.of(),
                    organizationPage.getNumber(),
                    organizationPage.getSize(),
                    organizationPage.getTotalElements(),
                    organizationPage.getTotalPages(),
                    organizationPage.isLast()
            );
        }

        // Map organizations to OrganizationResponse
        List<OrganizationListResponse> content = organizations.stream()
                .map(org -> {
                    // Check if the user is a member of the organization
                    Membership membership = membershipRepository
                            .findByUserAndOrganization(user, org)
                            .orElse(null);

                    String role = membership != null ? membership.getRole().name() : null;

                    return new OrganizationListResponse(
                            org.getId(),
                            org.getName(),
                            org.getDescription(),
                            org.getLogoUrl(),
                            org.getCreatedAt(),
                            org.getMemberCount(),
                            role,
                            membership != null
                    );
                })
                .toList();

        return new PageResponse<>(
                content,
                organizationPage.getNumber(),
                organizationPage.getSize(),
                organizationPage.getTotalElements(),
                organizationPage.getTotalPages(),
                organizationPage.isLast()
        );


    }


}
