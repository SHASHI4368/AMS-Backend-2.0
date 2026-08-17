package com.ams.dto.notification;

import com.ams.enums.NotificationTargetType;
import com.ams.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        boolean isRead,
        NotificationType type,
        NotificationTargetType targetType,
        Long referenceId,
        LocalDateTime createdAt
) {
}
