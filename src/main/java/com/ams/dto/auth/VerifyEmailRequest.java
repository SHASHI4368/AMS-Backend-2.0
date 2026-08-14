package com.ams.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Verification code is required")
        @Pattern(
                regexp = "\\d{6}",
                message = "Verification code must be a 6-digit number"
        )
        String code
) {
}
