package com.poc.analytics.analyticsservice.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {
    private List<Map<String, Object>> data;
    private long total;
    private int page;
    private int size;
    private Map<String, Object> metrics;
    private String aggregationWindow;
}