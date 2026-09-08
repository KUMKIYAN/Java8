# Microservices Design — Detailed Interview Answers
> 10 Scenario Based Questions with Point-wise Answers

---

## Q1. Cascading Failures — slow downstream exhausts thread pool

### What is happening
- Downstream payment service is slow but not down ✅
- Order service calls payment service synchronously ✅
- Each request waits for payment response ✅
- Threads are blocked waiting ✅
- Thread pool fills up with waiting threads ✅
- New order requests cannot get a thread ❌
- Order service starts failing even though it is fine ❌
- This is called cascade failure ❌

### How to design against it

**Fix 1 — Timeout**
- Set maximum wait time on every external call ✅
- Example: timeout = 3 seconds ✅
- After 3 seconds → fail fast ✅
- Thread released immediately ✅
- Thread pool not exhausted ✅

**Fix 2 — Circuit Breaker (Resilience4j)**
- Monitor failure rate per downstream ✅
- Failure > 50% → circuit OPENS ✅
- All calls blocked → fallback returned immediately ✅
- No thread waiting for failed service ✅
- Wait 60 seconds → HALF_OPEN → test calls ✅
- Recovered → circuit CLOSES ✅

**Fix 3 — Bulkhead**
- Separate thread pool per downstream service ✅
- Payment pool = 10 threads ✅
- Inventory pool = 10 threads ✅
- Payment slow → only payment pool affected ✅
- Order service main pool unaffected ✅

**Fix 4 — Async Communication (Kafka)**
- Do not call payment synchronously ❌
- Publish order event to Kafka ✅
- Return 202 Accepted immediately ✅
- Payment consumer processes async ✅
- No thread waiting ✅
- No cascade failure possible ✅

**Fallback Strategy**
- Return PENDING status to client ✅
- Queue order for retry ✅
- Notify customer payment processing ✅
- Process when payment recovers ✅

**Monitoring**
- CloudWatch thread pool metrics ✅
- Thread pool usage > 80% → PagerDuty alert ✅
- Circuit breaker state changes logged ✅

---

## Q2. Distributed Transactions — payment succeeds inventory fails

### Problem
- Checkout = reserve inventory + charge payment + create shipment ✅
- Three separate services three databases ✅
- Payment succeeds ✅
- Inventory reservation fails ✅
- Customer charged but no inventory reserved ❌
- 2PC = too slow + single point of failure ❌

### Solution — Saga Pattern

**Choreography approach (Kafka)**
- Each service publishes event after completing step ✅
- Next service listens and processes ✅

**Happy flow**
- Order service → publish OrderCreated ✅
- Inventory service → reserve → publish InventoryReserved ✅
- Payment service → charge → publish PaymentCharged ✅
- Shipping service → create → publish ShipmentCreated ✅

**Failure flow — Inventory fails**
- Inventory reservation fails ✅
- Publish InventoryReservationFailed event ✅
- No payment attempted ✅
- Order marked failed ✅
- Customer notified ✅

**Failure flow — Payment fails after inventory reserved**
- Payment fails ✅
- Publish PaymentFailed event ✅
- Inventory service listens → release reserved stock ✅
- This is called Compensation transaction ✅
- Order marked failed ✅
- Customer notified ✅

**Additional patterns**
- Outbox pattern → save event + data same @Transactional ✅
- Idempotency → each step checks already processed ✅
- @RetryableTopic → retry transient failures ✅
- DLT → permanent failures → alert + investigate ✅

**Tools**
- Choreography → Kafka events ✅
- Orchestration → Temporal or AWS Step Functions ✅

---

## Q3. Data Consistency — order confirmed but inventory not decremented

### Problem
- Order service marks order CONFIRMED ✅
- Inventory event missed or failed ❌
- Inventory never decremented ❌
- Customer dashboard shows confirmed but stock wrong ❌

### How to detect

**Reconciliation Job**
- Scheduled batch job runs nightly ✅
- Queries Order service for confirmed orders ✅
- Queries Inventory service for decremented stock ✅
- Compares counts ✅
- Finds discrepancies ✅
- Alerts team ✅
- Auto fixes or manual investigation ✅

**Consumer lag monitoring**
- Monitor Kafka consumer lag ✅
- High lag = events not processed ✅
- CloudWatch alarm → PagerDuty ✅

### How to prevent

