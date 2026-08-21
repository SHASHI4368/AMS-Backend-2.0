package com.ams.service;

import com.ams.dto.PageResponse;
import com.ams.dto.membership.MembershipRequestResponse;
import com.ams.dto.membership.MembershipResponse;
import com.ams.entity.*;
import com.ams.enums.*;
import com.ams.exception.ServiceException;
import com.ams.repository.*;
import com.ams.util.ServiceUtil;
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
public class MembershipService implements  IMembershipService {
    private final MembershipRepository membershipRepository;
    private final ProfileRepository profileRepository;
    private final NotificationService notificationService;
    private final OrganizationActionTokenService organizationActionTokenService;
    private final EmailService emailService;
    private final OrganizationActionTokenRepository organizationActionTokenRepository;
    private final ServiceUtil serviceUtil;
    private final OrganizationActivityService organizationActivityService;

    @Override
    @Transactional
    public List<MembershipRequestResponse> getPendingRequests(String email, Long organizationId) {
        // Get the user by email
        User user = serviceUtil.getUser(email);

        // Get the organization by ID
        Organization organization = serviceUtil.getOrganization(organizationId);

        // Check if the user is the owner of the organization
        if (!organization.getOwner().getId().equals(user.getId())) {
            throw new ServiceException("User is not the owner of the organization");
        }

        // Fetch pending membership requests for the organization
        return membershipRepository
                .findByStatusAndOrganization(
                    MembershipStatus.PENDING,
                    organization
                )
                .stream()
                .map(m -> {
                        Profile profile = profileRepository.findByUser(m.getUser())
                            .orElseThrow(() ->
                                    new ServiceException("Profile not found for user with email: " + m.getUser().getEmail())
                            );
                        return new MembershipRequestResponse(
                            m.getId(),
                            m.getUser().getEmail(),
                            profile.getFirstName() + " " + profile.getLastName(),
                            profile.getAvatarUrl(),
                            m.getRequestedAt()
                        );
                    }
                )
                .toList();

    }

    @Override
    @Transactional
    public void requestToJoinOrganization(String email, Long organizationId, String note) {
        // Check if the user exists
        User user = serviceUtil.getUser(email);

        // Check if the organization exists
        Organization organization = serviceUtil.getOrganization(organizationId);

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
                .requestedAt(LocalDateTime.now())
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
                user.getEmail() + " has requested to join your organization: " + organization.getName()
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

        // Log the activity
        organizationActivityService.logActivity(
                user,
                organization,
                "User " + user.getEmail() + " requested to join the organization",
                OrganizationActivityType.JOIN_REQUEST_SENT
        );
    }

    private void createMembershipNotification(
            User requestor,
            Organization organization,
            OrganizationAction action
    ) {

        if (action == OrganizationAction.ACCEPT) {

            notificationService.create(
                    requestor,
                    NotificationType.ORGANIZATION_JOIN_REQUEST_ACCEPTED,
                    NotificationTargetType.ORGANIZATION,
                    organization.getId(),
                    "Join request accepted",
                    "Your request to join "
                            + organization.getName()
                            + " has been accepted."
            );

        } else {

            notificationService.create(
                    requestor,
                    NotificationType.ORGANIZATION_JOIN_REQUEST_REJECTED,
                    NotificationTargetType.ORGANIZATION,
                    organization.getId(),
                    "Join request rejected",
                    "Your request to join "
                            + organization.getName()
                            + " has been rejected."
            );
        }
    }

    private void informRequestorAboutMembershipResult(
            Membership membership,
            OrganizationAction action
    ) {
        // Create notification for the requestor
        createMembershipNotification(
                membership.getUser(),
                membership.getOrganization(),
                action
        );

        // Send email notification to the requestor
        try{
            emailService.sendMembershipRequestResultEmail(
                    membership.getUser().getEmail(),
                    membership.getOrganization().getName(),
                    action == OrganizationAction.ACCEPT
            );
        }catch (MessagingException ex){
            throw new ServiceException(
                    "Membership was processed, but notification email could not be sent"
            );
        }
    }

    @Override
    @Transactional
    public void processEmailJoinRequestAction(String rawToken, OrganizationAction expectedAction) {
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
            membership.setJoinedAt(LocalDateTime.now());
        } else if(expectedAction == OrganizationAction.REJECT) {
            membership.setStatus(MembershipStatus.REJECTED);
        }

