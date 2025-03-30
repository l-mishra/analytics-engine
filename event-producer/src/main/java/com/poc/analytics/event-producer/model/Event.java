package com.poc.analytics.eventproducer.model;

import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
public class Event {
    private String eventId;
    private String eventType;
    private String userId;
    private String sessionId;
    private String pageUrl;
    private String elementId;
    private Map<String, Object> metadata;
    private Instant timestamp;
    private String browser;
    private String deviceType;
    private String country;
    private String city;
}