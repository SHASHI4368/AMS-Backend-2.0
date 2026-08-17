package com.ams.service;

import com.ams.dto.profile.ProfileResponse;
import com.ams.dto.profile.ProfileUpdateRequest;
import com.ams.entity.Profile;
import com.ams.entity.User;
import com.ams.repository.ProfileRepository;
import com.ams.repository.UserRepository;
import com.ams.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService implements IProfileService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ServiceUtil serviceUtil;

    @Override
    @Transactional
    public ProfileResponse getCurrentUserProfile(String email) {
        User user = serviceUtil.getUser(email);

        Profile profile = serviceUtil.getProfileByUser(user);

        return new ProfileResponse(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                profile.getAvatarUrl(),
                profile.getTelephone(),
                profile.getGender() != null ? profile.getGender().name() : null,
                profile.getBio(),
                profile.getTimezone()
        );
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(String email, ProfileUpdateRequest profileUpdateRequest) {
        User user = serviceUtil.getUser(email);

        Profile profile = serviceUtil.getProfileByUser(user);

        profile.setFirstName(profileUpdateRequest.firstName());
        profile.setLastName(profileUpdateRequest.lastName());
        profile.setAvatarUrl(profileUpdateRequest.avatarUrl());
        profile.setTelephone(profileUpdateRequest.telephone());
        profile.setGender(profileUpdateRequest.gender());
        profile.setBio(profileUpdateRequest.bio());
        profile.setTimezone(profileUpdateRequest.timezone());

        profileRepository.save(profile);

        return new ProfileResponse(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                profile.getAvatarUrl(),
                profile.getTelephone(),
                profile.getGender() != null ? profile.getGender().name() : null,
                profile.getBio(),
                profile.getTimezone()
        );

    }
}
