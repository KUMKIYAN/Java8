# Spring WebFlux — Interview Q&A
> 5 Questions with Correct Answers & Code Snippets

```

"Spring WebFlux is a reactive non-blocking
web framework introduced in Spring 5 ✅

Problem with Spring MVC:
→ one thread per request ❌
→ thread WAITS for DB/API response ❌
→ 1000 requests = 1000 threads ❌
→ thread pool exhaustion under high load ❌

WebFlux solves this:
→ small number of threads ✅
→ thread never waits ✅
→ handles many requests with few threads ✅
→ better throughput under high concurrency ✅

Built on Project Reactor:
→ Mono = 0 or 1 element (like Optional) ✅
→ Flux = 0 to N elements (like Stream) ✅
→ lazy — nothing runs until subscribed ✅

Key operators:
→ map     = sync transform T → R ✅
→ flatMap = async transform T → Mono/Flux ✅
→ filter  = predicate filtering ✅

WebClient:
→ replaces RestTemplate ✅
→ non-blocking HTTP client ✅
→ returns Mono/Flux ✅
→ built-in retry + timeout ✅

Backpressure:
→ consumer controls flow ✅
→ prevents overload ✅
→ limitRate, onBackpressureBuffer ✅

When to use WebFlux:
→ high concurrency ✅
→ streaming data ✅
→ microservices calling many services ✅

When NOT to use:
→ simple CRUD ✅
→ team not familiar ✅
→ blocking libraries like JDBC ✅"

```

## Q1. What is Spring WebFlux? Why use it over Spring MVC?

### Answer
```
Spring MVC (blocking):
→ one thread per request ✅
→ thread WAITS for DB/HTTP response ❌
→ 1000 requests = 1000 threads ❌
→ thread pool exhaustion under high load ❌

Spring WebFlux (non-blocking):
→ small number of threads ✅
→ thread never waits — handles other requests ✅
→ 1000 requests = few threads ✅
→ better throughput under high concurrency ✅
→ uses Project Reactor (Mono + Flux) ✅
→ response streamed over time ✅

When to use WebFlux:
→ high concurrency needed ✅
→ streaming data ✅
→ real-time updates ✅
→ microservices calling many services ✅

When to stick with MVC:
→ simple CRUD ✅
→ team not familiar with reactive ✅
→ blocking libraries used (JDBC) ✅
```

```java
// Spring MVC — blocking ❌
@RestController
public class OrderController {

    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable Long id) {
        // thread BLOCKED waiting for DB ❌
        return orderService.getOrder(id);
    }
}

// Spring WebFlux — non-blocking ✅
@RestController
public class OrderController {

    @GetMapping("/orders/{id}")
    public Mono<Order> getOrder(@PathVariable Long id) {
        // returns immediately — no blocking ✅
        return orderService.getOrder(id);
    }

    // stream multiple items ✅
    @GetMapping("/orders")
    public Flux<Order> getAllOrders() {
        return orderService.getAllOrders();
        // items emitted one by one ✅
    }

    // streaming — Server Sent Events ✅
    @GetMapping(value = "/orders/stream",
                produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Order> streamOrders() {
        return orderService.getAllOrders()
                .delayElements(Duration.ofSeconds(1));
    }
}
```

| | Spring MVC | Spring WebFlux |
|---|---|---|
| **Model** | Blocking | Non-blocking ✅ |
| **Thread** | One per request | Few shared ✅ |
| **Return type** | Object / List | Mono / Flux ✅ |
| **Concurrency** | Limited by threads | High ✅ |
| **Learning curve** | Easy | Steep ⚠️ |
| **Use for** | Simple CRUD | High concurrency ✅ |

---

## Q2. Difference between Mono and Flux in Project Reactor?

### Answer
```
Mono:
→ 0 or 1 element ✅
→ like Optional in Java ✅
→ use for: findById, create, update, delete ✅

Flux:
→ 0 to N elements ✅
→ like List/Stream in Java ✅
→ use for: findAll, streaming data ✅
→ emits items one by one ✅
→ supports backpressure ✅

Both:
→ lazy — nothing happens until subscribed ✅
→ async + non-blocking ✅
→ support operators (map, filter, flatMap) ✅
```

