# Spring Boot / Java — Interview Q&A (Part 4)
> 17 Questions with Correct Answers & Code Snippets

---

## Q1. Introduce yourself and walk through your most recent project?

### Answer
```
"I am a senior Java backend developer
with 12+ years of experience ✅

Tech stack:
→ Java 21 + Spring Boot microservices ✅
→ Kafka for event streaming (5+ years) ✅
→ AWS (ECS, Aurora, SQS, SNS, Lambda) ✅
→ Azure (AKS, Key Vault, Service Bus) ✅
→ Oracle, MySQL, MongoDB, DynamoDB, Aurora ✅
→ Avro schema for Kafka producers/consumers ✅
→ RestTemplate, RestClient, WebFlux, gRPC ✅

Most recent project — Payment Domain at Gap:
→ maintained 25+ Spring Boot microservices ✅
→ Payment Authorization Service →
   tokenization via Bluefin → Chase Gateway ✅
→ PCI DSS compliant payment system ✅
→ Kafka event driven architecture ✅
→ Jenkins CI/CD with Blue-Green deployment ✅

Key achievement:
→ migrated legacy frontend (2012) to React
→ estimated 45 days → completed in 5 days
  using AI tools (GitHub Copilot/Codex) ✅
→ backend migration in less than 1 day ✅

Monitoring:
→ Splunk + CloudWatch + New Relic alerts ✅
→ on-call → immediate response on alerts ✅"
```

---

## Q2. Difference between @RestController and @Controller?

### Answer
```
@Controller:
→ MVC web application ✅
→ returns view name (String) ✅
→ ViewResolver finds template ✅
→ Thymeleaf, JSP templates ✅
→ add @ResponseBody per method for JSON ✅

@RestController:
→ REST API ✅
→ = @Controller + @ResponseBody ✅
→ JSON/XML response automatically ✅
→ no ViewResolver ✅
→ Jackson converts POJO → JSON ✅
```

```java
// @Controller — returns view ✅
@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("user", "Kiyan");
        return "home"; // → home.html ✅
    }

    // @ResponseBody → JSON on specific method ✅
    @GetMapping("/data")
    @ResponseBody
    public Order getData() {
        return new Order(); // → JSON ✅
    }
}

// @RestController — JSON always ✅
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            orderService.getOrder(id)); // → JSON ✅
    }
}
```

| | @Controller | @RestController |
|---|---|---|
| **Returns** | View name | JSON/XML ✅ |
| **ViewResolver** | ✅ Yes | ❌ No |
| **@ResponseBody** | Per method | All methods ✅ |
| **Use for** | MVC web app | REST API ✅ |

---

## Q3. Different ways to create a bean in Spring Boot?

### Answer
```
All ways to create bean:

1. @Component stereotypes ✅
2. @Configuration + @Bean ✅
3. @Bean on @SpringBootApplication ✅
4. ApplicationContext.getBean() ✅
5. @Import ✅
6. @Conditional beans ✅

@Component / @Service / @Repository:
→ YOUR OWN classes ✅
→ auto detected via @ComponentScan ✅

@Configuration + @Bean:
→ THIRD PARTY classes ✅
→ configure before returning ✅
→ RestTemplate, KafkaTemplate, ObjectMapper ✅

ApplicationContext.getBean():
→ dynamic lookup ✅
→ avoid if possible ⚠️
```

```java
// @Component stereotypes ✅
@Component   public class OrderValidator { }
@Service     public class OrderService { }
@Repository  public class OrderRepository { }
@Controller  public class OrderController { }

// @Configuration + @Bean — third party ✅
@Configuration
public class AppConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://payment-service")
                .build(); // configure before return ✅
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper; // ✅
    }
}

// @Conditional bean ✅
@Bean
@ConditionalOnProperty(
    name = "feature.kafka.enabled",
    havingValue = "true")
public KafkaTemplate kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
}
```

| Way | Use when | Recommended |
|---|---|---|
| `@Component` | Your own class | ✅ Yes |
| `@Configuration + @Bean` | Third party | ✅ Yes |
| `ApplicationContext.getBean()` | Dynamic | ⚠️ Avoid |
| `@Conditional` | Feature flags | ✅ Yes |

---

## Q4. What is @Transactional? Explain propagation types?

