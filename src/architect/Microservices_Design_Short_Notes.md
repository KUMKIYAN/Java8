# Microservices Design — Interview Short Notes
> 10 Scenario Based Questions

---

## Q1. Cascading Failures — slow downstream exhausts thread pool

```
What happens-  slow payment → order threads wait | thread pool exhausted | order fails too ❌
Root cause-    no timeout | no circuit breaker | threads block indefinitely ❌
Fix 1-         timeout = max wait 3s | fail fast | dont wait forever ✅
Fix 2-         circuit breaker Resilience4j | CLOSED→OPEN→HALF_OPEN | fallback response ✅
Fix 3-         bulkhead = separate thread pool per downstream | payment pool 10 threads ✅
               payment slow → only payment pool exhausted | order pool unaffected ✅
Fix 4-         async Kafka | dont call payment directly | decouple ✅
Fallback-      return PENDING | queue for retry | customer notified ✅
Monitor-       CloudWatch thread pool metrics | PagerDuty alert ✅
```

---

## Q2. Distributed Transactions — payment succeeds inventory fails

```
Problem-       payment charged | inventory not reserved | inconsistency ❌
No 2PC-        distributed 2PC = too slow | single point failure | avoid ❌
Fix-           Saga pattern choreography ✅
Flow-          reserve inventory → SUCCESS → charge payment → SUCCESS → create shipment ✅
Failure-       inventory fails → compensation → refund payment ✅
               payment fails → compensation → release inventory ✅
Compensation-  reverse previous step | release inventory | refund payment ✅
Outbox-        save event+data same @Transactional | scheduler publishes ✅
Idempotency-   each step checks already done | processedEventId ✅
Tools-         Kafka choreography | or Temporal/Step Functions orchestration ✅
```

---

## Q3. Data Consistency — order confirmed but inventory not decremented

```
Problem-       missed event | order=confirmed | inventory=not decremented ❌
Detect-        reconciliation job | compare order count vs inventory ✅
               run nightly | find discrepancies | alert ✅
Prevent 1-     Outbox pattern | save+event same @Transactional | no missed event ✅
Prevent 2-     @RetryableTopic | retry failed events | DLT for permanent failures ✅
Prevent 3-     consumer idempotency | processedEventId table | no skip ✅
Prevent 4-     event sourcing | store all events | replay to rebuild state ✅
Monitor-       consumer lag CloudWatch | lag>threshold → alert ✅
Fix missed-    replay from earliest offset | reprocess all events ✅
```

---

## Q4. API Breaking Changes — field change consumed by 3 teams

```
Problem-       change field | 3 consumers | cannot deploy simultaneously ❌
Fix-           API versioning + backward compatibility ✅
Step 1-        add NEW field alongside OLD field ✅
               deprecate old | keep both temporarily ✅
Step 2-        version API /v1 /v2 ✅
               v1 = old field | v2 = new field ✅
Step 3-        notify consumers | give migration timeline ✅
Step 4-        each team migrates own pace ✅
Step 5-        deprecate v1 after all migrated ✅
OpenAPI-       @deprecated annotation | document migration guide ✅
Never-         never remove field without deprecation period ❌
Consumer driven contracts- Pact testing | verify before deploy ✅
```

---

## Q5. Debugging 8 Second Request — no single service abnormal

```
Problem-       8s total | no single service shows issue | distributed bottleneck ❌
Tool-          distributed tracing | trace ID across all services ✅
Tools-         Jaeger | Zipkin | AWS X-Ray | New Relic | Sleuth ✅
Step 1-        find trace ID from request ✅
Step 2-        open Jaeger/Zipkin | enter trace ID ✅
Step 3-        see waterfall diagram | all 6 services ✅
Step 4-        find which span took longest ✅
Step 5-        check that service logs ✅
Common causes- DB slow query | N+1 | external API slow | connection pool wait ✅
               GC pause | network latency | serialization ✅
Fix-           EXPLAIN ANALYZE slow query | index | optimize code ✅
```

---

## Q6. Cross-service Reporting — join Orders Payments Shipping

```
Problem-       each service own DB | no direct join | finance needs combined report ❌
Fix 1-         CQRS read model | dedicated reporting service ✅
               consume events from all 3 services ✅
               build denormalized report table ✅
               finance queries report service ✅
Fix 2-         data warehouse | ETL pipeline ✅
               nightly batch → copy to Redshift/Snowflake ✅
               finance queries warehouse ✅
Fix 3-         API composition | BFF service ✅
               calls all 3 services | combines in memory ✅
               only for small datasets ✅
Avoid-         direct DB cross queries ❌ | violates service boundary ❌
Best-          CQRS read model for real time ✅
               data warehouse for complex analytics ✅
```

