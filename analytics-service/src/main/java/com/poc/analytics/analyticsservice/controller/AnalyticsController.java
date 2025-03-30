package com.poc.analytics.analyticsservice.controller;

import com.poc.analytics.analyticsservice.service.AnalyticsService;
import com.poc.analytics.analyticsservice.model.AnalyticsQuery;
import com.poc.analytics.analyticsservice.model.AnalyticsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @PostMapping("/query")
    public ResponseEntity<AnalyticsResponse> queryAnalytics(@RequestBody AnalyticsQuery query) {
        return ResponseEntity.ok(analyticsService.executeQuery(query));
    }

    @GetMapping("/events/raw")
    public ResponseEntity<AnalyticsResponse> getRawEvents(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        AnalyticsQuery query = AnalyticsQuery.builder()
                .startTime(startTime)
                .endTime(endTime)
                .eventType(eventType)
                .productId(productId)
                .page(page)
                .size(size)
                .build();
        return ResponseEntity.ok(analyticsService.getRawEvents(query));
    }

    @GetMapping("/events/aggregated")
    public ResponseEntity<AnalyticsResponse> getAggregatedEvents(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String productId,
            @RequestParam(defaultValue = "1h") String aggregationWindow) {
        AnalyticsQuery query = AnalyticsQuery.builder()
                .startTime(startTime)
                .endTime(endTime)
                .eventType(eventType)
                .productId(productId)
                .aggregationWindow(aggregationWindow)
                .build();
        return ResponseEntity.ok(analyticsService.getAggregatedEvents(query));
    }

    @GetMapping("/events/metrics")
    public ResponseEntity<AnalyticsResponse> getEventMetrics(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String productId) {
        AnalyticsQuery query = AnalyticsQuery.builder()
                .startTime(startTime)
                .endTime(endTime)
                .eventType(eventType)
                .productId(productId)
                .build();
        return ResponseEntity.ok(analyticsService.getEventMetrics(query));
    }
}