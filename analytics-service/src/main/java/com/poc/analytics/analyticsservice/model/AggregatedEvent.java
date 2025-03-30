package com.poc.analytics.analyticsservice.model;

import lombok.Data;
import java.time.Instant;

@Data
public class AggregatedEvent {
    private String eventType;
    private String country;
    private Long count;
    private Long windowStart;
    private Long windowEnd;
    private Instant processedAt;
}