### Answer
```
@Transactional = atomicity
→ all operations succeed or NONE ✅
→ exception → rollback everything ✅

7 Propagation types:
REQUIRED      → use existing OR create new (DEFAULT) ✅
REQUIRES_NEW  → ALWAYS new transaction ✅
NESTED        → savepoint within existing ✅
SUPPORTS      → use if exists, run without if not ✅
NOT_SUPPORTED → suspend existing, run without ✅
MANDATORY     → must have existing → else exception ✅
NEVER         → must NOT have → else exception ✅


REQUIRED      → orderservice -> paymentservice
REQUIRES_NEW  → orderservice -> paymentservice -> Audit.save
NESTED        → save point between orderservice & paymentservice
SUPPORTS      → getProduct
NOT_SUPPORTED → ReportService.generate
MANDATORY     → orderservice -> paymentservice
NEVER         → reportService.generate
```

```java
// REQUIRED — default ✅
@Transactional
public void createOrder(OrderRequest req) {
    orderRepository.save(order);
    paymentService.charge(order); // same tx ✅
}

// REQUIRES_NEW — audit log ✅
// outer fails → audit STILL saved ✅
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveAuditLog(AuditLog log) {
    auditRepository.save(log);
}

// NESTED — savepoint ✅
@Transactional(propagation = Propagation.NESTED)
public void notify(Order order) {
    // fails → rollback to savepoint ✅
    // order creation NOT affected ✅
}

// MANDATORY — must have tx ✅
@Transactional(propagation = Propagation.MANDATORY)
public void updateInventory(Order order) {
    // no tx → exception ❌
}

// NEVER — email/report ✅
@Transactional(propagation = Propagation.NEVER)
public void sendEmail(Order order) {
    // tx exists → exception ❌
    emailService.send(order.getEmail());
}
```

| Propagation | Tx exists | No Tx |
|---|---|---|
| `REQUIRED` | Use existing ✅ | Create new ✅ |
| `REQUIRES_NEW` | Suspend + new ✅ | Create new ✅ |
| `NESTED` | Savepoint ✅ | Create new ✅ |
| `SUPPORTS` | Use existing ✅ | Run without ✅ |
| `NOT_SUPPORTED` | Suspend ✅ | Run without ✅ |
| `MANDATORY` | Use existing ✅ | Exception ❌ |
| `NEVER` | Exception ❌ | Run without ✅ |

---

## Q5. Checked vs Unchecked exceptions? @Transactional behavior?

### Answer
```
Checked Exception:
→ extends Exception ✅
→ compiler detects ✅
→ must handle in try/catch ✅
→ FileNotFoundException, IOException ✅
→ @Transactional → NO rollback by default ❌

Unchecked Exception:
→ extends RuntimeException ✅
→ not detected at compile time ✅
→ ArithmeticException, NullPointerException ✅
→ @Transactional → AUTO rollback ✅

Custom exceptions:
→ checked  → extend Exception ✅
→ unchecked → extend RuntimeException ✅
→ super(message) in constructor ✅
```

```java
// Custom checked ✅
public class PaymentProcessingException
        extends Exception {
    public PaymentProcessingException(String msg) {
        super(msg);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // Avoid generating a stack trace
    }
}

// Custom unchecked ✅
public class OrderNotFoundException
        extends RuntimeException {
    public OrderNotFoundException(String msg) {
        super(msg);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // Avoid generating a stack trace
    }
}

// RuntimeException → auto rollback ✅
@Transactional
public void createOrder() {
    orderRepository.save(order);
    throw new OrderNotFoundException("Not found");
    // auto rollback ✅
}

// Checked → NO rollback ❌
@Transactional
public void processPayment()
        throws PaymentProcessingException {
    orderRepository.save(order);
    throw new PaymentProcessingException("Failed");
    // NO rollback ❌ order SAVED ❌
}

// rollbackFor — force rollback on checked ✅
@Transactional(
    rollbackFor = PaymentProcessingException.class)
public void processPayment()
        throws PaymentProcessingException {
    orderRepository.save(order);
    throw new PaymentProcessingException("Failed");
    // NOW rollback ✅
}

// noRollbackFor — skip rollback on runtime ✅
@Transactional(
    noRollbackFor = OrderNotFoundException.class)
public void createOrder() {
    orderRepository.save(order);
    throw new OrderNotFoundException("Not found");
    // NO rollback → order SAVED ✅
}
```

| | Checked | Unchecked |
|---|---|---|
| **Extends** | Exception | RuntimeException ✅ |
| **Compiler** | Detects ✅ | Not detected ❌ |
| **Must handle** | ✅ Yes | Optional |
| **@Transactional** | No rollback ❌ | Auto rollback ✅ |
| **Force rollback** | rollbackFor ✅ | Default ✅ |

