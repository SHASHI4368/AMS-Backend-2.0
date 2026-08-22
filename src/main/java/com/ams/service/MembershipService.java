package com.ams.service;

import com.ams.dto.PageResponse;
import com.ams.dto.membership.MembershipInvitationResponse;
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
public class MembershipService implements IMembershipService {
    private final MembershipRepository membershipRepository;
    private final ProfileRepository profileRepository;
    private final NotificationService notificationService;
    private final OrganizationActionTokenService organizationActionTokenService;
    private final EmailService emailService;
    private final OrganizationActionTokenRepository organizationActionTokenRepository;
    private final ServiceUtil serviceUtil;
    private final OrganizationActivityService organizationActivityService;

    private void checkIfAnActiveMemberOfOrganization(User user, Organization organization) {
        Optional<Membership> existingMembership = membershipRepository.findByUserAndOrganization(user, organization);
        if (existingMembership.isPresent()) {
            Membership membership = existingMembership.get();

            if (membership.getStatus() == MembershipStatus.ACTIVE) {
                throw new ServiceException("You are already a member of this organization with id: " + organization.getName());
            } else if (membership.getStatus() == MembershipStatus.PENDING) {
                throw new ServiceException("Your membership request is already pending for this organization with id: " + organization.getName());
            } else if (membership.getStatus() == MembershipStatus.REJECTED) {
                throw new ServiceException("Your membership request has been rejected for this organization with id: " + organization.getName());
            }
        }
    }

    private Membership updateMembershipStatus(Membership membership, OrganizationAction expectedAction) {
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

        return membership;
    }

    private Membership processMembershipWithToken(String rawToken, OrganizationAction expectedAction) {
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

        // Update the membership status based on the action
        Membership updatedMembership = updateMembershipStatus(membership, expectedAction);

        // Mark the token as used
        actionToken.setUsedAt(LocalDateTime.now());

        membershipRepository.save(updatedMembership);
        organizationActionTokenRepository.save(actionToken);

        return membership;
    }

    private Membership processMembershipWithAction(Membership membership, OrganizationAction action) {
        // Update the membership status based on the action
        Membership updatedMembership = updateMembershipStatus(membership, action);

        membershipRepository.save(updatedMembership);

        return membership;
    }

    private void createInvitationNotification(
            User owner,
            String member,
            Organization organization,
            OrganizationAction action
    ) {

        if (action == OrganizationAction.ACCEPT) {

            notificationService.create(
                    owner,
                    NotificationType.ORGANIZATION_INVITATION_ACCEPTED,
                    NotificationTargetType.ORGANIZATION,
                    organization.getId(),
                    "Invitation accepted",
                    "Your invitation to "
                            + member + " for organization "
                            + organization.getName()
                            + " has been accepted."
            );

        } else {

            notificationService.create(
                    owner,
                    NotificationType.ORGANIZATION_INVITATION_REJECTED,
                    NotificationTargetType.ORGANIZATION,
                    organization.getId(),
                    "Invitation rejected",
                    "Your invitation to "
                            + organization.getName()
                            + " has been rejected."
            );
        }
    }

