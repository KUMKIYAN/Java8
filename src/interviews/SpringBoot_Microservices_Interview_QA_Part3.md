# Spring Boot / Microservices — Interview Q&A (Part 3)
> 20 Questions with Correct Answers & Recommendations

---

## Q1. Difference between @Scheduled and @Async? Can you use them together?

### Answer
```
@Scheduled:
→ run method at fixed time interval ✅
→ fixedRate  = every N ms regardless of completion ✅
→ fixedDelay = wait N ms AFTER previous completes ✅
→ cron       = specific time expression ✅
→ single thread by default ❌

@Async:
→ run method in separate thread ✅
→ caller not blocked ✅
→ used together with @Scheduled for parallel tasks ✅

Without @Async:
→ all @Scheduled tasks share ONE thread
→ task 1 running → task 2 waits ❌

With @Async:
→ each task runs on separate thread ✅
→ tasks run in parallel ✅
```

```java
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulerConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setThreadNamePrefix("scheduler-");
        executor.initialize();
        return executor;
    }
}

@Component
public class ScheduledTasks {

    // every 5 seconds regardless of completion
    @Scheduled(fixedRate = 5000)
    @Async("taskExecutor") // parallel ✅
    public void processOrders() { }

    // wait 3 seconds AFTER previous finishes
    @Scheduled(fixedDelay = 3000)
    @Async("taskExecutor")
    public void sendReports() { }

    // every day at 8am
    @Scheduled(cron = "0 0 8 * * ?")
    @Async("taskExecutor")
    public void dailyJob() { }
}
```

| | fixedRate | fixedDelay | cron |
|---|---|---|---|
| **Timing** | Every N ms | N ms after completion | Specific time |
| **Overlap** | ⚠️ Possible | ❌ No | ⚠️ Possible |
| **Use for** | Polling | Sequential | Specific schedule |

---

## Q2. Difference between JpaRepository, CrudRepository, PagingAndSortingRepository?

### Answer
```
Hierarchy:
Repository (marker)
    ↓
CrudRepository → basic CRUD ✅
    ↓
PagingAndSortingRepository → pagination + sorting ✅
    ↓
JpaRepository → JPA specific — most feature rich ✅

CrudRepository methods:
→ save(), delete(), deleteAll(), deleteAll(List<Long> ids)
→ findById(), findAll(), count(), existsById() ✅

PagingAndSortingRepository adds:
→ findAll(Pageable) ✅
→ findAll(Sort) ✅

JpaRepository adds:
→ saveAll() ✅
→ deleteAllInBatch() ✅ ->  // DELETE FROM orders -> faster than deleteAll() -> where delete one by one.
→ deleteAllByIdInBatch(List<Long> ids);
→ List<Order> findAllById(List<Long> ids); -> // findAllById() — fetch list by IDs
→ getReferenceById() ✅ -> // getReferenceById() — lazy proxy, no DB hit

→ flush() ✅ - persistance memory -> sends pending SQL to DB -> you must call save() first.
orderRepository.save(order);  // step 1 — in memory ✅
orderRepository.flush();      // step 2 — SQL sent ✅
Long id = order.getId();      // ID available ✅

→ saveAndFlush() ✅ -> SQL Sent to DB immediately. ID will also generaged here
Order saved = orderRepository.saveAndFlush(order);
Long id = saved.getId(); // ID available immediately ✅
```

```java
// JpaRepository — recommended ✅
public interface OrderRepository
        extends JpaRepository<Order, Long> { }

// pagination usage
public Page<Order> getOrders(int page, int size) {
    Pageable pageable = PageRequest.of(
        page, size,
        Sort.by("createdAt").descending()
    );
    return orderRepository.findAll(pageable);
}

// Page response:
// content       → list of items ✅
// totalElements → total records ✅
// totalPages    → total pages ✅
// number        → current page ✅
// first/last    → is first/last page ✅
```

| | CrudRepository | PagingAndSorting | JpaRepository |
|---|---|---|---|
| **Basic CRUD** | ✅ | ✅ | ✅ |
| **Pagination** | ❌ | ✅ | ✅ |
| **Flush** | ❌ | ❌ | ✅ |
| **Batch ops** | ❌ | ❌ | ✅ |
| **Recommended** | ❌ | ❌ | ✅ |

---

## Q3. What is @EnableAutoConfiguration? How does auto-configuration work?

