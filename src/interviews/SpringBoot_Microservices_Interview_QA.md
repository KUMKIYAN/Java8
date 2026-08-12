# Spring Boot / Microservices — Interview Q&A
> 20 Questions with Correct Answers & Recommendations

---

## Q1. What is the difference between @Component, @Service, @Repository, and @Controller?

### Answer
```
All four are technically interchangeable — all are @Component internally.
Used for readability and layer identification:

@Component   → generic Spring bean
@Service     → business logic layer
@Repository  → database/persistence layer
@Controller  → web/endpoint layer

@Repository  → extra benefit:  same exception regardless of DB ✅ 
               translates SQL exceptions
               to Spring DataAccessException hierarchy ✅
             

@RestController = @Controller + @ResponseBody combined
```

---

## Q2. Field injection vs Constructor injection — which is recommended?

### Answer
```
Field injection (@Autowired on field):
→ uses reflection to inject ❌
→ dependency not guaranteed ❌
→ breaks without Spring context ❌
→ testing needs @InjectMocks ❌
→ hides dependencies — too many not obvious ❌

Constructor injection (recommended ✅):
→ dependency mandatory — cannot create without it ✅
→ works without Spring context ✅
→ supports immutability (final fields) ✅
→ too many params = visible sign to refactor ✅
→ Lombok @RequiredArgsConstructor removes boilerplate ✅
```

```java
// ✅ Constructor injection
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository; // final ✅
    private final KafkaTemplate kafkaTemplate;
}
```

---

## Q3. What is @Transactional? What happens when called from same class?

### Answer
```
@Transactional:
→ atomicity — all operations succeed or all fail ✅
→ if any exception → entire transaction rolled back ✅
→ if success -> auto commit

Propagation types:
→ REQUIRED      → use existing or create new (DEFAULT) ✅
→ REQUIRES_NEW  → always create new transaction
→ NESTED        → nested within existing
→ MANDATORY     → must have existing transaction
→ SUPPORTS      → use existing if available
→ NOT_SUPPORTED → suspend existing transaction
→ NEVER         → must not have transaction

Self invocation problem:
→ Spring creates PROXY around class
→ external calls → proxy intercepts → @Transactional applied ✅
→ internal call (this.method()) → bypasses proxy ❌
→ @Transactional IGNORED on self invocation ❌

Fix:
→ move method to separate class ✅
→ Inject self via @Autowired ✅
→ inject self via ApplicationContext ✅
```

```java
// ❌ self invocation — @Transactional ignored
@Service
public class OrderService {
    public void createOrder() {
        processPayment(); // proxy bypassed ❌
    }
    @Transactional
    public void processPayment() { } // ignored ❌
}

// ✅ fix — separate class
@Service
public class PaymentService {
    @Transactional
    public void processPayment() { } // works ✅
}
```

---

## Q4. Difference between @SpringBootTest and @WebMvcTest?

### Answer
```
@SpringBootTest:
→ loads FULL application context ✅
→ all beans created (service, repo, controller)
→ slow — heavy ⚠️
→ end to end integration testing
→ needs real DB or @MockBean

@WebMvcTest:
→ loads ONLY web layer (controller) ✅
→ no service, no repository beans
→ fast — lightweight ✅
→ uses MockMvc to simulate HTTP requests
→ service must be @MockBean ✅

Other slice tests:
@DataJpaTest    → repository layer only
@JsonTest       → JSON serialization only - to assert json paths
                  @Autowired
                  private JacksonTester<Order> json;
                  assertThat(json.write(order)).hasJsonPathValue("$.status", "PAID").hasJsonPathValue("$.amount", 100.0);
                  assertThat(json.parse(content)).hasFieldOrPropertyWithValue("status", "PAID");
                  
@RestClientTest → REST clients only 
                  @Autowired
                  private MockRestServiceServer mockServer;
                  // tests REST clients - mock external API response
                  mockServer.expect(requestTo("/api/payments/1")) 
                            .andRespond(withSuccess("{\"id\":1,\"status\":\"PAID\"}", MediaType.APPLICATION_JSON));  
```

