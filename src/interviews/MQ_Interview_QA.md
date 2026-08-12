# Message Queue (MQ) — Interview Q&A
> 10 Questions with Correct Answers & Code Snippets

---

## Q1. What is a Message Queue? Why use it in microservices?

### Answer
```
MQ = Message Queue ✅
→ async communication ✅
→ producer does not wait ✅
→ decouples services ✅
→ absorbs traffic spikes ✅
→ consumer processes at own pace ✅
→ message retained if consumer down ✅
→ retry on failure ✅
→ DLQ for failed messages ✅

Without MQ:
→ Order Service → calls Payment Service ✅
→ Payment down → order fails ❌
→ cascade failure ❌

With MQ:
→ Order Service → puts in queue ✅
→ Payment down → message waits ✅
→ Payment up → processes ✅
→ no cascade failure ✅

Types:
→ RabbitMQ ✅
→ ActiveMQ ✅
→ AWS SQS ✅
→ Azure Service Bus ✅
→ IBM MQ ✅
→ Kafka (event streaming) ✅
```

| Without MQ | With MQ |
|---|---|
| Tight coupling ❌ | Loose coupling ✅ |
| Cascade failure ❌ | Resilient ✅ |
| Consumer must be up | Consumer can be down ✅ |
| Synchronous ❌ | Async ✅ |

---

## Q2. Point-to-Point vs Publish-Subscribe?

### Answer
```
Point to Point:
→ Queue based ✅
→ ONE consumer gets message ✅
→ message deleted after consumed ✅
→ PULL based ✅
→ load balanced between consumers ✅
→ SQS, RabbitMQ queue ✅

Pub/Sub:
→ Topic based ✅
→ ALL subscribers get message ✅
→ message NOT deleted after push ✅
→ PUSH based ✅
→ fan-out ✅
→ SNS, RabbitMQ exchange, Kafka ✅
```

```java
// Point to Point — SQS ✅
@SqsListener("payment-queue")
public void processPayment(OrderEvent event) {
    paymentService.process(event);
    // message deleted after processed ✅
    // other consumers do NOT get this ✅
}

// Pub/Sub — SNS ✅
snsClient.publish(PublishRequest.builder()
        .topicArn(topicArn)
        .message(toJson(event))
        .build());
// payment-service gets it ✅
// inventory-service gets it ✅
// notification-service gets it ✅

// Pub/Sub — Kafka ✅
@KafkaListener(topics = "order-events",
               groupId = "payment-group")
public void consume(OrderEvent event) { }
// payment-group gets message ✅
// inventory-group gets same message ✅
```

| | Point to Point | Pub/Sub |
|---|---|---|
| **Pattern** | Queue | Topic |
| **Consumers** | ONE gets message | ALL get message ✅ |
| **Delete** | After consumed ✅ | Not deleted ✅ |
| **Direction** | Pull ✅ | Push ✅ |
| **Fan-out** | ❌ No | ✅ Yes |
| **Example** | SQS, RabbitMQ Queue | SNS, Kafka Topic ✅ |

---

## Q3. What is Dead Letter Queue (DLQ)?

### Answer
```
DLQ = Dead Letter Queue ✅
→ receives bad/poison messages ✅
→ consumer retries N times ✅
→ still failing → moved to DLQ ✅
→ DLQ listener → log + alert ✅

Why message fails:
→ bad JSON format ❌
→ validation error ❌
→ downstream service down ❌
→ DB error ❌
→ business rule violation ❌

DLQ actions:
→ log + alert ✅
→ notify team (PagerDuty) ✅
→ manual investigation ✅
→ fix + replay messages ✅
```

```hcl
# Terraform — SQS + DLQ ✅
resource "aws_sqs_queue" "payment_dlq" {
  name = "payment-queue-dlq"
}

resource "aws_sqs_queue" "payment" {
  name = "payment-queue"
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.payment_dlq.arn
    maxReceiveCount     = 3 # retry 3 times ✅
  })
}
```

