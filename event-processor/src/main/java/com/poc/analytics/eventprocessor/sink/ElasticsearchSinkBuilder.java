package com.poc.analytics.eventprocessor.sink;

import com.poc.analytics.eventprocessor.model.Event;
import java.util.ArrayList;
import java.util.List;
import org.apache.flink.streaming.connectors.elasticsearch7.ElasticsearchSink;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class ElasticsearchSinkBuilder {

    public static SinkFunction<Event> createSink() {
        List<HttpHost> httpHosts = new ArrayList<>();
        httpHosts.add(new HttpHost("localhost", 9200, "http"));

        ElasticsearchSink.Builder<Event> esSinkBuilder = new ElasticsearchSink.Builder<>(
                httpHosts,
                (element, ctx, indexer) -> {
                    IndexRequest request = Requests.indexRequest()
                            .index("events")
                            .source(element.toString());
                    indexer.add(request);
                });

        esSinkBuilder.setBulkFlushMaxActions(1000);
        esSinkBuilder.setBulkFlushInterval(5000);

        return esSinkBuilder.build();
    }
}