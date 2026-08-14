package com.ams.dto.organization;

import java.time.LocalDateTime;

public record OrganizationListResponse(
        Long id,
        String name,
        String description,
        String logoUrl,
        LocalDateTime createdAt,
        int memberCount,
        String myRole,
        boolean isMember
) {
}
