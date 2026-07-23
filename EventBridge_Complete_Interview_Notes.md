# AWS EventBridge — Complete Interview Notes
> Serverless Event Bus — Quick Reference

---

## 1. What is EventBridge?

```
EventBridge = Serverless Event Bus (formerly CloudWatch Events)

→ routes events from sources to targets
→ fully managed — no infrastructure needed
→ connects AWS services + custom apps + SaaS apps
→ event-driven architecture backbone on AWS
→ renamed from CloudWatch Events in 2019
```

---

## 2. Key Concepts

```
Event           → JSON message describing something that happened
                  { "source": "order-service", "detail-type": "ORDER_PLACED" }

Event Bus       → channel that receives and routes events
                  (default bus, custom bus, partner bus)

Rule            → filter — matches events and routes to targets
                  "if eventType = ORDER_PLACED → send to Lambda"

Target          → destination that receives matched events
                  (Lambda, SQS, SNS, ECS, Step Functions, HTTP, etc.)

Schema Registry → catalog of event schemas (auto-discovered)
```

---

## 3. EventBridge vs SNS vs SQS

| | EventBridge | SNS | SQS |
|---|---|---|---|
| **Model** | Event routing | Pub/Sub push | Queue pull |
| **Filtering** | ✅ Rich content filtering | ✅ Basic | ❌ No |
| **Targets** | 20+ AWS services | Limited | One consumer |
| **SaaS integration** | ✅ Built-in | ❌ | ❌ |
| **Scheduling** | ✅ Built-in cron | ❌ | ❌ |
| **Replay** | ✅ Archive + replay | ❌ | ❌ |
| **Schema registry** | ✅ | ❌ | ❌ |
| **Throughput** | 10,000/sec default | High | High |
| **Use case** | Complex routing | Fan-out | Task queue |

---

## 4. Event Buses

### Default Bus
```
→ receives events from AWS services automatically
→ CloudTrail, EC2, RDS, S3 state changes etc.
→ cannot be deleted
→ all AWS service events go here

Example events on default bus:
→ EC2 instance state changed (running → stopped)
→ RDS DB instance created
→ S3 bucket policy changed
→ CodePipeline state change
```

### Custom Bus
```
→ create your own bus for application events
→ isolate events per domain/team
→ cross-account event sharing

// order-service-bus — only order events
// payment-service-bus — only payment events
```

### Partner Bus
```
→ receive events from SaaS partners
→ Shopify, Zendesk, Datadog, PagerDuty, GitHub, Stripe etc.
→ no custom integration needed ✅
```

---

## 5. Event Structure

```json
{
  "version": "0",
  "id": "uuid-1234",
  "source": "com.myapp.order-service",    ← who sent it
  "account": "123456789012",
  "time": "2024-01-15T10:30:00Z",
  "region": "us-east-1",
  "detail-type": "Order Placed",           ← event type
  "detail": {                              ← event payload
    "orderId": "ORD001",
    "customerId": "CUST001",
    "amount": 100.00,
    "status": "PLACED",
    "items": ["item1", "item2"]
  }
}
```

---

## 6. Rules and Filtering

### Event Pattern (filter)
```json
// match specific events only
{
  "source": ["com.myapp.order-service"],
  "detail-type": ["Order Placed"],
  "detail": {
    "amount": [{ "numeric": [">", 100] }],   // amount > 100
    "status": ["PLACED", "CONFIRMED"]         // status in list
  }
}
```

### Advanced filtering options
```json
// prefix matching
{ "detail": { "orderId": [{ "prefix": "ORD" }] }}

// anything but
{ "detail": { "status": [{ "anything-but": "CANCELLED" }] }}

// exists check
{ "detail": { "discount": [{ "exists": true }] }}

// numeric range
{ "detail": { "amount": [{ "numeric": [">=", 100, "<=", 1000] }] }}

// IP address CIDR
{ "detail": { "ip": [{ "cidr": "10.0.0.0/24" }] }}
```

---

## 7. Targets (20+ supported)

