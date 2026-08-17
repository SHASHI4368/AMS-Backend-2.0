package com.ams.dto.notification;

public record NotificationCreatedEvent(
        Long notificationId,
        String userEmail
) {
}