```java
@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService; // mock ✅

    @Test
    public void testGetOrder() throws Exception {
        mockMvc.perform(get("/api/orders/1"))
               .andExpect(status().isOk());
    }
}
```

---

## Q5. What is Circuit Breaker pattern? How implemented in Spring Boot?

### Answer
```
Circuit Breaker monitors failures and stops calling
a failing service — gives it time to recover.

States:
CLOSED    → normal operation, counting failures
            failures within threshold → stays CLOSED ✅
OPEN      → threshold exceeded → stop all calls ❌
            return fallback immediately ✅
            wait configured duration
HALF_OPEN → allow limited calls to test recovery
            success % passes → CLOSED ✅
            failure % fails  → OPEN again ❌

Configurations:
→ slidingWindowType: COUNT_BASED / TIME_BASED
→ failureRateThreshold: 50% default
→ waitDurationInOpenState: 60s
→ permittedCallsInHalfOpenState: 10
→ fallbackMethod when OPEN ✅
```

```java
@CircuitBreaker(name = "orderService",
                fallbackMethod = "fallback")
public Order getOrder(Long id) {
    return restClient.get()
            .uri("/orders/{id}", id)
            .retrieve()
            .body(Order.class);
}

public Order fallback(Long id, Exception e) {
    log.error("Circuit open: {}", e.getMessage());
    return new Order("DEFAULT"); // fallback ✅
}
```

```yaml
resilience4j:
  circuitbreaker:
    instances:
      orderService:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
        permittedNumberOfCallsInHalfOpenState: 5
```

---

## Q6. Synchronous vs Asynchronous communication — when to choose Kafka over REST?

### Answer
```
Synchronous (REST):
→ caller waits for response ✅
→ RestTemplate / RestClient / WebClient
→ tight coupling between services
→ cascade failure risk ❌

Choose REST when:
→ need immediate response
→ payment — success or fail?
→ stock check — available?
→ user login — token valid?

Asynchronous (Kafka):
→ fire and forget ✅
→ messages wait if downstream fails ✅
→ loose coupling ✅
→ no cascade failure ✅

Choose Kafka when:
→ fire and forget (notifications, emails)
→ high throughput needed
→ services need decoupling ✅
→ event replay needed ✅
→ downstream can be temporarily down ✅

Other async tools:
→ SQS        → simple queue, AWS managed
→ RabbitMQ   → complex routing
→ EventBridge→ event routing, AWS native
```

---

## Q7. What is API Gateway? Responsibilities?

### Answer
```
API Gateway = single entry point for all clients

Responsibilities:
→ routing → forward to correct service ✅
→ authentication → validate JWT ✅
→ rate limiting → 100 req/sec per client ✅ Rule
→ Throttling -> stop client overwelming with external calls -> with 429 Too Many Requests ✅ enforcing the rule.
→ request/response transformation ✅
→ load balancing → healthy instances ✅
→ SSL termination → HTTPS at gateway ✅
→ API versioning → /v1/* → old, /v2/* → new ✅
→ circuit breaker → stop routing to failing service ✅
→ caching → cache responses at gateway ✅
→ logging → centralized request logging ✅
→ CORS handling ✅
→ WAF integration → block malicious requests ✅

Popular gateways:
→ AWS API Gateway ✅
→ Kong, Nginx, Spring Cloud Gateway, Apigee
```

---

## Q8. What is Service Discovery? How do services find each other?

### Answer
```
Service Discovery = services find each other dynamically
                    without hardcoded IPs

Two types:

Client Side (Eureka):
→ service registers with Eureka on startup
→ caller queries Eureka → gets instance list
→ Ribbon/LoadBalancer chooses instance ✅

Server Side (AWS ALB):
→ client calls load balancer
→ load balancer queries registry
→ routes to healthy instance ✅

Eureka flow:
→ order-service starts → registers { name, ip, port }
→ payment-service → asks Eureka → "where is order-service?"
→ Eureka returns ip + port ✅
→ payment-service calls directly ✅

Heartbeat:
→ every 30 seconds → heartbeat to Eureka ✅
→ no heartbeat → removed from registry
→ no dead instances called ✅

Cloud alternatives:
→ AWS → ECS service discovery / Route 53
→ Kubernetes → built-in DNS ✅
```

