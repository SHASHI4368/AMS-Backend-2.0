package com.ams.dto;

public record LoginResponse(
        Long id,
        String email,
        String role
) {
}
