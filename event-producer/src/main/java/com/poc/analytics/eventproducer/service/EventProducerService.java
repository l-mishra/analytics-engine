package com.poc.analytics.eventproducer.service;

import com.poc.analytics.eventproducer.config.KafkaApplicationProperties;
import com.poc.analytics.eventproducer.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

    private final KafkaTemplate<String, Event> kafkaTemplate;

    private final List<String> eventTypes = Arrays.asList(
            "CLICK", "PAGE_LOAD", "SCROLL", "HOVER", "FORM_SUBMIT",
            "BUTTON_CLICK", "LINK_CLICK", "VIDEO_PLAY", "VIDEO_PAUSE");

    private final List<String> browsers = Arrays.asList(
            "Chrome", "Firefox", "Safari", "Edge", "Opera");

    private final List<String> deviceTypes = Arrays.asList(
            "DESKTOP", "MOBILE", "TABLET");

    private final List<String> countries = Arrays.asList(
            "US", "UK", "IN", "DE", "FR", "JP", "CA", "AU");

    @Scheduled(fixedRate = 1000) // Generate events every second
    public void generateAndSendEvents() {
        int numEvents = ThreadLocalRandom.current().nextInt(1, 10);
        for (int i = 0; i < numEvents; i++) {
            Event event = generateRandomEvent();
            kafkaTemplate.send(KafkaApplicationProperties.KAFKA_EVENTS_TOPIC, event.getEventId(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.debug("Sent event: {}", event.getEventId());
                        } else {
                            log.error("Failed to send event: {}", event.getEventId(), ex);
                        }
                    });
        }
    }

    private Event generateRandomEvent() {
        Event event = new Event();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventTypes.get(ThreadLocalRandom.current().nextInt(eventTypes.size())));
        event.setUserId("user-" + ThreadLocalRandom.current().nextInt(1000, 10000));
        event.setSessionId("session-" + ThreadLocalRandom.current().nextInt(100, 1000));
        event.setPageUrl("https://example.com/page" + ThreadLocalRandom.current().nextInt(1, 10));
        event.setElementId("element-" + ThreadLocalRandom.current().nextInt(1, 100));
        event.setTimestamp(Instant.now());
        event.setBrowser(browsers.get(ThreadLocalRandom.current().nextInt(browsers.size())));
        event.setDeviceType(deviceTypes.get(ThreadLocalRandom.current().nextInt(deviceTypes.size())));
        event.setCountry(countries.get(ThreadLocalRandom.current().nextInt(countries.size())));
        event.setCity("City-" + ThreadLocalRandom.current().nextInt(1, 100));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("screenResolution", "1920x1080");
        metadata.put("language", "en-US");
        metadata.put("timeZone", "UTC");
        metadata.put("platform", "web");
        event.setMetadata(metadata);

        return event;
    }
}