package com.poc.analytics.analyticsservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.support.HttpHeaders;

import java.time.Duration;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.poc.analytics.analyticsservice")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.uris}")
    private String elasticsearchUri;

    @Value("${spring.data.elasticsearch.connection-timeout}")
    private String connectionTimeout;

    @Value("${spring.data.elasticsearch.socket-timeout}")
    private String socketTimeout;

    @Override
    public ClientConfiguration clientConfiguration() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");

        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUri)
                .withConnectTimeout(Duration.parse(connectionTimeout))
                .withSocketTimeout(Duration.parse(socketTimeout))
                .withDefaultHeaders(headers)
                .build();
    }
}