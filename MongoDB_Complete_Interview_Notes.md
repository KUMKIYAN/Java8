# MongoDB — Complete Interview Notes
> Quick Reference for Interview & Development

---

## 1. What is MongoDB?

```
MongoDB = NoSQL document database
→ stores data as JSON-like documents (BSON)
→ schema-less — no fixed structure
→ horizontally scalable
→ open source (2009)

When to use MongoDB:
→ flexible/dynamic schema
→ hierarchical data (nested objects)
→ high write throughput
→ horizontal scaling needed
→ JSON/document storage
```

---

## 2. MongoDB vs SQL

| | MongoDB | SQL (PostgreSQL/MySQL) |
|---|---|---|
| **Data model** | Documents (JSON) | Tables (rows/columns) |
| **Schema** | Flexible | Fixed |
| **Relationships** | Embedded or reference | JOIN |
| **Query** | MongoDB Query Language | SQL |
| **Scale** | Horizontal ✅ | Vertical |
| **ACID** | ✅ (4.0+) | ✅ |
| **Use case** | Dynamic data, docs | Structured, relational |

---

## 3. Key Concepts

```
Database    → container of collections
Collection  → group of documents (like SQL table)
Document    → JSON object (like SQL row)
Field       → key-value pair (like SQL column)
_id         → unique identifier (auto-generated ObjectId)
Index       → improves query performance
Replica Set → copies of data for fault tolerance
Sharding    → horizontal scaling across servers
```

---

## 4. BSON vs JSON

```
JSON  = text based, human readable
BSON  = Binary JSON — MongoDB storage format

BSON adds:
→ more data types (Date, ObjectId, Binary, Decimal128)
→ faster to parse (binary format)
→ supports larger documents

ObjectId = 12 byte unique identifier
         = timestamp(4) + machine(3) + process(2) + counter(3)
```

---

## 5. CRUD Operations

### Insert
```js
// insert one
db.orders.insertOne({
    customerId: "CUST001",
    status: "PAID",
    amount: 100.50,
    items: ["laptop", "mouse"],
    createdAt: new Date()
})

// insert many
db.orders.insertMany([
    { customerId: "CUST001", amount: 100 },
    { customerId: "CUST002", amount: 200 }
])
```

### Find (SELECT)
```js
// find all
db.orders.find()

// find with filter
db.orders.find({ status: "PAID" })

// find one
db.orders.findOne({ _id: ObjectId("...") })

// projection — select specific fields
db.orders.find(
    { status: "PAID" },
    { customerId: 1, amount: 1, _id: 0 } // 1=include, 0=exclude
)

// sort, limit, skip
db.orders.find()
    .sort({ amount: -1 })  // -1 DESC, 1 ASC
    .limit(10)
    .skip(20)              // pagination
```

### Update
```js
// update one
db.orders.updateOne(
    { _id: ObjectId("...") },          // filter
    { $set: { status: "SHIPPED" } }    // update
)

// update many
db.orders.updateMany(
    { status: "PENDING" },
    { $set: { status: "PROCESSING" } }
)

// upsert — insert if not exists
db.orders.updateOne(
    { customerId: "CUST001" },
    { $set: { status: "PAID" } },
    { upsert: true }                   // insert if not found ✅
)

// update operators
$set      → set field value
$unset    → remove field
$inc      → increment number
$push     → add to array
$pull     → remove from array
$addToSet → add to array if not exists
```

### Delete
```js
// delete one
db.orders.deleteOne({ _id: ObjectId("...") })

// delete many
db.orders.deleteMany({ status: "CANCELLED" })

// delete all
db.orders.deleteMany({})
```

---

## 6. Query Operators

```js
// comparison
{ amount: { $gt: 100 } }          // greater than
{ amount: { $gte: 100 } }         // greater than or equal
{ amount: { $lt: 500 } }          // less than
{ amount: { $lte: 500 } }         // less than or equal
{ amount: { $eq: 100 } }          // equal
{ amount: { $ne: 100 } }          // not equal
{ status: { $in: ["PAID","SHIPPED"] } }  // in array
{ status: { $nin: ["CANCELLED"] } }      // not in array

// logical
{ $and: [{ status: "PAID" }, { amount: { $gt: 100 } }] }
{ $or:  [{ status: "PAID" }, { status: "SHIPPED" }] }
{ $not: [{ status: "CANCELLED" }] }
{ $nor: [{ status: "CANCELLED" }, { status: "FAILED" }] }

// element
{ note: { $exists: true } }       // field exists
{ amount: { $type: "number" } }   // field type check

// array
{ items: { $all: ["laptop", "mouse"] } }  // array contains all
{ items: { $size: 2 } }                   // array size = 2
{ items: "laptop" }                        // array contains value

// text search
db.orders.find({ $text: { $search: "laptop" } })

// regex
{ customerId: { $regex: /^CUST/i } }
```

