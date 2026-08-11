package com.ams.dto;

public record ProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role,
        String avatarUrl,
        String phoneNumber,
        String gender,
        String bio,
        String timezone
) {
}
