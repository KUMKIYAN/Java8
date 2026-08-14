Stereotype - all are @Component internally - readability - layer identification - generic Spring bean - business
logic layer - persistence layer - web/endpoint layer - Spring DataAccessException hierarchy

DI - FI - reflection - no gaurenatee / overlook - break - @InjectMock - refactor.
CI - > object - dependencies mandatory - SpringContext - too many params - refactor - immutability - RequiredArgsConstructor (only final fields). AllArgsConstructor (non final)

Transactional - atomicity - any exception - Propagation - Isolation - PROXY around class - proxy intercepts - this.method() → bypasses proxy (Ignored) - Inject self ->  @Autowired vs ApplicationContext - move method

@SpringBootTest - application context - all beans - slow — heavy - integration testing - containers/@MockBean
@WebMvcTest - web layer - no beans - fast - lightweight - simulate HTTP requests - @MockBean

Circuit Breaker - monitor - stop calling - give time - closed - counting - Open - fallback - wait - HalfOpen -
allow limited calls - What exception - record-exceptions - ignore-exceptions.
sliding - type + size + Min - failureRate - wait duration - permit calls in  - automatic-transition.

Synchronous vs Asynchronous communication -  RestTemplate / RestClient - wait - tight coupling - cascade failure - Immediate response - payment , user login and stock check.

Fire and forget - messages wait - loose coupling - no cascade - When it is needed (FAF)- notifications, emails - high throughput - decouple - replay - temporarily down - > Kafka - SQS - MQ - EventBridge.

API Gateway - single entry point - routing - authentication -  rate limiting - transformation - load balancing -
SSL termination - API versioning - caching - logging - CORS (Resource Sharing) handling - block malicious.

Service Discovery - find each other dynamically - IPs - registers - instance list - Ribbon/LB - picks healthy
instance - routes request - heartbeat to Eureka - removed - never called.

@Component - class level - auto-detected - own class, @Service, @Repository, @Controller
@Bean - method level - inside @Configuration - third party classes - full control - multiple beans - customize

@Profile - load config / beans based on environment - File names - spring.profiles.active - config server - git
repo - spring.cloud.config.server.git.uri - search path

Saga - big automic transaction - split - listens - saves - emits - Forward steps - Compensation

Choreography - emit - listen - no central - loosely coupled - hard to track - team boundaries clear - simple flows.

Orchestration - central orchestrator - tells - easy to track - one file - single point - complex flows - CC.
idempotency check - outbox - stuck saga - Semantics ->  desing Compensation -> side effect  - refund example

@Controller - MVC web applications - returns - ViewResolver - Thymeleaf/jsp/freeMarker - @ResponseBody - api
@RestController=Controller+ResponseBody - REST APIs - ResponseBody - Jackson - view resolver - ResponseEntity/POJO.

Actuator - built-in endpoints - monitor & manage - health/metrics/info/env/beans/mappings/loggers/threaddump/
heapdump/refresh/shutdown/circuitbreakers

What is @RefreshScope? When and why use it?
new property - without restarting - change property  - /actuator/refresh - destroy + recreat - pickup
/actuator/busrefresh - all pods

Difference between EAGER and LAZY loading in JPA?

Many - Lazy - on demand - Better performance - parent alone - Outside transaction  - LazyInitializationException -
orderRepository.findById(id) - in service - transaction end after this line
order.getItems() -> in controller - LazyInitializationException
within Transaction still N+1 -> fix - JOIN FETCH
One - Eager -> loads with parent - all n+1 query - no LazyInitializationException - fix - JOIN FETCH

N+1 - 1 query + N queries -> JOIN FETCH / @EntityGraph / @BatchSize (size=10) => N/10 queries

cache method result  - caller not blocked - non-blocking
@EnableAsync  - @Async  - PUBLIC - SAME class - ThreadPoolTaskExecutor - void - CompletableFuture<T>

microservice performing slowly - bottleneck - xray/jaeger - which service method - metrics -> p99 L - dashboards -
threaddump - heap dump - HikariCP

