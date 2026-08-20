package com.ams.dto.organization;

import java.time.LocalDateTime;

public record OrganizationActivityResponse(
        Long id,
        String activityType,
        String actor,
        String description,
        LocalDateTime createdAt
) {
}