```java
@SpringBootApplication
@EnableEurekaClient
public class OrderServiceApplication { }
```

---

## Q9. Difference between @Bean and @Component?

### Answer
```
@Component:
→ class level annotation ✅
→ auto-detected via @ComponentScan
→ YOU own the class → can annotate it ✅
→ @Service, @Repository, @Controller = @Component ✅

@Bean:
→ method level annotation ✅
→ inside @Configuration class ✅
→ third party classes you don't own ✅
→ full control over object creation ✅
→ customize the object with properties BEFORE handing it to Spring ✅
→ multiple beans of same type possible ✅

Examples of @Bean:
→ RestTemplate, RestClient, KafkaTemplate
→ ObjectMapper, DataSource
→ ThreadPoolTaskExecutor ✅
```

```java
// @Component — your own class
@Component
public class OrderValidator { }

// @Bean — third party
@Configuration
public class AppConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://api.example.com")
                .build(); // configured ✅
    }
}
```

---

## Q10. What is @Profile in Spring Boot?

### Answer
```
@Profile = load specific beans/config per environment

File naming:
application.yml      → common (all envs)
application-dev.yml  → dev specific ✅
application-stage.yml→ stage specific ✅
application-prod.yml → prod specific ✅

Activate profile:
→ application.yml: spring.profiles.active: dev
→ command line: --spring.profiles.active=prod ✅
→ env variable: SPRING_PROFILES_ACTIVE=prod ✅
```

```java
@Configuration
public class DataSourceConfig {

    @Bean
    @Profile("dev")           // dev only ✅
    public DataSource h2DataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2).build();
    }

    @Bean
    @Profile("prod")          // prod only ✅
    public DataSource auroraDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://aurora:5432/orderdb")
                .build();
    }

    @Bean
    @Profile("!prod")         // everything except prod ✅
    public MockPaymentService mockPayment() {
        return new MockPaymentService();
    }
}
```

---

## Q11. What is Saga pattern? Two types?

### Answer
```
Saga = split one big distributed transaction
       into multiple smaller service transactions

Each service:
→ listens to event ✅
→ saves to its own DB ✅
→ emits next event ✅

Forward steps  = normal happy path
Compensation   = undo previous step on failure ✅

Two types:

Choreography:
→ services emit events → others listen ✅
→ no central coordinator
→ pros: loosely coupled, simple ✅
→ cons: hard to track overall flow ❌
→ use when: team boundaries clear, simple flows

Orchestration:
→ central orchestrator controls flow ✅
→ tells each service what to do
→ pros: easy to track, one file ✅
→ cons: single point of failure ⚠️
→ tools: Temporal, AWS Step Functions
→ use when: complex flows, centralized control

Important considerations:
→ idempotency check ✅
→ outbox pattern ✅
→ stuck saga — Kafka at-least-once delivery ✅
→ Compensation Semantics ✅
```

```java
// Choreography
@KafkaListener(topics = "order-created")
@Transactional
public void handleOrderCreated(OrderEvent event) {
    if (processedRepo.existsByEventId(event.getEventId())) return;
    try {
        paymentService.charge(event.getAmount());
        kafkaTemplate.send("payment-processed", event); // ✅
    } catch (Exception e) {
        kafkaTemplate.send("payment-failed", event); // compensate ✅
    }
    processedRepo.save(event.getEventId());
}
```

---

## Q12. Difference between @RestController and @Controller?

### Answer
```
@Controller:
→ used for MVC web applications ✅
→ returns view name (String)
→ ViewResolver maps to template (.html / .jsp)
→ Thymeleaf, JSP, FreeMarker templates
→ add @ResponseBody per method for JSON ✅

@RestController = @Controller + @ResponseBody:
→ used for REST APIs ✅
→ @ResponseBody on ALL methods automatically
→ Jackson converts object to JSON ✅
→ no view resolver
→ returns ResponseEntity or POJO
```

```java
// @Controller — returns view
@Controller
public class HomeController {
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("user", "Kiyan");
        return "home"; // → home.html ✅
    }
}

// @RestController — returns JSON
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id)); // JSON ✅
    }
}
```