---

## Q6. Difference between HashMap, LinkedHashMap and TreeMap?

### Answer
```
HashMap:
→ no guaranteed order ❌
→ one null key ✅
→ null values ✅
→ not thread safe ❌
→ O(1) ✅
→ capacity 16, load 0.75 ✅

LinkedHashMap:
→ maintains INSERTION order ✅
→ one null key ✅
→ null values ✅
→ not thread safe ❌
→ O(1) ✅
→ LRU cache use case ✅

TreeMap:
→ SORTED order (natural/custom) ✅
→ NO null key ❌
→ null values ✅
→ not thread safe ❌
→ O(log n) — Red-Black tree ✅
→ range queries ✅
```

```java
// HashMap — general use ✅
Map<String, Order> map = new HashMap<>();
map.put(null, order);  // null key ✅
// no order guarantee ❌

// LinkedHashMap — insertion order ✅
Map<String, Order> linked = new LinkedHashMap<>();
linked.put("ORD001", order1); // first ✅
linked.put("ORD002", order2); // second ✅
// ORD001 → ORD002 always ✅

// LRU Cache ✅
Map<String, Order> lru =
    new LinkedHashMap<>(16, 0.75f, true) {
        protected boolean removeEldestEntry(
                Map.Entry e) {
            return size() > 100; // max 100 ✅
        }
    };

// TreeMap — sorted ✅
Map<String, Order> tree = new TreeMap<>();
tree.put("ORD003", order3);
tree.put("ORD001", order1);
// always: ORD001 → ORD002 → ORD003 ✅

tree.put(null, order); // NullPointerException ❌

// useful methods ✅
tree.firstKey();       // smallest ✅
tree.lastKey();        // largest ✅
tree.headMap("ORD002"); // < ORD002 ✅
```

| | HashMap | LinkedHashMap | TreeMap |
|---|---|---|---|
| **Order** | None ❌ | Insertion ✅ | Sorted ✅ |
| **Null key** | ✅ One | ✅ One | ❌ No |
| **Performance** | O(1) ✅ | O(1) ✅ | O(log n) |
| **Use for** | General | Order/LRU | Sorted/Range |

---

## Q7. Difference between Kafka and RabbitMQ?

### Answer
```
Kafka:
→ event streaming ✅
→ PULL based ✅ (consumer pulls)
→ one producer → many consumers (fan-out) ✅
→ message retention 14 days ✅
→ replay possible ✅
→ replication across brokers ✅
→ millions TPS ✅
→ offset based — consumer tracks position ✅

RabbitMQ:
→ message queue ✅
→ PUSH based ✅ (broker pushes)
→ one consumer per queue ✅
→ message deleted after consumed ✅
→ no replay ❌
→ complex routing (exchanges) ✅
→ lower throughput ✅
→ priority queues ✅

Choose Kafka when:
→ high throughput ✅
→ multiple consumers same event ✅
→ replay needed ✅
→ event sourcing ✅
→ microservices decoupling ✅

Choose RabbitMQ when:
→ complex routing ✅
→ simple task queue ✅
→ lower volume ✅
→ priority queues ✅
```

| | Kafka | RabbitMQ |
|---|---|---|
| **Model** | Pull ✅ | Push |
| **Consumers** | Many fan-out ✅ | One per queue |
| **Retention** | 14 days ✅ | Deleted after |
| **Replay** | ✅ Yes | ❌ No |
| **Throughput** | Millions ✅ | Lower |

---

## Q8. Java 21 features? Which used in projects?

### Answer
```
Java 21 key features:
1. Virtual Threads (Project Loom) ✅
2. Sequenced Collections ✅
3. Record Patterns ✅
4. Pattern Matching switch ✅
5. Sealed Classes ✅
6. String Templates (Preview) ✅
7. Structured Concurrency (Preview) ✅
8. ZGC Generational (JEP 439) ✅
```

