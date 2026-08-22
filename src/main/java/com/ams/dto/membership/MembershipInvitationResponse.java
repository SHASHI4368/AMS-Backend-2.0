package com.ams.dto.membership;

import java.time.LocalDateTime;

public record MembershipInvitationResponse(
        Long id,
        String email,
        String name,
        String avatarUrl,
        LocalDateTime invitedAt
) {
}