```java
// ── Mono — 0 or 1 element ─────────────────────────────────────
Mono<Order> mono1 = Mono.just(new Order());      // from value ✅
Mono<Order> mono2 = Mono.empty();                // no value ✅
Mono<Order> mono3 = Mono.error(new Exception()); // error ✅

// operators on Mono
orderRepository.findById(1L)
    .map(order -> order.getStatus())             // transform ✅
    .defaultIfEmpty("UNKNOWN")                   // if empty ✅
    .switchIfEmpty(Mono.error(
        new NotFoundException("Not found")));    // throw if empty ✅

// ── Flux — 0 to N elements ────────────────────────────────────
Flux<Order> flux1 = Flux.just(order1, order2, order3); // ✅
Flux<Order> flux2 = Flux.fromList(orderList);           // from list ✅
Flux<Integer> flux3 = Flux.range(1, 10);                // 1 to 10 ✅

// operators on Flux
orderRepository.findAll()
    .filter(o -> "PAID".equals(o.getStatus()))  // filter ✅
    .map(o -> OrderResponse.from(o))            // transform ✅
    .take(10)                                   // limit ✅
    .collectList();                             // Flux → Mono<List> ✅

// ── In Controller ─────────────────────────────────────────────
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    // Mono — single order ✅
    @GetMapping("/{id}")
    public Mono<Order> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(
                    new NotFoundException("Not found")));
    }

    // Mono — create ✅
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Order> createOrder(
            @RequestBody Mono<OrderRequest> request) {
        return request.flatMap(req ->
                orderRepository.save(toEntity(req)));
    }

    // Flux — all orders ✅
    @GetMapping
    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
```

| | Mono | Flux | Java equivalent |
|---|---|---|---|
| **Elements** | 0 or 1 | 0 to N | Optional vs Stream |
| **Use for** | findById, save | findAll, stream | Single vs Collection |
| **Empty** | `Mono.empty()` | `Flux.empty()` | Optional.empty() |
| **Error** | `Mono.error()` | `Flux.error()` | throw exception |
| **Convert** | `mono.flux()` | `flux.next()` | — |

---

## Q3. Key operators — map, flatMap, filter in Project Reactor?

### Answer
```
map:
→ sync transform T → R ✅
→ like Stream.map() ✅
→ use when transformation does NOT return Mono/Flux ✅

flatMap:
→ async transform T → Mono/Flux<R> ✅
→ like Stream.flatMap() ✅
→ use when transformation RETURNS Mono/Flux ✅
→ like thenCompose in CompletableFuture ✅
→ FLATTENS nested Mono<Mono<T>> → Mono<T> ✅

filter:
→ predicate based filtering ✅
→ like Stream.filter() ✅
→ condition true → keep, false → skip ✅

Key difference map vs flatMap:
→ map    returns Mono<Mono<T>> ❌ if used wrong
→ flatMap returns Mono<T>      ✅ flattened
```

