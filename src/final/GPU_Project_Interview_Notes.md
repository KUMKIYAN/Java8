# GPU Project — Interview Short Notes

---

## Q1. Project walkthrough-
```
GPO | JSON | custom logic | XML | EDI | Nexus | XML content |  tech stack | team size | challenge | scale | 
Azure blob | MongoDB | Kuber naties cluster | newrelic and splunk | saga
```

---

## Q2. EDI Downstream Down
```
Kafka holds | async | 14 days retention | cascade failure | reads from commit offset | manual ack | retry  | DLT  | alert | consumer lag Alert.
```

---

## Q3. JSON → XML Transformation
```

XSD schema validation | order types: create/update/cancel | deduplication publishable=true/false
multiple updates → only latest processed | JAXB marshalling @XmlRootElement @XmlElement | invalid - DLT | Valid - EDI

```

---

## Q4. MongoDB Experience
```
large messages → Azure Blob | store URL in MongoDB | inbound+outbound collections | gpo-attributes collection
aggregation pipeline delayed events | $match→$sort→$group→$limit | pick next eligible event per order | no ACK
collections | documents | _id | @Document @Field | @Aggregation | 

```

---

## Q5. Duplicate Messages
```
enable.idempotence=true | sequence number | broker ignores duplicate | acks=all ISR
consumer idempotency check | already processed→ack+skip | manual ack | DLT bad messages 

```

---

## Q6. MongoDB Aggregation vs SQL GROUP BY
```
aggregation=pipeline stages each output→next input | $match=WHERE | $group=GROUP BY | $sort | $limit | $lookup=JOIN
SQL GROUP BY=single operation | MongoDB=chain flexible | filter before+after group | $unwind flatten arrays
```

---

## Q7. Service Communication + Failure
```
Kafka async-     primary communication | producer not wait consumer | fully decoupled
Messages in broker- downstream down = messages wait safely | no cascade failure
Our service fails- resume from last committed offset | 14 days retention | no loss
Outbox pattern-  save data+event same @Transactional | scheduler publishes | guaranteed delivery
REST calls-      circuit breaker Resilience4j | CLOSED→OPEN(threshold)→HALF_OPEN(test)→CLOSED
Fallback-        fallback response when circuit OPEN | no waiting forever
Timeout-         configured on every external call | fail fast 
```

---

## Q8. Data Consistency
```
Outbox pattern | save data+event same @Transactional | both commit or rollback | publishable=false initially
scheduler every 1 hour picks publishable=false | publish→mark true | failure→stays false→retry next run | idempotency check

```
---

## Q10. Azure Blob + Claim Check Pattern
```
Kafka max 16MB | unknown message size | Azure Blob no size limit | store inbound+outbound | URL in Kafka message lightweight | EDI fetches from URL 
tradeoffs: extra network call | light latency | speed publish | Blob availability dependency | cleanup old Blobs | eventual consistency ok
```