```java
// SQS DLQ listener ✅
@SqsListener("payment-queue-dlq")
public void handleDlq(OrderEvent event) {
    log.error("Poison message: {}",
            event.getOrderId());
    alertService.notifyTeam(event); // ✅
    dlqRepository.save(event);      // ✅
}

// Kafka DLT ✅
@RetryableTopic(
    attempts       = "4",
    backoff        = @Backoff(delay = 1000, multiplier = 2.0),
    dltTopicSuffix = "-dlt"
)
@KafkaListener(topics = "order-events")
public void consume(OrderEvent event) {
    orderService.process(event);
}

@DltHandler
public void handleDlt(OrderEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    log.error("Poison message in: {}", topic);
    alertService.notifyTeam(event); // ✅
}

// RabbitMQ DLQ ✅
@Bean
public Queue paymentQueue() {
    return QueueBuilder
            .durable("payment-queue")
            .withArgument("x-dead-letter-exchange",
                "dlx-exchange") // DLQ ✅
            .build();
}
```

```
DLQ flow:
Message arrives ✅
    ↓
Consumer fails ❌
Retry 1 ❌
Retry 2 ❌
Retry 3 ❌
    ↓
→ DLQ ✅
→ log + alert + store ✅
→ fix + replay ✅
```

---

## Q4. RabbitMQ Exchange Types — Direct, Topic, Fanout, Headers?

### Answer
```
RabbitMQ flow:
Producer → Exchange → Queue → Consumer ✅
Exchange = router — decides which queue ✅

Direct:
→ exact routing key match ✅
→ "payment" → payment queue ✅
→ use for: specific service routing ✅

Topic:
→ pattern based routing ✅
→ * = one word ✅
→ # = zero or more words ✅
→ "payment.#" → payment.success, payment.failed ✅
→ most flexible ✅

Fanout:
→ sends to ALL queues ✅
→ ignores routing key ✅
→ broadcast / pub-sub ✅

Headers:
→ routes based on message headers ✅
→ ignores routing key ✅
→ rarely used ⚠️
```

```java
// Direct Exchange ✅
@Bean
public DirectExchange directExchange() {
    return new DirectExchange("payment-direct");
}
@Bean
public Binding directBinding() {
    return BindingBuilder.bind(paymentQueue())
            .to(directExchange())
            .with("payment"); // exact key ✅
}

// Topic Exchange ✅
@Bean
public TopicExchange topicExchange() {
    return new TopicExchange("payment-topic");
}
@Bean
public Binding topicBinding() {
    return BindingBuilder.bind(paymentQueue())
            .to(topicExchange())
            .with("payment.#"); // pattern ✅
}
rabbitTemplate.convertAndSend(
    "payment-topic", "payment.success", event); // ✅

// Fanout Exchange ✅
@Bean
public FanoutExchange fanoutExchange() {
    return new FanoutExchange("payment-fanout");
}
@Bean
public Binding fanoutPayment() {
    return BindingBuilder.bind(paymentQueue())
            .to(fanoutExchange()); // no routing key ✅
}
@Bean
public Binding fanoutInventory() {
    return BindingBuilder.bind(inventoryQueue())
            .to(fanoutExchange()); // all get it ✅
}

// Headers Exchange ✅
@Bean
public Binding headersBinding() {
    return BindingBuilder.bind(paymentQueue())
            .to(headersExchange())
            .where("type").matches("payment")
            .and("region").matches("US"); // ✅
}
```

