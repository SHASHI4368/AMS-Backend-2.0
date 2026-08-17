package com.ams.dto;

import com.ams.enums.WebSocketEventType;

public record WebSocketEvent(
        WebSocketEventType type
) {
}
