package com.ams.service;

import com.ams.entity.Notification;
import com.ams.entity.User;
import com.ams.enums.NotificationTargetType;
import com.ams.enums.NotificationType;
import com.ams.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {
    private final NotificationRepository notificationRepository;

    @Override
    public Notification create(
            User user,
            NotificationType type,
            NotificationTargetType targetType,
            Long referenceId,
            String title,
            String message
    ){
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .targetType(targetType)
                .referenceId(referenceId)
                .title(title)
                .message(message)
                .build();

        return notificationRepository.save(notification);
    }
}
