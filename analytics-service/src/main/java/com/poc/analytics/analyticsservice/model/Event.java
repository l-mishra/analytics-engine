package com.poc.analytics.analyticsservice.model;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.Map;

@Data
@Document(indexName = "events")
public class Event {
    @Field(type = FieldType.Keyword)
    private String eventId;

    @Field(type = FieldType.Keyword)
    private String eventType;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Keyword)
    private String sessionId;

    @Field(type = FieldType.Keyword)
    private String pageUrl;

    @Field(type = FieldType.Keyword)
    private String elementId;

    @Field(type = FieldType.Object)
    private Map<String, Object> metadata;

    @Field(type = FieldType.Date)
    private Instant timestamp;

    @Field(type = FieldType.Keyword)
    private String browser;

    @Field(type = FieldType.Keyword)
    private String deviceType;

    @Field(type = FieldType.Keyword)
    private String country;

    @Field(type = FieldType.Keyword)
    private String city;
}