```java
// ── map — sync transform ──────────────────────────────────────
orderRepository.findById(1L)
    .map(order -> order.getStatus())           // Order → String ✅
    .map(status -> status.toLowerCase())       // String → String ✅
    .map(order -> OrderResponse.from(order));  // Order → DTO ✅

// ── flatMap — async transform ─────────────────────────────────
orderRepository.findById(1L)
    .flatMap(order ->
        paymentRepository.findByOrderId(order.getId())
    ); // Mono<Payment> ✅

// ── WRONG — map when flatMap needed ──────────────────────────
Mono<Mono<Payment>> wrong = orderRepository.findById(1L)
    .map(order ->
        paymentRepository.findByOrderId(order.getId())
    ); // Mono<Mono<Payment>> ❌ nested!

// ── CORRECT — flatMap flattens ────────────────────────────────
Mono<Payment> correct = orderRepository.findById(1L)
    .flatMap(order ->
        paymentRepository.findByOrderId(order.getId())
    ); // Mono<Payment> ✅

// ── filter ────────────────────────────────────────────────────
orderRepository.findAll()
    .filter(order ->
        "PAID".equals(order.getStatus()))     // only PAID ✅
    .filter(order ->
        order.getAmount().compareTo(
            BigDecimal.valueOf(100)) > 0);    // amount > 100 ✅

// ── All operators together ────────────────────────────────────
orderRepository.findAll()
    .filter(o -> "PAID".equals(o.getStatus()))    // filter ✅
    .map(o -> OrderResponse.from(o))              // transform ✅
    .flatMap(o -> enrichmentService.enrich(o))    // async ✅
    .take(10)                                     // limit ✅
    .sort(Comparator.comparing(Order::getAmount)) // sort ✅
    .collectList()                                // Flux→Mono<List> ✅
    .defaultIfEmpty(Collections.emptyList())      // if empty ✅
    .onErrorReturn(Collections.emptyList())       // on error ✅
    .doOnNext(o -> log.info("Processing: {}", o)) // side effect ✅
    .doOnComplete(() -> log.info("Done"))         // on complete ✅
    .doOnError(e -> log.error("Error: {}", e));   // on error ✅

// ── zipWith — combine two Monos ──────────────────────────────
Mono<OrderResponse> combined = orderRepository
        .findById(1L)
        .zipWith(customerRepository.findById(customerId),
            (order, customer) ->
                new OrderResponse(order, customer)); // ✅

// ── merge / concat Flux ───────────────────────────────────────
Flux<Order> orders1 = orderRepository.findByStatus("PAID");
Flux<Order> orders2 = orderRepository.findByStatus("PENDING");

Flux<Order> merged      = Flux.merge(orders1, orders2);  // interleaved ✅
Flux<Order> concatenated = Flux.concat(orders1, orders2); // sequential ✅
```

| Operator | Input | Output | Use case |
|---|---|---|---|
| `map` | T | R | Sync transform ✅ |
| `flatMap` | T | Mono/Flux\<R\> | Async transform ✅ |
| `filter` | T | T or empty | Filter condition ✅ |
| `take(n)` | Flux | Flux | Limit elements ✅ |
| `collectList()` | Flux | Mono\<List\> | Collect all ✅ |
| `zipWith` | Mono+Mono | Mono | Combine two ✅ |
| `defaultIfEmpty` | empty | default | Handle empty ✅ |
| `switchIfEmpty` | empty | Mono/Flux | Replace empty ✅ |
| `onErrorReturn` | error | fallback | Handle error ✅ |
| `doOnNext` | T | T | Side effect ✅ |

---

## Q4. What is WebClient? How is it different from RestTemplate?

### Answer
```
RestTemplate:
→ synchronous blocking ❌
→ thread waits for response ❌
→ deprecated in Spring 5 ⚠️
→ one thread per request ❌

WebClient:
→ async non-blocking ✅
→ Spring 5+ replacement ✅
→ returns Mono/Flux ✅
→ works in MVC + WebFlux ✅
→ supports streaming ✅
→ built-in retry + timeout ✅
→ connection pooling ✅
→ fluent API ✅
```

