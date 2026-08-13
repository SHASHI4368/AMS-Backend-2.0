package com.ams.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
        @NotBlank(message = "Organization name is required")
        @Size(max = 100, message = "Organization name must not exceed 100 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        String logoUrl
) {
}
