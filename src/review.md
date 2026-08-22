Distributed event streaming platform  - async communication - Decoupling - cascade Failure - High Throughput - etention + replay - Fan-out - Event sourcing

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


