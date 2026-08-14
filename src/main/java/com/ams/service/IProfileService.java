package com.ams.service;

import com.ams.dto.profile.ProfileResponse;
import com.ams.dto.profile.ProfileUpdateRequest;

public interface IProfileService {
     ProfileResponse getCurrentUserProfile(String email);
     ProfileResponse updateProfile(String email, ProfileUpdateRequest profileUpdateRequest);

}
