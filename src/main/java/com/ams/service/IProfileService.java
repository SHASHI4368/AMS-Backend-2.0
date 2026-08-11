package com.ams.service;

import com.ams.dto.ProfileResponse;

public interface IProfileService {
     ProfileResponse getCurrentUserProfile(String email);
}
