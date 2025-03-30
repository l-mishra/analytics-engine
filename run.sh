#!/bin/bash

# Get the directory of this script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Build the classpath
CP="$DIR/event-processor/target/event-processor-0.0.1-SNAPSHOT.jar"

# Add Flink library JARs to classpath if FLINK_HOME is set
if [ -n "$FLINK_HOME" ]; then
    for jar in "$FLINK_HOME/lib"/*.jar; do
        CP="$CP:$jar"
    done
fi

# Add shaded ASM dependency to classpath
CP="$CP:$(find ~/.m2/repository/org/apache/flink/flink-shaded-asm-9 -name '*.jar' | head -n 1)"

# Run the application with JVM arguments
java \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.base/java.io=ALL-UNNAMED \
    --add-opens java.base/java.util=ALL-UNNAMED \
    --add-opens java.base/java.util.concurrent=ALL-UNNAMED \
    -cp "$CP" \
    com.poc.analytics.eventprocessor.EventProcessorApplication 