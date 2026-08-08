# Spring Boot / Microservices — Interview Q&A (Part 2)
> 10 Questions with Correct Answers & Recommendations

---

## Q1. Difference between @PathVariable and @RequestParam?

### Answer
```
@PathVariable:
→ part of URL path ✅
→ mandatory by default ✅
→ used for resource identification
→ GET /orders/1 → id=1

@RequestParam:
→ query string (?key=value) ✅
→ optional with defaultValue ✅
→ used for filtering, pagination, sorting
→ GET /orders?status=PAID&page=0
```

```java
// @PathVariable — resource ID in URL
@GetMapping("/orders/{id}")
public Order getOrder(@PathVariable Long id) {
    return orderService.getOrder(id);
}
// URL: GET /orders/1 ✅

// @RequestParam — optional filters
@GetMapping("/orders")
public List<Order> getOrders(
    @RequestParam(required = false) String status,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    return orderService.getOrders(status, page, size);
}
// URL: GET /orders?status=PAID&page=0&size=10 ✅

// both together
@GetMapping("/customers/{customerId}/orders")
public List<Order> getCustomerOrders(
    @PathVariable Long customerId,          // mandatory ✅
    @RequestParam(required = false)
    String status) {                         // optional ✅
    return orderService.getOrders(customerId, status);
}
// URL: GET /customers/1/orders?status=PAID ✅
```

| | @PathVariable | @RequestParam |
|---|---|---|
| **Location** | URL path `/orders/{id}` | Query string `?status=PAID` |
| **Mandatory** | ✅ Yes (default) | ❌ Optional |
| **Use for** | Resource ID | Filters, pagination |

---

## Q2. Difference between PUT and PATCH? How to implement PATCH?

### Answer
```
PUT:
→ replace ENTIRE resource ✅
→ idempotent — same result every call ✅
→ missing fields → set to null ⚠️
→ must send ALL fields

PATCH:
→ partial update ✅
→ only send changed fields ✅
→ not guaranteed idempotent ⚠️
→ missing fields → unchanged ✅
```

```java
// PUT — full update
@PutMapping("/orders/{id}")
public Order updateOrder(
        @PathVariable Long id,
        @RequestBody Order order) {  // full object ✅
    return orderService.update(id, order);
}

// PATCH — partial update
@PatchMapping("/orders/{id}")
public Order patchOrder(
        @PathVariable Long id,
        @RequestBody Map<String, Object> fields) { // only changed ✅
    return orderService.patch(id, fields);
}

// service — apply only provided fields
@Transactional
public Order patch(Long id, Map<String, Object> fields) {
    Order order = orderRepository.findById(id).orElseThrow();

    fields.forEach((key, value) -> {
        switch (key) {
            case "status" -> order.setStatus((String) value);
            case "amount" -> order.setAmount(
                    new BigDecimal(value.toString()));
            case "note"   -> order.setNote((String) value);
        }
    });
    return orderRepository.save(order);
}
```

```bash
# PUT — must send everything
PUT /orders/1
{ "customerId": "C1", "status": "PAID",
  "amount": 100, "note": "updated" }

# PATCH — only changed fields
PATCH /orders/1
{ "status": "SHIPPED" }  # only status changes ✅
```

| | PUT | PATCH |
|---|---|---|
| **Updates** | Entire object | Specific fields |
| **Missing fields** | Set to null ⚠️ | Unchanged ✅ |
| **Idempotent** | ✅ Yes | ⚠️ Not guaranteed |
| **Body** | Full object | Map of changes |

---

## Q3. What is @ControllerAdvice and @ExceptionHandler?

### Answer
```
@ControllerAdvice:
→ global exception handler ✅
→ applies to ALL controllers ✅
→ can limit to specific package/class ✅

@RestControllerAdvice:
→ @ControllerAdvice + @ResponseBody ✅
→ returns JSON automatically ✅
→ recommended for REST APIs ✅

@ExceptionHandler:
→ method level annotation ✅
→ maps specific exception to handler ✅
→ returns proper HTTP status + error response ✅
```

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // specific exception → 404
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            OrderNotFoundException e,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                    404,
                    e.getMessage(),
                    request.getRequestURI(),  // URL ✅
                    request.getMethod(),      // GET/POST ✅
                    LocalDateTime.now()
                ));
    }

    // validation errors → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(
                        err.getField(),
                        err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    // catch all → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception e, HttpServletRequest request) {
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(
                    500, "Something went wrong",
                    request.getRequestURI(),
                    request.getMethod(),
                    LocalDateTime.now()
                ));
    }
}