```java
// ── WebClient setup ───────────────────────────────────────────
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080/api")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer ->
                    configurer.defaultCodecs()
                        .maxInMemorySize(1024 * 1024)) // 1MB ✅
                .build();
    }
}

// ── All HTTP methods ──────────────────────────────────────────
@Service
@RequiredArgsConstructor
public class OrderClientService {

    private final WebClient webClient;

    // GET single — Mono ✅
    public Mono<Order> getOrder(Long id) {
        return webClient.get()
                .uri("/orders/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                    res -> Mono.error(
                        new NotFoundException("Not found")))
                .onStatus(HttpStatusCode::is5xxServerError,
                    res -> Mono.error(
                        new ServiceException("Server error")))
                .bodyToMono(Order.class); // ✅
    }

    // GET list — Flux ✅
    public Flux<Order> getAllOrders() {
        return webClient.get()
                .uri("/orders")
                .retrieve()
                .bodyToFlux(Order.class);
    }

    // GET with params ✅
    public Flux<Order> getOrdersByStatus(String status) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/orders")
                    .queryParam("status", status)
                    .queryParam("page", 0)
                    .build())
                .retrieve()
                .bodyToFlux(Order.class);
    }

    // POST ✅
    public Mono<Order> createOrder(OrderRequest request) {
        return webClient.post()
                .uri("/orders")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Order.class);
    }

    // PUT ✅
    public Mono<Order> updateOrder(Long id, Order order) {
        return webClient.put()
                .uri("/orders/{id}", id)
                .bodyValue(order)
                .retrieve()
                .bodyToMono(Order.class);
    }

    // PATCH ✅
    public Mono<Order> updateStatus(Long id, String status) {
        return webClient.patch()
                .uri("/orders/{id}", id)
                .bodyValue(Map.of("status", status))
                .retrieve()
                .bodyToMono(Order.class);
    }

    // DELETE ✅
    public Mono<Void> deleteOrder(Long id) {
        return webClient.delete()
                .uri("/orders/{id}", id)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // ── Parallel calls ────────────────────────────────────────
    public Mono<OrderDashboard> getDashboard(Long customerId) {
        Mono<List<Order>> orders =
            getAllOrders().collectList();

        Mono<Customer> customer =
            webClient.get()
                .uri("/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(Customer.class);

        return Mono.zip(orders, customer,
            (o, c) -> new OrderDashboard(o, c)); // ✅
    }

    // ── Retry ─────────────────────────────────────────────────
    public Mono<Order> getOrderWithRetry(Long id) {
        return webClient.get()
                .uri("/orders/{id}", id)
                .retrieve()
                .bodyToMono(Order.class)
                .retryWhen(Retry.backoff(3,
                    Duration.ofSeconds(1))); // retry 3 times ✅
    }

    // ── Timeout ───────────────────────────────────────────────
    public Mono<Order> getOrderWithTimeout(Long id) {
        return webClient.get()
                .uri("/orders/{id}", id)
                .retrieve()
                .bodyToMono(Order.class)
                .timeout(Duration.ofSeconds(5)) // 5s ✅
                .onErrorReturn(TimeoutException.class,
                    new Order("DEFAULT")); // fallback ✅
    }

    // ── Subscribe — fire and forget ───────────────────────────
    public void publishEvent(OrderEvent event) {
        webClient.post()
                .uri("/events")
                .bodyValue(event)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                    null,
                    error -> log.error("Failed: {}",
                            error.getMessage()),
                    () -> log.info("Published ✅")
                );
    }
}
```

| | RestTemplate | WebClient |
|---|---|---|
| **Type** | Blocking ❌ | Non-blocking ✅ |
| **Return** | Object directly | Mono/Flux ✅ |
| **Threading** | One per request | Few shared ✅ |
| **Status** | Deprecated ⚠️ | Recommended ✅ |
| **Streaming** | ❌ No | ✅ Yes |
| **Retry** | Manual | Built-in ✅ |
| **Timeout** | Factory config | `.timeout()` ✅ |
| **Works in** | MVC only | MVC + WebFlux ✅ |

---

## Q5. What is Backpressure in Reactive Programming?

### Answer
```
Problem:
→ Producer sends data FASTER than consumer can process ❌
→ consumer overwhelmed → OutOfMemoryError ❌

Backpressure:
→ consumer tells producer "slow down" ✅
→ consumer controls the flow ✅
→ prevents overload ✅

Analogy:
Without: factory ships 1000 boxes/min →
         warehouse handles 100/min → FLOODS ❌
With:    warehouse says "send 100/min only" ✅
         no overflow ✅

Four strategies:
1. Buffer  → store overflow in memory ✅
2. Drop    → discard overflow items ✅
3. Latest  → keep only most recent ✅
4. Error   → throw exception on overflow ✅
```