---

## 7. Aggregation Pipeline

```js
// pipeline = array of stages
// each stage transforms documents

db.orders.aggregate([
    // Stage 1 — filter (WHERE)
    { $match: { status: "PAID" } },

    // Stage 2 — group (GROUP BY)
    { $group: {
        _id: "$customerId",
        totalAmount: { $sum: "$amount" },
        orderCount:  { $count: {} },
        avgAmount:   { $avg: "$amount" },
        maxAmount:   { $max: "$amount" },
        minAmount:   { $min: "$amount" }
    }},

    // Stage 3 — sort (ORDER BY)
    { $sort: { totalAmount: -1 } },

    // Stage 4 — limit
    { $limit: 10 },

    // Stage 5 — project (SELECT specific fields)
    { $project: {
        _id: 0,
        customerId: "$_id",
        totalAmount: 1,
        orderCount: 1
    }}
])
```

### All aggregation stages
```js
$match      → filter documents (WHERE)
$group      → group and aggregate
$sort       → sort documents
$project    → reshape/select fields
$limit      → limit results
$skip       → skip documents
$unwind     → flatten array to separate documents
$lookup     → JOIN with another collection
$addFields  → add computed fields
$replaceRoot→ replace document root
$count      → count documents
$facet      → multiple aggregations in one pass
$bucket     → group into ranges
```

### $lookup — JOIN
```js
db.orders.aggregate([
    {
        $lookup: {
            from:         "customers",  // collection to join
            localField:   "customerId", // field in orders
            foreignField: "_id",        // field in customers
            as:           "customer"    // output array field
        }
    },
    { $unwind: "$customer" },           // flatten array → object
    {
        $project: {
            orderId:      "$_id",
            amount:       1,
            customerName: "$customer.name",
            customerEmail:"$customer.email"
        }
    }
])
```

### $unwind — flatten array
```js
// document:
{ _id: 1, items: ["laptop", "mouse", "keyboard"] }

// after $unwind: { path: "$items" }
{ _id: 1, items: "laptop" }
{ _id: 1, items: "mouse" }
{ _id: 1, items: "keyboard" }
// one document per array element ✅
```

---

## 8. Indexes

```js
// create index
db.orders.createIndex({ status: 1 })           // single field
db.orders.createIndex({ customerId: 1, status: 1 }) // compound
db.orders.createIndex({ title: "text" })        // text search
db.orders.createIndex({ location: "2dsphere" }) // geospatial
db.orders.createIndex(
    { createdAt: 1 },
    { expireAfterSeconds: 86400 }               // TTL index — auto delete
)
db.orders.createIndex(
    { email: 1 },
    { unique: true }                            // unique index
)

// list indexes
db.orders.getIndexes()

// drop index
db.orders.dropIndex("status_1")

// explain — check if index used
db.orders.find({ status: "PAID" }).explain("executionStats")
// COLLSCAN = full scan ❌
// IXSCAN   = index scan ✅
```

---

## 9. Schema Design

### Embedding (denormalization)
```js
// embed related data inside document
{
    _id: ObjectId("..."),
    customerId: "CUST001",
    status: "PAID",
    // embedded address — no separate collection
    address: {
        street: "123 Main St",
        city:   "New York",
        zip:    "10001"
    },
    // embedded items array
    items: [
        { name: "Laptop", price: 999, qty: 1 },
        { name: "Mouse",  price: 29,  qty: 2 }
    ]
}
// pros: single query, fast reads ✅
// cons: data duplication, large documents ❌
```

### Referencing (normalization)
```js
// store reference (_id) to another collection
{
    _id: ObjectId("..."),
    customerId: ObjectId("CUST001"),  // reference ✅
    status: "PAID",
    items: [
        ObjectId("ITEM001"),           // reference ✅
        ObjectId("ITEM002")
    ]
}
// pros: no duplication, smaller documents ✅
// cons: multiple queries needed (JOIN via $lookup) ❌
```

