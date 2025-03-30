package com.poc.analytics.analyticsservice.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnalyticsQuery {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String eventType;
    private String productId;
    private String aggregationWindow;
    private int page;
    private int size;
    private String queryType; // "raw", "aggregated", "metrics"
}