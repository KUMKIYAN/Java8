# PostgreSQL + Spring Boot — Complete Guide
> Quick Reference for Interview & Development

---

## 1. Setup & Dependencies

### pom.xml
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### application.properties
```properties
# DataSource
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=secret
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Show bind parameters (actual ? values)
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql=TRACE
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

---

## 2. Entity

```java
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE) // PostgreSQL prefers SEQUENCE
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String status;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(columnDefinition = "jsonb")      // PostgreSQL JSONB column
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;    // stores flexible JSON data

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

---

## 3. Repository

```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Derived query — Spring generates SQL automatically
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(String status);
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);

    // JPQL query
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatus(@Param("status") String status);

    // JPQL JOIN FETCH — avoid N+1
    @Query("SELECT o FROM Order o JOIN FETCH o.customer WHERE o.status = :status")
    List<Order> findByStatusWithCustomer(@Param("status") String status);

    // Native query — PostgreSQL specific
    @Query(value = "SELECT * FROM orders WHERE customer_id = :customerId",
           nativeQuery = true)
    List<Order> findByCustomerNative(@Param("customerId") Long customerId);

    // Native query — JSONB operator
    @Query(value = "SELECT * FROM orders WHERE metadata->>'category' = :category",
           nativeQuery = true)
    List<Order> findByCategory(@Param("category") String category);

    // Modifying — UPDATE
    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    // Modifying — DELETE
    @Modifying
    @Transactional
    @Query("DELETE FROM Order o WHERE o.status = :status")
    void deleteByStatus(@Param("status") String status);

    // Pessimistic lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") Long id);

    // Pagination
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
}
```

---

## 4. Service

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    // READ — routes to reader endpoint (Aurora)
    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
    }

    // WRITE — routes to writer endpoint
    @Transactional
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrder(Long id, Order order) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Not found: " + id));
        existing.setStatus(order.getStatus());
        existing.setAmount(order.getAmount());
        return orderRepository.save(existing);
    }

    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    // Pessimistic lock — prevent race condition
    @Transactional
    public Order updateWithLock(Long id, String status) {
        Order order = orderRepository.findByIdWithLock(id)
                .orElseThrow(() -> new OrderNotFoundException("Not found: " + id));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    // Pagination
    @Transactional(readOnly = true)
    public Page<Order> getOrdersPaged(Long customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByCustomerId(customerId, pageable);
    }
}
```

---

## 5. Controller

```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // GET /api/orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getOrders();
    }

    // GET /api/orders/1
    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    // GET /api/orders/paged?page=0&size=10
    @GetMapping("/paged")
    public Page<Order> getOrdersPaged(
            @RequestParam Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.getOrdersPaged(customerId, page, size);
    }

    // POST /api/orders
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(@RequestBody @Valid Order order) {
        return orderService.createOrder(order);
    }

    // PUT /api/orders/1
    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable Long id,
                             @RequestBody @Valid Order order) {
        return orderService.updateOrder(id, order);
    }

    // DELETE /api/orders/1
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}
```

---

## 6. Relationships

### @OneToMany / @ManyToOne
```java
@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();
}

@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
```

### @ManyToMany
```java
@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses = new ArrayList<>();
}

@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToMany(mappedBy = "courses")
    private List<Student> students = new ArrayList<>();
}
```

---

## 7. JSONB Support

### Entity with JSONB column
```java
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> attributes; // flexible JSON data
}
```

### Query JSONB
```java
// extract text value
@Query(value = "SELECT * FROM products WHERE attributes->>'color' = :color",
       nativeQuery = true)
List<Product> findByColor(@Param("color") String color);

// contains check
@Query(value = "SELECT * FROM products WHERE attributes @> :json\\:\\:jsonb",
       nativeQuery = true)
List<Product> findByAttributes(@Param("json") String json);

// nested path
@Query(value = "SELECT * FROM products WHERE attributes#>>'{specs,size}' = :size",
       nativeQuery = true)
