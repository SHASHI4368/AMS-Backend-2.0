package com.ams.service;

import com.ams.dto.OrganizationRequest;
import com.ams.dto.OrganizationResponse;
import com.ams.entity.Membership;
import com.ams.entity.Organization;
import com.ams.entity.User;
import com.ams.enums.MembershipStatus;
import com.ams.enums.OrganizationRole;
import com.ams.exception.ServiceException;
import com.ams.repository.MembershipRepository;
import com.ams.repository.OrganizationRepository;
import com.ams.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    @Override
    @Transactional
    public OrganizationResponse createOrganization(String email, OrganizationRequest request) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ServiceException("User not found with email: " + email)
                );

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
    public List<OrganizationResponse> getMyOrganizations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ServiceException("User not found with email: " + email)
                );

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
    public OrganizationResponse getOrganizationById(String email, Long organizationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ServiceException("User not found with email: " + email)
                );

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() ->
                        new ServiceException("Organization not found with id: " + organizationId)
                );

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
}