```java
// 1. Virtual Threads ✅
// spring.threads.virtual.enabled: true
ExecutorService executor =
    Executors.newVirtualThreadPerTaskExecutor();
executor.submit(() -> processOrder()); // ✅
// millions of virtual threads ✅
// few OS threads ✅

// 2. Sequenced Collections ✅
SequencedCollection<Order> orders =
        new ArrayList<>();
orders.addFirst(order1);  // ✅
orders.addLast(order2);   // ✅
orders.getFirst();        // ✅
orders.getLast();         // ✅
orders.reversed();        // ✅

// 3. Record Patterns ✅
if (obj instanceof Order(Long id, String status)) {
    System.out.println(id + " " + status); // ✅
}

// 4. Pattern Matching switch ✅
String result = switch (payment) {
    case CreditCard cc -> "credit: " + cc.getLast4();
    case PayPal pp     -> "paypal: " + pp.getEmail();
    case null          -> "no payment";
    default            -> "unknown";
};

// 5. Sealed Classes ✅
public sealed class Payment
    permits CreditCard, PayPal, GiftCard { }

public final class CreditCard
        extends Payment { }    // allowed ✅

// 6. ZGC Generational ✅
// -XX:+UseZGC -XX:+ZGenerational
// low latency GC ✅

ZGC (Java 11-20):
        → low latency GC ✅
        → concurrent compaction ✅
        → pause < 1ms ✅

ZGC Generational (Java 21):
        → same low latency ✅
        → ADDS generational concept ✅
young + old generation ✅
        → better throughput ✅
        → better memory efficiency ✅
        → -XX:+UseZGC -XX:+ZGenerational ✅

// 7. String Templates (Preview) ✅
String name = "Kiyan";
String msg = STR."Hello \{name}"; // "Hello Kiyan" ✅
```

---

## Q9. How to handle distributed transactions across microservices?

### Answer
```
Saga Pattern ✅

Choreography (used in project):
→ services emit events ✅
→ others listen + react ✅
→ no central coordinator ✅
→ team boundaries clear ✅
→ loosely coupled ✅

Orchestration:
→ central orchestrator ✅
→ complex flows ✅
→ single point of failure risk ⚠️
→ tools: Temporal, AWS Step Functions ✅

Key considerations:
✅ Outbox Pattern    → no lost events
✅ Idempotency       → no duplicate processing
✅ Compensation      → reverse on failure
✅ Stuck saga alert  → monitoring + timeout
```

```java
// Choreography — event driven ✅
@KafkaListener(topics = "order-created-events")
@Transactional
public void handleOrderCreated(
        OrderEvent event, Acknowledgment ack) {

    // idempotency check ✅
    if (processedRepo.existsByEventId(
            event.getEventId())) {
        ack.acknowledge();
        return;
    }

    try {
        // validate + save + outbox
        // SAME transaction ✅
        Order order = orderRepo.save(toEntity(event));
        outboxRepo.save(OutboxEvent.builder()
                .payload(toXml(order))
                .published(false)
                .build());

        processedRepo.save(event.getEventId());
        ack.acknowledge();

    } catch (Exception e) {
        // compensation ✅
        kafkaTemplate.send("order-failed-events",
            OrderFailedEvent.builder()
                .orderId(event.getOrderId())
                .reason(e.getMessage())
                .build());
        ack.acknowledge();
    }
}

// Compensation — reverse steps ✅
@KafkaListener(topics = "order-failed-events")
@Transactional
public void handleCompensation(
        OrderFailedEvent event) {
    orderRepo.findByOrderId(event.getOrderId())
            .ifPresent(order -> {
                order.setStatus("CANCELLED");
                orderRepo.save(order); // ✅
            });
}
```

---

## Q10. OutOfMemoryError — how to debug and fix?

### Answer
```
Real experience:
→ missing @Qualifier → two KafkaTemplates ✅
→ new object created per message ✅
→ 5000 messages → 5000 objects ❌
→ OutOfMemoryError ❌
→ fixed with @Qualifier ✅

Debug steps:
Step 1 → CloudWatch memory metrics ✅
Step 2 → /actuator/threaddump ✅
Step 3 → /actuator/heapdump ✅
         → open in Eclipse MAT ✅
         → Leak Suspects Report ✅
         → found KafkaTemplate × 5000 ✅
Step 4 → add @Qualifier → singleton ✅
Step 5 → verify memory drops ✅

Common causes:
→ missing @Qualifier → multiple beans ✅
→ static collection growing ❌
→ cache without eviction ❌
→ unclosed streams ❌
```

```java
// ❌ Problem — missing @Qualifier
@Autowired
private KafkaTemplate kafkaTemplate;
// new instance per injection ❌

// ✅ Fix — @Qualifier
@Autowired
@Qualifier("orderKafkaTemplate")
private KafkaTemplate<String, OrderEvent>
        kafkaTemplate; // singleton ✅

// Cache without eviction — fix ✅
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager manager =
            new CaffeineCacheManager();
    manager.setCaffeine(
        Caffeine.newBuilder()
            .maximumSize(1000)              // max ✅
            .expireAfterWrite(
                Duration.ofMinutes(30)));   // TTL ✅
    return manager;
}

// JVM flags ✅
// -XX:+HeapDumpOnOutOfMemoryError
// -XX:HeapDumpPath=/tmp/heapdump.hprof
// -XX:+UseZGC -XX:+ZGenerational
```