### Answer
```
@SpringBootApplication =
    @SpringBootConfiguration +
    @EnableAutoConfiguration + ← triggers auto-config
    @ComponentScan

Internal flow:
1. App starts
2. @EnableAutoConfiguration triggers
3. SpringFactoriesLoader reads
   META-INF/spring/AutoConfiguration.imports
4. loads ALL candidate classes
5. each evaluated against @Conditional
6. passes → bean registered ✅
7. fails  → skipped ❌

Spring Boot 2.x → spring.factories
Spring Boot 3.x → AutoConfiguration.imports ✅

All @Conditional annotations:
@ConditionalOnClass         → class on classpath ✅
@ConditionalOnMissingClass  → class NOT on classpath
@ConditionalOnBean          → bean already exists
@ConditionalOnMissingBean   → bean NOT exists ✅ most common
@ConditionalOnProperty      → property set in config
@ConditionalOnExpression    → SpEL expression true
@ConditionalOnWebApplication→ is web application
@ConditionalOnResource      → resource file exists
@ConditionalOnJava          → specific Java version
@ConditionalOnSingleCandidate → one bean of type
@ConditionalOnCloudPlatform → running on cloud
```

```java
// Example auto-config class
@AutoConfiguration
@ConditionalOnClass(DataSource.class)       // if on classpath ✅
@ConditionalOnMissingBean(DataSource.class) // if no existing bean ✅
public class DataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource(DataSourceProperties props) {
        return props.initializeDataSourceBuilder().build();
    }
}
```

---

## Q4. Difference between @Value and @ConfigurationProperties?

### Answer
```
@Value:
→ single property injection ✅
→ supports SpEL expressions ✅
→ supports default values ✅
→ scattered across classes ⚠️

@ConfigurationProperties: (prefix = "app.order")
→ group of related properties ✅
→ type safe binding ✅ -> wrong type → compile error not runtime
→ supports validation (@Valid) ✅
→ supports nested objects ✅ -> inner class maps to nested yml structure
→ centralized config ✅
→ recommended for complex config ✅
```

```java
// @Value — single property
@Service
public class OrderService {
    @Value("${jwt.secret}")
    private String jwtSecret;              // single value ✅

    @Value("${jwt.expiry:86400000}")       // default value ✅
    private long jwtExpiry;

    @Value("${app.allowed-statuses}")
    private List<String> statuses;         // list ✅

    @Value("#{${app.timeout} * 2}")        // SpEL ✅
    private long doubleTimeout;
}

// @ConfigurationProperties — group ✅
@ConfigurationProperties(prefix = "app.order")
@Validated
@Data
public class OrderProperties {

    @Min(1) @Max(10)
    private int maxRetry;

    @NotNull
    private int timeout;

    private List<String> allowedStatuses;

    private Db db = new Db(); // nested ✅

    @Data
    public static class Db {
        private String host;
        private int port;
    }
}
```

| | @Value | @ConfigurationProperties |
|---|---|---|
| **Level** | Field | Class |
| **Properties** | Single | Group ✅ |
| **Type safe** | ⚠️ Basic | ✅ Full |
| **Validation** | ❌ Hard | ✅ @Valid |
| **Nested** | ❌ No | ✅ Yes |
| **SpEL** | ✅ Yes | ❌ No |
| **Recommended** | Simple values | Complex config ✅ |

---

## Q5. Difference between @Transactional(readOnly=true) and without?

### Answer
```
readOnly = true:
→ routes to READ REPLICA ✅
→ FlushMode.NEVER → no dirty checking ✅
→ no entity snapshots (memory saving) ✅
→ DB optimizes read-only transactions ✅
    → DB acquires READ LOCK only ✅
    → lighter than write lock ✅
    → multiple readers simultaneously ✅
    → no transaction log needed ✅
    → no rollback segment needed ✅
    → DB skips write overhead ✅
→ better performance ✅

readOnly = false (default):
→ routes to WRITER ✅
→ FlushMode.AUTO → dirty checking enabled ✅
→ tracks entity changes ✅
→ commits on method end ✅

dirty checking:
→ Hibernate snapshots loaded entities
→ on flush → compares with current state
→ if changed → generates UPDATE SQL
→ readOnly=true → skips → faster ✅

Best practice:
→ class level readOnly=true ✅
→ override with @Transactional for writes ✅
```