**Prevention 1 — Outbox Pattern**
- Save order + event in same @Transactional ✅
- Both commit or both rollback ✅
- No missed events ✅
- Scheduler publishes pending events ✅

**Prevention 2 — @RetryableTopic**
- Auto retry failed events ✅
- Exponential backoff 1s → 2s → 4s ✅
- Max retries exceeded → DLT ✅
- Alert on DLT ✅

**Prevention 3 — Consumer Idempotency**
- Check processedEventId before processing ✅
- Prevent duplicate processing ✅
- Manual ack after success ✅

**Prevention 4 — Event Sourcing**
- Store all events not just state ✅
- Replay events to rebuild inventory ✅
- Audit trail complete ✅

**Prevention 5 — Saga Compensation**
- If inventory service fails ✅
- Compensation event published ✅
- Order service reverts confirmed status ✅

---

## Q4. API Breaking Changes — field change consumed by 3 teams

### Problem
- Change field name or type in API response ✅
- Three teams consuming the API ✅
- Cannot coordinate simultaneous deploy ❌
- Risk breaking all three consumers ❌

### Solution — Backward Compatible Rollout

**Step 1 — Add new field alongside old**
- Keep OLD field in response ✅
- Add NEW field in response ✅
- Both exist temporarily ✅
- Consumers can migrate at own pace ✅

**Step 2 — Version the API**
- Create /api/v2/orders with new field ✅
- Keep /api/v1/orders with old field ✅
- New consumers use v2 ✅
- Old consumers stay on v1 ✅

**Step 3 — Notify consumers**
- Send deprecation notice to all 3 teams ✅
- Document migration guide ✅
- Set sunset date for v1 ✅
- Example: v1 deprecated in 3 months ✅

**Step 4 — Monitor usage**
- Log which consumers call v1 ✅
- Track migration progress ✅
- Remind teams approaching sunset ✅

**Step 5 — Remove old version**
- All teams migrated to v2 ✅
- Decommission v1 ✅
- Remove old field ✅

**Documentation**
- OpenAPI @deprecated annotation ✅
- Swagger shows deprecated endpoints ✅
- Migration examples in docs ✅

**Consumer Driven Contracts**
- Pact testing ✅
- Verify API change does not break consumers ✅
- Run before every deploy ✅

---

## Q5. Debugging 8 Second Request — no single service abnormal

### Problem
- User reports 8 second response time ✅
- Check individual service logs → all normal ✅
- No single service shows issue ❌
- Need to trace across 6 microservices ✅

### Solution — Distributed Tracing

**Step 1 — Find trace ID**
- Every request gets unique trace ID ✅
- Propagated across all services ✅
- Find trace ID from user request ✅
- Check API Gateway logs or user request header ✅

**Step 2 — Open tracing tool**
- Jaeger or Zipkin or AWS X-Ray ✅
- Enter trace ID ✅
- See complete request journey ✅

**Step 3 — Analyze waterfall diagram**
- See all 6 services as spans ✅
- Each span shows start + duration ✅
- Find which span took longest ✅
- Example: DB call in Order service = 6 seconds ❌

**Step 4 — Investigate bottleneck**
- Check that service logs in detail ✅
- Check DB slow query log ✅
- Check external API call timing ✅
- Check connection pool wait time ✅

**Common causes**
- DB slow query → missing index ✅
- N+1 queries → JOIN FETCH fix ✅
- External API slow → timeout + circuit breaker ✅
- Connection pool exhausted → HikariCP tuning ✅
- GC pause → JVM tuning ✅
- Network latency between services ✅
- Serialization overhead ✅

**Tools used**
- Spring Sleuth → auto trace ID ✅
- Zipkin → visualize traces ✅
- New Relic → APM trace analysis ✅
- Splunk → search by trace ID ✅

---

## Q6. Cross-service Reporting — join Orders Payments Shipping

### Problem
- Finance team needs joined report ✅
- Orders DB + Payments DB + Shipping DB ✅
- Each service has own database ✅
- Cannot query across DBs directly ❌
- Cannot hammer production DBs ❌

### Solution Options

**Option 1 — CQRS Read Model (Recommended for real-time)**
- Create dedicated Reporting Service ✅
- Subscribe to events from all 3 services ✅
- OrderCreated → store in report DB ✅
- PaymentCharged → update report DB ✅
- ShipmentCreated → update report DB ✅
- Finance queries Reporting Service only ✅
- Denormalized table = fast queries ✅
- No join needed ✅
- Eventual consistency acceptable ✅

