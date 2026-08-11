package com.ams.controller;

import com.ams.dto.ApiResponse;
import com.ams.dto.ProfileResponse;
import com.ams.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base-path}/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentUser(
            Authentication authentication
    ) {
        System.out.println(authentication.getName());
        ProfileResponse loginResponse = profileService.getCurrentUserProfile(authentication.getName());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Current user profile fetched successfully",
                        loginResponse
                )
        );
    }
}