```
Lambda Function      → serverless processing
SQS Queue           → queue for async processing
SNS Topic           → fan-out notifications
ECS Task            → run container task
Step Functions      → orchestrate workflow
API Gateway         → call REST endpoint
EventBridge Bus     → cross-account/region routing
Kinesis Stream      → real-time streaming
Firehose            → stream to S3/Elasticsearch
CloudWatch Logs     → log events
EC2 Run Command     → run command on EC2
SSM Automation      → run SSM documents
CodePipeline        → trigger CI/CD pipeline
CodeBuild           → trigger build
Batch Job           → trigger batch job
HTTP Endpoint       → call any HTTP URL
```

---

## 8. Spring Boot Integration

### Dependencies
```xml
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter</artifactId>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>eventbridge</artifactId>
</dependency>
```

### Publish event to EventBridge
```java
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final EventBridgeClient eventBridgeClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.eventbridge.bus-name}")
    private String eventBusName;

    public void publishOrderPlaced(Order order) {
        try {
            // build event detail
            String detail = objectMapper.writeValueAsString(Map.of(
                "orderId",    order.getId(),
                "customerId", order.getCustomerId(),
                "amount",     order.getAmount(),
                "status",     "PLACED"
            ));

            // put event to EventBridge
            PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                    .source("com.myapp.order-service")       // source
                    .detailType("Order Placed")               // event type
                    .detail(detail)                           // payload
                    .eventBusName(eventBusName)               // which bus
                    .build();

            PutEventsResponse response = eventBridgeClient.putEvents(
                PutEventsRequest.builder()
                    .entries(entry)
                    .build()
            );

            if (response.failedEntryCount() > 0) {
                log.error("Failed to publish event: {}", response.entries());
            } else {
                log.info("Event published successfully");
            }

        } catch (Exception e) {
            log.error("Error publishing event: {}", e.getMessage());
        }
    }

    // publish multiple events in batch (max 10 per call)
    public void publishBatch(List<Order> orders) {
        List<PutEventsRequestEntry> entries = orders.stream()
                .map(order -> PutEventsRequestEntry.builder()
                        .source("com.myapp.order-service")
                        .detailType("Order Placed")
                        .detail(toJson(order))
                        .eventBusName(eventBusName)
                        .build())
                .toList();

        eventBridgeClient.putEvents(
            PutEventsRequest.builder()
                .entries(entries)
                .build()
        );
    }
}
```

### Configuration
```java
@Configuration
public class EventBridgeConfig {

    @Bean
    public EventBridgeClient eventBridgeClient() {
        return EventBridgeClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
```

### application.yml
```yaml
aws:
  eventbridge:
    bus-name: order-service-bus

spring:
  cloud:
    aws:
      region:
        static: us-east-1
```

---

## 9. Receiving Events (via SQS or Lambda)

### Via SQS (best practice)
```
EventBridge Rule → SQS Queue → Spring Boot consumer

Why SQS between EventBridge and consumer:
→ EventBridge delivers once — no retry if consumer down ❌
→ SQS buffers + retries + DLQ ✅
```

```java
// consume EventBridge event from SQS
@Service
public class OrderEventConsumer {

    @SqsListener("order-placed-queue")
    public void handleOrderPlaced(String message) {
        // EventBridge wraps payload in envelope
        EventBridgeEvent event = objectMapper.readValue(message,
                EventBridgeEvent.class);

        OrderDetail order = objectMapper.convertValue(
                event.getDetail(), OrderDetail.class);

        log.info("Processing order: {}", order.getOrderId());
        processOrder(order);
    }
}

// EventBridge event wrapper
@Data
public class EventBridgeEvent {
    private String source;
    private String detailType;
    private Map<String, Object> detail;  // actual payload
    private String time;
    private String region;
}
```

### Via Lambda (serverless)
```java
// Lambda function triggered by EventBridge
public class OrderEventHandler
        implements RequestHandler<Map<String, Object>, Void> {

    @Override
    public Void handleRequest(Map<String, Object> event, Context context) {
        Map<String, Object> detail =
                (Map<String, Object>) event.get("detail");

        String orderId = (String) detail.get("orderId");
        log.info("Processing order: {}", orderId);
        return null;
    }
}
```

