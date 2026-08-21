# AWS SNS — Complete Interview Notes
> Simple Notification Service — Quick Reference

---

## 1. What is SNS?

```
SNS = Simple Notification Service

→ fully managed pub/sub messaging service
→ publisher sends ONE message to SNS topic
→ SNS delivers to ALL subscribers simultaneously
→ push-based (SNS pushes to subscribers)
→ serverless — no infrastructure to manage
```

---

## 2. Key Concepts

```
Topic       → channel where messages are published
             like a radio station broadcast

Publisher   → sends message to topic
             (your Spring Boot app, Lambda, CloudWatch)

Subscriber  → receives message from topic
             (SQS, Lambda, Email, HTTP, SMS, Mobile Push)

Message     → data sent to topic
             max size = 256KB
```

---

## 3. How SNS Works

```
Publisher
    ↓ publishes message
SNS Topic
    ↓ fans out to ALL subscribers simultaneously
    ├── SQS Queue 1      (order processing)
    ├── SQS Queue 2      (inventory update)
    ├── Lambda Function  (send email)
    ├── HTTP endpoint    (webhook)
    └── SMS              (alert)

One publish → many receivers ✅
```

---

## 4. SNS vs SQS

| | SNS | SQS |
|---|---|---|
| **Model** | Pub/Sub (push) | Queue (pull) |
| **Delivery** | Push to subscribers | Consumer pulls |
| **Retention** | No storage | Up to 14 days |
| **Receivers** | Multiple simultaneously | One consumer at a time |
| **Use case** | Fan-out, broadcast | Decoupled processing |
| **Direction** | One to many | One to one |

---

## 5. SNS Fan-out Pattern (most important)

```
Without fan-out:
Publisher → SQS 1 (payment)    ← 3 separate publishes ❌
Publisher → SQS 2 (inventory)
Publisher → SQS 3 (notification)

With SNS fan-out:
Publisher → SNS Topic → SQS 1 (payment)     ← one publish ✅
                      → SQS 2 (inventory)
                      → SQS 3 (notification)
```

```java
// Spring Boot — publish to SNS
@Service
public class OrderEventPublisher {

    @Autowired
    private SnsTemplate snsTemplate;

    public void publishOrderPlaced(Order order) {
        snsTemplate.sendNotification(
            "arn:aws:sns:us-east-1:123456789:order-events",
            order,
            "ORDER_PLACED"  // subject
        );
        // SNS delivers to ALL subscribers simultaneously ✅
    }
}
```

---

## 6. SNS Subscriber Types

```
1. SQS          → most common — durable queue ✅
2. Lambda       → serverless processing
3. HTTP/HTTPS   → webhook endpoint
4. Email        → send email notification
5. Email-JSON   → send email in JSON format
6. SMS          → text message
7. Mobile Push  → iOS (APNs), Android (FCM/GCM)
8. Firehose     → stream to S3/Elasticsearch
```

---

## 7. Message Filtering

```
Without filtering:
ALL subscribers receive ALL messages ❌
→ payment service receives shipping messages too

With filtering:
Each subscriber gets ONLY relevant messages ✅
```

```json
// SNS filter policy on SQS subscription
{
  "eventType": ["ORDER_PLACED", "ORDER_CANCELLED"]
}
// this SQS only receives ORDER_PLACED and ORDER_CANCELLED
// ignores PAYMENT_PROCESSED, SHIPPING_UPDATED etc ✅
```

```java
// Spring Boot — publish with message attributes for filtering
@Service
public class OrderEventPublisher {

    public void publishEvent(String eventType, Order order) {
        Map<String, MessageAttributeValue> attributes = new HashMap<>();
        attributes.put("eventType", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(eventType)  // "ORDER_PLACED"
                .build());

        snsClient.publish(PublishRequest.builder()
                .topicArn("arn:aws:sns:us-east-1:123:order-events")
                .message(objectMapper.writeValueAsString(order))
                .messageAttributes(attributes)  // ← for filtering ✅
                .build());
    }
}
```

---

## 8. SNS + SQS Together

```
Best practice — always put SQS between SNS and consumer:

SNS Topic
    ↓
SQS Queue   ← buffer + retry + DLQ ✅
    ↓
Consumer (Lambda / Spring Boot)

Why:
→ SNS delivers once — if consumer down → message LOST ❌
→ SQS holds message up to 14 days ✅
→ SQS retries on failure ✅
→ SQS has DLQ for poison messages ✅
```

---

## 9. Standard vs FIFO Topic

### Standard Topic
```
→ best effort ordering (may be out of order)
→ at-least-once delivery (may get duplicate)
→ high throughput — unlimited messages/sec
→ all subscriber types supported
→ use for: notifications, alerts, fan-out
```

### FIFO Topic
```
→ strict ordering guaranteed ✅
→ exactly-once delivery ✅
→ limited throughput (300 msg/sec)
→ only SQS FIFO subscribers
→ name must end with .fifo
→ use for: financial events, order processing
```

```java
// FIFO topic publish — needs MessageGroupId
snsClient.publish(PublishRequest.builder()
        .topicArn("arn:aws:sns:us-east-1:123:order-events.fifo")
        .message(orderJson)
        .messageGroupId("customer-" + customerId)  // ordering per customer ✅
        .messageDeduplicationId(UUID.randomUUID().toString()) // dedup ✅
        .build());
```