// Error response DTO
public record ErrorResponse(
    int status,
    String message,
    String path,
    String method,
    LocalDateTime timestamp
) {}
```

```json
// Response:
{
  "status": 404,
  "message": "Order not found: 1",
  "path": "/api/orders/1",
  "method": "GET",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Q4. What is Spring Security? How does the security filter chain work?

### Answer
```
Spring Security = Chain of Responsibility pattern
→ every request passes through filter chain
→ each filter handles one concern
→ calls chain.doFilter() → pass to next ✅
→ or stops chain if auth fails ❌

Filter chain order:
SecurityContextPersistenceFilter  → load security context
UsernamePasswordAuthenticationFilter → authenticate
JwtAuthFilter (custom)            → validate JWT ✅
ExceptionTranslationFilter        → handle 401/403
FilterSecurityInterceptor         → check roles
Controller                        → actual request ✅

ExceptionTranslationFilter:
→ AuthenticationException → 401 Unauthorized ✅
→ AccessDeniedException   → 403 Forbidden ✅

SecurityContextHolder:
→ stores authenticated user per request (ThreadLocal) ✅
→ cleared after request completes
```

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

// Custom JWT Filter
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token == null) {
            chain.doFilter(request, response); // pass ✅
            return;
        }

        if (!jwtService.isTokenValid(token)) {
            response.setStatus(401); // stop chain ❌
            return;
        }

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                userDetails, null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext()
                .setAuthentication(auth); // ✅

        chain.doFilter(request, response); // continue ✅
    }
}

// get current user anywhere
Authentication auth = SecurityContextHolder
        .getContext().getAuthentication();
String username = auth.getName(); ✅
```

---

## Q5. What is @OneToMany and @ManyToOne mapping in JPA?

### Answer
```
@OneToMany → parent side (Student has many Courses)
@ManyToOne → child side (Course belongs to Student)

mappedBy:
→ defined on PARENT side (OneToMany) ✅
→ points to FIELD NAME in child class ✅
→ no extra join table created ✅

@JoinColumn:
→ defined on CHILD side (ManyToOne) ✅
→ creates foreign key column in child table ✅

CascadeType.ALL:
→ delete parent → delete all children ✅

orphanRemoval=true:
→ remove child from list → delete child ✅
```

```java
// Parent — Student
@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(
        mappedBy      = "student",    // field name in Course ✅
        cascade       = CascadeType.ALL,
        orphanRemoval = true,
        fetch         = FetchType.LAZY
    )
    private List<Course> courses = new ArrayList<>();

    // helper — keep both sides in sync ✅
    public void addCourse(Course course) {
        courses.add(course);
        course.setStudent(this);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
        course.setStudent(null);
    }
}

// Child — Course
@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @ManyToOne(fetch = FetchType.LAZY) // override EAGER default ✅
    @JoinColumn(name = "student_id")   // FK column ✅
    private Student student;
}
```

```sql
-- tables created:
-- student (id, name)
-- course  (id, title, student_id) ← FK ✅
-- NO extra join table ✅
```

```
Default fetch types:
@OneToMany  → LAZY  ✅
@ManyToMany → LAZY  ✅
@ManyToOne  → EAGER ⚠️ (override with LAZY)
@OneToOne   → EAGER ⚠️ (override with LAZY)

mappedBy rule:
mappedBy = "student"
            ↑
            field name in Course class ✅
            NOT column name, NOT class name
```

---

## Q6. Difference between HashMap and ConcurrentHashMap?

### Answer
```
HashMap:
→ NOT thread safe ❌ (NO locking at all)
→ multiple threads → data corruption ❌
→ allows ONE null key ✅
→ allows null values ✅
→ faster — no synchronization overhead ✅
→ use in single threaded environment ✅
→ initial capacity 16, load factor 0.75

