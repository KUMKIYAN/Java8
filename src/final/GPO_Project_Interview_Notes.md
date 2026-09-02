# GPU Project — Interview Short Notes

---

## Q1. Walk me through your most recent project — what was your role, tech stack and what problem did you solve?
```
GPO | saga | SON | custom logic | XML | EDI | Nexus | XML content |  tech stack | team size | challenge | scale | 
Azure blob | MongoDB | Kuber naties cluster | newrelic and splunk  
```

---

## Q2. In your GPO project you mentioned Kafka. What happens if EDI team downstream is down? How did you handle it?
```
Kafka holds | async | 14 days retention | cascade failure | reads from commit offset | manual ack 
retry  | DLT  | alert | consumer lag Alert.
```

---

## Q3. You mentioned JSON to XML transformation. How did you handle different order types with different mapping rules?
```
XSD schema validation | JAXB marshalling | @XmlRootElement @XmlElement | invalid - DLT | Valid - EDI
order types: create/update/cancel | deduplication publishable=true/false
multiple updates → only latest processed 


```
---

## Q4. This role mentions MongoDB. Have you worked with MongoDB? How is it different from relational DB?
```
large messages → Azure Blob | store URL in MongoDB | inbound+outbound collections | gpo-attributes collection
collections | documents | _id | MongoRepository | @Document | @Field | @Aggregation 
aggregation pipeline delayed events | $match→$sort→$group→$limit | pick next eligible event per order | no ACK


```
---
## Q5. What is your approach to handling duplicate messages in Kafka? Give a real example from your project.
```
enable.idempotence=true | sequence number | broker ignores duplicate | acks=all ISR
consumer idempotency check | already processed→ack+skip | manual ack | bad messages 

```

---

## Q6. What is the difference between MongoDB aggregation pipeline and SQL GROUP BY? Give example from your project.
```
aggregation=pipeline stages each output→next input | $match=WHERE | $group=GROUP BY | $sort | $limit | $lookup=JOIN
MongoDB=chain flexible
SQL GROUP BY=single operation 
```

---

## Q7. Your project uses microservices. How do services communicate? What happens if one service fails?
```
Kafka async  | producer not wait consumer | messages in broker | casecade failure 
our service fails→resume last offset | outbox pattern | REST calls→circuit breaker Resilience4j CLOSED→OPEN→HALF_OPEN | fallback
```

---

## Q8. How did you ensure data consistency in your GPU project when publishing to multiple downstream systems?
```
Outbox pattern | save data+event same @Transactional | both commit or rollback | publishable=false initially
scheduler every 1 hour picks publishable=false | publish true | idempotency check

```
---

## Q10. You mentioned Azure Blob for storing large messages. Why did you choose Blob storage over sending directly in Kafka? What are the tradeoffs?
```
Kafka max 16MB | unknown message size | Azure Blob no size limit | store inbound+outbound | URL in Kafka message lightweight | EDI fetches from URL 
tradeoffs: extra network call | light latency | speed publish | Blob availability dependency | cleanup old Blobs | eventual consistency ok
```