```java
@Service
@Transactional(readOnly = true) // default all methods read ✅
public class OrderService {

    public List<Order> getAllOrders() { } // readOnly ✅
    public Order getOrder(Long id) { }   // readOnly ✅

    @Transactional // override for write ✅
    public Order createOrder(OrderRequest req) { }

    @Transactional // override for write ✅
    public void deleteOrder(Long id) { }
}
```

| | readOnly=true | readOnly=false |
|---|---|---|
| **Routing** | Read replica ✅ | Writer ✅ |
| **FlushMode** | NEVER ✅ | AUTO |
| **Dirty check** | ❌ Disabled | ✅ Enabled |
| **Performance** | ✅ Faster | Normal |
| **Use for** | SELECT | INSERT/UPDATE/DELETE |

---

## Q6. What is ResponseEntity? When and why use it?

### Answer
```
ResponseEntity = full control over HTTP response
→ set custom HTTP status codes ✅
→ add custom headers ✅
→ return empty body (204) ✅
→ return different types conditionally ✅

Without ResponseEntity:
→ always returns 200 OK ⚠️
→ cannot set custom headers ❌
```

```java
// 200 OK
return ResponseEntity.ok(order);

// 201 Created ✅
return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(order);

// 201 with Location header ✅
return ResponseEntity
        .created(URI.create("/api/orders/" + order.getId()))
        .body(order);

// 204 No Content ✅
return ResponseEntity.noContent().build();

// 404 Not Found
return ResponseEntity.notFound().build();

// custom headers
return ResponseEntity.ok()
        .header("X-Order-Id", order.getId().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .body(order);

// conditional
return orderRepository.findById(id)
        .map(order -> ResponseEntity.ok(order))
        .orElse(ResponseEntity.notFound().build());

return ResponseEntity
        .accepted()           // 202 ✅
        .body("Request accepted — processing");
```

| Status | Method | Use case |
|---|---|---|
| 200 OK | `ResponseEntity.ok(body)` | GET success |
| 201 Created | `ResponseEntity.created(uri)` | POST success |
| 204 No Content | `ResponseEntity.noContent()` | DELETE success |
| 400 Bad Request | `ResponseEntity.badRequest()` | Validation error |
| 404 Not Found | `ResponseEntity.notFound()` | Not found |

---

## Q7. Difference between @OneToOne and @ManyToMany in JPA?

### Answer
```
@OneToOne:
→ FK column in one table ✅
→ @JoinColumn on owning side ✅
→ mappedBy on inverse side ✅
→ default EAGER ⚠️ → override with LAZY

@ManyToMany:
→ creates JOIN TABLE (third table) ✅
→ @JoinTable defines table + columns ✅
→ mappedBy on inverse side ✅
→ cannot add extra columns ❌
→ fix: create explicit join entity ✅
```

```java
// @OneToOne
@Entity
public class User {
    @OneToOne(mappedBy = "user",
              cascade = CascadeType.ALL,
              fetch = FetchType.LAZY)
    private Address address;
}

@Entity
public class Address {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // FK ✅
    private User user;
}

// @ManyToMany — basic
@Entity
public class Student {
    @ManyToMany
    @JoinTable(
        name = "student_course",
        joinColumns        = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses = new ArrayList<>();
}

@Entity
public class Course {
    @ManyToMany(mappedBy = "courses")
    private List<Student> students = new ArrayList<>();
}

// @ManyToMany with extra columns → join entity ✅
@Entity
@Table(name = "student_course")
public class StudentCourse {

    @EmbeddedId
    private StudentCourseId id;

    @ManyToOne @MapsId("studentId")
    private Student student;

    @ManyToOne @MapsId("courseId")
    private Course course;

    private LocalDate enrolledAt; // extra column ✅
    private String grade;         // extra column ✅
}
```

| | @OneToOne | @ManyToMany |
|---|---|---|
| **Join table** | ❌ FK in one table | ✅ Third table |
| **Default fetch** | EAGER ⚠️ | LAZY ✅ |
| **Extra columns** | ✅ In entity | ❌ Need join entity |

---

## Q8. When does save() INSERT vs UPDATE in JPA?

### Answer
```
save() checks entity ID:
→ ID null     → INSERT (persist) ✅
→ ID present  → UPDATE (merge) ✅

Internally:
isNew() = true  → entityManager.persist() → INSERT ✅
isNew() = false → entityManager.merge()   → UPDATE ✅

isNew() checks:
→ @Id field is null → new ✅
→ @Version field null → new ✅
→ implements Persistable → isNew() method ✅

Dirty checking (no save needed):
→ inside @Transactional
→ change entity field → auto UPDATE on commit ✅
```

