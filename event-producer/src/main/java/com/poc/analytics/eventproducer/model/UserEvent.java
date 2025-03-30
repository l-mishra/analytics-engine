package com.poc.analytics.eventproducer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private String eventId;
    private LocalDateTime timestamp;
    private String userId;
    private String eventType;
    private String sessionId;
    private String productId;
    private Map<String, String> metadata;
    private Map<String, Object> eventData;
}