    private void informOwnerAboutInvitationResult(
            Membership membership,
            OrganizationAction action
    ) {
        // Create notification for the owner
        createInvitationNotification(
                membership.getOrganization().getOwner(),
                serviceUtil.getUserName(membership.getUser()),
                membership.getOrganization(),
                action
        );

        // Send email notification to the owner
        try{
            emailService.sendInvitationResultEmail(
                    membership.getOrganization().getOwner().getEmail(),
                    membership.getOrganization().getName(),
                    action == OrganizationAction.ACCEPT
            );
        }catch (MessagingException ex){
            throw new ServiceException(
                    "Membership was processed, but notification email could not be sent"
            );
        }
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
    public List<MembershipRequestResponse> getPendingRequests(String email, Long organizationId) {
        // Get the user by email
        User user = serviceUtil.getUser(email);

        // Get the organization by ID
        Organization organization = serviceUtil.getOrganization(organizationId);

        // Check if the user is the owner of the organization
        serviceUtil.isUserOwnerOfOrganization(user, organization);

        // Fetch pending membership requests for the organization
        return membershipRepository
                .findByStatusAndOrganization(
                    MembershipStatus.PENDING,
                    organization
                )
                .stream()
                .filter(m -> m.getRequestedAt() != null) // Filter only membership requests (not invitations)
                .map(m -> {
                        Profile profile = profileRepository.findByUser(m.getUser())
                            .orElseThrow(() ->
                                    new ServiceException("Profile not found for user with email: " + m.getUser().getEmail())
                            );
                        return new MembershipRequestResponse(
                            m.getId(),
                            m.getUser().getEmail(),
                            serviceUtil.getUserName(m.getUser()),
                            profile.getAvatarUrl(),
                            m.getRequestedAt()
                        );
                    }
                )
                .toList();

    }

    @Override
    public List<MembershipInvitationResponse> getPendingInvitations(String email, Long organizationId) {
        // Get the user by email
        User user = serviceUtil.getUser(email);

        // Get the organization by ID
        Organization organization = serviceUtil.getOrganization(organizationId);

        // Check if the user is the owner of the organization
        serviceUtil.isUserOwnerOfOrganization(user, organization);

        // Fetch pending membership requests for the organization
        return membershipRepository
                .findByStatusAndOrganization(
                        MembershipStatus.PENDING,
                        organization
                )
                .stream()
                .filter(m -> m.getInvitedAt() != null) // Filter only membership invitations (not requests)
                .map(m -> {
                            Profile profile = profileRepository.findByUser(m.getUser())
                                    .orElseThrow(() ->
                                            new ServiceException("Profile not found for user with email: " + m.getUser().getEmail())
                                    );
                            return new MembershipInvitationResponse(
                                    m.getId(),
                                    m.getUser().getEmail(),
                                    serviceUtil.getUserName(m.getUser()),
                                    profile.getAvatarUrl(),
                                    m.getInvitedAt()
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
        checkIfAnActiveMemberOfOrganization(user, organization);

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

    @Override
    @Transactional
    public void inviteToJoinOrganization(String email, Long organizationId, Long userId, String note) {
        // Check if the user exists
        User owner = serviceUtil.getUser(email);

        // Check if the organization exists
        Organization organization = serviceUtil.getOrganization(organizationId);

        // Check if the user is the owner of the organization
        serviceUtil.isUserOwnerOfOrganization(owner, organization);

        // Check if the invited user exists
        User invitedUser = serviceUtil.getUser(userId);

        // Check if the user is already a member of the organization
        checkIfAnActiveMemberOfOrganization(invitedUser, organization);

        // Create a new membership
        Membership membership = Membership.builder()
                .user(invitedUser)
                .organization(organization)
                .status(MembershipStatus.PENDING)
                .role(OrganizationRole.MEMBER)
                .invitedAt(LocalDateTime.now())
                .build();

        membershipRepository.save(membership);

        // Create notification for the invited user
        notificationService.create(
                invitedUser,
                NotificationType.ORGANIZATION_INVITATION,
                NotificationTargetType.ORGANIZATION,
                organization.getId(),
                "Organization Invitation",
                "You have been invited to join the organization: " + organization.getName()
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

        // Send email notification to the invited user
        try{
            emailService.sendOrganizationInvitationEmail(
                    invitedUser.getEmail(),
                    owner,
                    note,
                    organization,
                    acceptToken,
                    rejectToken
            );
        }catch (MessagingException ex) {
            throw new ServiceException(
                    "Failed to send email notification to the invited user: " + ex.getMessage()
            );
        }

        // Log the activity
        organizationActivityService.logActivity(
                owner,
                organization,
                "User " + invitedUser.getEmail() + " has been invited to join the organization",
                OrganizationActivityType.MEMBER_INVITED
        );
    }

    @Override
    @Transactional
    public void processEmailJoinRequestAction(String rawToken, OrganizationAction expectedAction) {

        // Process the token and update the membership status
        Membership updatedMembership = processMembershipWithToken(rawToken, expectedAction);

        // Inform the requestor about the result
        informRequestorAboutMembershipResult(updatedMembership, expectedAction);

        // Log the activity
        organizationActivityService.logActivity(
                updatedMembership.getUser(),
                updatedMembership.getOrganization(),
                "Membership request of" + updatedMembership.getUser() + " has been " + expectedAction.name().toLowerCase(),
                expectedAction == OrganizationAction.ACCEPT ? OrganizationActivityType.JOIN_REQUEST_ACCEPTED :
                        OrganizationActivityType.JOIN_REQUEST_REJECTED
        );
    }

    @Override
    @Transactional
    public void respondToJoinRequest(String ownerEmail, Long membershipId, OrganizationAction action) {
        // Get the membership by ID
        Membership membership = serviceUtil.getMembership(membershipId);

        // Check if the owner is the owner of the organization
        serviceUtil.isUserOwnerOfOrganization(serviceUtil.getUser(ownerEmail), membership.getOrganization());

        // Process the membership and update the status
        Membership updatedMembership = processMembershipWithAction(membership, action);

        // Inform the requestor about the result
        informRequestorAboutMembershipResult(updatedMembership, action);

        // Log the activity
        organizationActivityService.logActivity(
                updatedMembership.getUser(),
                updatedMembership.getOrganization(),
                "Membership request of" + updatedMembership.getUser() + " has been " + action.name().toLowerCase(),
                action == OrganizationAction.ACCEPT ? OrganizationActivityType.JOIN_REQUEST_ACCEPTED :
                        OrganizationActivityType.JOIN_REQUEST_REJECTED
        );

    }

    @Override
    @Transactional
    public void processEmailInvitationAction(String rawToken, OrganizationAction expectedAction) {
        // Process the token and update the membership status
        Membership updatedMembership = processMembershipWithToken(rawToken, expectedAction);

        // Inform the owner about the result
        informOwnerAboutInvitationResult(updatedMembership, expectedAction);

        // Log the activity
        organizationActivityService.logActivity(
                updatedMembership.getOrganization().getOwner(),
                updatedMembership.getOrganization(),
                "Invitation for " + serviceUtil.getUserName(updatedMembership.getUser()) + " has been " + expectedAction.name().toLowerCase(),
                expectedAction == OrganizationAction.ACCEPT ? OrganizationActivityType.JOIN_REQUEST_ACCEPTED :
                        OrganizationActivityType.JOIN_REQUEST_REJECTED
        );
    }

    @Override
    @Transactional
    public void respondToInvitation(String userEmail, Long membershipId, OrganizationAction action) {
        // Get the membership by ID
        Membership membership = serviceUtil.getMembership(membershipId);

        Membership updatedMembership = processMembershipWithAction(membership, action);

        // Inform the owner about the result
        informOwnerAboutInvitationResult(updatedMembership, action);

        // Log the activity
        organizationActivityService.logActivity(
                updatedMembership.getOrganization().getOwner(),
                updatedMembership.getOrganization(),
                "Invitation for " + updatedMembership.getUser() + " has been " + action.name().toLowerCase(),
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
