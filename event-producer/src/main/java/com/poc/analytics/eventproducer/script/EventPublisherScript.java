package com.poc.analytics.eventproducer.script;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.analytics.eventproducer.config.KafkaApplicationProperties;
import com.poc.analytics.eventproducer.model.UserEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Component
public class EventPublisherScript implements CommandLineRunner {

        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;

        @Autowired
        private ObjectMapper objectMapper;

        private final Random random = new Random();
        private final List<String> eventTypes = Arrays.asList(
                        "page_view", "button_click", "form_submit", "video_play",
                        "document_download", "search_query", "user_login", "user_logout");
        private final List<String> productIds = Arrays.asList(
                        "product1", "product2", "product3", "product4", "product5");

        @Override
        public void run(String... args) throws Exception {
                // Publish 1000 events
                for (int i = 0; i < 1000; i++) {
                        UserEvent event = generateRandomEvent();
                        String eventJson = objectMapper.writeValueAsString(event);
                        kafkaTemplate.send(KafkaApplicationProperties.KAFKA_EVENTS_TOPIC, eventJson);

                        // Add a small delay between events
                        Thread.sleep(10);
                }
        }

        private UserEvent generateRandomEvent() {
                return UserEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .timestamp(LocalDateTime.now())
                                .userId("user" + random.nextInt(1000))
                                .eventType(eventTypes.get(random.nextInt(eventTypes.size())))
                                .sessionId(UUID.randomUUID().toString())
                                .productId(productIds.get(random.nextInt(productIds.size())))
                                .metadata(Map.of(
                                                "browser", "Chrome",
                                                "os", "Windows",
                                                "device", "desktop",
                                                "ip", "192.168.1." + random.nextInt(255)))
                                .eventData(Map.of(
                                                "duration", random.nextInt(300),
                                                "page", "/page" + random.nextInt(10),
                                                "referrer", "https://example.com"))
                                .build();
        }
}