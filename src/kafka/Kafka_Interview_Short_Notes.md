# Kafka — Interview Questions & Short Notes

---

## Q1. 10 partitions 5 consumers — what happens? How to optimize?

```
Current-       5 consumers < 10 partitions | each consumer reads 2 partitions | works not optimal ⚠️
Optimize-      increase consumers to 10 | one per partition | max parallelism | reduce latency ✅
concurrency-   concurrency=10 in @KafkaListener ✅
Never-         consumers > partitions = extra idle waste resources ❌
```

---

## Q2. Consumer slow — back pressure handling?

```
Check-         partitions = consumers | if not increase both ✅
Tune props-    max.poll.records increase | fetch.min.bytes | fetch.max.wait.ms ✅
Pause/resume-  consumer.pause() when overwhelmed | process pending | consumer.resume() ✅
Async-         CompletableFuture parallel processing | non-blocking ✅
Batch-         listener.type=batch | saveAll to DB not one by one ✅
Monitor-       consumer lag CloudWatch | lag>1000 → PagerDuty | auto scale ECS ✅
```

---

## Q3. Schema Registry? Avro vs JSON?

```
Registry-      central store schemas | producer registers | consumer fetches | validates before publish ✅
Contract-      deviation → rejected | BACKWARD=default | FORWARD | FULL compatibility ✅
Avro-          binary format | 5-10x smaller | strongly typed | schema validated | bad msg rejected ✅
JSON-          text large | no validation | no type safety ❌
BACKWARD-      new consumer reads old messages | add field with DEFAULT value ✅
```

---

## Q4. Exactly-once delivery — how achieve?

```
Layer 1-       enable.idempotence=true | PID + sequence number | broker ignores duplicate ✅
Layer 2-       acks=all | all ISR brokers confirm | no message loss ✅
Layer 3-       manual ack | enable.auto.commit=false | ack ONLY after processing ✅
Layer 4-       consumer idempotency check | processedEventId table | exists→ack+skip ✅
Outbox-        save data+event same @Transactional | scheduler publishes | guaranteed delivery ✅
Retry-         retries=3 | @RetryableTopic for transient failures ✅
```

---

## Q5. Consumer group rebalancing — when happens? Impact? Fix?

```
Triggers-      consumer joins/leaves/crashes | session timeout exceeded | partition count changes ✅
Impact-        ALL consumers pause | stop the world | latency spike | messages accumulate ❌
Fix 1-         static membership | group.instance.id=fixed ID | restart→same partition | no rebalance ✅
Fix 2-         cooperative rebalancing Kafka 2.4+ | only affected partitions moved | others continue ✅
Fix 3-         tune session.timeout.ms=60s | heartbeat.interval.ms=3s | gives restart time ✅
```

---

## Q6. auto.offset.reset earliest vs latest — GPU project?

```
Applies when-  new consumer group joins | no committed offset | offset expired ✅
earliest-      reads ALL available messages | full history | replay ✅
latest-        reads only NEW messages | ignores old | from now ✅
GPU project-   earliest | must process ALL orders | no order missed ✅
Currency-      latest | only current rate matters | old rates irrelevant ✅
none-          throws exception | use to detect issues ✅
```

---

## Q7. Same message published twice to EDI — how handle?

```
Outbox pattern-    save event published=false | scheduler picks false | publish | mark true after success ✅
Never picked-      published=true never picked again | stays false on fail → retry ✅
Producer-          enable.idempotence=true | sequence number | broker ignores duplicate ✅
Consumer-          EDA checks eventId | processedEventId table | exists→skip ✅
Together-          zero duplicate delivery | exactly once guarantee ✅
```

---

## Q8. @RetryableTopic — how works in GPU project?

```
What-          method level annotation on @KafkaListener | auto creates retry topics + DLT ✅
Config-        attempts=max retries | include=retry these exceptions | exclude=skip these ✅
Backoff-       fixed=every 1s | exponential=1s→2s→4s ✅
Auto topics-   order-events-retry-0 | retry-1 | retry-2 | order-events-dlt ✅
DLT-           @DltHandler called | log + save DLT table | alert team | investigate ✅
GPU project-   transient EDI failures→retry | bad XML→skip→DLT | alert on DLT ✅
Fires when-    exception THROWN | NOT when ack not received ❌
```

---

## Q9. Kafka message ordering — GPU project?

```
Problem-       ORD001 has CREATE|UPDATE|CANCEL | without key=round robin | cancel before create ❌
Fix-           use orderNumber as partition key | hash(orderNumber)%partitions ✅
Result-        same orderNumber=same partition | partition is ordered | create→update→cancel ✅
Limitation-    ordering within partition only | NOT across partitions ❌
Delay logic-   previous event not acked <1hr | introduce delay | strict sequence ✅
Without key-   round robin | each event different partition | no ordering ❌
```

---

## Q10. Consumer lag — monitor and alert?

```
What-          lag = published - consumed | lag=0 healthy | lag>0 falling behind ⚠️
Monitor-       CloudWatch lag metric | Grafana dashboard | New Relic | kafka-consumer-groups.sh ✅
Alert-         lag>10000 → CloudWatch alarm → SNS → email + PagerDuty ✅
Investigate-   check consumer logs | DB performance | external API | consumer crashed ✅
Fix-           increase consumers | batch processing | optimize code | scale ECS | increase partitions ✅
```

---

## Quick Reference

```
Partitions=consumers-  max parallelism one per partition ✅
Back pressure-         pause/resume | batch | CompletableFuture | auto scale ✅
Avro-                  binary 5-10x smaller | validated | strongly typed ✅
Exactly once-          idempotence + acks=all + manual ack + idempotency check ✅
Rebalancing-           static membership group.instance.id | cooperative Kafka 2.4+ ✅
earliest-              GPU all orders | latest=currency rates ✅
Outbox-                published=false → scheduler → published=true ✅
@RetryableTopic-       include/exclude | fixed/exponential backoff | DLT ✅
Ordering-              partition key=orderNumber | same key=same partition ✅
Lag-                   lag>10000 → alert → investigate → fix ✅
```
