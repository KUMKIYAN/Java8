# Kafka Interview Q&A
6 Questions with Correct Answers and Code Snippets

---

## Q1. at-most-once vs at-least-once vs exactly-once

At most once: message may be LOST. commit BEFORE processing.
At least once: may DUPLICATE. commit AFTER processing. handle with idempotency.
Exactly once: no loss no duplicate. needs all three layers.

Three layers:
1. Producer: ENABLE_IDEMPOTENCE=true + ACKS=all
2. Consumer: manual commit + idempotency check
3. Kafka transactions: executeInTransaction + read_committed

read_committed:
→ consumer reads ONLY committed messages ✅
→ skips uncommitted/aborted ✅
→ no dirty reads ✅

// Producer — executeInTransaction ✅
@Service
public class OrderService {

    private final KafkaTemplate<String, Object>
            kafkaTemplate;

    public void processOrder(Order order) {

        // all sends in ONE transaction ✅
        kafkaTemplate.executeInTransaction(
                operations -> {

            // send to order topic ✅
            operations.send(
                "order-events",
                order.getOrderId(),
                order);

            // send to payment topic ✅
            operations.send(
                "payment-events",
                order.getOrderId(),
                PaymentEvent.from(order));

            // send to inventory topic ✅
            operations.send(
                "inventory-events",
                order.getOrderId(),
                InventoryEvent.from(order));

            // all three committed together ✅
            // any failure → all rolled back ✅
            return true;
        });
    }
}

// Consumer — read_committed ✅
spring:
kafka:
consumer:
isolation-level: read_committed
# only reads committed messages ✅
# skips aborted transactions ✅

---

## Q2. Consumer Group and Rebalance

Consumer group = consumers sharing same task.
Each partition assigned to ONE consumer only.

Partition rules:
3 partitions 3 consumers = 1 each
3 partitions 2 consumers = one gets 2
3 partitions 4 consumers = one idle

Rebalance triggers: new consumer joins, consumer crashes, partition count changes.
During rebalance: ALL consumers STOP. partitions reassigned. resume after.
Duplicate risk: processed but not committed. another consumer reprocesses.
Fix: static group membership. group.instance.id + session.timeout.ms=30000

---

## Q3. @KafkaListener with Acknowledgment vs without

Without Ack (auto commit):
- offset committed automatically
- may commit BEFORE processing
- crash = message LOST
- AT MOST ONCE

With Ack (manual commit):
- YOU control when to commit
- commit AFTER success
- crash before ack = redelivered
- AT LEAST ONCE - recommended for production

Code:

@KafkaListener(topics = "order-events")
public void consume(ConsumerRecord<String, OrderEvent> record, Acknowledgment ack) {
    try {
        processOrder(record.value());
        ack.acknowledge(); // commit AFTER success
    } catch (Exception e) {
        log.error("Failed - will retry");
        // no ack = redelivered on restart
    }
}

application.yml:
spring.kafka.consumer.enable-auto-commit: false
spring.kafka.listener.ack-mode: MANUAL

---

## Q4. @RetryableTopic and @DltHandler

@RetryableTopic: automatic retry with backoff. creates retry topics. after all retries sends to DLT.

Topics created automatically:
order-events (original)
order-events-retry-0 (retry 1 after 1s)
order-events-retry-1 (retry 2 after 2s)
order-events-retry-2 (retry 3 after 4s)
order-events-dlt (dead letter)

@DltHandler: called when all retries exhausted. log + alert + store poison messages.

Code:

@RetryableTopic(
    attempts = "4",
    backoff = @Backoff(delay = 1000, multiplier = 2.0),
    dltTopicSuffix = "-dlt",
    include = { TransientException.class },
    exclude = { ValidationException.class }
)
@KafkaListener(topics = "order-events")
public void consume(OrderEvent event) {
    processOrder(event);
}

@DltHandler
public void handleDlt(OrderEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    log.error("Poison message in: {}", topic);
    alertService.notifyTeam(event);
    poisonMessageRepo.save(event);
}

---

## Q5. Outbox Pattern

Problem: save order to DB. Kafka down. event LOST. payment never notified. inconsistent.

Solution: save order + event in SAME @Transactional.
Both succeed or both rollback.
Scheduler picks unpublished events and publishes.
Eventual consistency because scheduler runs at intervals.

Code:

@Transactional
public Order placeOrder(OrderRequest request) {
    Order order = orderRepository.save(toEntity(request));
    outboxRepository.save(OutboxEvent.builder()
        .id(UUID.randomUUID().toString())
        .eventType("ORDER_PLACED")
        .payload(toJson(order))
        .published(false)
        .build());
    return order;
}

@Scheduled(fixedDelay = 5000)
@Transactional
public void publishOutboxEvents() {
    List<OutboxEvent> events = outboxRepository.findByPublishedFalse();
    events.forEach(event -> {
        try {
            kafkaTemplate.send("order-events", event.getPayload());
            event.setPublished(true);
            outboxRepository.save(event);
        } catch (Exception e) {
            log.error("Publish failed - retry next cycle");
        }
    });
}

Debezium alternative: CDC watches outbox table. publishes automatically. no scheduler needed.

---

## Q6. Producer Producing Messages Slowly - How to Debug

Step 1 - Check metrics:
/actuator/metrics/kafka.producer.record.send.rate
kafka.producer.request.latency.avg

Step 2 - Check producer config:

linger.ms too HIGH:
BAD:  config.put(ProducerConfig.LINGER_MS_CONFIG, 50000); // 50 seconds
GOOD: config.put(ProducerConfig.LINGER_MS_CONFIG, 20);    // 20ms

batch.size too SMALL:
BAD:  config.put(ProducerConfig.BATCH_SIZE_CONFIG, 100);
GOOD: config.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536); // 64KB

no compression:
BAD:  config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
GOOD: config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

Step 3 - Check application code:

Synchronous sending BAD:
for (Order order : orders) {
    kafkaTemplate.send("order-events", order).get(); // blocking - slow
}

Async sending GOOD:
for (Order order : orders) {
    kafkaTemplate.send("order-events", order); // non-blocking - fast
}

Step 4 - Check broker:
broker CPU/memory high = scale brokers
network latency = check connectivity
disk I/O slow = check broker disk

Optimized producer config:
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
props.put(ProducerConfig.ACKS_CONFIG,               "all");
props.put(ProducerConfig.BATCH_SIZE_CONFIG,         65536);
props.put(ProducerConfig.LINGER_MS_CONFIG,          20);
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG,   "snappy");
props.put(ProducerConfig.RETRIES_CONFIG,            3);

Debug checklist:
linger.ms too high     = reduce to 20ms
batch.size too small   = increase to 64KB
compression off        = enable snappy
synchronous .get()     = switch to async
slow serializer        = use JSON or Avro
broker overloaded      = scale brokers
network high latency   = check connectivity
message size too large = compress or split

---

## Quick Reference

at most once   = may lose message. commit before process.
at least once  = may duplicate. commit after process. use idempotency.
exactly once   = idempotence + manual commit + transactions.

consumer group = share partitions. rebalance on join/leave.
rebalance      = all pause. fix with static group.instance.id.

without ack    = auto commit. may lose.
with ack       = manual. commit after success. recommended.

RetryableTopic = auto retry + backoff + DLT.
DltHandler     = handle poison messages + alert + store.

outbox         = DB + event same transaction + scheduler publishes.
slow producer  = check linger.ms batch.size async compression.
