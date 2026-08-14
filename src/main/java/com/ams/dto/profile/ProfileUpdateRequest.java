package com.ams.dto.profile;

import com.ams.enums.Gender;

public record ProfileUpdateRequest(
        String firstName,
        String lastName,
        String avatarUrl,
        String telephone,
        String timezone,
        String bio,
        Gender gender
) {
}
