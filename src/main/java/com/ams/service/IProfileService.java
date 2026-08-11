package com.ams.service;

import com.ams.dto.ProfileResponse;
import com.ams.dto.ProfileUpdateRequest;

public interface IProfileService {
     ProfileResponse getCurrentUserProfile(String email);
     ProfileResponse updateProfile(String email, ProfileUpdateRequest profileUpdateRequest);
}
