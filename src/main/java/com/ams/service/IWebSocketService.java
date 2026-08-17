package com.ams.service;

import com.ams.enums.WebSocketEventType;

public interface IWebSocketService {
    void sendEvent(String userId, WebSocketEventType eventType);
}
