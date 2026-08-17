package com.ams.event;

import com.ams.dto.notification.NotificationCreatedEvent;
import com.ams.enums.WebSocketEventType;
import com.ams.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final WebSocketService webSocketService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleNotificationCreated(NotificationCreatedEvent event) {
        webSocketService.sendEvent(
                event.userEmail(),
                WebSocketEventType.NOTIFICATION_UPDATED
        );
    }
}
