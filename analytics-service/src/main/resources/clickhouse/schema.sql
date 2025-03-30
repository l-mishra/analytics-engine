-- Create database if not exists
CREATE DATABASE IF NOT EXISTS analytics;

-- Create table for raw events
CREATE TABLE IF NOT EXISTS analytics.raw_events
(
    event_id String,
    timestamp DateTime,
    user_id String,
    event_type String,
    session_id String,
    product_id String,
    metadata String,
    event_data String
) ENGINE = MergeTree()
ORDER BY (timestamp, event_id)
PARTITION BY toYYYYMM(timestamp);

-- Create table for aggregated events
CREATE TABLE IF NOT EXISTS analytics.aggregated_events
(
    timestamp DateTime,
    event_type String,
    product_id String,
    count UInt64,
    unique_users UInt64,
    aggregation_window String
) ENGINE = AggregatingMergeTree()
ORDER BY (timestamp, event_type, product_id)
PARTITION BY toYYYYMM(timestamp);

-- Create materialized view for hourly aggregations
CREATE MATERIALIZED VIEW IF NOT EXISTS analytics.hourly_aggregations
ENGINE = AggregatingMergeTree()
ORDER BY (timestamp, event_type, product_id)
PARTITION BY toYYYYMM(timestamp)
AS SELECT
    toStartOfHour(timestamp) as timestamp,
    event_type,
    product_id,
    count() as count,
    uniqExact(user_id) as unique_users,
    '1h' as aggregation_window
FROM analytics.raw_events
GROUP BY timestamp, event_type, product_id;

-- Create materialized view for daily aggregations
CREATE MATERIALIZED VIEW IF NOT EXISTS analytics.daily_aggregations
ENGINE = AggregatingMergeTree()
ORDER BY (timestamp, event_type, product_id)
PARTITION BY toYYYYMM(timestamp)
AS SELECT
    toStartOfDay(timestamp) as timestamp,
    event_type,
    product_id,
    count() as count,
    uniqExact(user_id) as unique_users,
    '1d' as aggregation_window
FROM analytics.raw_events
GROUP BY timestamp, event_type, product_id; 