ConcurrentHashMap:
→ thread safe ✅
→ locks at BUCKET level ✅
→ multiple threads can write different buckets ✅
→ NO null keys ❌
→ NO null values ❌
→ slightly slower — synchronization ⚠️
→ use in multi-threaded environment ✅
→ initial capacity 16, load factor 0.75

Synchronized HashMap (avoid):
→ Collections.synchronizedMap(new HashMap<>())
→ locks ENTIRE map — slower ❌
→ ConcurrentHashMap preferred ✅
```

```java
// HashMap — single thread ✅
Map<String, Order> map = new HashMap<>();
map.put("ORD001", order1);
map.put(null, order2);    // null key ✅

// ConcurrentHashMap — multi thread ✅
Map<String, Order> safeMap = new ConcurrentHashMap<>();
safeMap.put("ORD001", order1);
safeMap.put(null, order2); // ❌ NullPointerException

// atomic operations — ConcurrentHashMap only ✅
safeMap.putIfAbsent("ORD001", order);
safeMap.computeIfAbsent("ORD001", k -> new Order());
safeMap.merge("ORD001", newOrder, (old, n) -> n);
```

| | HashMap | ConcurrentHashMap |
|---|---|---|
| **Thread safe** | ❌ No | ✅ Yes |
| **Locking** | None | Bucket level ✅ |
| **Null key** | ✅ One allowed | ❌ Not allowed |
| **Null value** | ✅ Yes | ❌ No |
| **Performance** | ✅ Faster | Slightly slower |
| **Use when** | Single thread | Multi thread |

---

## Q7. What is Optional in Java? How used in Spring Boot?

### Answer
```
Optional = Java 8 wrapper for nullable values
→ avoid NullPointerException ✅
→ explicit about nullable return ✅
→ forces caller to handle null ✅

All Optional methods:
→ orElse()       → default value ✅
→ orElseGet()    → default via supplier (lazy) ✅
→ orElseThrow()  → throw exception ✅
→ isPresent()    → check if value exists ✅
→ isEmpty()      → check if empty ✅
→ ifPresent()    → execute if exists ✅
→ map()          → transform value ✅
→ filter()       → filter value ✅
→ get()          → risky — use with care ⚠️

Method          Interface       Input       Output
ifPresent()     Consumer        value       nothing
orElse()        direct value    nothing     default value
orElseGet()     Supplier        nothing     default value
orElseThrow()   Supplier        nothing     exception
map()           Function        value       new value
filter()        Predicate       value       boolean

Optional rules:
✅ use as return type for nullable results
✅ use orElseThrow for mandatory data
✅ use map/filter to chain operations
❌ never use as method parameter
❌ never use as field in entity
❌ never use get() without isPresent() check
```

```java
// Repository
Optional<Order> findById(Long id);

// orElseThrow — most common ✅
public Order getOrder(Long id) {
    return orderRepository.findById(id)
            .orElseThrow(() ->
                new OrderNotFoundException("Not found: " + id));
}

// orElse — default value
Order order = orderRepository.findById(id)
        .orElse(new Order("DEFAULT"));

// orElseGet — lazy default (better performance)
Order order = orderRepository.findById(id)
        .orElseGet(() -> createDefaultOrder());

// ifPresent — execute if exists
orderRepository.findById(id)
        .ifPresent(order -> processOrder(order)); ✅

// map — transform value
String status = orderRepository.findById(id)
        .map(Order::getStatus)
        .orElse("UNKNOWN"); ✅

// chain multiple operations
String name = orderRepository.findById(id)
        .map(Order::getCustomer)
        .map(Customer::getName)
        .orElse("Unknown"); ✅

// ❌ never do this
Optional<Order> opt = orderRepository.findById(id);
if (opt.isPresent()) {
    return opt.get(); // same as null check ❌
}
```

---

## Q8. What is Kubernetes? How does it relate to ECS?

### Answer
```
Both:
→ container orchestration platforms ✅
→ run Docker containers ✅
→ auto scaling ✅
→ health checks ✅
→ load balancing ✅

