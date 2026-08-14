package com.ams.dto.organization;


import java.time.LocalDateTime;

public record OrganizationResponse(
        Long id,
        String name,
        String description,
        String logoUrl,
        LocalDateTime createdAt,
        int memberCount,
        String myRole
) {
}
