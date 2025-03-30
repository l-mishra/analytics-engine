package com.poc.analytics.analyticsservice.config;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.poc.analytics.analyticsservice")
public class ElasticsearchIndexConfig extends ElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.uris}")
    private String elasticsearchUri;

    @Value("${spring.data.elasticsearch.connection-timeout}")
    private String connectionTimeout;

    @Value("${spring.data.elasticsearch.socket-timeout}")
    private String socketTimeout;

    private static final String RAW_EVENTS_INDEX = "raw-events";
    private static final String AGGREGATED_EVENTS_INDEX = "aggregated-events";

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUri)
                .withConnectTimeout(Duration.parse(connectionTimeout))
                .withSocketTimeout(Duration.parse(socketTimeout))
                .build();
    }

    @PostConstruct
    public void createIndices(ElasticsearchOperations elasticsearchOperations) {
        // Create raw events index
        createRawEventsIndex(elasticsearchOperations);

        // Create aggregated events index
        createAggregatedEventsIndex(elasticsearchOperations);
    }

    private void createRawEventsIndex(ElasticsearchOperations elasticsearchOperations) {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(RAW_EVENTS_INDEX));

        if (!indexOperations.exists()) {
            Map<String, Object> mapping = new HashMap<>();
            Map<String, Object> properties = new HashMap<>();

            // Define mapping for raw events
            properties.put("timestamp", createFieldMapping("date"));
            properties.put("userId", createFieldMapping("keyword"));
            properties.put("eventType", createFieldMapping("keyword"));
            properties.put("sessionId", createFieldMapping("keyword"));
            properties.put("productId", createFieldMapping("keyword"));
            properties.put("metadata", createFieldMapping("object"));
            properties.put("eventData", createFieldMapping("object"));

            mapping.put("properties", properties);
            mapping.put("dynamic", true);

            indexOperations.create(mapping);
        }
    }

    private void createAggregatedEventsIndex(ElasticsearchOperations elasticsearchOperations) {
        IndexOperations indexOperations = elasticsearchOperations
                .indexOps(IndexCoordinates.of(AGGREGATED_EVENTS_INDEX));

        if (!indexOperations.exists()) {
            Map<String, Object> mapping = new HashMap<>();
            Map<String, Object> properties = new HashMap<>();

            // Define mapping for aggregated events
            properties.put("timestamp", createFieldMapping("date"));
            properties.put("eventType", createFieldMapping("keyword"));
            properties.put("productId", createFieldMapping("keyword"));
            properties.put("count", createFieldMapping("long"));
            properties.put("uniqueUsers", createFieldMapping("long"));
            properties.put("aggregationWindow", createFieldMapping("keyword")); // e.g., "1h", "1d"

            mapping.put("properties", properties);
            mapping.put("dynamic", false);

            indexOperations.create(mapping);
        }
    }

    private Map<String, Object> createFieldMapping(String type) {
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("type", type);
        return mapping;
    }
}