```java
// INSERT — no ID
Order newOrder = Order.builder()
        .customerId("CUST001").status("PENDING").build();
Order saved = orderRepository.save(newOrder); // INSERT ✅
// saved.getId() → generated ID ✅

// UPDATE — has ID
Order existing = Order.builder()
        .id(1L).status("PAID").build();
orderRepository.save(existing); // UPDATE ✅

// dirty checking — no save() needed ✅
@Transactional
public void updateStatus(Long id, String status) {
    Order order = orderRepository.findById(id).orElseThrow();
    order.setStatus(status); // change field
    // NO save() needed — auto UPDATE on commit ✅
}

// persist vs merge
em.persist(entity); // INSERT — entity must have no ID ✅
em.merge(entity);   // UPDATE — entity has ID ✅
```

| Scenario | Method | SQL |
|---|---|---|
| New entity (id=null) | persist() | INSERT ✅ |
| Existing (has id) | merge() | UPDATE ✅ |
| Inside @Transactional | dirty checking | AUTO UPDATE ✅ |

---

## Q9. What is @Lock? Difference between optimistic and pessimistic locking?

### Answer
```
Pessimistic Locking:
→ DB row locked immediately (SELECT FOR UPDATE) ✅
→ other transactions WAIT ✅
→ use when: high contention, financial data ✅
→ risk: deadlock ⚠️

Optimistic Locking:
→ no DB lock ✅
→ @Version field checked on UPDATE ✅
→ version mismatch → OptimisticLockException ✅
→ use when: low contention, read-heavy ✅
→ better performance ✅
→ no deadlock ✅
```

```java
// Pessimistic — SELECT FOR UPDATE ✅
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT o FROM Order o WHERE o.id = :id")
Optional<Order> findByIdWithLock(@Param("id") Long id);

// SQL: SELECT * FROM orders WHERE id=1 FOR UPDATE ✅

// Optimistic — @Version ✅
@Entity
public class Order {
    @Id private Long id;
    private String status;

    @Version
    private Long version; // auto managed by JPA ✅
}

// Thread 1 reads → version=1
// Thread 2 reads → version=1
// Thread 1 updates → version becomes 2 ✅
// Thread 2 updates → version 1 ≠ 2 → OptimisticLockException ❌

// handle with retry ✅
@Retryable(value = OptimisticLockingFailureException.class,
           maxAttempts = 3,
           backoff = @Backoff(delay = 100))
public Order updateOrder(Long id, String status) {
    Order order = orderRepository.findById(id).orElseThrow();
    order.setStatus(status);
    return orderRepository.save(order);
}
```

| | Pessimistic | Optimistic |
|---|---|---|
| **Mechanism** | DB row lock | @Version field |
| **Other tx** | Wait ⏳ | Proceed — may fail |
| **Deadlock** | ⚠️ Yes | ✅ No |
| **Performance** | ⚠️ Lower | ✅ Better |
| **Use when** | High contention | Low contention |

---

## Q10. @Query with nativeQuery=true — when to use over JPQL?

### Answer
```
JPQL (default):
→ uses entity/field names ✅
→ database independent ✅
→ no DB specific functions ❌

Native Query (nativeQuery=true):
→ uses table/column names ✅
→ database specific ⚠️
→ DB specific functions ✅
→ better for complex/bulk queries ✅

When to use native:
→ DB specific functions (DATE_FORMAT, JSONB) ✅
→ complex joins JPQL cannot handle ✅
→ bulk update/delete performance ✅
→ window functions ✅
```

```java
// JPQL — entity names ✅
@Query("SELECT o FROM Order o WHERE o.status = :status")
List<Order> findByStatus(@Param("status") String status);

// Native — table names + DB functions ✅
@Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') month, " +
               "SUM(amount) total FROM orders " +
               "WHERE YEAR(created_at) = :year " +
               "GROUP BY DATE_FORMAT(created_at, '%Y-%m')",
       nativeQuery = true)
List<Object[]> findMonthlySummary(@Param("year") int year);

// Bulk update — native faster ✅
@Modifying
@Transactional
@Query(value = "UPDATE orders SET status='EXPIRED' " +
               "WHERE created_at < NOW() - INTERVAL 30 DAY",
       nativeQuery = true)
int expireOldOrders();

// Native with pagination ✅
@Query(value = "SELECT * FROM orders WHERE status = :status",
       countQuery = "SELECT COUNT(*) FROM orders WHERE status = :status",
       nativeQuery = true)
Page<Order> findByStatusPaged(@Param("status") String status,
                               Pageable pageable);
```

