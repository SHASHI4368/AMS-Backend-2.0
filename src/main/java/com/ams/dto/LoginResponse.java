package com.ams.dto;

public record LoginResponse(
        String jwt,
        String email,
        String role
) {
}
