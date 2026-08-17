package com.ams.service;

import com.ams.dto.PageResponse;
import com.ams.dto.notification.NotificationResponse;
import com.ams.entity.Notification;
import com.ams.entity.User;
import com.ams.enums.NotificationTargetType;
import com.ams.enums.NotificationType;
import com.ams.repository.NotificationRepository;
import com.ams.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {
    private final NotificationRepository notificationRepository;
    private final ServiceUtil serviceUtil;

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

    @Override
    @Transactional
    public PageResponse<NotificationResponse> getMyNotifications(String email, int page, int size) {
        // Check if the user exists
        User user = serviceUtil.getUser(email);

        // Fetch notifications for the user with pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notificationPage = notificationRepository.findByUser(user, pageable);
        List<Notification> notifications = notificationPage.getContent();

        // If no notifications are found, return an empty page response
        if (notifications.isEmpty()) {
            return new PageResponse<>(
                    List.of(),
                    notificationPage.getNumber(),
                    notificationPage.getSize(),
                    notificationPage.getTotalElements(),
                    notificationPage.getTotalPages(),
                    notificationPage.isLast()
            );
        }

        // Map notifications to NotificationResponse DTOs
        List<NotificationResponse> notificationResponses = notifications.stream()
                .map(notification -> new NotificationResponse(
                        notification.getId(),
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.isRead(),
                        notification.getType(),
                        notification.getTargetType(),
                        notification.getReferenceId(),
                        notification.getCreatedAt()
                ))
                .toList();

        return new PageResponse<>(
                notificationResponses,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.isLast()
        );
    }

    @Override
    @Transactional
    public void markAsRead(String email, Long notificationId) {
        // Check if the user exists
        User user = serviceUtil.getUser(email);

        // Check if the notification exists and belongs to the user
        Notification notification = serviceUtil.getNotification(notificationId);
        if(!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Notification does not belong to the user");
        }

        // Mark the notification as read
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {
        // Check if the user exists
        User user = serviceUtil.getUser(email);

        // Fetch all unread notifications for the user
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalse(user);

        // Mark all unread notifications as read
        unreadNotifications
                .forEach(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });


    }

    @Override
    @Transactional
    public void deleteNotification(String email, Long notificationId) {
        // Check if the user exists
        User user = serviceUtil.getUser(email);

        // Check if the notification exists and belongs to the user
        Notification notification = serviceUtil.getNotification(notificationId);
        if(!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Notification does not belong to the user");
        }

        // Delete the notification
        notificationRepository.delete(notification);

    }

    @Override
    @Transactional
    public void deleteAllNotifications(String email) {
        // Check if the user exists
        User user = serviceUtil.getUser(email);

        // Fetch all notifications for the user
        List<Notification> notifications = notificationRepository.findByUser(user);

        // Delete all notifications for the user
        notificationRepository.deleteAll(notifications);
    }
}