---

## 10. Scheduling (Cron Jobs)

```
EventBridge Scheduler replaces CloudWatch Events cron
→ schedule Lambda, ECS tasks, Step Functions etc.
→ rate or cron expression
```

### Rate expression
```
rate(5 minutes)   → every 5 minutes
rate(1 hour)      → every hour
rate(1 day)       → every day
```

### Cron expression
```
cron(0 12 * * ? *)      → every day at noon UTC
cron(0 8 ? * MON-FRI *) → weekdays at 8am UTC
cron(0 0 1 * ? *)       → first day of month at midnight
```

### Terraform — scheduled rule
```hcl
# trigger Lambda every day at 8am
resource "aws_cloudwatch_event_rule" "daily_report" {
  name                = "daily-report-rule"
  schedule_expression = "cron(0 8 * * ? *)"
}

resource "aws_cloudwatch_event_target" "lambda" {
  rule      = aws_cloudwatch_event_rule.daily_report.name
  target_id = "DailyReportLambda"
  arn       = aws_lambda_function.daily_report.arn
}
```

---

## 11. Archive and Replay

```
Archive:
→ store all events matching a pattern
→ replay them later for debugging or reprocessing

Use cases:
→ bug in consumer → fix bug → replay missed events ✅
→ new service → replay historical events to populate data ✅
→ disaster recovery → replay events to rebuild state ✅
```

```hcl
# Terraform — create archive
resource "aws_cloudwatch_event_archive" "order_archive" {
  name             = "order-events-archive"
  event_source_arn = aws_cloudwatch_event_bus.order_bus.arn
  retention_days   = 30  # keep 30 days of events

  event_pattern = jsonencode({
    source = ["com.myapp.order-service"]
  })
}
```

---

## 12. Schema Registry

```
Schema Registry:
→ auto-discovers event schemas from EventBridge
→ generates code bindings (Java, Python, TypeScript)
→ documents event structure ✅

Enable schema discovery:
→ EventBridge → Schema Registry → Enable discovery

Generated Java code:
→ strongly typed event classes
→ no manual JSON parsing needed ✅
```

---

## 13. Cross-Account and Cross-Region

```
Cross-account:
Account A (producer) → EventBridge → Account B (consumer)

Use case:
→ central event bus in shared services account
→ all microservice accounts send events there
→ centralized event routing ✅
```

```hcl
# allow Account B to send events to Account A bus
resource "aws_cloudwatch_event_bus_policy" "cross_account" {
  event_bus_name = aws_cloudwatch_event_bus.main.name

  policy = jsonencode({
    Statement = [{
      Effect    = "Allow"
      Principal = { AWS = "arn:aws:iam::ACCOUNT_B_ID:root" }
      Action    = "events:PutEvents"
      Resource  = aws_cloudwatch_event_bus.main.arn
    }]
  })
}
```

---

## 14. Error Handling and DLQ

```
EventBridge delivery fails:
→ retries with exponential backoff (up to 24 hours)
→ after max retries → sends to DLQ (SQS) ✅
```

```hcl
# Terraform — add DLQ to EventBridge rule target
resource "aws_cloudwatch_event_target" "order_lambda" {
  rule      = aws_cloudwatch_event_rule.order_placed.name
  target_id = "OrderLambda"
  arn       = aws_lambda_function.order_processor.arn

  dead_letter_config {
    arn = aws_sqs_queue.dlq.arn  # failed events go here ✅
  }

  retry_policy {
    maximum_retry_attempts       = 3
    maximum_event_age_in_seconds = 3600  # retry for 1 hour
  }
}
```

---

## 15. Terraform — Full EventBridge Setup