---

## Q11. Difference between @Autowired on constructor, field, setter?

### Answer
```
Field injection ❌ NOT recommended:
→ uses reflection ❌
→ cannot make final ❌
→ hides dependencies ❌
→ breaks without Spring ❌

Constructor injection ✅ RECOMMENDED:
→ final fields — immutable ✅
→ works without Spring ✅
→ too many params = refactor signal ✅
→ unit test — no Spring needed ✅
→ Lombok @RequiredArgsConstructor ✅

Setter injection:
→ optional dependencies ✅
→ @Autowired(required = false) ✅
→ rarely used ✅
```

```java
// ❌ Field injection
@Service
public class OrderService {
    @Autowired
    private OrderRepository repo; // reflection ❌
}

// ✅ Constructor injection
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repo;    // final ✅
    private final KafkaTemplate kafka;     // final ✅
    private final PaymentService payment;  // final ✅
}

// unit test — no Spring needed ✅
class OrderServiceTest {
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
            mock(OrderRepository.class),
            mock(KafkaTemplate.class),
            mock(PaymentService.class)
        ); // ✅
    }
}

// Setter — optional dep ✅
@Autowired(required = false)
public void setNotificationService(
        NotificationService service) {
    this.notificationService = service;
}
```

| | Field | Constructor | Setter |
|---|---|---|---|
| **Immutable** | ❌ No | ✅ final | ❌ No |
| **Without Spring** | ❌ Fails | ✅ Works | ❌ Fails |
| **Dependency** | Mandatory | Mandatory ✅ | Optional ✅ |
| **Recommended** | ❌ No | ✅ Yes | ⚠️ Sometimes |

---

## Q12. Difference between Mono.just(), Mono.fromCallable(), Mono.defer()?

### Answer
```
Mono.just():
→ value created IMMEDIATELY ✅
→ even before subscribe ❌
→ static value — same always ✅
→ use for: known value ✅

Mono.fromCallable():
→ value created LAZILY on subscribe ✅
→ wraps blocking/sync code ✅
→ needs subscribeOn(Schedulers.boundedElastic) ✅
→ use for: blocking DB call ✅

Mono.defer():
→ entire Mono created LAZILY on subscribe ✅
→ fresh Mono per subscriber ✅
→ dynamic result per call ✅
→ use for: conditional logic ✅
```

```java
// Mono.just() — static, immediate ✅
Mono<String> name = Mono.just("Kiyan"); // ✅

// problem — time captured NOW ❌
Mono<String> time = Mono.just(
        LocalDateTime.now().toString());
// always same time ❌

// Mono.fromCallable() — lazy, blocking ✅
Mono<Order> order = Mono.fromCallable(() ->
        orderRepository.findById(1L) // blocking ✅
            .orElseThrow())
        .subscribeOn(
            Schedulers.boundedElastic()); // ✅

// correct time ✅
Mono<String> time = Mono.fromCallable(() ->
        LocalDateTime.now().toString());
// fresh time on each subscribe ✅

// Mono.defer() — fresh Mono per subscriber ✅
Mono<Order> order = Mono.defer(() -> {
    if (isAdmin()) {
        return orderRepo.findAllOrders(); // ✅
    } else {
        return orderRepo.findMyOrders();  // ✅
    }
});

// counter example ✅
// just() — same value ❌
Mono<Integer> c1 = Mono.just(count++);
// always 0 ❌

// defer() — fresh value ✅
Mono<Integer> c2 = Mono.defer(() ->
        Mono.just(count++));
// 0, 1, 2... per subscriber ✅
```

| | `Mono.just()` | `Mono.fromCallable()` | `Mono.defer()` |
|---|---|---|---|
| **Evaluated** | Immediately ❌ | On subscribe ✅ | On subscribe ✅ |
| **Per subscriber** | Same value | Same value | Fresh Mono ✅ |
| **Blocking ok** | ❌ No | ✅ Yes | ✅ Yes |
| **Use for** | Static value | Blocking call | Dynamic Mono |

---

