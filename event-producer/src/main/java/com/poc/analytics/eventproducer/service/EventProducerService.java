package com.poc.analytics.eventproducer.service;

import com.poc.analytics.eventproducer.config.KafkaApplicationProperties;
import com.poc.analytics.eventproducer.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProducerService {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    private final List<String> eventTypes = Arrays.asList(
            "CLICK", "PAGE_LOAD", "SCROLL", "HOVER", "FORM_SUBMIT",
            "BUTTON_CLICK", "LINK_CLICK", "VIDEO_PLAY", "VIDEO_PAUSE");

    private final List<String> productIds = Arrays.asList(
            "product1", "product2", "product3", "product4", "product5");

    @Scheduled(fixedRate = 1000) // Generate events every second
    public void generateAndSendEvents() {
        int numEvents = ThreadLocalRandom.current().nextInt(1, 10);
        for (int i = 0; i < numEvents; i++) {
            UserEvent event = generateRandomEvent();
            kafkaTemplate.send(KafkaApplicationProperties.KAFKA_EVENTS_TOPIC, event.getEventId(), event)
                    .addCallback(
                            result -> log.debug("Sent event: {}", event.getEventId()),
                            ex -> log.error("Failed to send event: {}", event.getEventId(), ex));
        }
    }

    private UserEvent generateRandomEvent() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("screenResolution", "1920x1080");
        metadata.put("language", "en-US");
        metadata.put("timeZone", "UTC");
        metadata.put("platform", "web");

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("browser", "Chrome");
        eventData.put("deviceType", "DESKTOP");
        eventData.put("country", "US");
        eventData.put("city", "New York");

        return UserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .userId("user-" + ThreadLocalRandom.current().nextInt(1000, 10000))
                .eventType(eventTypes.get(ThreadLocalRandom.current().nextInt(eventTypes.size())))
                .sessionId("session-" + ThreadLocalRandom.current().nextInt(100, 1000))
                .productId(productIds.get(ThreadLocalRandom.current().nextInt(productIds.size())))
                .metadata(metadata)
                .eventData(eventData)
                .build();
    }
}