---

## Q13. What is Spring Boot Actuator? Which endpoints used in production?

### Answer
```
Actuator = monitor and manage Spring Boot application
           provides built-in endpoints ✅

Key endpoints:
→ /health      → app health + DB + Kafka status ✅
→ /metrics     → JVM, CPU, memory, HTTP stats ✅
→ /info        → app version, build info ✅
→ /env         → environment properties ✅
→ /beans       → all Spring beans ✅
→ /mappings    → all REST endpoints ✅
→ /loggers     → change log level at runtime ✅
→ /threaddump  → active threads ✅
→ /heapdump    → JVM heap dump ✅
→ /refresh     → reload @RefreshScope beans ✅
→ /shutdown    → graceful shutdown ✅
→ /circuitbreakers → Resilience4j stats ✅

Used in production:
→ /health   → ECS health check ✅
→ /metrics  → Prometheus + Grafana ✅
→ /loggers  → change log level without restart ✅

# change to DEBUG — see detailed logs ✅
POST http://localhost:8080/actuator/loggers/com.kiyan.order
Content-Type: application/json
{
  "configuredLevel": "DEBUG"
}

# check current level of specific package
GET http://localhost:8080/actuator/loggers/com.kiyan.order

# response:
{
  "configuredLevel": "INFO",   # what you set ✅
  "effectiveLevel": "INFO"     # what is active ✅
}

# check all loggers
GET http://localhost:8080/actuator/loggers
```

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, loggers, circuitbreakers
  endpoint:
    health:
      show-details: always
```

---

## Q14. What is @RefreshScope? When and why use it?

### Answer
```
@RefreshScope = refresh bean with new property value
                without restarting application ✅

Flow:
→ change property in Git/Config Server
→ call /actuator/refresh
→ @RefreshScope bean destroyed + recreated ✅
→ new value picked up ✅
→ no restart needed ✅

Spring Cloud Bus (multi-pod):
→ /actuator/busrefresh on ONE pod
→ Kafka broadcasts to ALL pods ✅
→ all pods refreshed simultaneously ✅

Without Bus:
→ must call /refresh on each pod separately ❌
```

```java
@RefreshScope
@Component
public class FeatureFlag {

    @Value("${feature.enable-discount:false}")
    private boolean enableDiscount; // refreshed at runtime ✅

    public boolean isDiscountEnabled() {
        return enableDiscount;
    }
}
```

---

## Q15. Difference between EAGER and LAZY loading in JPA?

### Answer
```
Default fetch types:
@OneToMany  → LAZY  (default) ✅
@ManyToMany → LAZY  (default) ✅
@ManyToOne  → EAGER (default) ⚠️
@OneToOne   → EAGER (default) ⚠️

LAZY:
→ child loaded on demand ✅
→ better performance ✅
→ risk: LazyInitializationException ❌
→ accessing outside transaction fails ❌

EAGER:
→ always loads with parent ✅
→ no LazyInitializationException ✅
→ performance issue — loads everything ❌
→ N+1 risk ❌

LAZY recommended:
→ load only what you need ✅
→ fix LazyInit with JOIN FETCH / @EntityGraph ✅
```

```java
@Entity
public class Order {

    @OneToMany(mappedBy = "order",
               fetch = FetchType.LAZY)  // default ✅
    private List<OrderItem> items;

    @ManyToOne(fetch = FetchType.LAZY)  // override EAGER ✅
    private Customer customer;
}

// fix LazyInitializationException
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
Optional<Order> findByIdWithItems(@Param("id") Long id);

@EntityGraph(attributePaths = {"items", "customer"})
Optional<Order> findById(Long id); // ✅
```

---

## Q16. What is N+1 problem in JPA? How to fix it?

### Answer
```
N+1 = 1 query for parent + N queries for each child

Example:
→ 1 query: SELECT * FROM departments (returns 10)
→ 10 queries: SELECT * FROM employees WHERE dept_id = ?
→ total = 11 queries ❌
→ 1000 departments = 1001 queries ❌