## Q13. Difference between @SpringBootTest, @WebMvcTest, @DataJpaTest?

### Answer
```
@SpringBootTest:
→ full application context ✅
→ all beans loaded ✅
→ slow ⚠️
→ end to end integration ✅
→ real DB or @MockBean ✅

@WebMvcTest:
→ ONLY controller layer ✅
→ no service, no repository ✅
→ MockMvc auto configured ✅
→ service must be @MockBean ✅
→ fast ✅

@DataJpaTest:
→ ONLY JPA layer ✅
→ H2 in-memory default ✅
→ @Transactional → rollback after each ✅
→ no service, no controller ✅
→ fast ✅
```

```java
// @SpringBootTest — full integration ✅
@SpringBootTest
@AutoConfigureMockMvc
class OrderIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private OrderRepository repo; // real ✅
    @MockBean  private PaymentService pay;   // mock ✅

    @Test
    void testCreateOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(APPLICATION_JSON)
                .content("{\"amount\": 100}"))
                .andExpect(status().isCreated()); // ✅
    }
}

// @WebMvcTest — controller only ✅
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private OrderService service; // ✅

    @Test
    void testGetOrder() throws Exception {
        when(service.getOrder(1L))
                .thenReturn(new Order(1L, "PAID"));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("PAID")); // ✅
    }
}

// @DataJpaTest — repository only ✅
@DataJpaTest // H2 default ✅
class OrderRepositoryTest {

    @Autowired private OrderRepository repo;
    @Autowired private TestEntityManager em;

    @Test
    void testFindByStatus() {
        em.persistAndFlush(
            Order.builder().status("PAID").build());

        List<Order> result = repo.findByStatus("PAID");
        assertThat(result).hasSize(1); // ✅
    }
}
```

| | @SpringBootTest | @WebMvcTest | @DataJpaTest |
|---|---|---|---|
| **Context** | Full ✅ | Web layer | JPA layer |
| **Speed** | Slow ⚠️ | Fast ✅ | Fast ✅ |
| **DB** | Real/Mock | None | H2 ✅ |
| **Use for** | Integration | Controller | Repository ✅ |

---

## Q14. What is Circuit Breaker? How implemented in project?

### Answer
```
Circuit Breaker:
→ monitors failures ✅
→ stops calling failed service ✅
→ gives recovery time ✅
→ prevents cascade failures ✅
→ fallback when OPEN ✅

States:
CLOSED    → normal, counting failures ✅
OPEN      → threshold exceeded → blocked ✅
            fallback returned ✅
HALF_OPEN → test calls ✅
            pass % > threshold → CLOSED ✅
            fail % > threshold → OPEN ✅

Key configs:
→ slidingWindowType: COUNT/TIME ✅
→ failureRateThreshold: 50% ✅
→ minimumNumberOfCalls: 5 ✅
→ waitDurationInOpenState: 60s ✅
→ permittedCallsInHalfOpen: 5 ✅
→ recordExceptions ✅
→ ignoreExceptions ✅
```

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        failureRateThreshold: 50
        minimumNumberOfCalls: 5
        waitDurationInOpenState: 60s
        permittedNumberOfCallsInHalfOpenState: 5
        recordExceptions:
          - java.io.IOException
        ignoreExceptions:
          - com.kiyan.exception.ValidationException
```

```java
@Service
public class PaymentService {

    @CircuitBreaker(
        name = "paymentService",
        fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService")
    public CompletableFuture<PaymentResponse>
            processPayment(PaymentRequest request) {
        return webClient.post()
                .uri("/payments")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .toFuture();
    }

    // fallback — OPEN state ✅
    public CompletableFuture<PaymentResponse>
            paymentFallback(
                PaymentRequest request,
                Exception e) {
        log.error("Circuit OPEN: {}", e.getMessage());
        return CompletableFuture.completedFuture(
            PaymentResponse.builder()
                    .status("PENDING")
                    .message("Payment queued")
                    .build()); // ✅
    }
}
```

| Config | Purpose |
|---|---|
| `slidingWindowSize` | Last N calls ✅ |
| `failureRateThreshold` | % to open ✅ |
| `minimumNumberOfCalls` | Min before calc ✅ |
| `waitDurationInOpenState` | How long OPEN ✅ |
| `permittedCallsInHalfOpen` | Test calls ✅ |

---

## Q15. Difference between @Entity, @Table, @Column?

### Answer
```
@Entity:
→ mandatory ✅
→ marks class as DB table ✅
→ must have @Id ✅
→ must have no-arg constructor ✅

@Table:
→ optional ✅
→ without it → table name = class name ✅
→ custom name, unique constraints, indexes ✅

@Column:
→ optional ✅
→ without it → column name = field name ✅
→ customise: name, length, nullable,
             unique, precision, scale ✅

Without @Table → class name as table ✅
Without @Column → field name as column ✅
Without @Entity → JPA ignores class ❌
```

```java
@Entity
@Table(
    name = "orders",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"order_number"})
    },
    indexes = {
        @Index(
            name = "idx_customer_id",
            columnList = "customer_id")
    }
)
public class Order {