| | JPQL | Native |
|---|---|---|
| **Uses** | Entity/field names | Table/column names |
| **DB independent** | ✅ Yes | ❌ No |
| **DB functions** | ❌ No | ✅ Yes |
| **Performance** | Good | ✅ Better for bulk |

---

## Q11. What is Spring Boot DevTools?

### Answer
```
DevTools features:
→ automatic restart on code change ✅
→ LiveReload — browser auto refresh ✅
→ disables template caching in dev ✅
→ H2 console enabled ✅
→ property defaults for dev ✅
→ NOT included in production ✅

How restart works:
→ two class loaders
→ base loader → third party jars (not reloaded)
→ restart loader → YOUR code (fast reload) ✅
→ faster than full restart ✅
```

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional> <!-- not in prod ✅ -->
</dependency>
```

```yaml
spring:
  devtools:
    restart:
      enabled: true
    livereload:
      enabled: true
  thymeleaf:
    cache: false  # see template changes instantly ✅
```

| Feature | Without DevTools | With DevTools |
|---|---|---|
| **Code change** | Manual restart | Auto restart ✅ |
| **Browser** | Manual refresh | LiveReload ✅ |
| **Template cache** | Enabled | Disabled ✅ |
| **Production** | N/A | Auto disabled ✅ |

---

## Q12. Difference between @RequestBody and @ResponseBody?

### Answer
```
@RequestBody:
→ deserializes HTTP request body → Java object ✅
→ Jackson converts JSON → POJO ✅
→ used on method PARAMETER ✅
→ used with POST, PUT, PATCH ✅
→ supports @Valid validation ✅

@ResponseBody:
→ serializes Java object → HTTP response ✅
→ Jackson converts POJO → JSON ✅
→ used on method or class ✅
→ @RestController = @Controller + @ResponseBody ✅
```

```java
// @RequestBody — receive JSON
@PostMapping("/orders")
public ResponseEntity<Order> createOrder(
        @RequestBody @Valid OrderRequest request) {
    return ResponseEntity.status(201)
            .body(orderService.create(request));
}

// @ResponseBody — return JSON
@Controller
public class OrderController {

    @GetMapping("/orders/{id}")
    @ResponseBody // ← return JSON ✅
    public Order getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping("/home")
    // no @ResponseBody → view name
    public String home() {
        return "home"; // → home.html ✅
    }
}

// @RestController = @Controller + @ResponseBody ✅
@RestController // both implicit ✅
public class OrderController {
    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.getOrder(id); // → JSON ✅
    }
}
```

| | @RequestBody | @ResponseBody |
|---|---|---|
| **Direction** | Request → Java | Java → Response |
| **Used on** | Method parameter | Method or class |
| **Jackson** | JSON → POJO ✅ | POJO → JSON ✅ |
| **Validation** | @Valid ✅ | N/A |

---

## Q13. What is @EnableWebSecurity? Is it required?

### Answer
```
@EnableWebSecurity:
→ enables Spring Security web support ✅
→ you take full control ✅

Spring Boot 2.x → required ✅
Spring Boot 3.x → optional (auto-configured) ✅
                  but recommended for clarity ✅

@EnableMethodSecurity:
→ enables @PreAuthorize, @PostAuthorize ✅
→ enables @Secured, @RolesAllowed ✅
→ needed alongside SecurityFilterChain ✅
```

```java
@Configuration
@EnableWebSecurity       // optional Spring Boot 3 ✅
@EnableMethodSecurity    // needed for @PreAuthorize ✅
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// method level security ✅
@GetMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public List<Order> getAllOrders() { }
```

---

## Q14. What is @Modifying in Spring Data JPA?

### Answer
```
@Modifying:
→ required for UPDATE/DELETE @Query ✅
→ NOT needed for SELECT ✅
→ returns int (rows affected) ✅
→ must be used with @Transactional ✅

clearAutomatically=true:
→ clears persistence context after query ✅
→ prevents stale data ✅

flushAutomatically=true:
→ flushes pending changes before query ✅
```

```java
// UPDATE — needs @Modifying ✅
@Modifying
@Transactional
@Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
int updateStatus(@Param("id") Long id,
                 @Param("status") String status);

// DELETE — needs @Modifying ✅
@Modifying
@Transactional
@Query("DELETE FROM Order o WHERE o.status = :status")
int deleteByStatus(@Param("status") String status);

