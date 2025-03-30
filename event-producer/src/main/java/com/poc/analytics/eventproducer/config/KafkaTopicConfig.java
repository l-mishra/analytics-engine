package com.poc.analytics.eventproducer.config;

import static com.poc.analytics.eventproducer.config.KafkaApplicationProperties.KAFKA_ALERTS_TOPIC;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
public class KafkaTopicConfig {

        @Bean
        public KafkaAdmin kafkaAdmin() {
                return new KafkaAdmin(Map.of(
                                "bootstrap.servers", KafkaApplicationProperties.KAFKA_BOOTSTRAP_SERVERS));
        }

        @Bean
        public NewTopic eventsTopic() {
                return TopicBuilder.name(KafkaApplicationProperties.KAFKA_EVENTS_TOPIC)
                                .partitions(3)
                                .replicas(1)
                                .configs(Map.of(
                                                "cleanup.policy", "delete",
                                                "retention.ms", String.valueOf(7 * 24 * 60 * 60 * 1000) // 7 days
                                                                                                        // retention
                                ))
                                .build();
        }

        @Bean
        public NewTopic alertsTopic() {
                return TopicBuilder.name(KAFKA_ALERTS_TOPIC)
                                .partitions(3)
                                .replicas(3)
                                .configs(Map.of(
                                                "cleanup.policy", "delete",
                                                "retention.ms", String.valueOf(24 * 60 * 60 * 1000) // 24 hours
                                                                                                    // retention
                                ))
                                .build();
        }
}