---

## 10. Dead Letter Queue for SNS

```
SNS delivery fails → retry → still fails → DLQ

SNS → HTTP endpoint (down)
    → retry 3 times
    → still down
    → message sent to DLQ (SQS) ✅
    → investigate later
```

```yaml
# CloudFormation / CDK
# set DLQ on SNS subscription
RedrivePolicy:
  deadLetterTargetArn: arn:aws:sqs:us-east-1:123:my-dlq
```

---

## 11. SNS Message Structure

```json
{
  "Type": "Notification",
  "MessageId": "uuid",
  "TopicArn": "arn:aws:sns:us-east-1:123:order-events",
  "Subject": "ORDER_PLACED",
  "Message": "{\"orderId\":\"ORD001\",\"amount\":100}",
  "Timestamp": "2024-01-15T10:30:00.000Z",
  "SignatureVersion": "1",
  "Signature": "...",
  "MessageAttributes": {
    "eventType": {
      "Type": "String",
      "Value": "ORDER_PLACED"
    }
  }
}
```

---

## 12. Spring Boot Integration

### Dependencies
```xml
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter-sns</artifactId>
</dependency>
```

### application.yml
```yaml
spring:
  cloud:
    aws:
      region:
        static: us-east-1
      credentials:
        access-key: ${AWS_ACCESS_KEY}
        secret-key: ${AWS_SECRET_KEY}

# SNS topic ARN
cloud:
  aws:
    sns:
      topic:
        order-events: arn:aws:sns:us-east-1:123:order-events
```

### Publish message
```java
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SnsTemplate snsTemplate;

    @Value("${cloud.aws.sns.topic.order-events}")
    private String orderEventsTopic;

    // Option 1 — SnsTemplate (simple)
    public void publishOrderEvent(OrderEvent event) {
        snsTemplate.sendNotification(
                orderEventsTopic,
                event,
                "ORDER_PLACED"
        );
    }

    // Option 2 — SnsClient (full control)
    public void publishWithAttributes(OrderEvent event) {
        snsClient.publish(PublishRequest.builder()
                .topicArn(orderEventsTopic)
                .message(objectMapper.writeValueAsString(event))
                .subject("ORDER_PLACED")
                .messageAttributes(Map.of(
                    "eventType", MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue("ORDER_PLACED")
                            .build()
                ))
                .build());
    }
}
```

### Receive via SQS (subscribed to SNS)
```java
@Service
public class OrderEventConsumer {

    @SqsListener("order-events-queue")  // SQS subscribed to SNS
    public void handleOrderEvent(@NotificationMessage OrderEvent event) {
        // @NotificationMessage unwraps SNS envelope automatically ✅
        processOrder(event);
    }
}
```

---

## 13. SNS Security

### Topic Policy (who can publish)
```json
{
  "Statement": [{
    "Effect": "Allow",
    "Principal": {
      "Service": "events.amazonaws.com"  // CloudWatch Events can publish
    },
    "Action": "SNS:Publish",
    "Resource": "arn:aws:sns:us-east-1:123:order-events"
  }]
}
```

### Encryption
```
→ SSE (Server Side Encryption) using AWS KMS
→ encrypt messages at rest ✅
→ enable on topic creation or update
```

---

## 14. SNS Use Cases

```
1. Fan-out             → one event → multiple services
2. Alert/Monitoring    → CloudWatch alarm → SNS → Email/SMS
3. Mobile Push         → send push notifications to iOS/Android
4. Email notifications → order confirmation, password reset
5. Cross-region        → replicate events across AWS regions
6. Webhook             → notify external systems via HTTP
```

---

## 15. Common Interview Questions

| Question | Answer |
|---|---|
| **SNS vs SQS** | SNS = push pub/sub (one to many), SQS = pull queue (one to one) |
| **Fan-out pattern** | One SNS publish → multiple SQS queues simultaneously |
| **Message retention** | SNS has NO retention — deliver or lose. Use SQS for retention |
| **Max message size** | 256KB |
| **Filter policy** | Filter messages per subscriber using message attributes |
| **FIFO vs Standard** | FIFO = ordered + exactly once (300/sec), Standard = unordered + at-least-once (unlimited) |
| **SNS + SQS why** | SQS adds durability, retry, DLQ to SNS delivery |
| **DLQ in SNS** | Set on subscription — failed deliveries go to DLQ SQS |
| **Who can publish** | IAM policy or topic policy controls publish permissions |
| **Encryption** | SSE with KMS for messages at rest |

---

## 16. SNS vs Kafka vs SQS

| | SNS | SQS | Kafka |
|---|---|---|---|
| **Model** | Pub/Sub push | Queue pull | Pub/Sub pull |
| **Retention** | None | 14 days | Days/forever |
| **Replay** | ❌ No | ❌ No | ✅ Yes |
| **Ordering** | ❌ (FIFO topic yes) | ❌ (FIFO queue yes) | ✅ Per partition |
| **Throughput** | High | High | ✅ Very high |
| **Fan-out** | ✅ Built-in | ❌ | ✅ Consumer groups |
| **Use case** | Notifications, alerts | Task queue | Event streaming |