DB Level - EXPLAIN ANALYZE - index - required columns - pagination - avoid N+1 - readOnly=true - log slow query -
DB feature  - SET GLOBAL slow_query_log = 'ON'; SET GLOBAL long_query_time = 1; /var/log/*.log

data structures - caching - CompletableFuture - parallel stream - @Async - circuit breaker  - timeout set - GC pressure - virtual threads

Infrastructure Level - CDN - CPU and memory - scale horizontally

@PathVariable - URL path - mandatory - resource identification - /orders/1
@RequestParam - query string - optional - filtering, pagination, sorting - /orders?status=PAID&page=0

PUT - replace - null - Idempotent - same call 100 times - same result
PATCH - partial update - missing fields unchanged - send only changed fields -
not guaranteed idempotent - based on operation  
{ "status": "PAID" } call 10 times no chage  - idempotent
{ "amount": "+10" } call 10 times not idempotent

@ControllerAdvice: - global ex handler - applies on all controller - can limit to package / class / annotation
@ControllerAdvice("com.kiyan.order")
@ControllerAdvice(assignableTypes = {OrderController.class, PaymentController.class})
@ControllerAdvice(annotations = RestController.class)
@RestControllerAdvice: - @ControllerAdvice + @ResponseBody - returns JSON - REST APIs
@ExceptionHandler - method level - maps Ex - handler

Spring Security - every request passes - each filter - one concern - chain.doFilter() - stops chain

SecurityContextPersistenceFilter
UsernamePasswordAuthenticationFilter
JwtAuthFilter
ExceptionTranslationFilter - AuthenticationException - AccessDeniedException
FilterSecurityInterceptor
Controller

SecurityContextHolder - stores authenticated user

@OneToMany - parent side - PARENT side - @ManyToOne - child side - @JoinColumn no extra join table

HashMap:
→ NOT thread safe ❌ (NO locking at all)
→ multiple threads → data corruption ❌
→ allows ONE null key ✅
→ allows null values ✅
→ faster — no synchronization overhead ✅
→ use in single threaded environment ✅
→ initial capacity 16, load factor 0.75
fail fast
Red black tree


→ thread safe ✅
→ locks at BUCKET level ✅
→ multiple threads can write different buckets ✅
→ NO null keys ❌
→ NO null values ❌
→ slightly slower — synchronization ⚠️
→ use in multi-threaded environment ✅
→ initial capacity 16, load factor 0.75
fail safe,
Red black tree


Optional = Java 8 wrapper for nullable values
NullPointerException
forces caller to handle null


Both - orchestration platforms - Docker containers - auto scaling - health checks - load balancing
K8s - open source - any cloud - multi cloud - standard - complex to setup - costlier - large workloads
ECS - AWS native - only AWS - fully managed - Fargate - cheaper and simpler - native integration.

@Mock - pure Mockito - no Spring context - @InjectMocks - fast - unit tests
@MockBean - Spring managed - replaces real bean - @WebMvcTest / @SpringBootTest - slower - integration tests



Order placed but Payment fails
order-service saves order - SAME transaction - Kafka publishes - payment-service - payment-processed - PAID
- payment-failed - > COMPENSATE	 - CANCELLED

Outbox Pattern - no lost events - Idempotency check - Compensation - Manual commit - @RetryableTopic - DLT

Repository - CrudRepository - PagingAndSortingRepository - JpaRepository - most feature rich:

saveAll(List<Order> orders)  -> save list in one call

flush()  -> send pending SQL to DB -> but NOT permanent yet -> can rollback
saveAndFlush() -> Order saveAndFlush(Order order); -> save + send SQL immediately -> DB update happen immediately within @Transactional -> but can rollback. with save DB update happen after @Transactional.

deleteAllInBatch() ->  // DELETE FROM orders -> faster than deleteAll() -> where delete one by one
deleteAllByIdInBatch(List<Long> ids);

// getReferenceById() — lazy proxy, no DB hit
Order getReferenceById(Long id);

// findAllById() — fetch list by IDs
List<Order> findAllById(List<Long> ids);

@EnableAutoConfiguration?
Internal flow:
1. App starts
2. @EnableAutoConfiguration triggers
3. SpringFactoriesLoader reads META-INF/spring/AutoConfiguration.imports
4. loads ALL candidate classes
5. each evaluated against @Conditional
6. passes → bean registered ✅
7. fails  → skipped ❌

@Value - single property - supports SpEL - default values - scattered
@ConfigurationProperties - grouped - centralized config - supports validation @Validated - Prefix - nested objects

@Transactional(readOnly=true) - READ REPLICA - no dirty checking - no entity snapshots - better performance
readOnly = false - routes to WRITER - FlushMode.AUTO - tracks - commits

dirty checking - snapshots - on flush - compare generates UPDATE SQL. (this will skip for read only)

ResponseEntity.ok(body)
ResponseEntity.created(uri)
ResponseEntity.noContent()
ResponseEntity.badRequest()
ResponseEntity.notFound()

















