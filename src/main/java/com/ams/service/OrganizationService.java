package com.ams.service;

import com.ams.dto.PageResponse;
import com.ams.dto.organization.OrganizationListResponse;
import com.ams.dto.organization.OrganizationRequest;
import com.ams.dto.organization.OrganizationResponse;
import com.ams.dto.organization.UpdateOrganizationRequest;
import com.ams.entity.Membership;
import com.ams.entity.Organization;
import com.ams.entity.OrganizationActionToken;
import com.ams.entity.User;
import com.ams.enums.*;
import com.ams.exception.ServiceException;
import com.ams.repository.MembershipRepository;
import com.ams.repository.OrganizationActionTokenRepository;
import com.ams.repository.OrganizationRepository;
import com.ams.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService implements IOrganizationService {
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final OrganizationActionTokenService organizationActionTokenService;
    private final EmailService emailService;
    private final OrganizationActionTokenRepository organizationActionTokenRepository;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ServiceException("User not found with email: " + email)
                );
    }

    private Organization getOrganization(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() ->
                        new ServiceException("Organization not found with id: " + organizationId)
                );
    }

    @Override
    @Transactional
    public OrganizationResponse createOrganization(String email, OrganizationRequest request) {
        User owner = getUser(email);

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
    @Transactional
    public List<OrganizationResponse> getMyOrganizations(String email) {
        User user = getUser(email);

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
        User user = getUser(email);

        Organization organization = getOrganization(organizationId);

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
        User user = getUser(email);

        Organization organization = getOrganization(organizationId);

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
        User user = getUser(email);

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

    @Override
    @Transactional
    public void requestToJoinOrganization(String email, Long organizationId, String note) {
        // Check if the user exists
        User user = getUser(email);

        // Check if the organization exists
        Organization organization = getOrganization(organizationId);

        // Check if the user is already a member of the organization
        Optional<Membership> existingMembership =  membershipRepository
                .findByUserAndOrganization(user, organization);

        if (existingMembership.isPresent()) {
            Membership membership = existingMembership.get();

            if (membership.getStatus() == MembershipStatus.ACTIVE) {
                throw new ServiceException("You are already a member of this organization with id: " + organizationId);
            } else if (membership.getStatus() == MembershipStatus.PENDING) {
                throw new ServiceException("Your membership request is already pending for this organization with id: " + organizationId);
            } else if (membership.getStatus() == MembershipStatus.REJECTED) {
                throw new ServiceException("Your membership request has been rejected for this organization with id: " + organizationId);
            }
        }

        // Create a new membership
        Membership membership = Membership.builder()
                .user(user)
                .organization(organization)
                .status(MembershipStatus.PENDING)
                .role(OrganizationRole.MEMBER)
                .build();

        membershipRepository.save(membership);

        // Create notification for the organization owner
        User owner = organization.getOwner();

        notificationService.create(
                owner,
                NotificationType.ORGANIZATION_JOIN_REQUEST,
                NotificationTargetType.ORGANIZATION,
                organization.getId(),
                "Organization Join Request",
                "User " + user.getEmail() + " has requested to join your organization: " + organization.getName()
        );

        // Create emai action tokens
        String acceptToken = organizationActionTokenService.generateToken(
                membership,
                OrganizationAction.ACCEPT
        );

        String rejectToken = organizationActionTokenService.generateToken(
                membership,
                OrganizationAction.REJECT
        );

        // Send email notification to the organization owner
        try{
            emailService.sendOrganizationJoinRequestEmail(
                    owner.getEmail(),
                    user,
                    note,
                    organization,
                    acceptToken,
                    rejectToken
            );
        }catch (MessagingException ex){
            throw new ServiceException(
                    "Failed to send email notification to the organization owner: " + ex.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public void processEmailAction(String rawToken, OrganizationAction expectedAction) {
        // Hash the raw token
        String hashedToken = organizationActionTokenService.hashToken(rawToken);

        // Check if the token exists
        OrganizationActionToken actionToken = organizationActionTokenRepository
                .findByTokenHash(hashedToken)
                .orElseThrow(() ->
                        new ServiceException("Invalid or expired token")
                );

        // Check if the token has already used
        if(actionToken.getUsedAt() != null){
            throw new ServiceException("This token has already been used");
        }

        // Check if the token is expired
        if(actionToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ServiceException("This token has expired");
        }

        // Check if the action matches the expected action
        if(actionToken.getAction() != expectedAction) {
            throw new ServiceException("This token is not valid for the expected action");
        }

        // Get the membership associated with the token
        Membership membership = actionToken.getMembership();

        // Check if the membership is still pending
        if(membership.getStatus() != MembershipStatus.PENDING) {
            throw new ServiceException("This membership is no longer pending");
        }

        // Update the membership status based on the action
        if(expectedAction == OrganizationAction.ACCEPT) {
            membership.setStatus(MembershipStatus.ACTIVE);
        } else if(expectedAction == OrganizationAction.REJECT) {
            membership.setStatus(MembershipStatus.REJECTED);
        }

        // Mark the token as used
        actionToken.setUsedAt(LocalDateTime.now());

        membershipRepository.save(membership);
        organizationActionTokenRepository.save(actionToken);

    }
}