List<Product> findBySize(@Param("size") String size);
```

### JSONB operators quick reference
```
->    extract as JSON    data->'status'        → "PAID"
->>   extract as TEXT    data->>'status'       → PAID
#>    path as JSON       data#>'{a,b}'         → "NYC"
#>>   path as TEXT       data#>>'{a,b}'        → NYC
@>    contains           data @> '{"s":"PAID"}'
<@    contained by       '{"s":"PAID"}' <@ data
?     key exists         data ? 'status'
?|    any key exists     data ?| array['a','b']
?&    all keys exist     data ?& array['a','b']
||    merge JSON         data || '{"k":"v"}'
-     delete key         data - 'status'
#-    delete path        data #- '{a,b}'
```

---

## 8. Caching (L2 Cache with PostgreSQL)

```properties
# application.properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
spring.jpa.properties.hibernate.cache.use_query_cache=true
```

```java
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Order {
    // cached entity
}

@Service
public class OrderService {

    @Cacheable(value = "orders", key = "#id")
    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }

    @CachePut(value = "orders", key = "#order.id")
    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    @CacheEvict(value = "orders", key = "#id")
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
```

---

## 9. Connection Pool (HikariCP)

```properties
# HikariCP — Spring Boot default
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.connection-timeout=3000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=2000
spring.datasource.hikari.connection-test-query=SELECT 1
```

---

## 10. Aurora PostgreSQL — Read/Write Split

```properties
# Writer
spring.datasource.writer.url=jdbc:postgresql://writer-endpoint:5432/mydb
spring.datasource.writer.username=admin
spring.datasource.writer.password=secret

# Reader
spring.datasource.reader.url=jdbc:postgresql://reader-endpoint:5432/mydb
spring.datasource.reader.username=admin
spring.datasource.reader.password=secret
```

```java
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.writer")
    public DataSource writerDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.reader")
    public DataSource readerDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public DataSource routingDataSource() {
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put("writer", writerDataSource());
        dataSources.put("reader", readerDataSource());

        AbstractRoutingDataSource routing = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                        ? "reader"   // @Transactional(readOnly=true) → reader
                        : "writer";  // @Transactional → writer
            }
        };

        routing.setTargetDataSources(dataSources);
        routing.setDefaultTargetDataSource(writerDataSource());
        return routing;
    }
}
```

---

## 11. Schema Migration (Flyway)

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

```sql
-- src/main/resources/db/migration/V1__create_orders.sql
CREATE TABLE orders (
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    status      VARCHAR(50) NOT NULL,
    amount      DECIMAL(10,2),
    metadata    JSONB,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_metadata ON orders USING GIN(metadata); -- JSONB index
```

---

## 12. PostgreSQL vs MySQL — Key Differences

| Feature | MySQL | PostgreSQL |
|---|---|---|
| **Dependency** | `mysql-connector-j` | `postgresql` |
| **JDBC URL** | `jdbc:mysql://` | `jdbc:postgresql://` |
| **ID Strategy** | `IDENTITY` | `SEQUENCE` preferred |
| **JSON** | `JSON_EXTRACT()` | `->>`, `@>` (JSONB) |
| **Case sensitive** | No (default) | Yes (default) |
| **String concat** | `CONCAT()` | `\|\|` operator |
| **Arrays** | Limited | Native array type |
| **Full text search** | Basic | Advanced |
| **JPQL queries** | Same ✅ | Same ✅ |
| **Spring Data** | Same ✅ | Same ✅ |

---

## 13. Common Interview Questions

| Question | Answer |
|---|---|
| **JPA vs Hibernate** | JPA = interface/contract, Hibernate = implementation |
| **@Transactional(readOnly=true)** | Routes to reader, optimizes reads, no dirty checking |
| **N+1 problem fix** | `JOIN FETCH` or `@EntityGraph` |
| **Pessimistic vs Optimistic lock** | Pessimistic = `SELECT FOR UPDATE`, Optimistic = `@Version` |
| **ddl-auto in prod** | Always `validate` or `none` — use Flyway |
| **JSONB vs JSON** | JSONB = binary, faster, indexable — always prefer JSONB |
| **@Modifying required** | For UPDATE/DELETE `@Query` — tells Spring it's a write op |
| **Composite PK** | `@EmbeddedId` or `@IdClass` |
| **@JoinTable** | Creates join table for ManyToMany — no extra @Entity needed |
| **HikariCP default pool** | `maximum-pool-size=10`, `minimum-idle=10` |