// clearAutomatically — prevent stale data ✅
@Modifying(clearAutomatically = true,
           flushAutomatically = true)
@Transactional
@Query("UPDATE Order o SET o.amount = o.amount * 1.1 " +
       "WHERE o.status = 'PENDING'")
int applyPriceIncrease();
```

| | Needed | Not Needed |
|---|---|---|
| **@Modifying** | UPDATE, DELETE | SELECT |
| **@Transactional** | Always with @Modifying | Read queries |
| **clearAutomatically** | After bulk update ✅ | Simple ops |
| **Returns** | int (rows affected) | List/Object |

---

## Q15. What is Pageable and Page? How to implement pagination?

### Answer
```
Pageable:
→ interface — defines pagination request ✅
→ PageRequest.of(page, size, sort) ✅
→ page number (0 based) ✅

Page<T>:
→ content         → list of items ✅
→ totalElements   → total records ✅
→ totalPages      → total pages ✅
→ number          → current page ✅
→ first/last      → is first/last ✅
→ hasNext/hasPrevious ✅

Slice<T> — lighter than Page ✅:
→ no total count query ✅
→ faster for large datasets ✅
→ use for infinite scroll ✅
```

```java
// Repository ✅
Page<Order> findAll(Pageable pageable);
Page<Order> findByStatus(String status, Pageable pageable);
Slice<Order> findByCustomerId(String id, Pageable pageable); // lighter ✅

// Service — multiple sort fields ✅
public Page<OrderResponse> getOrders(
        int page, int size, String sortBy, String direction) {

    Sort sort = direction.equalsIgnoreCase("DESC")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);
    return orderRepository.findAll(pageable)
            .map(OrderResponse::from);
}

// multiple sorts ✅
Sort sort = Sort.by(
    Sort.Order.desc("createdAt"),
    Sort.Order.asc("customerId")
);

// Controller ✅
@GetMapping
public Page<OrderResponse> getOrders(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String direction) {
    return orderService.getOrders(page, size, sortBy, direction);
}
```

| | Page | Slice |
|---|---|---|
| **Total count** | ✅ Yes | ❌ No |
| **totalPages** | ✅ Yes | ❌ No |
| **Performance** | ⚠️ Slower | ✅ Faster |
| **Use when** | Need count | Infinite scroll |

---

## Q16. What is @Specification in Spring Data JPA?

### Answer
```
Specification:
→ dynamic queries with optional filters ✅
→ null filters automatically ignored ✅
→ uses Criteria API under the hood ✅
→ reduces boilerplate vs raw Criteria API ✅
→ one static method per filter ✅
→ combine with Specification.where().and() ✅
→ reusable filter conditions ✅

Without Specification:
→ one method per filter combination ❌
→ combinations explode ❌

With Specification:
→ one findAll(Specification) ✅
→ combine any filters dynamically ✅
```

```java
// Repository ✅
public interface OrderRepository
        extends JpaRepository<Order, Long>,
                JpaSpecificationExecutor<Order> { }

// Specification class ✅
public class OrderSpecification {

    public static Specification<Order> hasStatus(String status) {
        return (root, query, cb) ->
                status == null ? null
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> hasMinAmount(BigDecimal min) {
        return (root, query, cb) ->
                min == null ? null
                : cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Order> betweenDates(
            LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from == null) return cb.lessThanOrEqualTo(
                    root.get("date"), to);
            if (to == null)   return cb.greaterThanOrEqualTo(
                    root.get("date"), from);
            return cb.between(root.get("date"), from, to);
        };
    }

    public static Specification<Order> titleContains(String keyword) {
        return (root, query, cb) ->
                keyword == null ? null
                : cb.like(cb.lower(root.get("title")),
                          "%" + keyword.toLowerCase() + "%");
    }
}

// Service — combine ✅
public Page<OrderResponse> search(
        OrderSearchRequest req, Pageable pageable) {

    Specification<Order> spec = Specification
        .where(OrderSpecification.hasStatus(req.status()))
        .and(OrderSpecification.hasMinAmount(req.minAmount()))
        .and(OrderSpecification.betweenDates(req.from(), req.to()))
        .and(OrderSpecification.titleContains(req.keyword()));
    // null specs ignored automatically ✅

    return orderRepository.findAll(spec, pageable)
            .map(OrderResponse::from);
}
```

---

## Q17. Difference between @SpringBootApplication and @Configuration?

### Answer
```
@SpringBootApplication =
    @SpringBootConfiguration +
    @EnableAutoConfiguration +
    @ComponentScan

