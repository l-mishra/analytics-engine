package com.poc.analytics.eventproducer.config;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class KafkaPropertiesConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaPropertiesConfig.class);

    @PostConstruct
    public void logKafkaProperties() {
        logger.info("Kafka Bootstrap Servers: {}", KafkaApplicationProperties.KAFKA_BOOTSTRAP_SERVERS);
        logger.info("Kafka Events Topic: {}", KafkaApplicationProperties.KAFKA_EVENTS_TOPIC);
        logger.info("Kafka Key Serializer: {}", KafkaApplicationProperties.KAFKA_KEY_SERIALIZER);
        logger.info("Kafka Value Serializer: {}", KafkaApplicationProperties.KAFKA_VALUE_SERIALIZER);
        logger.info("Kafka Trusted Packages: {}", KafkaApplicationProperties.KAFKA_TRUSTED_PACKAGES);
    }
}