```hcl
# Custom event bus
resource "aws_cloudwatch_event_bus" "order_bus" {
  name = "order-service-bus"
}

# Rule — match Order Placed events
resource "aws_cloudwatch_event_rule" "order_placed" {
  name           = "order-placed-rule"
  event_bus_name = aws_cloudwatch_event_bus.order_bus.name

  event_pattern = jsonencode({
    source      = ["com.myapp.order-service"]
    detail-type = ["Order Placed"]
  })
}

# Target 1 — SQS for payment processing
resource "aws_cloudwatch_event_target" "payment_queue" {
  rule           = aws_cloudwatch_event_rule.order_placed.name
  event_bus_name = aws_cloudwatch_event_bus.order_bus.name
  target_id      = "PaymentQueue"
  arn            = aws_sqs_queue.payment_queue.arn
}

# Target 2 — Lambda for notification
resource "aws_cloudwatch_event_target" "notification_lambda" {
  rule           = aws_cloudwatch_event_rule.order_placed.name
  event_bus_name = aws_cloudwatch_event_bus.order_bus.name
  target_id      = "NotificationLambda"
  arn            = aws_lambda_function.notification.arn

  dead_letter_config {
    arn = aws_sqs_queue.dlq.arn
  }
}

# Target 3 — ECS task for inventory update
resource "aws_cloudwatch_event_target" "inventory_ecs" {
  rule           = aws_cloudwatch_event_rule.order_placed.name
  event_bus_name = aws_cloudwatch_event_bus.order_bus.name
  target_id      = "InventoryECS"
  arn            = aws_ecs_cluster.main.arn

  ecs_target {
    task_definition_arn = aws_ecs_task_definition.inventory.arn
    launch_type         = "FARGATE"
    network_configuration {
      subnets = var.private_subnets
    }
  }
}
```

---

## 16. Common Interview Questions

| Question | Answer |
|---|---|
| **EventBridge vs SNS** | EventBridge = rich filtering + 20+ targets + scheduling + SaaS. SNS = simple fan-out push |
| **EventBridge vs CloudWatch Events** | Same service — renamed to EventBridge in 2019 with extra features |
| **Default vs Custom bus** | Default = AWS service events. Custom = your app events |
| **Max targets per rule** | 5 targets per rule |
| **Max events per PutEvents** | 10 events per API call |
| **Max event size** | 256KB |
| **Retry behavior** | Exponential backoff up to 24 hours |
| **DLQ** | Set on target — failed deliveries after retries go to SQS DLQ |
| **Archive replay** | Archive events → fix bug → replay to reprocess missed events |
| **Scheduling** | Built-in cron/rate expressions — replaces CloudWatch Events cron |
| **Schema registry** | Auto-discovers event schemas, generates typed code bindings |
| **Cross-account** | Set bus policy to allow other accounts to PutEvents |
| **Partner events** | Receive events from SaaS (Shopify, Stripe, GitHub) without custom integration |
| **Input transformer** | Transform event payload before sending to target |

---

## 17. Best Practices

```
✅ Use custom bus per domain (order-bus, payment-bus)
✅ Always put SQS between EventBridge and consumer (durability)
✅ Set DLQ on all targets (no lost events)
✅ Use schema registry for event documentation
✅ Enable archive for event replay capability
✅ Use specific event patterns (not catch-all rules)
✅ Tag all EventBridge resources
✅ Use input transformer to reshape payload for targets
✅ Monitor with CloudWatch metrics (MatchedEvents, FailedInvocations)
❌ Never use default bus for application events
❌ Never send sensitive data in events without encryption
❌ Never skip DLQ — you will lose events on failures
```

---

## 18. EventBridge vs Kafka

| | EventBridge | Kafka |
|---|---|---|
| **Managed** | ✅ Fully managed | ❌ Self-managed (or MSK) |
| **Retention** | Archive (configurable) | Days to forever |
| **Replay** | ✅ Archive replay | ✅ Offset replay |
| **Throughput** | 10,000/sec default | ✅ Millions/sec |
| **Ordering** | ❌ Not guaranteed | ✅ Per partition |
| **Filtering** | ✅ Rich content filter | ❌ Consumer filters |
| **SaaS events** | ✅ Built-in partners | ❌ Custom only |
| **Scheduling** | ✅ Built-in | ❌ No |
| **Cost** | Per event | Per broker hour |
| **Use case** | AWS event routing | High throughput streaming |
