package com.ams.service;

import com.ams.dto.ProfileResponse;
import com.ams.entity.Profile;
import com.ams.entity.User;
import com.ams.repository.ProfileRepository;
import com.ams.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService implements IProfileService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Override
    public ProfileResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found with email: " + email)
                );

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found for user with email: " + email)
                );

        return new ProfileResponse(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                profile.getAvatarUrl(),
                profile.getTelephone(),
                profile.getGender() != null ? profile.getGender().name() : null,
                profile.getBio()
        );
    }
}
