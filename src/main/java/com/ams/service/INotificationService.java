package com.ams.service;

import com.ams.dto.PageResponse;
import com.ams.dto.notification.NotificationResponse;
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
    PageResponse<NotificationResponse> getMyNotifications(String email, int page, int size);
    void markAsRead(String email, Long notificationId);
    void markAllAsRead(String email);
    void deleteNotification(String email, Long notificationId);
    void deleteAllNotifications(String email);
}