→ entry point of application ✅
→ should be in ROOT package ✅
→ triggers auto-configuration ✅
→ is itself a @Configuration class ✅
→ only ONE per app ✅

@Configuration:
→ marks class as bean factory ✅
→ @Bean methods = bean definitions ✅
→ uses CGLIB proxy → singleton guaranteed ✅
→ multiple @Configuration classes allowed ✅
→ no auto-configuration triggered ✅

CGLIB proxy:
→ ensures @Bean methods return same instance ✅
→ @Component does NOT have this ⚠️
```

```java
@SpringBootApplication // ✅ entry point
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean // can define beans here ✅
    public CommandLineRunner runner() {
        return args -> System.out.println("Started!");
    }
}

// separate @Configuration classes ✅
@Configuration
public class KafkaConfig {
    @Bean
    public KafkaTemplate<String, OrderEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, OrderEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>(props());
    }

    @Bean
    public KafkaTemplate<String, String> stringTemplate() {
        return new KafkaTemplate<>(producerFactory()); // same instance ✅
        // CGLIB intercepts → returns existing bean ✅
    }
}
```

| | @SpringBootApplication | @Configuration |
|---|---|---|
| **Purpose** | App entry point | Bean factory |
| **Auto-config** | ✅ Triggers | ❌ No |
| **Multiple** | ❌ One only | ✅ Many |
| **@Bean methods** | ✅ Yes | ✅ Yes |
| **CGLIB proxy** | ✅ Yes | ✅ Yes |

---

## Q18. What is @ConditionalOnProperty? Give a real example.

### Answer
```
@ConditionalOnProperty:
→ bean created ONLY if property exists ✅
→ bean created ONLY if property has specific value ✅
→ havingValue → expected value ✅
→ matchIfMissing → behavior when property missing ✅

Real use cases:
→ enable/disable features via config ✅
→ switch between implementations ✅
→ enable Kafka only if enabled=true ✅
→ choose payment provider ✅
```

```yaml
app:
  features:
    kafka.enabled: true
    notifications.enabled: false
    payment.provider: stripe
```

```java
// enable Kafka only if true ✅
@Bean
@ConditionalOnProperty(
    name           = "app.features.kafka.enabled",
    havingValue    = "true",
    matchIfMissing = false
)
public KafkaTemplate<String, OrderEvent> kafkaTemplate() { }

// disable notifications ✅
@Service
@ConditionalOnProperty(
    name        = "app.features.notifications.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class NotificationService { }

// switch payment provider ✅
@Service
@ConditionalOnProperty(
    name = "app.features.payment.provider",
    havingValue = "stripe"
)
public class StripePaymentProvider
        implements PaymentProvider { }

@Service
@ConditionalOnProperty(
    name = "app.features.payment.provider",
    havingValue = "paypal"
)
public class PaypalPaymentProvider
        implements PaymentProvider { }
```

| Attribute | Purpose |
|---|---|
| `name` | Property name to check ✅ |
| `havingValue` | Expected value ✅ |
| `matchIfMissing=true` | Enable if property missing ✅ |
| `matchIfMissing=false` | Disable if property missing ✅ |

---

## Q19. What is @Retryable? How to configure it?

### Answer
```
@Retryable:
→ retry on specific exceptions ✅
→ configure max attempts ✅
→ fixed delay between retries ✅
→ exponential backoff ✅
→ random delay (jitter) → avoid thundering herd ✅
→ @Recover → fallback when all retries exhausted ✅
→ requires @EnableRetry ✅
```

```java
@SpringBootApplication
@EnableRetry // ← required ✅
public class Application { }

// fixed delay ✅
@Retryable(
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000) // wait 2s ✅
)
public Payment processPayment(BigDecimal amount) {
    return externalApi.charge(amount);
}

@Recover // fallback ✅
public Payment recoverPayment(Exception e, BigDecimal amount) {
    log.error("Failed after retries: {}", e.getMessage());
    return Payment.failed(amount);
}

// specific exceptions ✅
@Retryable(
    value   = { TransientException.class },  // retry ✅
    exclude = { ValidationException.class }, // never retry ✅
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000)
)
public Order fetchOrder(Long id) { }

