package com.ams.dto;

import jakarta.validation.constraints.NotBlank;

public record OrganizationRequest(
        @NotBlank(message = "Organization name is required")
        String name,
        String description,
        String logoUrl
) {
}
