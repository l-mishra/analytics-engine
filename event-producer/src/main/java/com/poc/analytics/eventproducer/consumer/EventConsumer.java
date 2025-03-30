package com.poc.analytics.eventproducer.consumer;

import com.poc.analytics.eventproducer.config.KafkaApplicationProperties;
import com.poc.analytics.eventproducer.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventConsumer {

    @KafkaListener(topics = KafkaApplicationProperties.KAFKA_EVENTS_TOPIC, groupId = "event-producer-group")
    public void consumeEvent(Event event) {
        log.info("Received event: {}", event);
    }

    @KafkaListener(topics = KafkaApplicationProperties.KAFKA_ALERTS_TOPIC, groupId = "event-producer-group")
    public void consumeAlert(String alert) {
        log.info("Received alert: {}", alert);
    }
}