Detect:
→ enable SQL logging → count queries ✅
→ p6spy library ✅
→ Hibernate Statistics ✅

Fix options: In Department object Employee object also load well in advance with one Query.
→ JOIN FETCH ✅
→ @EntityGraph ✅
→ @BatchSize ✅
→ @Fetch(FetchMode.SUBSELECT) ✅
```

```java
// ❌ N+1 problem
List<Department> depts = deptRepository.findAll(); // 1 query
for (Department dept : depts) {
    dept.getEmployees(); // N queries ❌
}

// ✅ Fix 1 — JOIN FETCH
@Query("SELECT d FROM Department d JOIN FETCH d.employees")
List<Department> findAllWithEmployees(); // 1 query ✅

// ✅ Fix 2 — @EntityGraph
@EntityGraph(attributePaths = {"employees"})
List<Department> findAll(); // 1 query ✅

// ✅ Fix 3 — @BatchSize
@OneToMany(mappedBy = "department")
@BatchSize(size = 10)
private List<Employee> employees; // N/10 queries ✅
```

---

## Q17. Difference between save() and saveAndFlush()?

### Answer
```
save():
→ stores in persistence context (memory) ✅
→ SQL NOT sent to DB immediately
→ SQL sent at end of transaction (flush)
→ committed when @Transactional ends ✅

saveAndFlush():
→ stores in persistence context ✅
→ SQL sent to DB IMMEDIATELY ✅
→ still within transaction
→ can still rollback if exception ✅
→ committed when @Transactional ends

Key difference:
flush  = send SQL to DB (not permanent) ✅
commit = permanently save (no rollback) ✅

Use saveAndFlush() when:
→ need ID generated immediately ✅
→ verify DB constraint immediately ✅
→ next query depends on saved data ✅
```

```java
@Transactional
public void example() {

    // save() — SQL not sent yet
    orderRepository.save(order); // in memory only

    // saveAndFlush() — SQL sent immediately
    Order saved = orderRepository.saveAndFlush(newOrder);
    Long id = saved.getId(); // ID available immediately ✅

    // use ID in same transaction
    auditRepository.save(new Audit(id, "CREATED")); // ✅
}
```

| | save() | saveAndFlush() |
|---|---|---|
| SQL sent | End of transaction | Immediately ✅ |
| Commits | ❌ No | ❌ No |
| Rollback | ✅ Yes | ✅ Yes |

---

## Q18. What is @Cacheable? How used in projects?

### Answer
```
@Cacheable = cache method result
             next call returns from cache ✅
             no DB hit ✅

All cache annotations:
@Cacheable  → cache result on first call ✅
@CacheEvict → remove from cache ✅
@CachePut   → always update cache ✅
@Caching    → combine multiple annotations ✅

Cache providers:
→ Simple (ConcurrentHashMap) — dev only
→ Redis (ElastiCache) — production ✅
→ Caffeine — in-memory, fast ✅

Always evict on update/delete:
→ stale data returned if not evicted ❌
```

```java
@EnableCaching // ← required ✅
@SpringBootApplication
public class Application { }

@Service
public class OrderService {

    @Cacheable(value = "orders",
               key = "#id",
               unless = "#result == null") // don't cache null ✅
    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }

    @CachePut(value = "orders", key = "#order.id")
    public Order updateOrder(Order order) {
        return orderRepository.save(order); // updates DB + cache ✅
    }

    @CacheEvict(value = "orders", key = "#id")
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id); // removes from cache ✅
    }

    @CacheEvict(value = "orders", allEntries = true)
    public void clearAllCache() { } // clear everything ✅
}
```

---

## Q19. What is @Async? What configuration is needed?

### Answer
```
@Async = run method in separate thread
         caller not blocked ✅
         non-blocking - caller does other work  ✅

Requirements:
1. @EnableAsync on main class ✅
2. @Async on method ✅
3. Method must be PUBLIC ✅
4. Cannot call from SAME class (proxy issue) ❌
5. ThreadPoolTaskExecutor (recommended) ✅

Return types:
→ void                  → fire and forget ✅
→ CompletableFuture<T>  → get result later ✅
```

```java
// Step 1
@SpringBootApplication
@EnableAsync // ← required ✅
public class Application { }