ECS (AWS Elastic Container Service):
→ AWS native — only works on AWS ✅
→ simpler to set up ✅
→ fully managed by AWS ✅
→ Fargate = serverless (no EC2 needed) ✅
→ cheaper for simple workloads ✅
→ native AWS integration (ALB, CloudWatch, ECR) ✅

Kubernetes (K8s):
→ open source — works anywhere ✅
   AWS (EKS), Azure (AKS), GCP (GKE), on-premise
→ more complex to set up ⚠️
→ more powerful and flexible ✅
→ industry standard ✅
→ larger community ✅
→ costlier — control plane charges ⚠️
→ better for multi-cloud ✅
```

### Same concept — different names

| Concept | ECS | Kubernetes |
|---|---|---|
| **Container config** | Task Definition | Pod Spec |
| **Running unit** | Task | Pod |
| **Scaling group** | Service | Deployment |
| **Networking** | ALB + Target Group | Service + Ingress |
| **Config** | Parameter Store | ConfigMap |
| **Secrets** | Secrets Manager | Secret |
| **Auto scaling** | Application Auto Scaling | HPA |
| **Health check** | /actuator/health | Liveness + Readiness Probe |

```yaml
# Kubernetes deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 2
  template:
    spec:
      containers:
        - name: order-service
          image: myregistry/order-service:latest
          ports:
            - containerPort: 8080
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1024Mi"
              cpu: "500m"
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
          env:
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-secret
                  key: password
```

```
Choose ECS when:
→ already on AWS ✅
→ simpler setup ✅
→ small to medium workloads ✅
→ cost sensitive ✅

Choose Kubernetes when:
→ multi-cloud strategy ✅
→ large complex workloads ✅
→ fine-grained control needed ✅
→ on-premise + cloud hybrid ✅
```

---

## Q9. Difference between @Mock and @MockBean?

### Answer
```
@Mock (Mockito):
→ pure Mockito — no Spring context ✅
→ used with @InjectMocks ✅
→ fast — no Spring context loaded ✅
→ used in unit tests ✅

@MockBean (Spring Boot):
→ Spring managed mock ✅
→ replaces real bean in Spring context ✅
→ used with @WebMvcTest / @SpringBootTest ✅
→ slower — loads Spring context ⚠️
→ used in integration tests ✅
```

```java
// @Mock + @InjectMocks — pure unit test ✅
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository; // Mockito mock ✅

    @Mock
    private KafkaTemplate kafkaTemplate;

    @InjectMocks
    private OrderService orderService; // inject mocks ✅

    @Test
    public void testGetOrder() {
        Order order = new Order(1L, "PAID", BigDecimal.TEN);
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        Order result = orderService.getOrder(1L);

        assertEquals("PAID", result.getStatus());
        verify(orderRepository).findById(1L); // verify called ✅
    }
}