### When to embed vs reference
```
EMBED when:
→ data always accessed together
→ one-to-few relationship
→ data does not change often
→ child cannot exist without parent

REFERENCE when:
→ data accessed independently
→ one-to-many or many-to-many
→ data changes frequently
→ documents would be too large
```

---

## 10. Spring Boot + MongoDB

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

### application.yml
```yaml
spring:
  data:
    mongodb:
      uri:      mongodb://localhost:27017/orderdb
      database: orderdb
      # Atlas connection
      # uri: mongodb+srv://user:pass@cluster.mongodb.net/orderdb
```

### Document (Entity)
```java
@Document(collection = "orders")  // maps to orders collection ✅
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    private String id;              // MongoDB ObjectId ✅

    @Indexed                        // creates index ✅
    private String customerId;

    @Indexed
    private String status;

    private BigDecimal amount;

    private List<OrderItem> items;  // embedded documents ✅

    private Address address;        // embedded object ✅

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @DBRef                          // reference to another collection
    private Customer customer;
}

@Data
public class OrderItem {
    private String name;
    private BigDecimal price;
    private int quantity;
}
```

### Repository
```java
@Repository
public interface OrderRepository
        extends MongoRepository<Order, String> {

    // derived queries
    List<Order> findByStatus(String status);
    List<Order> findByCustomerId(String customerId);
    List<Order> findByAmountGreaterThan(BigDecimal amount);
    Optional<Order> findByCustomerIdAndStatus(
            String customerId, String status);
    long countByStatus(String status);
    void deleteByStatus(String status);

    // custom JPQL-like query
    @Query("{ 'status': ?0, 'amount': { $gt: ?1 } }")
    List<Order> findByStatusAndMinAmount(
            String status, BigDecimal minAmount);

    // with projection
    @Query(value = "{ 'customerId': ?0 }",
           fields = "{ 'status': 1, 'amount': 1 }")
    List<Order> findStatusAndAmountByCustomer(String customerId);

    // pagination
    Page<Order> findByStatus(String status, Pageable pageable);
}
```

### MongoTemplate — complex queries
```java
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final MongoTemplate mongoTemplate;

    // dynamic query
    public List<Order> findWithFilters(String status,
                                       BigDecimal minAmount,
                                       LocalDate from) {
        Query query = new Query();

        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }
        if (minAmount != null) {
            query.addCriteria(Criteria.where("amount").gte(minAmount));
        }
        if (from != null) {
            query.addCriteria(Criteria.where("createdAt").gte(from));
        }

        query.with(Sort.by("createdAt").descending());
        query.limit(100);

        return mongoTemplate.find(query, Order.class);
    }

    // aggregation
    public List<CategorySummary> summarizeByStatus() {
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("status").is("PAID")),
            Aggregation.group("status")
                .sum("amount").as("totalAmount")
                .count().as("orderCount"),
            Aggregation.sort(Sort.by("totalAmount").descending())
        );

        return mongoTemplate
                .aggregate(agg, "orders", CategorySummary.class)
                .getMappedResults();
    }

    // update specific field
    public void updateStatus(String id, String status) {
        Query query  = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("status", status);
        mongoTemplate.updateFirst(query, update, Order.class);
    }

    // bulk update
    public void bulkUpdateStatus(String oldStatus,
                                  String newStatus) {
        Query query  = new Query(Criteria.where("status").is(oldStatus));
        Update update = new Update().set("status", newStatus);
        mongoTemplate.updateMulti(query, update, Order.class);
    }
}
```

---

## 11. Transactions (4.0+)

```java
// MongoDB supports multi-document ACID transactions (4.0+)
// requires Replica Set

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MongoTemplate mongoTemplate;

    @Transactional  // Spring manages MongoDB transaction ✅
    public void createOrderWithPayment(Order order,
                                        Payment payment) {
        mongoTemplate.save(order);     // save order
        mongoTemplate.save(payment);   // save payment
        // both committed or both rolled back ✅
    }
}

// application.yml — enable transactions
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/orderdb?replicaSet=rs0
```

---

## 12. Replica Set and Sharding

Shard 1 = Replica Set 1 (primary + 2 secondary) ✅
Shard 2 = Replica Set 2 (primary + 2 secondary) ✅
Shard 3 = Replica Set 3 (primary + 2 secondary) ✅

