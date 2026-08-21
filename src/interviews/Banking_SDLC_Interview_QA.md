# Banking / Full SDLC — Interview Q&A
> 5 Questions based on Job Description
> Banking + Java + Spring Boot + AWS + Full SDLC + Leadership

---

## Q1. End-to-end ownership in Banking/Financial — walk through a project?

### Answer
```
Gap Inc — Payment Domain — 3 years ✅
25+ Spring Boot microservices ✅
AWS ECS + Aurora + Kafka ✅
PCI DSS compliant ✅
```

### Complete payment flow
```
Step 1 — Receive from OMS:
→ credit card details ✅
→ order details ✅
→ customer address ✅

Step 2 — ACI Fraud Validation:
→ send details to ACI vendor ✅
→ ACCEPT  → proceed to auth ✅
→ DENY    → decline + notify OMS ✅
→ CHALLENGE → bot/agent verify ✅
              customer confirms ✅

Step 3 — Authorization:
→ card → Vault Service → VaultID ✅
→ Bluefin encrypts ✅
→ Chase payment gateway ✅
→ card network → authorization ✅
→ hold placed — not deducted ✅
→ PCI DSS — card never stored ✅

Step 4 — Capture:
→ order shipped ✅
→ invoice generated ✅
→ capture amount from Chase ✅
→ idempotency — no double charge ✅

Step 5 — Other flows:
→ cancel → void auth ✅
→ return → refund ✅
→ reference refund ✅
→ blind refund ✅
→ credit memo (price match) ✅
→ return recharge ✅

Step 6 — Account Updater:
→ card about to expire ✅
→ call card network ✅
→ get new card details ✅
→ customer unaware ✅
→ seamless experience ✅
```

### End-to-end ownership
```
Requirements:
→ business + OMS team ✅
→ Confluence documentation ✅
→ JIRA stories ✅

Development:
→ 25+ Spring Boot microservices ✅
→ Kafka event driven ✅
→ AWS ECS + Aurora ✅

Testing:
→ JUnit + Mockito ✅
→ Cucumber integration tests ✅
→ UAT support ✅

Deployment:
→ Jenkins Blue-Green ✅
→ CM ticket — PCI compliance ✅
→ zero downtime ✅

Production:
→ Splunk + CloudWatch ✅
→ PagerDuty on-call ✅
→ immediate response ✅

Banking compliance:
→ PCI DSS — card never stored ✅
→ idempotency — no double charge ✅
→ full audit trail ✅
```

---

## Q2. Performance tuning and troubleshooting — real example?

### Real example — DHL
```
Project: Ownership Transfer Service ✅

Problem:
→ receiving PO extract files ✅
→ ASN + Advance Notification files ✅
→ from Nexus vendor ✅
→ processing never completed ❌
→ running for days ❌
→ blocking downstream systems ❌

How identified:
→ colleague raised alert ✅
→ code review immediately ✅
→ spotted issue by reading code ✅
→ division used instead of
  multiplication ❌
→ caused infinite loop ✅
→ counter never reached
  termination condition ❌

Root cause:
→ developer typo — wrong operator ✅
→ no unit test caught it ❌
→ no boundary check ❌

Fix:
→ corrected operator ✅
→ added unit tests ✅
→ added boundary validation ✅
→ added per-file logging ✅
→ can track progress now ✅

Result:
→ files processed in minutes ✅
→ never seen again ✅

Long-term solution:
→ unit tests for all processors ✅
→ alert if process > 30 minutes ✅
→ code review checklist updated ✅
```

### Other performance examples
```
Payment domain:
→ N+1 fixed with JOIN FETCH ✅
→ HikariCP pool tuning ✅
→ Kafka consumer lag reduced ✅
→ slow query fixed with index ✅
→ TRUNC(column) → BETWEEN fix ✅

OOM fix:
→ missing @Qualifier ✅
→ KafkaTemplate × 5000 objects ❌
→ heap dump + Eclipse MAT ✅
→ added @Qualifier → singleton ✅
→ memory back to normal ✅
```

