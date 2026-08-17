package com.ams.service;

import com.ams.dto.WebSocketEvent;
import com.ams.enums.WebSocketEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
public class WebSocketService implements IWebSocketService{
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void sendEvent(String userId, WebSocketEventType eventType) {
        WebSocketEvent event = new WebSocketEvent(eventType);
        simpMessagingTemplate.convertAndSendToUser(userId,"/queue/events",event);
    }
}
