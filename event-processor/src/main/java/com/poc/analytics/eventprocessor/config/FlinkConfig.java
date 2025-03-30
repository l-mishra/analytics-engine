package com.poc.analytics.eventprocessor.config;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FlinkConfig {

    public static void configureEnvironment(StreamExecutionEnvironment env) {
        // Set restart strategy
        env.setRestartStrategy(
                RestartStrategies.fixedDelayRestart(
                        3, // number of restart attempts
                        Time.seconds(10) // delay between attempts
                ));

        // Set parallelism
        env.setParallelism(4);
    }
}