// @MockBean — Spring integration test ✅
@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // replaces real bean in Spring context ✅
    private OrderService orderService;

    @Test
    public void testGetOrder() throws Exception {
        Order order = new Order(1L, "PAID", BigDecimal.TEN);
        when(orderService.getOrder(1L)).thenReturn(order);

        mockMvc.perform(get("/api/orders/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("PAID"));
    }
}
```

| | @Mock | @MockBean |
|---|---|---|
| **Framework** | Mockito | Spring Boot Test |
| **Spring context** | ❌ Not loaded | ✅ Loaded |
| **Speed** | ✅ Fast | ⚠️ Slower |
| **Use with** | @InjectMocks | @WebMvcTest / @SpringBootTest |
| **Use for** | Unit tests | Integration tests |
| **Replaces bean** | ❌ No | ✅ In Spring context |

---

## Q10. Order placed but Payment fails — how to handle data consistency?

### Answer
```
Problem:
→ order saved in DB ✅
→ payment fails ❌
→ order exists but not paid → inconsistent ❌

Solution — Saga Pattern + Outbox + Compensation:

Step 1 → order-service saves order (PENDING)
Step 2 → outbox saves event SAME transaction ✅
Step 3 → Kafka publishes order-placed event
Step 4 → payment-service processes payment
Step 5a → success → emit payment-processed ✅
          order-service → update PAID ✅
Step 5b → failure → emit payment-failed ❌
          order-service → COMPENSATE → CANCELLED ✅

Key guarantees:
✅ Outbox Pattern     → no lost events
✅ Idempotency check  → no duplicate processing
✅ Compensation       → cancel order if payment fails
✅ Manual commit      → no lost messages
✅ @RetryableTopic    → retry transient failures
✅ DLT               → handle poison messages
```

```java
// Order Service — place order
@Transactional // DB + outbox in ONE transaction ✅
public Order placeOrder(OrderRequest request) {
    Order order = Order.builder()
            .customerId(request.customerId())
            .amount(request.amount())
            .status(OrderStatus.PENDING) // PENDING ✅
            .build();
    orderRepository.save(order);

    // outbox — same transaction ✅
    outboxRepository.save(new OutboxEvent(
        UUID.randomUUID().toString(),
        "ORDER_PLACED",
        toJson(order)
    ));
    return order;
}

// compensation — payment failed
@KafkaListener(topics = "payment-failed")
@Transactional
public void handlePaymentFailed(PaymentEvent event) {
    if (processedRepo.existsByEventId(event.getEventId())) return;

    Order order = orderRepository
            .findById(event.getOrderId()).orElseThrow();
    order.setStatus(OrderStatus.CANCELLED); // compensate ✅
    orderRepository.save(order);

    kafkaTemplate.send("order-cancelled", event);
    processedRepo.save(event.getEventId());
}

// success — payment processed
@KafkaListener(topics = "payment-processed")
@Transactional
public void handlePaymentSuccess(PaymentEvent event) {
    if (processedRepo.existsByEventId(event.getEventId())) return;

    Order order = orderRepository
            .findById(event.getOrderId()).orElseThrow();
    order.setStatus(OrderStatus.PAID); // update ✅
    orderRepository.save(order);
    processedRepo.save(event.getEventId());
}

// Payment Service
@KafkaListener(topics = "order-placed")
@Transactional
public void processPayment(OrderEvent event) {
    if (processedRepo.existsByEventId(event.getEventId())) return;

    try {
        chargeCustomer(event.getCustomerId(), event.getAmount());
        kafkaTemplate.send("payment-processed",
            new PaymentEvent(event.getOrderId(), "SUCCESS")); ✅
    } catch (PaymentException e) {
        kafkaTemplate.send("payment-failed",
            new PaymentEvent(event.getOrderId(), "FAILED")); ✅
    }
    processedRepo.save(event.getEventId());
}
```

```
Flow:
Order Service          Kafka            Payment Service
     |                   |                    |
     |-- PENDING ------  |                    |
     |-- outbox -------  |                    |
     |-- publish ------> | order-placed ----> |
     |                   |                    |-- charge card
     |                   | payment-processed <|-- success ✅
     |<-- PAID --------  |                    |
     |                   | payment-failed  <--|-- failure ❌
     |<-- CANCELLED ----  |                   |
```

---

## Quick Reference — Key Points

| Topic | Key Point |
|---|---|
| @PathVariable | URL path — mandatory |
| @RequestParam | Query string — optional with default |
| PUT vs PATCH | PUT = full replace. PATCH = partial fields only |
| @ControllerAdvice | Global exception handler for all controllers |
| @RestControllerAdvice | @ControllerAdvice + @ResponseBody ✅ |
| Spring Security | Chain of filters — each handles one concern |
| SecurityContextHolder | Stores auth per request (ThreadLocal) |
| @OneToMany mappedBy | Points to FIELD NAME in child class |
| @ManyToOne default | EAGER — always override with LAZY ✅ |
| HashMap thread safe | ❌ NOT thread safe — NO locking |
| ConcurrentHashMap | ✅ Thread safe — bucket level locking |
| ConcurrentHashMap null | ❌ No null keys or values allowed |
| Optional orElseThrow | Most common — throw if empty ✅ |
| ECS vs K8s | ECS = AWS only simple. K8s = multi-cloud powerful |
| @Mock | Mockito — unit test — no Spring context |
| @MockBean | Spring — integration test — replaces bean |
| Payment failure | Saga + Outbox + Compensation + Idempotency ✅ |