        // Mark the token as used
        actionToken.setUsedAt(LocalDateTime.now());

        membershipRepository.save(membership);
        organizationActionTokenRepository.save(actionToken);

        // Inform the requestor about the result
        informRequestorAboutMembershipResult(membership, expectedAction);

        // Log the activity
        organizationActivityService.logActivity(
                membership.getUser(),
                membership.getOrganization(),
                "Membership request of" + membership.getUser() + " has been " + expectedAction.name().toLowerCase(),
                expectedAction == OrganizationAction.ACCEPT ? OrganizationActivityType.JOIN_REQUEST_ACCEPTED :
                        OrganizationActivityType.JOIN_REQUEST_REJECTED
        );
    }

    @Override
    public void respondToJoinRequest(String ownerEmail, Long membershipId, OrganizationAction action) {
        // Get the membership by ID
        Membership membership = serviceUtil.getMembership(membershipId);

        // Check if the user is the owner of the organization
        if (!membership.getOrganization().getOwner().getEmail().equals(ownerEmail)) {
            throw new ServiceException("You are not the owner of this organization");
        }

        // Check if the membership is still pending
        if (membership.getStatus() != MembershipStatus.PENDING) {
            throw new ServiceException("This membership request is no longer pending");
        }

        // Update the membership status based on the action
        if (action == OrganizationAction.ACCEPT) {
            membership.setStatus(MembershipStatus.ACTIVE);
            membership.setJoinedAt(LocalDateTime.now());
        } else if (action == OrganizationAction.REJECT) {
            membership.setStatus(MembershipStatus.REJECTED);
        }

        // Save the updated membership
        membershipRepository.save(membership);

        // Inform the requestor about the result
        informRequestorAboutMembershipResult(membership, action);

        // Log the activity
        organizationActivityService.logActivity(
                membership.getUser(),
                membership.getOrganization(),
                "Membership request of" + membership.getUser() + " has been " + action.name().toLowerCase(),
                action == OrganizationAction.ACCEPT ? OrganizationActivityType.JOIN_REQUEST_ACCEPTED :
                        OrganizationActivityType.JOIN_REQUEST_REJECTED
        );

    }

    @Override
    @Transactional
    public PageResponse<MembershipResponse> getOrganizationMembers(
            String email,
            Long organizationId,
            String search,
            int page,
            int size
    ) {
        // Check if the user exists
        User user = serviceUtil.getUser(email);

        // Check if the organization exists
        Organization organization = serviceUtil.getOrganization(organizationId);

        // Check if the user is a member of the organization
        Optional<Membership> membershipOpt = membershipRepository.findByUserAndOrganization(user, organization);
        if (membershipOpt.isEmpty() || membershipOpt.get().getStatus() != MembershipStatus.ACTIVE) {
            throw new ServiceException("You are not a member of this organization");
        }

        // Fetch organization members with pagination
        String normalizedSearch = search == null ? "" : search.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "joinedAt"));
        Page<Membership> membershipPage = membershipRepository
                .findOrganizationMembers(organizationId, MembershipStatus.ACTIVE, normalizedSearch, pageable);
        List<Membership> memberships = membershipPage.getContent();

        // If no members are found, return an empty page response
        if (memberships.isEmpty()) {
            return new PageResponse<>(
                    List.of(),
                    membershipPage.getNumber(),
                    membershipPage.getSize(),
                    membershipPage.getTotalElements(),
                    membershipPage.getTotalPages(),
                    membershipPage.isLast()
            );
        }

        // Map memberships to MembershipResponse DTOs
        List<MembershipResponse> membershipResponses = memberships.stream()
                .map(m -> {
                    Profile profile = serviceUtil.getProfileByUser(m.getUser());
                    return new MembershipResponse(
                            m.getId(),
                            m.getUser().getEmail(),
                            profile.getFirstName(),
                            profile.getLastName(),
                            profile.getTelephone(),
                            profile.getAvatarUrl(),
                            m.getJoinedAt(),
                            m.getRole().name()
                    );
                }).toList();

        return new PageResponse<>(
                membershipResponses,
                membershipPage.getNumber(),
                membershipPage.getSize(),
                membershipPage.getTotalElements(),
                membershipPage.getTotalPages(),
                membershipPage.isLast()
        );

    }
}
