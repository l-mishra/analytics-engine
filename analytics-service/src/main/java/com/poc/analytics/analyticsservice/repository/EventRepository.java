package com.poc.analytics.analyticsservice.repository;

import com.poc.analytics.analyticsservice.model.Event;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends ElasticsearchRepository<Event, String> {
}