| Exchange | Routes by | Use case |
|---|---|---|
| **Direct** | Exact key match | Specific service ✅ |
| **Topic** | Pattern (* #) | Flexible routing ✅ |
| **Fanout** | All queues | Broadcast ✅ |
| **Headers** | Message headers | Complex rules ✅ |

---

## Q5. Message Acknowledgement — Auto-ack vs Manual-ack?

### Answer
```
Auto ack:
→ message deleted AS SOON as delivered ✅
→ consumer crashes → message LOST ❌
→ AT MOST ONCE delivery ❌
→ NOT recommended ❌

Manual ack:
→ message deleted ONLY after ack ✅
→ consumer crashes → message requeued ✅
→ AT LEAST ONCE delivery ✅
→ recommended for production ✅

Negative ack (nack):
→ processing failed ✅
→ requeue = true → back to queue ✅
→ requeue = false → DLQ ✅
```

```yaml
# application.yml
spring:
  rabbitmq:
    listener:
      simple:
        acknowledge-mode: manual # ✅
```

```java
// Manual Ack ✅
@RabbitListener(queues = "payment-queue")
public void consume(
        OrderEvent event,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG)
        long deliveryTag) throws IOException {

    try {
        orderService.process(event);
        channel.basicAck(deliveryTag, false); // ack ✅
    } catch (Exception e) {
        channel.basicNack(
            deliveryTag,
            false,
            true);   // requeue=true → retry ✅
                     // requeue=false → DLQ ✅
    }
}
```

| | Auto Ack | Manual Ack |
|---|---|---|
| **Delete when** | On delivery ❌ | After ack ✅ |
| **Crash safe** | ❌ No | ✅ Yes |
| **Delivery** | At most once ❌ | At least once ✅ |
| **Recommended** | ❌ No | ✅ Yes |

---

## Q6. Message TTL and Message Priority in RabbitMQ?

### Answer
```
TTL (Time To Live):
→ message deleted after TTL expires ✅
→ even if not consumed ✅
→ queue level OR message level ✅
→ expired → DLQ ✅

Use cases:
→ OTP expires 5 minutes ✅
→ flash sale messages expire ✅
→ stale order requests ✅

Priority:
→ messages have priority level ✅
→ high priority processed FIRST ✅
→ range: 0-255 ✅
→ 0=lowest, 255=highest ✅

Use cases:
→ payment > notification ✅
→ premium customer > regular ✅
→ urgent orders > normal ✅
```

```java
// TTL — queue level ✅
@Bean
public Queue paymentQueue() {
    return QueueBuilder
            .durable("payment-queue")
            .withArgument("x-message-ttl", 60000) // 60s ✅
            .withArgument("x-dead-letter-exchange",
                "dlx-exchange") // expired → DLQ ✅
            .build();
}

// TTL — message level ✅
MessageProperties props = new MessageProperties();
props.setExpiration("30000"); // 30 seconds ✅
rabbitTemplate.send("payment-queue",
        new Message(body, props));

// Priority Queue ✅
@Bean
public Queue priorityQueue() {
    return QueueBuilder
            .durable("payment-priority-queue")
            .withArgument("x-max-priority", 10) // ✅
            .build();
}

// high priority ✅
MessageProperties highProps = new MessageProperties();
highProps.setPriority(10); // highest ✅
rabbitTemplate.send("payment-priority-queue",
        new Message(paymentBody, highProps));

// low priority ✅
MessageProperties lowProps = new MessageProperties();
lowProps.setPriority(1); // lowest ✅
rabbitTemplate.send("payment-priority-queue",
        new Message(notificationBody, lowProps));
```

| | TTL | Priority |
|---|---|---|
| **Purpose** | Auto expire ✅ | Process important first ✅ |
| **Config** | x-message-ttl | x-max-priority ✅ |
| **Range** | milliseconds | 0-255 ✅ |

---

## Q7. Message Durability and Persistence — How to ensure no message loss?

### Answer
```
Two things needed:

1. Queue Durable:
→ queue survives RabbitMQ restart ✅
→ durable = true ✅
→ durable = false → queue gone ❌

2. Message Persistent:
→ message written to DISK ✅
→ survives restart ✅
→ not just in memory ❌

BOTH needed for zero message loss ✅

Three layers:
Layer 1 → Queue durable ✅
Layer 2 → Message persistent ✅
Layer 3 → Manual ack ✅
All three = zero message loss ✅
```

```java
// Durable queue ✅
@Bean
public Queue paymentQueue() {
    return QueueBuilder
            .durable("payment-queue") // survives restart ✅
            .withArgument("x-dead-letter-exchange",
                "dlx-exchange")
            .build();
}

// Persistent message ✅
MessageProperties props = new MessageProperties();
props.setDeliveryMode(
    MessageDeliveryMode.PERSISTENT); // written to disk ✅
rabbitTemplate.send("payment-queue",
        new Message(body, props));
```

```yaml
# application.yml ✅
spring:
  rabbitmq:
    publisher-confirm-type: correlated  # publisher confirm ✅
    publisher-returns: true             # return if unroutable ✅
    listener:
      simple:
        acknowledge-mode: manual        # manual ack ✅
```

| | Durable Queue | Persistent Message | Manual Ack |
|---|---|---|---|
| **Protects from** | Broker restart | Broker restart | Consumer crash |
| **Config** | `.durable()` | `PERSISTENT` | `acknowledge-mode: manual` |
| **Required** | ✅ Yes | ✅ Yes | ✅ Yes |

---

## Q8. RabbitMQ Clustering and Mirrored Queues?

### Answer
```
Clustering:
→ multiple RabbitMQ nodes ✅
→ share users, exchanges, queues ✅
→ load balanced ✅
→ one node down → others serve ✅
→ solves single point of failure ✅

Mirrored Queue (Classic):
→ queue replicated to all nodes ✅
→ master + mirror copies ✅
→ master dies → mirror promoted ✅
→ no message loss ✅

Quorum Queue (modern - recommended):
→ RabbitMQ 3.8+ ✅
→ Raft consensus algorithm ✅
→ more reliable than mirrored ✅
→ recommended now ✅
```

```java
// Classic Mirrored Queue ✅
@Bean
public Queue mirroredQueue() {
    return QueueBuilder
            .durable("payment-queue")
            .withArgument("x-ha-policy", "all")
            .withArgument("x-ha-sync-mode", "automatic")
            .build();
}

// Quorum Queue (recommended) ✅
@Bean
public Queue quorumQueue() {
    return QueueBuilder
            .durable("payment-quorum-queue")
            .quorum() // ✅
            .build();
}
```

```yaml
# Cluster config ✅
spring:
  rabbitmq:
    addresses: rabbit1:5672,rabbit2:5672,rabbit3:5672
    username: admin
    password: ${RABBIT_PASSWORD}
```

```
3 Node Cluster:
Node 1: [master] ← primary ✅
Node 2: [mirror] ← backup ✅
Node 3: [mirror] ← backup ✅

Node 1 crashes:
Node 2: [master] ← promoted ✅
Node 3: [mirror] ← running ✅
→ no message loss ✅
→ no SPOF ✅
```

| | Mirrored Queue | Quorum Queue |
|---|---|---|
| **Version** | Classic | RabbitMQ 3.8+ ✅ |
| **Algorithm** | Simple replication | Raft consensus ✅ |
| **Recommended** | ❌ Old | ✅ Yes |

---

## Q9. RabbitMQ vs AWS SQS — When to choose each?

### Answer
```
RabbitMQ:
→ self hosted ✅
→ you manage + maintain ✅
→ complex routing (exchanges) ✅
→ message priority ✅
→ message TTL ✅
→ on-premise possible ✅
→ more features ✅

SQS:
→ fully managed by AWS ✅
→ no server management ✅
→ auto scaling ✅
→ simple P2P only ✅
→ 14 days retention ✅
→ integrates with AWS services ✅

SQS Standard:
→ unlimited TPS ✅
→ at least once ✅
→ may be out of order ⚠️
→ cheaper ✅

SQS FIFO:
→ exactly once ✅
→ strict ordering ✅
→ 3000 TPS max ⚠️
→ more expensive ✅
```

```java
// RabbitMQ — complex routing ✅
@Bean
public Queue priorityQueue() {
    return QueueBuilder.durable("payment-queue")
            .withArgument("x-max-priority", 10)
            .build();
}

// SQS — simple ✅
@SqsListener("payment-queue")
public void consume(OrderEvent event) {
    orderService.process(event); // ✅
}
```

```hcl
# SQS Terraform ✅
resource "aws_sqs_queue" "payment" {
  name                      = "payment-queue"
  message_retention_seconds = 1209600  # 14 days ✅
  receive_wait_time_seconds = 20       # long polling ✅
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = 3
  })
}
```

| | RabbitMQ | AWS SQS |
|---|---|---|
| **Managed** | Self hosted | ✅ AWS managed |
| **Routing** | ✅ Complex | Simple only |
| **Priority** | ✅ Yes | ❌ No |
| **Ordering** | ✅ Yes | FIFO only ✅ |
| **Setup** | Complex ⚠️ | ✅ Simple |
| **Use for** | Complex/on-prem | AWS simple ✅ |

---

## Q10. What is Azure Service Bus? How different from RabbitMQ and SQS?

### Answer
```
Azure Service Bus:
→ fully managed message broker ✅
→ Microsoft Azure ✅
→ like RabbitMQ but cloud managed ✅
→ like SQS but more features ✅

Two types:
→ Queue = P2P ✅
→ Topic + Subscription = pub/sub ✅

Features:
→ message ordering ✅
→ duplicate detection ✅
→ dead letter queue ✅
→ message TTL ✅
→ sessions (grouped messages) ✅
→ scheduled messages ✅
→ transactions ✅
→ 14 days retention ✅
```

```java
// Send message ✅
@Service
public class PaymentPublisher {

    public void sendPayment(OrderEvent event) {
        ServiceBusMessage message =
            new ServiceBusMessage(toJson(event))
                .setContentType("application/json")
                .setMessageId(
                    UUID.randomUUID().toString());
        sender.sendMessage(message); // ✅
    }
}

// Receive message ✅
@ServiceBusListener(destination = "payment-queue")
public void consume(
        OrderEvent event,
        ServiceBusReceivedMessageContext context) {
    try {
        orderService.process(event);
        context.complete(); // ack ✅
    } catch (Exception e) {
        context.deadLetter(); // → DLQ ✅
    }
}
```

| | RabbitMQ | AWS SQS | Azure Service Bus |
|---|---|---|---|
| **Managed** | ❌ Self | ✅ AWS | ✅ Azure |
| **Queue** | ✅ | ✅ | ✅ |
| **Topic/Sub** | ✅ Exchange | ❌ (use SNS) | ✅ Native |
| **Priority** | ✅ | ❌ | ❌ |
| **Transactions** | ✅ | ❌ | ✅ |
| **Cloud** | Any | AWS only | Azure only |

### When to choose
```
RabbitMQ:
→ on-premise ✅
→ complex routing ✅
→ message priority ✅

AWS SQS:
→ already on AWS ✅
→ simple queue ✅
→ serverless ✅

Azure Service Bus:
→ already on Azure ✅
→ enterprise features ✅
→ Queue + Topic needed ✅
→ transactions needed ✅
```

---

## Quick Reference — All Key Points

| Topic | Key Point |
|---|---|
| MQ purpose | Async + decouple + absorb spikes ✅ |
| P2P | One consumer — pull based ✅ |
| Pub/Sub | All consumers — push based ✅ |
| DLQ | Failed after retries → investigate ✅ |
| Direct exchange | Exact routing key ✅ |
| Topic exchange | Pattern routing (#.*) ✅ |
| Fanout exchange | All queues — broadcast ✅ |
| Headers exchange | Header based routing ✅ |
| Auto ack | Risky — message lost ❌ |
| Manual ack | Safe — at least once ✅ |
| nack requeue=true | Back to queue ✅ |
| nack requeue=false | DLQ ✅ |
| TTL | Auto expire message ✅ |
| Priority | High priority first ✅ |
| Durable queue | Survives restart ✅ |
| Persistent message | Written to disk ✅ |
| Mirrored queue | Replicated across nodes ✅ |
| Quorum queue | Modern replacement — Raft ✅ |
| RabbitMQ | Complex routing + on-premise ✅ |
| SQS | AWS managed + simple ✅ |
| Azure Service Bus | Azure + enterprise features ✅ |