    @Id
    @GeneratedValue(strategy =
            GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "order_number",
        nullable = false,       // NOT NULL ✅
        unique = true,          // UNIQUE ✅
        length = 50             // VARCHAR(50) ✅
    )
    private String orderNumber;

    @Column(
        name = "amount",
        precision = 10,         // total digits ✅
        scale = 2               // decimal places ✅
    )
    private BigDecimal amount;

    @Column(
        name = "created_at",
        updatable = false       // cannot update ✅
    )
    private LocalDateTime createdAt;

    // no @Column → field name used ✅
    private boolean active;    // column: active ✅
}
```

| Annotation | Required | Without it |
|---|---|---|
| `@Entity` | ✅ Mandatory | Class ignored ❌ |
| `@Table` | ❌ Optional | Class name ✅ |
| `@Column` | ❌ Optional | Field name ✅ |
| `@Id` | ✅ Mandatory | Error ❌ |

---

## Q16. @PathVariable, @RequestParam, @RequestBody, @RequestHeader?

### Answer
```
@PathVariable:
→ URL path — mandatory ✅
→ identifies resource ✅
→ /orders/1 → id=1 ✅

@RequestParam:
→ query string — optional ✅
→ filters, pagination ✅
→ default values ✅
→ /orders?status=PAID&page=0 ✅

@RequestBody:
→ request body ✅
→ POST/PUT/PATCH ✅
→ JSON → POJO (Jackson) ✅
→ @Valid for validation ✅

@RequestHeader:
→ HTTP headers ✅
→ JWT token ✅
→ API versioning ✅
→ correlation ID ✅
→ Content-Type ✅
```

```java
@RestController
@RequestMapping("/api")
public class OrderController {

    // @PathVariable — URL ✅
    @GetMapping("/orders/{id}")
    public Order getOrder(
            @PathVariable Long id) { // ✅
        return orderService.getOrder(id);
    }

    // multiple path variables ✅
    @GetMapping("/customers/{cId}/orders/{oId}")
    public Order getCustomerOrder(
            @PathVariable Long cId,
            @PathVariable Long oId) { // ✅
        return orderService.getOrder(cId, oId);
    }

    // @RequestParam — query string ✅
    @GetMapping("/orders")
    public Page<Order> getOrders(
            @RequestParam(required = false)
            String status,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "10")
            int size) { // ✅
        return orderService.getOrders(
                status, page, size);
    }

    // @RequestBody — JSON body ✅
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(
            @RequestBody @Valid
            OrderRequest request) { // ✅
        return ResponseEntity.status(201)
                .body(orderService.create(request));
    }

    // @RequestHeader ✅
    @GetMapping("/orders/secure")
    public List<Order> getSecureOrders(
            @RequestHeader("Authorization")
            String token,             // JWT ✅

            @RequestHeader(
                value = "X-API-Version",
                defaultValue = "v1")
            String apiVersion,        // versioning ✅

            @RequestHeader(
                value = "X-Correlation-ID",
                required = false)
            String correlationId) {   // tracing ✅
        return orderService.getOrders();
    }

    // all together ✅
    @PutMapping("/customers/{cId}/orders/{oId}")
    public Order update(
            @PathVariable Long cId,        // URL ✅
            @PathVariable Long oId,        // URL ✅
            @RequestParam(defaultValue = "false")
            boolean notify,                // query ✅
            @RequestBody @Valid
            OrderRequest request,          // body ✅
            @RequestHeader("Authorization")
            String token) {                // header ✅
        return orderService.update(cId, oId, request);
    }
}
```

| Annotation | Location | Mandatory | Use for |
|---|---|---|---|
| `@PathVariable` | URL path | ✅ Yes | Resource ID |
| `@RequestParam` | Query string | ❌ Optional | Filters |
| `@RequestBody` | Request body | ✅ Yes | POST/PUT data |
| `@RequestHeader` | HTTP header | ❌ Optional | JWT, version |

---

## Q17. HashMap vs ConcurrentHashMap — internal thread safety?

### Answer
```
HashMap:
→ NOT thread safe ❌
→ no lock at all ❌
→ multiple threads → data corruption ❌
→ infinite loop possible ❌
→ one null key ✅
→ O(1) — faster ✅