**Option 2 — Data Warehouse (for complex analytics)**
- Nightly ETL pipeline ✅
- Copy data from all 3 services to Redshift or Snowflake ✅
- Finance runs complex SQL queries ✅
- No production DB impact ✅
- Historical data available ✅
- Slight delay acceptable ✅

**Option 3 — API Composition (small datasets)**
- BFF (Backend for Frontend) service ✅
- Calls all 3 services APIs ✅
- Combines data in memory ✅
- Returns combined response ✅
- Only for small datasets ✅
- Not for bulk reporting ❌

**What to avoid**
- Direct cross-DB queries ❌
- Violates service boundaries ❌
- Creates tight coupling ❌
- Breaks microservice principles ❌

---

## Q7. Idempotency and Retries — customer charged twice

### Problem
- Order service calls payment service ✅
- Network timeout occurs ❌
- Order service retries ✅
- First call already succeeded on payment side ✅
- Second call charges again ❌
- Customer charged twice ❌

### Solution — Idempotency Key

**Step 1 — Generate unique key**
- Client generates UUID idempotency key ✅
- Send in request header X-Idempotency-Key ✅
- Same key for original + all retries ✅

**Step 2 — Payment service checks key**
- Before processing check key in DB ✅
- Key exists → return SAME previous response ✅
- Key not exists → process payment ✅
- Save key after successful processing ✅

**Step 3 — Storage**
- Redis for fast key lookup ✅
- TTL = 24 hours ✅
- DB idempotency table as backup ✅

**Step 4 — Producer idempotence (Kafka)**
- enable.idempotence = true ✅
- Sequence number per message ✅
- Broker ignores duplicate sequence ✅

**Response handling**
- Always return same response for same key ✅
- 200 OK with original result ✅
- Client safe to retry multiple times ✅

**Testing**
- Retry 3 times with same key ✅
- Customer charged exactly once ✅
- Idempotent operation ✅

---

## Q8. Rolling Deployment — 30 second connection errors

### What is happening
- Rolling deployment replaces pods one by one ✅
- Old pod terminating ✅
- Load balancer still routing traffic to it ✅
- In-flight requests fail ❌
- New pod starting but not ready yet ✅
- LB routes to new pod before ready ❌
- 30 second window of errors ❌

### Solutions

**Fix 1 — preStop hook**
- Add preStop lifecycle hook ✅
- Sleep 15 seconds before shutdown ✅
- Gives LB time to stop routing ✅
- In-flight requests complete ✅

**Fix 2 — terminationGracePeriodSeconds**
- Set to 60 seconds ✅
- Pod waits 60s before force kill ✅
- In-flight requests finish ✅

**Fix 3 — readinessProbe**
- Configure readinessProbe on /actuator/health ✅
- Pod marked ready only when app warm ✅
- LB only routes to READY pods ✅
- New pod not ready → no traffic ✅

**Fix 4 — Deployment strategy**
- maxUnavailable = 0 ✅
- maxSurge = 1 ✅
- New pod ready before old removed ✅
- Always sufficient capacity ✅

**Fix 5 — Blue-Green Deployment**
- Deploy to inactive slot ✅
- Validate ✅
- Switch ALB all at once ✅
- Instant zero downtime ✅
- Instant rollback if needed ✅

**Kubernetes YAML settings**
```yaml
lifecycle:
  preStop:
    exec:
      command: ["sleep", "15"]
terminationGracePeriodSeconds: 60
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
strategy:
  rollingUpdate:
    maxUnavailable: 0
    maxSurge: 1
```

---

## Q9. Kafka Consumer Lag Growing — peak traffic

### Problem
- Consumer lag keeps growing ✅
- Peak traffic overwhelms consumers ✅
- Downstream data becomes stale ❌

### Diagnose causes

**Cause 1 — Consumers less than partitions**
- 5 consumers | 10 partitions ✅
- Each consumer handles 2 partitions ✅
- Not optimal ✅

**Cause 2 — Slow DB queries**
- Consumer processes fast ✅
- DB write is slow ❌
- N+1 queries ❌
- No indexes ❌

**Cause 3 — External API calls**
- Consumer calls external API per message ✅
- API slow → consumer slow ❌