// Step 2 — thread pool
@Configuration
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

// Step 3 — use @Async
@Service
public class NotificationService {

    @Async("taskExecutor")
    public void sendEmail(Order order) {
        emailService.send(order.getEmail()); // background ✅
    }

    @Async("taskExecutor")
    public CompletableFuture<String> sendSMS(String phone) {
        smsService.send(phone);
        return CompletableFuture.completedFuture("sent"); ✅
    }
}

// Step 4 — call from DIFFERENT class ✅
@Service
@RequiredArgsConstructor
public class OrderService {
    private final NotificationService notificationService;

    public Order createOrder(OrderRequest request) {
        Order order = orderRepository.save(toEntity(request));
        notificationService.sendEmail(order); // async ✅
        return order; // returns immediately ✅
    }
}
```

---

## Q20. Spring Boot microservice performing slowly — how to identify and fix?

### Answer
```
Step 1 — Identify bottleneck:
→ distributed tracing (X-Ray/Jaeger)
   → which service/method is slow ✅
→ /actuator/metrics → p99 latency ✅
→ CloudWatch/Grafana dashboards
→ /actuator/threaddump → threads blocked? ✅
→ heap dump → memory leak? ✅

Step 2 — DB Level:
→ EXPLAIN ANALYZE → check query time ✅
→ full scan vs index scan
→ introduce index if full scan ✅
→ return only required columns ✅
→ pagination applied? ✅
→ N+1 problem → JOIN FETCH / @EntityGraph ✅
→ read replicas for read queries ✅
→ @Transactional(readOnly=true) ✅
→ FlushMode.NEVER for read-only ✅
→ connection pool exhausted? → increase HikariCP ✅
→ slow query log ✅

Step 3 — Application Level:
→ right data structures used? ✅
→ caching applied? @Cacheable ✅
→ CompletableFuture parallel calls ✅
→ parallel streams ✅
→ @Async for non-blocking operations ✅
→ circuit breaker → slow downstream? ✅
→ timeout set on all external calls? ✅
→ GC pressure → ZGC Generational Java 21 ✅
→ virtual threads Java 21 ✅

Step 4 — Infrastructure Level:
→ ECS task CPU/memory too low? ✅
→ scale horizontally → more pods ✅
→ CDN for static content ✅
```

```java
// readOnly → read replica ✅
@Transactional(readOnly = true)
public List<Order> getOrders() {
    return orderRepository.findAll();
}

// parallel → reduce latency ✅
CompletableFuture<List<Order>> orders =
    CompletableFuture.supplyAsync(() -> getOrders());
CompletableFuture<Customer> customer =
    CompletableFuture.supplyAsync(() -> getCustomer());
CompletableFuture.allOf(orders, customer).join();

// caching → avoid repeated DB calls ✅
@Cacheable(value = "orders", key = "#id")
public Order getOrder(Long id) {
    return orderRepository.findById(id).orElseThrow();
}
```

---

## Quick Reference — Key Points

| Topic | Key Point |
|---|---|
| @Component vs @Bean | @Component = your class. @Bean = third party |
| Constructor injection | Always preferred over field injection |
| @Transactional self call | Proxy bypassed — move to separate class |
| @SpringBootTest vs @WebMvcTest | Full context vs web layer only |
| Circuit Breaker states | CLOSED → OPEN → HALF_OPEN → CLOSED |
| Sync vs Async | REST = immediate. Kafka = decouple + resilient |
| LAZY vs EAGER | LAZY recommended. Fix with JOIN FETCH |
| N+1 problem | JOIN FETCH or @EntityGraph ✅ |
| save() vs saveAndFlush() | saveAndFlush = SQL now, both commit at end |
| @Async requirement | @EnableAsync + public method + different class |
| @RefreshScope | Refresh bean without restart via /actuator/refresh |
| @Profile | Different beans/config per environment |
| Saga types | Choreography = distributed. Orchestration = centralized |
| Service Discovery | Eureka — register + heartbeat + query ✅ |
| API Gateway | Single entry + auth + rate limit + routing |