ConcurrentHashMap:
→ thread safe ✅
→ bucket level locking ✅
→ 16 locks created initially ✅
→ lock on specific node only ✅
→ other nodes accessible ✅
→ NO null key ❌
→ NO null value ❌
→ atomic operations ✅

Java 8+ improvement:
→ CAS (Compare And Swap) ✅
→ synchronized on individual node ✅
→ even more concurrent ✅
```

```java
// HashMap — NOT safe ❌
Map<String, Order> map = new HashMap<>();
ExecutorService executor =
        Executors.newFixedThreadPool(10);
for (int i = 0; i < 1000; i++) {
    final int idx = i;
    executor.submit(() ->
        map.put("key" + idx, new Order())); // ❌
}
// result: corrupted data ❌

// ConcurrentHashMap — safe ✅
Map<String, Order> safe =
        new ConcurrentHashMap<>();
for (int i = 0; i < 1000; i++) {
    final int idx = i;
    executor.submit(() ->
        safe.put("key" + idx, new Order())); // ✅
}
// result: correct ✅

// null rules ✅
map.put(null, order);   // HashMap — ok ✅
safe.put(null, order);  // ConcurrentHashMap — NPE ❌

// atomic operations ✅
safe.putIfAbsent("ORD001", order);      // ✅
safe.computeIfAbsent("ORD001",
        k -> new Order());              // ✅
safe.merge("ORD001", newOrder,
        (old, n) -> n);                // ✅
```

```
ConcurrentHashMap buckets:

Bucket 0  [lock] ← Thread 1 ✅
Bucket 1  [free] ← Thread 2 ✅
Bucket 2  [free] ← Thread 3 ✅
Bucket 3  [lock] ← Thread 4 waits ⏳

Different buckets → parallel ✅
Same bucket → wait ⏳
```

| | HashMap | ConcurrentHashMap |
|---|---|---|
| **Thread safe** | ❌ No | ✅ Yes |
| **Locking** | None | Bucket/CAS ✅ |
| **Null key** | ✅ One | ❌ No |
| **Null value** | ✅ Yes | ❌ No |
| **Performance** | ✅ Faster | Slightly slower |
| **Atomic ops** | ❌ No | ✅ Yes |

---

## Quick Reference — All 17 Key Points

| Topic | Key Point |
|---|---|
| @RestController | = @Controller + @ResponseBody ✅ |
| @Controller | MVC — returns view name ✅ |
| @Component | Your own class ✅ |
| @Bean | Third party class — configure before return ✅ |
| REQUIRED | Default propagation — use/create tx ✅ |
| REQUIRES_NEW | Always new — audit logs ✅ |
| NESTED | Savepoint — partial rollback ✅ |
| Checked exception | No rollback — use rollbackFor ✅ |
| Unchecked exception | Auto rollback ✅ |
| HashMap | O(1), no order, null key ok ✅ |
| LinkedHashMap | O(1), insertion order, LRU ✅ |
| TreeMap | O(log n), sorted, no null key ✅ |
| Kafka vs RabbitMQ | Pull vs Push. Replay vs No replay ✅ |
| Virtual Threads | Millions, lightweight, Java 21 ✅ |
| Saga Choreography | Team boundaries clear, events ✅ |
| OOM debug | heapdump + Eclipse MAT + @Qualifier ✅ |
| Constructor injection | final + testable + recommended ✅ |
| Mono.just() | Static value — immediate ✅ |
| Mono.fromCallable() | Blocking call — lazy ✅ |
| Mono.defer() | Fresh Mono per subscriber ✅ |
| @SpringBootTest | Full context — slow ✅ |
| @WebMvcTest | Controller only — fast ✅ |
| @DataJpaTest | JPA + H2 + rollback ✅ |
| Circuit CLOSED | Normal — counting failures ✅ |
| Circuit OPEN | Blocked — fallback ✅ |
| Circuit HALF_OPEN | Test calls — % based ✅ |
| @Entity | Mandatory ✅ |
| @Table | Optional — custom name ✅ |
| @Column | Optional — custom column ✅ |
| ConcurrentHashMap | Bucket lock + CAS ✅ |
