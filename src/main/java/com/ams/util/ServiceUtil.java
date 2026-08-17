package com.ams.util;

import com.ams.entity.*;
import com.ams.exception.ServiceException;
import com.ams.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceUtil {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final NotificationRepository notificationRepository;

    public User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ServiceException("User not found with email: " + email)
                );
    }

    public Organization getOrganization(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() ->
                        new ServiceException("Organization not found with id: " + organizationId)
                );
    }

    public Profile getProfileByUser(User user) {
        return profileRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found for user with email: " + user.getEmail())
                );
    }

    public Membership getMembership(Long membershipId) {
        return membershipRepository.findById(membershipId)
                .orElseThrow(() ->
                        new ServiceException("Membership not found with id: " + membershipId)
                );
    }

    public Notification getNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ServiceException("Notification not found with id: " + notificationId)
                );
    }
}
