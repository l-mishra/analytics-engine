package com.poc.analytics.eventproducer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class KafkaApplicationProperties {
    public static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String KAFKA_EVENTS_TOPIC = "user-events";
    public static final String KAFKA_ALERTS_TOPIC = "user-events";
    public static final String KAFKA_KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    public static final String KAFKA_VALUE_SERIALIZER = "org.springframework.kafka.support.serializer.JsonSerializer";
    public static final String KAFKA_TRUSTED_PACKAGES = "com.poc.analytics.eventproducer.model";
}