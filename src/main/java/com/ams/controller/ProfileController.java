package com.ams.controller;

import com.ams.dto.ApiResponse;
import com.ams.dto.ProfileResponse;
import com.ams.dto.ProfileUpdateRequest;
import com.ams.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.base-path}/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentUser(
            Authentication authentication
    ) {
        ProfileResponse loginResponse = profileService.getCurrentUserProfile(authentication.getName());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Current user profile fetched successfully",
                        loginResponse
                )
        );
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Valid
            @RequestBody
            ProfileUpdateRequest profileUpdateRequest,
            Authentication authentication
    ) {
        ProfileResponse profile = profileService.updateProfile(authentication.getName(), profileUpdateRequest);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Current user profile updated successfully",
                        profile
                )
        );
    }
}