```java
// ── limitRate — request N items at a time ─────────────────────
orderRepository.findAll()
    .limitRate(10)               // process 10 at a time ✅
    .map(order -> processOrder(order))
    .subscribe();

// ── onBackpressureBuffer ──────────────────────────────────────
Flux.interval(Duration.ofMillis(1))  // fast producer
    .onBackpressureBuffer(100)        // buffer 100 items ✅
    .delayElements(Duration.ofMillis(100)) // slow consumer
    .subscribe(item -> process(item));

// ── onBackpressureDrop — discard overflow ─────────────────────
Flux.interval(Duration.ofMillis(1))
    .onBackpressureDrop(dropped ->
        log.warn("Dropped: {}", dropped)) // drop + log ✅
    .delayElements(Duration.ofMillis(100))
    .subscribe();

// ── onBackpressureLatest — keep newest only ───────────────────
Flux.interval(Duration.ofMillis(1))
    .onBackpressureLatest()      // keep only latest ✅
    .delayElements(Duration.ofMillis(100))
    .subscribe();

// ── Custom subscriber — consumer controls ─────────────────────
orderRepository.findAll()
    .subscribe(new BaseSubscriber<Order>() {

        @Override
        protected void hookOnSubscribe(Subscription sub) {
            request(10); // ask for 10 first ✅
        }

        @Override
        protected void hookOnNext(Order order) {
            processOrder(order);
            request(1); // ask for 1 more after each ✅
        }
    });

// ── Kafka + WebFlux backpressure ──────────────────────────────
@Service
public class OrderConsumer {

    public Flux<Order> consumeOrders() {
        return kafkaReceiver
            .receive()
            .limitRate(100)              // 100 at a time ✅
            .map(record -> record.value())
            .flatMap(event ->
                orderService.process(event),
                10)                      // max 10 concurrent ✅
            .onBackpressureBuffer(1000); // buffer 1000 ✅
    }
}
```

| Strategy | Overflow behavior | Use case |
|---|---|---|
| `limitRate` | Request N at a time | General ✅ |
| `onBackpressureBuffer` | Store in buffer | Short bursts ✅ |
| `onBackpressureDrop` | Drop overflow | Metrics, logs ✅ |
| `onBackpressureLatest` | Keep newest only | Real-time data ✅ |
| `onBackpressureError` | Throw exception | Strict no-loss ✅ |

---

## Quick Reference — All 5 Key Points

```
WebFlux      = non-blocking reactive framework ✅
Mono         = 0 or 1 element (like Optional) ✅
Flux         = 0 to N elements (like Stream) ✅
map          = sync transform T → R ✅
flatMap      = async transform T → Mono/Flux<R> ✅
filter       = predicate based filtering ✅
WebClient    = non-blocking HTTP client ✅
RestTemplate = blocking — deprecated ⚠️
Backpressure = consumer controls flow ✅
limitRate    = process N items at a time ✅
Buffer       = store overflow ✅
Drop         = discard overflow ✅
Latest       = keep newest only ✅
```

| Topic | Key Point |
|---|---|
| WebFlux vs MVC | WebFlux = non-blocking. MVC = blocking ✅ |
| Mono | 0 or 1 element. like Optional ✅ |
| Flux | 0 to N elements. like Stream ✅ |
| map | sync T → R. no Mono/Flux returned ✅ |
| flatMap | async T → Mono/Flux<R>. flattens nested ✅ |
| filter | predicate. keep matching elements ✅ |
| WebClient | non-blocking. Mono/Flux return. retry + timeout ✅ |
| RestTemplate | blocking. deprecated. avoid ⚠️ |
| Backpressure | consumer controls speed. prevents overload ✅ |
| limitRate | request N at a time ✅ |
| Buffer strategy | store overflow in memory ✅ |
| Drop strategy | discard overflow — use for metrics ✅ |
| Latest strategy | keep newest — use for real-time ✅ |