### Debugging tools used
```
→ Splunk — log search ✅
→ CloudWatch — metrics + alarms ✅
→ /actuator/heapdump → Eclipse MAT ✅
→ /actuator/threaddump ✅
→ slow query log ✅
→ EXPLAIN ANALYZE ✅
→ New Relic ✅
→ Grafana ✅
```

---

## Q3. Leading code reviews and mentoring — example?

### Team structure
```
Onsite:
→ 2 developers ✅
→ 2 QA ✅

Offshore:
→ 2 developers ✅
→ 2 QA ✅

Total: 4 developers + 4 QA ✅
```

### Code review checklist
```
Step 1 — Requirements check:
→ compare PR with requirements ✅
→ does it solve right problem ✅
→ any missing scenarios ✅

Step 2 — Code quality:
→ data structures correct ✅
→ design patterns applied ✅
→ no code duplication ✅
→ proper exception handling ✅
→ logging at right levels ✅
→ no hardcoded values ✅
→ unit tests written ✅
→ readable + maintainable ✅

Step 3 — Performance:
→ N+1 queries checked ✅
→ indexes on queried columns ✅
→ DB calls optimized ✅
→ no unnecessary loops ✅
→ caching considered ✅

Step 4 — Security:
→ no sensitive data logged ✅
→ no hardcoded credentials ✅
→ input validation ✅

Step 5 — Feedback:
→ unclear code → call developer ✅
→ explain what needs change ✅
→ not satisfied → live demo ✅
→ show me locally ✅
→ constructive feedback ✅
```

### Mentoring approach
```
Before coding:
→ ask developer their approach ✅
→ if gap → clarify ✅
→ proactive conversation ✅
→ prevents rework ✅

During development:
→ pair programming on complex ✅
→ explain design patterns ✅
→ share best practices ✅

Knowledge sharing:
→ weekly sessions ✅
→ design pattern discussions ✅
→ performance tips ✅
→ security best practices ✅
```

### Agile delivery
```
→ daily standups ✅
→ sprint planning ✅
→ story creation + estimation ✅
→ unblocking team members ✅
→ retrospectives ✅
```

---

## Q4. Databases, messaging technologies and caching — what have you worked with?

### Relational Databases
```
Oracle:
→ BBC, enterprise projects ✅
→ complex queries ✅
→ stored procedures ✅

MySQL:
→ payment domain ✅
→ Gap Inc ✅
→ Aurora MySQL ✅

Azure SQL:
→ BT project ✅
→ Azure cloud ✅

DB2:
→ legacy mainframe projects ✅
→ IBM mainframe integration ✅

Aurora PostgreSQL:
→ AWS projects ✅
→ writer + reader endpoints ✅
→ AbstractRoutingDataSource ✅
```

### NoSQL Databases
```
MongoDB:
→ document store ✅
→ JSON documents ✅
→ on-premise to cloud migration ✅
→ sharding + replica sets ✅

DynamoDB:
→ AWS key-value + document ✅
→ partition key lookups ✅
→ GSI for list queries ✅
→ pay per request billing ✅
```

### Messaging Technologies
```
Kafka (primary - 5+ years):
→ event streaming ✅
→ payment, OMS, supply chain ✅
→ Avro schema ✅
→ Schema Registry ✅
→ @RetryableTopic + DLT ✅
→ Outbox pattern ✅
→ millions TPS ✅

IBM MQ:
→ legacy enterprise projects ✅
→ point to point ✅

AWS SQS:
→ cloud projects ✅
→ standard + FIFO ✅
→ DLQ configured ✅

Azure Service Bus:
→ Azure projects ✅
→ Queue + Topic ✅

RabbitMQ:
→ exchange types ✅
→ Direct, Topic, Fanout ✅
```

### Caching — Real example at DHL
```
Problem:
→ transportation partners data ✅
→ Bangladesh → Ohio route ✅
→ different partners per lane ✅
→ DB call every request ❌
→ slow response ❌

Solution:
→ Spring Cache @Cacheable ✅
→ cache by origin + destination ✅
→ avoids DB call every time ✅
→ response time improved ✅

Cache clear mechanism:
→ @CacheEvict endpoint ✅
→ new partner added ✅
→ clear cache ✅
→ next call → fresh from DB ✅
```

