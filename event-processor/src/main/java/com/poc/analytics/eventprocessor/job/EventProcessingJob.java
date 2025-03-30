package com.poc.analytics.eventprocessor.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.analytics.eventprocessor.model.AggregatedEvent;
import com.poc.analytics.eventprocessor.model.Event;
import com.poc.analytics.eventprocessor.sink.ClickHouseSink;
import com.poc.analytics.eventprocessor.sink.ElasticsearchSinkBuilder;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class EventProcessingJob {

    public static void createJob(StreamExecutionEnvironment env) throws Exception {
        // Create Kafka source
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("user-events")
                .setGroupId("event-processor-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> eventStream = env.fromSource(
                source,
                WatermarkStrategy.<String>forMonotonousTimestamps()
                        .withTimestampAssigner((event, timestamp) -> {
                            try {
                                ObjectMapper mapper = new ObjectMapper();
                                Event parsedEvent = mapper.readValue(event, Event.class);
                                return parsedEvent.getTimestamp().toEpochMilli();
                            } catch (Exception e) {
                                return 0L;
                            }
                        }),
                "Kafka Source");

        // Parse events
        DataStream<Event> parsedEvents = eventStream
                .map(event -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(event, Event.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(event -> event != null);

        // Aggregate events by event type and country
        DataStream<AggregatedEvent> aggregatedEvents = parsedEvents
                .keyBy(event -> event.getEventType() + ":" + event.getCountry())
                .window(TumblingEventTimeWindows.of(Time.minutes(5)))
                .process(new EventWindowFunction());

        // Store raw events in Elasticsearch
        parsedEvents.addSink(ElasticsearchSinkBuilder.createSink());

        // Store aggregated events in ClickHouse
        aggregatedEvents.addSink(ClickHouseSink.createSink());

        // Send alerts for high-frequency events
        KafkaSink<String> alertSink = KafkaSink.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setRecordSerializer(KafkaRecordSerializationSchema.<String>builder()
                        .setTopic("alerts")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .build();

        aggregatedEvents
                .filter(event -> event.getCount() >= 1)
                .map(event -> {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "HIGH_FREQUENCY_EVENT");
                    alert.put("eventType", event.getEventType());
                    alert.put("country", event.getCountry());
                    alert.put("count", event.getCount());
                    alert.put("timestamp", System.currentTimeMillis());
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.writeValueAsString(alert);
                    } catch (Exception e) {
                        return "{}";
                    }
                })
                .sinkTo(alertSink);
    }

    private static class EventWindowFunction
            extends ProcessWindowFunction<Event, AggregatedEvent, String, TimeWindow> {
        @Override
        public void process(String key, Context context, Iterable<Event> events,
                Collector<AggregatedEvent> out) {
            Map<String, Long> aggregated = new HashMap<>();
            for (Event event : events) {
                aggregated.merge(event.getEventType(), 1L, Long::sum);
            }

            String[] parts = key.split(":");
            String eventType = parts[0];
            String country = parts[1];

            AggregatedEvent result = new AggregatedEvent();
            result.setEventType(eventType);
            result.setCountry(country);
            result.setCount(aggregated.getOrDefault(eventType, 0L));
            result.setWindowStart(context.window().getStart());
            result.setWindowEnd(context.window().getEnd());

            out.collect(result);
        }
    }
}