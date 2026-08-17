package com.ams.controller;

import com.ams.dto.ApiResponse;
import com.ams.dto.PageResponse;
import com.ams.dto.notification.NotificationResponse;
import com.ams.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.base-path}/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getMyNotifications(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication
    ) {
        PageResponse<NotificationResponse> response = notificationService
                .getMyNotifications(authentication.getName(), page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "My notifications fetched successfully",
                        response
                )
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(
            @PathVariable("notificationId") Long notificationId,
            Authentication authentication
    ) {
        notificationService.markAsRead(authentication.getName(), notificationId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Notification marked as read successfully",
                        null
                )
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllNotificationsAsRead(
            Authentication authentication
    ) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "All notifications marked as read successfully",
                        null
                )
        );
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable("notificationId") Long notificationId,
            Authentication authentication
    ) {
        notificationService.deleteNotification(authentication.getName(), notificationId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Notification deleted successfully",
                        null
                )
        );
    }

    @DeleteMapping("/my")
    public ResponseEntity<ApiResponse<Void>> deleteAllNotifications(
            Authentication authentication
    ) {
        notificationService.deleteAllNotifications(authentication.getName());
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "All notifications deleted successfully",
                        null
                )
        );
    }
}