```java
// Caching example ✅
@Cacheable(
    value = "transportationPartners",
    key = "#origin + '-' + #destination")
public List<Partner> getPartners(
        String origin, String destination) {
    return partnerRepository
            .findByRoute(origin, destination);
}

@CacheEvict(
    value = "transportationPartners",
    allEntries = true)
public void addPartner(Partner partner) {
    partnerRepository.save(partner);
    // cache cleared → fresh data ✅
}

// Cache config — Caffeine ✅
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager manager =
            new CaffeineCacheManager();
    manager.setCaffeine(
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(
                Duration.ofMinutes(30)));
    return manager;
}
```

---

## Q5. Complete SDLC + UAT + Production support — real example?

### Full SDLC ownership
```
Requirements phase:
→ high level from manager ✅
→ document in Confluence ✅
→ technical architecture ✅
→ sequence diagrams ✅
→ brainstorm sessions ✅
→ refine until conclusion ✅
→ manager sign-off ✅

Development phase:
→ create JIRA stories ✅
→ estimation with team ✅
→ ask developer approach BEFORE coding ✅
→ clarify understanding gaps ✅
→ proactive conversations ✅
→ daily standups ✅
→ code reviews ✅

QA phase:
→ involve QA early ✅
→ clarify QA understanding ✅
→ defect triage ✅
   real vs misunderstanding ✅
→ fix + re-validate ✅
→ thorough regression ✅

UAT phase:
→ full attention on UAT calls ✅
→ solve issues on the spot ✅
→ product manager testing ✅
→ handle all questions ✅
→ no defect leaked to prod ✅

Deployment:
→ Jenkins Blue-Green ✅
→ CM ticket for compliance ✅
→ deploy inactive slot ✅
→ approve → swap ✅
→ zero downtime ✅

Production support:
→ first person on production calls ✅
→ Splunk + CloudWatch ✅
→ PagerDuty on-call ✅
→ analyze immediately ✅
→ fix + deploy ✅
```

### Real production incident
```
At Gap payment domain:

Problem:
→ Chase Gateway timing out ❌
→ 5XX errors spike ✅
→ PagerDuty alert fired ✅

Response:
→ jumped on call immediately ✅
→ checked CloudWatch logs ✅
→ found 5XX errors spike ✅
→ checked circuit breaker metrics ✅
→ circuit was OPEN ✅
→ fallback returning PENDING ✅

Resolution:
→ contacted Chase team ✅
→ their side issue confirmed ✅
→ held orders in queue ✅
→ Chase recovered ✅
→ circuit CLOSED ✅
→ orders processed ✅
→ zero data loss ✅
→ customers notified ✅

Root cause analysis:
→ documented in Confluence ✅
→ added better alerting ✅
→ adjusted circuit breaker thresholds ✅
→ never repeated ✅
```

---

## Quick Reference — This JD Key Points

| Requirement | Your Experience |
|---|---|
| Banking/Financial domain | Gap payment domain — 3 years ✅ |
| End-to-end ownership | Requirements to production ✅ |
| Java + Spring Boot | 12+ years ✅ |
| AWS services | ECS, Aurora, SQS, Lambda ✅ |
| RESTful APIs | 25+ microservices ✅ |
| Full SDLC | Confluence + JIRA + deploy ✅ |
| Unit testing | JUnit + Mockito ✅ |
| Integration testing | Cucumber ✅ |
| UAT support | On calls + on spot fixes ✅ |
| Production support | First on call ✅ |
| Relational DB | Oracle, MySQL, Aurora ✅ |
| NoSQL DB | MongoDB, DynamoDB ✅ |
| Messaging | Kafka 5+ years, IBM MQ, SQS ✅ |
| Caching | Caffeine + @Cacheable ✅ |
| Performance tuning | N+1, indexes, OOM, infinite loop ✅ |
| Code reviews | 4 devs + 4 QA team ✅ |
| Mentoring | Proactive + pair programming ✅ |
| Agile | Sprints + standups ✅ |
| Root cause analysis | Document + prevent recurrence ✅ |
| Architecture reviews | Technical docs + brainstorm ✅ |
