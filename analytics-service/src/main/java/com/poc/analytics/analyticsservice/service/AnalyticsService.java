package com.poc.analytics.analyticsservice.service;

import com.poc.analytics.analyticsservice.model.Event;
import com.poc.analytics.analyticsservice.model.AggregatedEvent;
import com.poc.analytics.analyticsservice.repository.EventRepository;
import com.poc.analytics.analyticsservice.model.AnalyticsQuery;
import com.poc.analytics.analyticsservice.model.AnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

        private final EventRepository eventRepository;
        private final ElasticsearchTemplate elasticsearchTemplate;
        private final JdbcTemplate clickHouseJdbcTemplate;

        private Instant toInstant(LocalDateTime dateTime) {
                return dateTime.atZone(ZoneId.systemDefault()).toInstant();
        }

        public List<Event> getRawEvents(String eventType, Instant startTime, Instant endTime) {
                BoolQuery boolQuery = BoolQuery.of(q -> q
                                .must(TermQuery.of(t -> t
                                                .field("eventType")
                                                .value(eventType))._toQuery())
                                .must(RangeQuery.of(r -> r
                                                .field("timestamp")
                                                .from(startTime.toString())
                                                .to(endTime.toString()))._toQuery()));

                Query searchQuery = NativeQuery.builder()
                                .withQuery(boolQuery._toQuery())
                                .build();

                SearchHits<Event> searchHits = elasticsearchTemplate.search(searchQuery, Event.class);
                return searchHits.stream()
                                .map(SearchHit::getContent)
                                .collect(Collectors.toList());
        }

        public List<AggregatedEvent> getAggregatedEvents(String eventType, String country, Instant startTime,
                        Instant endTime) {
                String sql = "SELECT event_type, country, count, window_start, window_end, processed_at " +
                                "FROM aggregated_events " +
                                "WHERE event_type = ? AND country = ? " +
                                "AND window_start >= ? AND window_end <= ? " +
                                "ORDER BY window_start DESC";

                return clickHouseJdbcTemplate.query(sql,
                                (rs, rowNum) -> {
                                        AggregatedEvent event = new AggregatedEvent();
                                        event.setEventType(rs.getString("event_type"));
                                        event.setCountry(rs.getString("country"));
                                        event.setCount(rs.getLong("count"));
                                        event.setWindowStart(
                                                        rs.getTimestamp("window_start").toInstant().toEpochMilli());
                                        event.setWindowEnd(rs.getTimestamp("window_end").toInstant().toEpochMilli());
                                        event.setProcessedAt(rs.getTimestamp("processed_at").toInstant());
                                        return event;
                                },
                                eventType, country, Timestamp.from(startTime), Timestamp.from(endTime));
        }

        public Map<String, Long> getEventCountsByType(Instant startTime, Instant endTime) {
                String sql = "SELECT event_type, sum(count) as total_count " +
                                "FROM aggregated_events " +
                                "WHERE window_start >= ? AND window_end <= ? " +
                                "GROUP BY event_type";

                return clickHouseJdbcTemplate.query(sql,
                                (rs, rowNum) -> Map.entry(
                                                rs.getString("event_type"),
                                                rs.getLong("total_count")),
                                Timestamp.from(startTime), Timestamp.from(endTime))
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        public Map<String, Long> getEventCountsByCountry(String eventType, Instant startTime, Instant endTime) {
                String sql = "SELECT country, sum(count) as total_count " +
                                "FROM aggregated_events " +
                                "WHERE event_type = ? AND window_start >= ? AND window_end <= ? " +
                                "GROUP BY country";

                return clickHouseJdbcTemplate.query(sql,
                                (rs, rowNum) -> Map.entry(
                                                rs.getString("country"),
                                                rs.getLong("total_count")),
                                eventType, Timestamp.from(startTime), Timestamp.from(endTime))
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        public List<Map<String, Object>> getHourlyEventTrends(String eventType, String country, int hours) {
                Instant endTime = Instant.now();
                Instant startTime = endTime.minus(hours, ChronoUnit.HOURS);

                String sql = "SELECT toStartOfHour(window_start) as hour, sum(count) as total_count " +
                                "FROM aggregated_events " +
                                "WHERE event_type = ? AND country = ? " +
                                "AND window_start >= ? AND window_end <= ? " +
                                "GROUP BY hour " +
                                "ORDER BY hour DESC";

                return clickHouseJdbcTemplate.queryForList(sql, eventType, country, Timestamp.from(startTime),
                                Timestamp.from(endTime));
        }

        public AnalyticsResponse executeQuery(AnalyticsQuery query) {
                switch (query.getQueryType()) {
                        case "raw":
                                List<Event> rawEvents = getRawEvents(
                                                query.getEventType(),
                                                toInstant(query.getStartTime()),
                                                toInstant(query.getEndTime()));
                                return AnalyticsResponse.builder()
                                                .data(rawEvents.stream()
                                                                .map(event -> {
                                                                        Map<String, Object> eventMap = new HashMap<>();
                                                                        eventMap.put("id", event.getEventId());
                                                                        eventMap.put("eventType", event.getEventType());
                                                                        eventMap.put("timestamp", event.getTimestamp());
                                                                        eventMap.put("data", event.getMetadata());
                                                                        return eventMap;
                                                                })
                                                                .collect(Collectors.toList()))
                                                .total(rawEvents.size())
                                                .page(query.getPage())
                                                .size(query.getSize())
                                                .build();

                        case "aggregated":
                                List<AggregatedEvent> aggregatedEvents = getAggregatedEvents(
                                                query.getEventType(),
                                                null, // country is not part of the query model yet
                                                toInstant(query.getStartTime()),
                                                toInstant(query.getEndTime()));
                                return AnalyticsResponse.builder()
                                                .data(aggregatedEvents.stream()
                                                                .map(event -> {
                                                                        Map<String, Object> eventMap = new HashMap<>();
                                                                        eventMap.put("eventType", event.getEventType());
                                                                        eventMap.put("windowStart",
                                                                                        event.getWindowStart());
                                                                        eventMap.put("windowEnd", event.getWindowEnd());
                                                                        eventMap.put("count", event.getCount());
                                                                        return eventMap;
                                                                })
                                                                .collect(Collectors.toList()))
                                                .total(aggregatedEvents.size())
                                                .aggregationWindow(query.getAggregationWindow())
                                                .build();

                        case "metrics":
                                Map<String, Long> eventCounts = getEventCountsByType(
                                                toInstant(query.getStartTime()),
                                                toInstant(query.getEndTime()));
                                return AnalyticsResponse.builder()
                                                .metrics(Map.of("eventCounts", eventCounts))
                                                .build();

                        default:
                                throw new IllegalArgumentException("Unsupported query type: " + query.getQueryType());
                }
        }

        public AnalyticsResponse getRawEvents(AnalyticsQuery query) {
                List<Event> rawEvents = getRawEvents(
                                query.getEventType(),
                                toInstant(query.getStartTime()),
                                toInstant(query.getEndTime()));
                return AnalyticsResponse.builder()
                                .data(rawEvents.stream()
                                                .map(event -> {
                                                        Map<String, Object> eventMap = new HashMap<>();
                                                        eventMap.put("id", event.getEventId());
                                                        eventMap.put("eventType", event.getEventType());
                                                        eventMap.put("timestamp", event.getTimestamp());
                                                        eventMap.put("data", event.getMetadata());
                                                        return eventMap;
                                                })
                                                .collect(Collectors.toList()))
                                .total(rawEvents.size())
                                .page(query.getPage())
                                .size(query.getSize())
                                .build();
        }

        public AnalyticsResponse getAggregatedEvents(AnalyticsQuery query) {
                List<AggregatedEvent> aggregatedEvents = getAggregatedEvents(
                                query.getEventType(),
                                null, // country is not part of the query model yet
                                toInstant(query.getStartTime()),
                                toInstant(query.getEndTime()));
                return AnalyticsResponse.builder()
                                .data(aggregatedEvents.stream()
                                                .map(event -> {
                                                        Map<String, Object> eventMap = new HashMap<>();
                                                        eventMap.put("eventType", event.getEventType());
                                                        eventMap.put("windowStart", event.getWindowStart());
                                                        eventMap.put("windowEnd", event.getWindowEnd());
                                                        eventMap.put("count", event.getCount());
                                                        return eventMap;
                                                })
                                                .collect(Collectors.toList()))
                                .total(aggregatedEvents.size())
                                .aggregationWindow(query.getAggregationWindow())
                                .build();
        }

        public AnalyticsResponse getEventMetrics(AnalyticsQuery query) {
                Map<String, Long> eventCounts = getEventCountsByType(
                                toInstant(query.getStartTime()),
                                toInstant(query.getEndTime()));
                return AnalyticsResponse.builder()
                                .metrics(Map.of("eventCounts", eventCounts))
                                .build();
        }
}