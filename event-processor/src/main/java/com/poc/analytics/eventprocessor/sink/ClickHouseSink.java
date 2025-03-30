package com.poc.analytics.eventprocessor.sink;

import com.poc.analytics.eventprocessor.model.AggregatedEvent;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ClickHouseSink {

        public static SinkFunction<AggregatedEvent> createSink() {
                return JdbcSink.sink(
                                "INSERT INTO aggregated_events (event_type, country, count, window_start, window_end, processed_at) VALUES (?, ?, ?, ?, ?, ?)",
                                new JdbcStatementBuilder<AggregatedEvent>() {
                                        @Override
                                        public void accept(PreparedStatement ps, AggregatedEvent event)
                                                        throws SQLException {
                                                ps.setString(1, event.getEventType());
                                                ps.setString(2, event.getCountry());
                                                ps.setLong(3, event.getCount());
                                                ps.setTimestamp(4, new Timestamp(event.getWindowStart()));
                                                ps.setTimestamp(5, new Timestamp(event.getWindowEnd()));
                                                ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                                        }
                                },
                                JdbcExecutionOptions.builder()
                                                .withBatchSize(1000)
                                                .withBatchIntervalMs(5000)
                                                .withMaxRetries(3)
                                                .build(),
                                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                                                .withUrl("jdbc:clickhouse://localhost:8123/analytics")
                                                .withDriverName("com.clickhouse.jdbc.ClickHouseDriver")
                                                .build());
        }
}