**Cause 4 — max.poll.records too low**
- Default 500 → reduce fetch batch ✅
- Or too high → processing takes too long ✅

**Cause 5 — Single threaded processing**
- One thread per consumer ✅
- No parallel processing ❌

### Remediation

**Fix 1 — Increase partitions and consumers**
- Add partitions to match consumers ✅
- consumers = partitions = max parallelism ✅

**Fix 2 — Batch processing**
- listener.type = batch ✅
- Process 500 records at once ✅
- saveAll to DB instead of one by one ✅
  
  - SINGLE = one message 
    BATCH  = list of messages faster 
    RECORD = same as SINGLE 
    BATCH most efficient for high volume 

**Fix 3 — Parallel async processing**
- CompletableFuture per message ✅
- Non-blocking processing ✅
- Multiple messages parallel ✅

**Fix 4 — Consumer pause and resume**
- consumer.pause() when overwhelmed ✅
- Process pending batch ✅
- consumer.resume() when ready ✅

**Fix 5 — Auto scale ECS**
- CloudWatch consumer lag metric ✅
- Lag > 10000 → add more ECS tasks ✅
- Each task = new consumer instance ✅

**Fix 6 — Tune consumer properties**
- max.poll.records = 500 ✅
- fetch.min.bytes = 1MB ✅
- fetch.max.wait.ms = 500ms ✅

**Monitor**
- CloudWatch lag alarm > 10000 ✅
- PagerDuty on-call alert ✅
- Grafana consumer lag dashboard ✅

---

## Q10. Circuit Breaker vs Bulkhead — flaky 3rd party API

### Circuit Breaker alone — where it falls short
- 100 slow requests arrive simultaneously ✅
- All 100 threads waiting for 3rd party ❌
- Thread pool exhausted BEFORE threshold reached ❌
- Circuit never opens in time ❌
- Main app cascade failure ❌
- Circuit breaker monitors failure rate ✅
- But threads exhausted before enough failures counted ❌

### Bulkhead alone — where it falls short
- Bulkhead limits to 10 threads for 3rd party ✅
- Main app protected ✅
- BUT 10 threads all slow and failing ❌
- Keeps hammering failed 3rd party ❌
- Wastes 10 threads continuously ❌
- No automatic stop mechanism ❌
- No fallback returned ❌

### Why combine both

**Bulkhead handles**
- Isolates 3rd party to dedicated pool ✅
- Payment pool 10 threads only ✅
- Main app thread pool never touched ✅
- Cascade failure prevented ✅

**Circuit Breaker handles**
- Monitors failure rate in bulkhead pool ✅
- Failures > 50% → circuit OPENS ✅
- Immediately returns fallback ✅
- Stops wasting bulkhead threads ✅
- Gives 3rd party time to recover ✅

**Combined flow**
- 3rd party flaky ✅
- Bulkhead limits to 10 threads ✅
- Failures counted in those 10 ✅
- > 50% fail → circuit OPENS ✅
- Fallback returned immediately ✅
- 10 threads released ✅
- 60 seconds wait ✅
- HALF_OPEN test calls ✅
- 3rd party recovered → CLOSED ✅
- Main app never affected ✅

**Configuration**
```yaml
resilience4j:
  bulkhead:
    instances:
      thirdParty:
        maxConcurrentCalls: 10
  circuitbreaker:
    instances:
      thirdParty:
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
        permittedCallsInHalfOpen: 5
```

---

## Quick Reference

| Question | Key Pattern | Key Tool |
|---|---|---|
| Cascade failure | CB + Bulkhead + Timeout | Resilience4j ✅ |
| Distributed tx | Saga + Compensation | Kafka/Temporal ✅ |
| Data consistency | Outbox + Reconciliation | Scheduler ✅ |
| API versioning | /v1 /v2 + Deprecation | OpenAPI ✅ |
| Distributed trace | Trace ID waterfall | Jaeger/Zipkin ✅ |
| Cross-service report | CQRS read model | Event driven ✅ |
| Idempotency | X-Idempotency-Key | Redis ✅ |
| Rolling deploy | preStop + readiness | Kubernetes ✅ |
| Kafka lag | Partitions=consumers + batch | Auto scale ✅ |
| CB + Bulkhead | Isolate + Stop | Resilience4j ✅ |
