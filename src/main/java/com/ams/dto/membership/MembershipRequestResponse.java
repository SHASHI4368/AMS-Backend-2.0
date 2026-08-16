package com.ams.dto.membership;

import java.time.LocalDateTime;

public record MembershipRequestResponse(
        Long id,
        String email,
        String name,
        String avatarUrl,
        LocalDateTime requestedAt
) {
}