// exponential backoff ✅
@Retryable(
    maxAttempts = 4,
    backoff = @Backoff(
        delay      = 1000, // 1s
        multiplier = 2.0   // 1s, 2s, 4s, 8s ✅
    )
)
public void callService() { }

// random delay — jitter ✅
@Retryable(
    maxAttempts = 3,
    backoff = @Backoff(
        delay    = 1000,
        maxDelay = 3000,
        random   = true // 1-3s random ✅
    )
)
public String callApi() { }
```

| Strategy | Config | Pattern | Use when |
|---|---|---|---|
| **Fixed** | `delay=2000` | 2s, 2s, 2s | Simple retry ✅ |
| **Exponential** | `multiplier=2` | 1s, 2s, 4s | Rate limit ✅ |
| **Random/Jitter** | `random=true` | 1-3s random | Multiple clients ✅ |

---

## Q20. Difference between @Primary and @Qualifier?

### Answer
```
@Primary:
→ mark ONE bean as default ✅
→ injected when no @Qualifier specified ✅
→ only ONE @Primary per type ✅
→ good for default implementation ✅

@Qualifier:
→ specify exact bean by name ✅
→ overrides @Primary ✅
→ used on injection point ✅
→ more explicit and precise ✅

When to use:
→ @Primary → one clear default ✅
→ @Qualifier → need specific bean ✅
```

```java
// @Primary — default ✅
@Service
@Primary
public class EmailNotificationService
        implements NotificationService { }

@Service
public class SMSNotificationService
        implements NotificationService { }

// @Primary injected by default ✅
@Service
@RequiredArgsConstructor
public class OrderService {
    private final NotificationService notificationService;
    // → EmailNotificationService ✅
}

// @Qualifier — specific bean ✅
@Service
public class AlertService {

    public AlertService(
        @Qualifier("emailNotificationService")
        NotificationService emailService,

        @Qualifier("smsNotificationService")
        NotificationService smsService
    ) { }
}

// custom @Qualifier ✅
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface EmailQualifier { }

@Service
@EmailQualifier
public class EmailNotificationService
        implements NotificationService { }

// inject ✅
public AlertService(@EmailQualifier NotificationService email) { }
```

| | @Primary | @Qualifier |
|---|---|---|
| **Purpose** | Default bean | Specific bean ✅ |
| **Location** | On bean | On injection point ✅ |
| **Overrides** | ❌ Overridden by @Qualifier | ✅ Overrides @Primary |
| **Multiple** | ❌ One per type | ✅ Many |
| **Use when** | Clear default | Specific impl ✅ |

---

## Quick Reference — All 20 Key Points

| Topic | Key Point |
|---|---|
| @Scheduled + @Async | @Scheduled = single thread. @Async = parallel ✅ |
| fixedRate vs fixedDelay | fixedRate = every N ms. fixedDelay = N ms after done |
| JpaRepository | Most feature rich — recommended ✅ |
| Auto-configuration | Reads imports file → @Conditional filters → bean ✅ |
| @Value | Single property + SpEL + default ✅ |
| @ConfigurationProperties | Group + type safe + validation + nested ✅ |
| readOnly=true | Read replica + no dirty check + faster ✅ |
| ResponseEntity | Full control — status + headers + body ✅ |
| @ManyToMany extra cols | Create explicit join entity ✅ |
| save() INSERT vs UPDATE | null ID = INSERT. has ID = UPDATE ✅ |
| Pessimistic lock | SELECT FOR UPDATE — others wait ✅ |
| Optimistic lock | @Version — exception on conflict ✅ |
| nativeQuery=true | DB specific functions + complex queries ✅ |
| DevTools | Auto restart + LiveReload + no cache ✅ |
| @RequestBody | JSON → Java object + @Valid ✅ |
| @ResponseBody | Java → JSON response ✅ |
| @EnableWebSecurity | Optional Spring Boot 3 — recommended ✅ |
| @Modifying | Required for UPDATE/DELETE @Query ✅ |
| clearAutomatically | Prevents stale data after bulk update ✅ |
| Page vs Slice | Page = count query. Slice = faster no count ✅ |
| @Specification | Dynamic filters + null ignored + reusable ✅ |
| @SpringBootApplication | Entry point = @Config + @EnableAuto + @Scan ✅ |
| @ConditionalOnProperty | Bean created only if property matches ✅ |
| @Retryable | Retry + backoff + jitter + @Recover fallback ✅ |
| @Primary | Default bean when multiple same type ✅ |
| @Qualifier | Specific bean — overrides @Primary ✅ |
