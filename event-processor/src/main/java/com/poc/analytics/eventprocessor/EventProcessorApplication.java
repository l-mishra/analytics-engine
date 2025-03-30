package com.poc.analytics.eventprocessor;

import com.poc.analytics.eventprocessor.config.FlinkConfig;
import com.poc.analytics.eventprocessor.job.EventProcessingJob;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;

public class EventProcessorApplication {
    public static void main(String[] args) throws Exception {
        // Add JVM arguments for Java module access
        System.setProperty("illegal.reflection.warn", "false");
        System.setProperty("illegal.reflection.ignore", "true");
        System.setProperty("java.lang.reflect.allowAccessToPrivateFields", "true");
        System.setProperty("java.lang.reflect.allowAccessToPrivateMethods", "true");
        System.setProperty("java.lang.reflect.allowAccessToPrivateConstructors", "true");

        // Configure Flink client
        Configuration conf = new Configuration();
        conf.setString(RestOptions.ADDRESS, "localhost");
        conf.setInteger(RestOptions.PORT, 8081);

        // Create remote execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createRemoteEnvironment(
                "localhost", 8081, conf);

        // Configure execution environment
        env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 10000));

        // Create and execute the job
        EventProcessingJob.createJob(env);
        env.execute("Event Processing Job");
    }
}