---

## Q7. Idempotency and Retries — customer charged twice

```
Problem-       order calls payment | timeout | retry | first call succeeded ❌
               customer charged twice ❌
Fix-           idempotency key ✅
Flow-          client generates UUID idempotency key ✅
               send in header X-Idempotency-Key ✅
               payment service checks key before processing ✅
               key exists → return SAME response ✅
               key not exists → process → save key ✅
Storage-       Redis fast lookup | DB idempotency table ✅
TTL-           idempotency key TTL = 24 hours ✅
Producer-      enable.idempotence=true | sequence number ✅
Response-      always return same response for same key ✅
Test-          retry 3 times | customer charged once ✅
```

---

## Q8. Rolling Deployment — 30 second connection errors

```
Problem-       rolling deploy | old pods terminating | new pods starting ❌
               requests hit terminating pods → connection error ❌
Root cause-    load balancer not updated fast enough ❌
               pod removed before LB drains connections ❌
Fix 1-         preStop hook | sleep 15s before shutdown ✅
               gives LB time to stop routing ✅
Fix 2-         terminationGracePeriodSeconds = 60s ✅
               finish in-flight requests ✅
Fix 3-         readinessProbe | pod not ready until warm ✅
               LB only routes to ready pods ✅
Fix 4-         maxUnavailable=0 | maxSurge=1 ✅
               new pod ready before old removed ✅
Fix 5-         Blue-Green deployment ✅
               instant switch | zero downtime ✅
```

---

## Q9. Kafka Consumer Lag Growing — peak traffic

```
Problem-       consumer lag growing | downstream stale | peak traffic ❌
Diagnose-      check consumer CPU/memory | check DB slow queries ✅
               check external API calls | check partition count ✅
               check max.poll.records | check thread count ✅
Cause 1-       consumers < partitions | increase consumers = partitions ✅
Cause 2-       slow DB | optimize queries | batch saveAll ✅
Cause 3-       external API slow | async | circuit breaker ✅
Cause 4-       max.poll.records too low | increase ✅
Cause 5-       consumer crashed | check logs | restart ✅
Fix 1-         increase partitions + consumers ✅
Fix 2-         batch processing listener.type=batch ✅
Fix 3-         CompletableFuture parallel processing ✅
Fix 4-         consumer.pause() when overwhelmed | resume when ready ✅
Fix 5-         auto scale ECS on lag metric ✅
Monitor-       CloudWatch lag > 10000 → PagerDuty ✅
```

---

## Q10. Circuit Breaker vs Bulkhead — flaky 3rd party API

```
Circuit breaker alone fails when-
→ 100 slow requests simultaneously ❌
→ thread pool exhausted BEFORE threshold reached ❌
→ circuit never opens ❌
→ cascade failure happens ❌

Bulkhead alone fails when-
→ all 10 bulkhead threads slow/failing ❌
→ no automatic stop calling ❌
→ keeps hammering failed service ❌
→ wastes resources ❌

Why combine both ✅
→ bulkhead = limit threads per 3rd party ✅
   payment 10 threads | others unaffected ✅
→ circuit breaker = stop when failures > 50% ✅
   open circuit | fallback immediately ✅
→ together = isolated + stopped ✅

Real example-  3rd party API flaky ✅
               bulkhead 10 threads ✅
               circuit breaker 50% threshold ✅
               flaky → threads slow → circuit opens ✅
               fallback returned | main app healthy ✅
```

---

## Quick Reference

```
Cascade failure-  timeout + circuit breaker + bulkhead + async ✅
Saga-             choreography events | compensation on failure | no 2PC ✅
Consistency-      Outbox + retry + idempotency + reconciliation job ✅
API versioning-   /v1 /v2 | deprecate old | both fields temporarily ✅
Distributed trace- Jaeger/Zipkin | trace ID | waterfall diagram | find slow span ✅
Reporting-        CQRS read model | data warehouse | avoid cross DB queries ✅
Idempotency-      X-Idempotency-Key UUID | check before process | Redis ✅
Rolling deploy-   preStop hook | readinessProbe | maxUnavailable=0 | Blue-Green ✅
Kafka lag-        increase consumers=partitions | batch | parallel | auto scale ✅
CB+Bulkhead-      bulkhead=isolate | CB=stop calling | combine both ✅
```
