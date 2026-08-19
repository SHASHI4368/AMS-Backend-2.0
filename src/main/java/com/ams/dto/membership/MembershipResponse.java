package com.ams.dto.membership;

import java.time.LocalDateTime;

public record MembershipResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String avatarUrl,
        LocalDateTime joinedAt,
        String role
) {
}
