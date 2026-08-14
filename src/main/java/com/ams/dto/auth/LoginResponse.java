package com.ams.dto.auth;

public record LoginResponse(
        Long id,
        String email,
        String role
) {
}
