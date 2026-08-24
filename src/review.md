Distributed event streaming platform  - async communication - Decoupling - cascade Failure - High Throughput - Retention + replay - Fan-out - Event sourcing

At most once - auto ack before processing - in case of crash → message LOST - no duplicate - but data loss

At least once - manual ack after processing  - in case of Crash - reprocessed - duplicate

Exactly once - no loss + no duplicate - FOUR things - idempotence - acks - manual ack - Idempotency check - processedEventId - DB Constraint

-----

Spring Cloud components:

1. Spring Cloud Config ✅
   → centralized config server ✅
   → Git backed ✅
   → dev/stage/prod configs ✅

2. Eureka (Service Discovery) ✅
   → services register ✅
   → find each other ✅

3. Spring Cloud Gateway ✅
   → API Gateway ✅
   → routing + filters ✅
   → rate limiting ✅

4. Resilience4j ✅
   → circuit breaker ✅
   → retry ✅
   → rate limiter ✅

5. Spring Cloud Sleuth ✅
   → distributed tracing ✅
   → trace ID across services ✅

6. Feign Client ✅
   → declarative REST client ✅
   → no RestTemplate needed ✅


------
"I have led development teams across
onsite and offshore:

Team structure:
→ 2 developers onsite ✅
→ 2 developers offshore ✅
→ 2 QA onsite + offshore ✅

Code review approach:

Step 1 — Requirements check:
→ compare PR with requirements ✅
→ does it solve the right problem ✅

Step 2 — Code quality:
→ data structures used correctly ✅
→ design patterns applied ✅
→ no code duplication ✅
→ proper exception handling ✅
→ logging at right levels ✅
→ no hardcoded values ✅
→ unit tests written ✅

Step 3 — Performance check:
→ N+1 queries ✅
→ indexes on queried columns ✅
→ DB calls optimized ✅
→ no unnecessary loops ✅

Step 4 — Feedback:
→ unclear code → call developer ✅
→ explain what needs change ✅
→ not satisfied → live demo ✅
→ show me locally how it works ✅

Mentoring:
→ knowledge sharing sessions ✅
→ explain design patterns ✅
→ pair programming on
complex features ✅
→ guide on best practices ✅
→ constructive feedback ✅
→ team improved significantly ✅

Agile:
→ daily standups ✅
→ sprint planning ✅
→ unblocking team members ✅"


------------------------------------------------------------------------------------

A = Authentication + API design ✅
C = Concurrency + async ✅
I = Idempotency ✅
D = DB optimization ✅
S = Scaling + infra ✅
I = Integration + recovery ✅

Client
↓ JWT + rate limit
API Gateway
↓ 202 Accepted
Payment API (Virtual Threads)
↓ idempotency check
Redis (fast duplicate check)
↓ publish
Kafka (order-payment-events)
↓ consume
Payment Consumer Service
↓ Outbox pattern
Aurora (writer endpoint)
↓ schedule publish
Kafka
↓ downstream
Notification + Settlement


Step 1 — A: Authentication (JWT) ✅
"First secure the API"
→ JWT validation at API Gateway ✅
→ rate limiting per client ✅
→ 429 Too Many Requests on exceed ✅

Step 2 — C: Concurrency + Async ✅
"Handle 10k-30k requests"
→ Virtual Threads ✅
millions of lightweight threads ✅
→ CompletableFuture ✅
async processing ✅
→ Kafka for async ✅
API receives → publishes to Kafka ✅
consumer processes payment ✅
client gets 202 Accepted ✅

Step 3 — I: Idempotency ✅
"No duplicate transactions"
→ idempotency key in header ✅
X-Idempotency-Key: UUID ✅
→ check DB before processing ✅
→ duplicate → return same response ✅
→ Redis for fast lookup ✅
→ DB unique constraint ✅

Step 4 — D: DB optimization ✅
"Fast DB operations"
→ HikariCP connection pool ✅
→ indexes on payment columns ✅
→ EXPLAIN ANALYZE slow queries ✅
→ select only required columns ✅
→ avoid N+1 — JOIN FETCH ✅
→ CQRS — separate read/write ✅
write → Aurora writer ✅
read → Aurora reader ✅
→ Outbox Pattern ✅
save + event same transaction ✅

Step 5 — S: Scaling + Infra ✅
"Handle spikes 30k/sec"
→ horizontal scaling ECS ✅
→ auto scaling CPU > 70% ✅
→ ALB distributes traffic ✅
→ Kafka partitions ✅
parallel consumption ✅

Step 6 — I: Integration + Recovery ✅
"Failure handling"
→ Circuit Breaker (Resilience4j) ✅
payment gateway down → fallback ✅
→ @RetryableTopic DLT ✅
failed → retry → DLT ✅
→ DLQ monitoring + alerts ✅
→ CloudWatch + PagerDuty ✅

------------------------------------------------------------------------------------