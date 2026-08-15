package com.ams.service;

import com.ams.entity.Notification;
import com.ams.entity.User;
import com.ams.enums.NotificationTargetType;
import com.ams.enums.NotificationType;

public interface INotificationService {
    Notification create(
            User user,
            NotificationType type,
            NotificationTargetType targetType,
            Long referenceId,
            String title,
            String message
    );
}