Simple analogy

    Sharding = divide library into 3 floors ✅
    floor 1 = A-H books ✅
    floor 2 = I-P books ✅
    floor 3 = Q-Z books ✅
    
    Each floor = Replica Set ✅
    = original + 2 copies ✅
    for high availability ✅

### Replica Set
```
Primary   → accepts reads and writes
Secondary → copies of primary (read replicas)
Arbiter   → votes in elections, no data

If primary fails:
→ election happens
→ secondary becomes primary ✅
→ automatic failover
```

### Sharding (horizontal scaling)
```
Shard Key → determines which shard stores document

db.orders.shardCollection(
    "orderdb.orders",
    { customerId: 1 }  // shard key ✅
)

Shard 1 → customerId A-M
Shard 2 → customerId N-Z

→ distributes data across servers ✅
→ handles massive datasets ✅
```

---

## 13. Common Interview Questions

| Question | Answer |
|---|---|
| **MongoDB vs SQL** | MongoDB = document/flexible schema. SQL = table/fixed schema |
| **When to use MongoDB** | Dynamic schema, hierarchical data, high write throughput |
| **_id field** | Auto-generated ObjectId — unique per document |
| **Embed vs Reference** | Embed = accessed together (fast). Reference = independent access |
| **$lookup** | JOIN between collections in aggregation |
| **$unwind** | Flatten array into separate documents |
| **Index types** | Single, Compound, Text, Geospatial, TTL, Unique |
| **COLLSCAN vs IXSCAN** | COLLSCAN = full scan ❌. IXSCAN = index scan ✅ |
| **Replica Set** | Primary + Secondaries — fault tolerance + read scaling |
| **Sharding** | Horizontal scaling — distribute data across servers |
| **Transactions** | Supported from 4.0+ on Replica Set |
| **CAP theorem** | MongoDB = CP by default (consistency + partition tolerance) |
| **MongoTemplate vs Repository** | Repository = simple queries. MongoTemplate = complex/dynamic |
| **$match early** | Always put $match first in pipeline — filter before processing |
| **explain()** | Check if query uses index or full collection scan |

---

## 14. Aggregation — Real Examples

### Find first delayed event per order
```js
db.getCollection("OrderOutboundAudit").aggregate([
    // filter DELAYED
    { $match: { "outputMessages.transmitStatus": "DELAYED" }},

    // extract timestamp from _id
    { $addFields: {
        extractedTimestamp: {
            $arrayElemAt: [{ $split: ["$_id", "_"] }, 3]
        }
    }},

    // sort oldest first
    { $sort: { extractedTimestamp: 1 }},

    // group by order — keep first (oldest)
    { $group: {
        _id: "$metaDataReport.statistics.ORDER_NUMBER",
        firstEvent: { $first: "$$ROOT" }
    }},

    // reshape output
    { $replaceRoot: {
        newRoot: {
            $mergeObjects: [
                "$firstEvent",
                { orderNumber: "$_id" }
            ]
        }
    }}
])
```

### Latest record per order
```js
db.getCollection("OrderInboundAudit").aggregate([
    { $match: {
        "metaDataReport.statistics.ORDER_NUMBER": {
            $in: ["61256930"]
        }
    }},
    { $sort: { lastUpdatedTs: -1 }},
    { $group: {
        _id: "$metaDataReport.statistics.ORDER_NUMBER",
        lastRecord: { $first: "$$ROOT" }
    }},
    { $project: {
        _id: 0,
        orderNumber:  "$_id",
        errorMessage: { $ifNull: ["$lastRecord.errorMessage", ""] },
        inputMessage: { $ifNull: ["$lastRecord.inputMessage.message", ""] }
    }},
    { $sort: { orderNumber: 1 }}
])
```

---

## 15. Best Practices

```
✅ always index frequently queried fields
✅ use $match early in pipeline to reduce documents
✅ use projection to fetch only needed fields
✅ embed data accessed together
✅ reference data accessed independently
✅ use explain() to verify index usage
✅ set TTL index for auto-expiring data (sessions, logs)
✅ use Replica Set in production (fault tolerance)
✅ shard on high-cardinality field (customerId not status)
✅ limit document size (16MB max per document)
❌ never use _id as shard key (hotspot)
❌ never skip indexes on large collections
❌ never use $where (JavaScript execution — slow, injection risk)
❌ never store large files in documents (use GridFS)
```
