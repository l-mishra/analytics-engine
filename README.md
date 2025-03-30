# Analytics Engine

A real-time analytics engine built with Apache Flink, Kafka, Elasticsearch, and ClickHouse. This project processes streaming events, performs real-time analytics, and provides query capabilities through a REST API.

## Project Structure

The project consists of three main modules:

1. **event-producer**: A Kafka producer that generates sample events
2. **event-processor**: Flink jobs for processing and analyzing events
3. **query-api**: REST API for querying analytics results

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- Docker and Docker Compose (for running infrastructure)
- Apache Flink 1.16.1
- Apache Kafka
- Elasticsearch 7.x
- ClickHouse

## Infrastructure Setup

1. Start the required services using Docker Compose:
```bash
docker-compose up -d
```

This will start:
- Kafka
- Elasticsearch
- ClickHouse
- Flink JobManager
- Flink TaskManager

## Building the Project

Build all modules:
```bash
mvn clean package -DskipTests
```

## Running the Components

### 1. Event Producer

The event producer generates sample events and sends them to Kafka. To start the producer:

```bash
java -jar event-producer/target/event-producer-0.0.1-SNAPSHOT.jar
```

This will start generating events and sending them to the following Kafka topics:
- `user-events`
- `product-events`
- `order-events`

### 2. Event Processor (Flink Jobs)

The event processor contains Flink jobs for processing events. To submit a job:

```bash
flink run event-processor/target/event-processor-0.0.1-SNAPSHOT.jar
```

The processor includes the following jobs:
- User Activity Analysis
- Product Performance Analysis
- Order Analytics

### 3. Query API

The query API provides REST endpoints for querying analytics results. To start the API:

```bash
java -jar query-api/target/query-api-0.0.1-SNAPSHOT.jar
```

## API Usage

### User Analytics

1. Get user activity metrics:
```bash
curl -X GET "http://localhost:8080/api/analytics/users/activity?userId=123"
```

2. Get user engagement score:
```bash
curl -X GET "http://localhost:8080/api/analytics/users/engagement?userId=123"
```

### Product Analytics

1. Get product performance metrics:
```bash
curl -X GET "http://localhost:8080/api/analytics/products/performance?productId=456"
```

2. Get product trends:
```bash
curl -X GET "http://localhost:8080/api/analytics/products/trends?productId=456"
```

### Order Analytics

1. Get order statistics:
```bash
curl -X GET "http://localhost:8080/api/analytics/orders/stats?orderId=789"
```

2. Get order trends:
```bash
curl -X GET "http://localhost:8080/api/analytics/orders/trends"
```

## Monitoring

- Flink Web UI: http://localhost:8081
- Elasticsearch: http://localhost:9200
- ClickHouse: http://localhost:8123

## Configuration

Configuration files for each module can be found in their respective `src/main/resources` directories:

- `event-producer/src/main/resources/application.properties`
- `event-processor/src/main/resources/application.properties`
- `query-api/src/main/resources/application.properties`

## Development

### Adding New Analytics

1. Create a new Flink job in the `event-processor` module
2. Implement the processing logic
3. Add corresponding REST endpoints in the `query-api` module
4. Update the documentation

### Adding New Event Types

1. Create a new event class in the `event-producer` module
2. Add event generation logic
3. Create corresponding Flink processing logic
4. Add query endpoints

## Troubleshooting

1. Check Flink job status:
```bash
flink list
```

2. View Flink job logs:
```bash
flink logs <job-id>
```

3. Check Kafka topics:
```bash
kafka-topics.sh --list --bootstrap-server localhost:9092
```

4. Monitor Elasticsearch indices:
```bash
curl -X GET "http://localhost:9200/_cat/indices?v"
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 