# Spring Complete Guide — Short Notes

---

## AOP (Aspect Oriented Programming)

```
What-         cross-cutting concerns | logging | security | transactions | auditing
Core concepts- Aspect=class with cross-cutting logic | JoinPoint=method execution point
              Pointcut=expression selects which methods | Advice=code that runs | Weaving=linking aspects
Advice types- @Before=before method | @After=always after | @AfterReturning=on success
              @AfterThrowing=on exception | @Around=wraps method (most powerful)
Self invocation- internal this.method() bypasses proxy | @Transactional ignored | fix=inject self or separate class
Order-        @Around(before) → @Before → method → @AfterReturning/@AfterThrowing → @After → @Around(after)
Spring vs AspectJ- Spring=runtime proxy method only | AspectJ=compile time method+field+constructor
Pointcut-     execution(* com.example.service.*.*(..)) | @annotation(Loggable) | within(package) | bean(name)
Use cases-    method logging | performance timing | security checks | audit | caching | rate limiting
```

---

## SOLID Principles

```
S- Single Responsibility | one class one job | one reason to change
O- Open/Closed | open for extension closed for modification | use interface/abstraction not if-else
L- Liskov Substitution | subclass substitutable for parent | do not break parent contract | Square extends Rectangle bad
I- Interface Segregation | small focused interfaces | don't force implement unused methods
D- Dependency Inversion | depend on abstractions not implementations | inject interface not concrete class
```

---

## RestTemplate

```
What-         synchronous HTTP client | maintenance mode Spring 5+ | use WebClient for new projects
Setup-        spring-boot-starter-web | @Bean RestTemplate
GET-          getForObject(url, Type.class) | getForEntity returns ResponseEntity with headers+status
POST-         postForObject(url, body, Type.class) | postForEntity for full response
PUT-          restTemplate.put(url, body, pathVars) returns void
DELETE-       restTemplate.delete(url, pathVars) returns void
PATCH-        no direct method | use exchange(url, HttpMethod.PATCH, request, Type.class)
exchange()-   most flexible | any method | custom headers | full response control
HttpEntity-   contains payload + HttpHeaders | use for custom headers
Errors-       4xx=HttpClientErrorException | 5xx=HttpServerErrorException | network=ResourceAccessException
Timeout-      HttpComponentsClientHttpRequestFactory | connectTimeout | readTimeout | connectionRequestTimeout
Interceptors- implement ClientHttpRequestInterceptor | add auth headers globally | logging
PUT vs PATCH- PUT=full replace all fields | PATCH=partial only changed fields | PUT always idempotent
```

---

## Kafka

```
What-         event streaming platform | producer/consumer | millions events/sec | fault tolerant | replay
Concepts-     Topic=named channel | Partition=ordered immutable sequence | Offset=unique message ID per partition
              Broker=Kafka server | Consumer Group=share reading partitions | ISR=in-sync replicas
Replication-  Leader handles reads/writes | followers replicate | ISR can become new leader
acks-         0=no ack fire-forget | 1=leader only | all=all ISR safest
Idempotence-  enable.idempotence=true | sequence number per message | broker ignores duplicate sequence
Delivery-     at-most-once=may lose | at-least-once=may duplicate | exactly-once=idempotent+manual ack
Outbox-       save business data + event same @Transactional | scheduler publishes | no lost events
Producer-     batch.size+linger.ms | compression.type | acks | retries | buffer.memory
Consumer-     fetch.min.bytes | fetch.max.wait.ms | max.poll.records | auto.offset.reset | enable.auto.commit
Manual ack-   ack.acknowledge() after processing | offset committed only on success | at-least-once
Rebalancing-  consumer joins/leaves | ALL consumers pause | partitions redistributed | performance concern
Batch-        max-poll-records=500 default | try/catch per message | bad=DLQ | good continue | ack whole batch
vs RabbitMQ-  Kafka=log-based replay+retention | RabbitMQ=consumed=gone no replay | Kafka=millions/sec
Static membership- group.instance.id=fixed ID | restart=same partition | no rebalancing | K8s HOSTNAME
```

---

## 12-Factor App

```
1-Codebase-       one repo per service | one codebase many deploys
2-Dependencies-   explicit declarations | pom.xml requirements.txt | no system-wide packages
3-Config-         environment variables | never hardcoded | varies per deployment
4-Backing Services- DB/cache/queue = attached resources | swap without code change
5-Build-Run-      strict separation | build=compile | release=build+config | run=execute
6-Processes-      stateless share-nothing | persistent data in DB | enables horizontal scaling
7-Port Binding-   self-contained | expose via port | no external web server injected
8-Concurrency-    scale out not up | different workloads different process types
9-Disposability-  fast startup | graceful shutdown | finish in-flight requests
10-Dev/Prod Parity- same tools environments | reduce works-on-my-machine | deploy frequently
11-Logs-          write to stdout | execution environment captures | ELK/Splunk aggregates
12-Admin Processes- one-off tasks isolated | DB migrations | same environment as app
```

---

## Microservice Design Patterns

```
Decomposition-    by Business Capability (what biz does) | by Subdomain DDD (bounded contexts)
Database-         DB per Service=recommended loose coupling | Shared DB=anti-pattern tight coupling
                  CQRS=separate read/write models | Event Sourcing=store events not state
Communication-    API Gateway=single entry point | async=Kafka/RabbitMQ | Service Mesh=Istio Envoy sidecar
Reliability-      Circuit Breaker=CLOSED→OPEN→HALF_OPEN | Retry=backoff | Bulkhead=separate thread pools | Timeout=never wait forever
Consistency-      Saga=distributed transactions | Choreography=events emitted | Orchestration=central coordinator
                  Outbox=guaranteed delivery same tx | Two-Phase Commit=rarely used too slow
Deployment-       Blue-Green=two envs instant switch | Canary=10% traffic gradual | Rolling=one by one
                  Strangler Fig=gradual monolith migration | Feature Flag=enable disable runtime
Observability-    Centralized Logging=ELK Splunk | Distributed Tracing=X-Ray Jaeger | Health Check=/actuator/health
Security-         JWT propagated between services | mTLS=both sides verify | API Key=internal | OAuth2 client credentials=M2M
Challenges-       network latency | cascade failures | distributed transactions | operational overhead | no @Transactional across services
```

---

## Spring Security

```
What-             authentication(who) + authorization(what) | filter chain | Chain of Responsibility
Filter chain-     SecurityContextPersistenceFilter | UsernamePasswordAuthFilter | JwtAuthFilter
                  ExceptionTranslationFilter(401/403) | FilterSecurityInterceptor(roles)
Authentication-   credentials → AuthenticationManager → AuthenticationProvider → UserDetailsService → SecurityContext
Authorization-    authenticated user has role | can access URL | FilterSecurityInterceptor decides
JWT-              3 parts: Header(algorithm) | Payload(sub+roles+exp+iat+iss) | Signature(tamper proof)
JWT flow-         login→validate→JWT issued | client sends Authorization: Bearer token | server validates
Password-         BCryptPasswordEncoder | Argon2 | PBKDF2 | adaptive+salted+brute-force resistant
CSRF-             disabled in REST JWT | attacker tricks logged-in user | JWT in header cannot be stolen
CORS-             Cross Origin Resource Sharing | browser blocks different domain | server must allow origins
Method security-  @EnableMethodSecurity | @PreAuthorize("hasRole('ADMIN')") | @PostAuthorize | @Secured
OAuth2-           spring-boot-starter-oauth2-client | Google/GitHub login | client-id + client-secret in yml
mTLS-             mutual TLS | both sides verify certificates | Istio handles automatically | zero trust
SessionPolicy-    STATELESS for JWT | no server-side sessions | SecurityContextHolder ThreadLocal
```

---

## Java 15-21 Key Features

```
Java 15- Text Blocks final (multi-line strings no escape) | Hidden Classes for frameworks
Java 16- Records final | instanceof pattern matching (no cast) | Stream.toList() unmodifiable
Java 17- Sealed Classes final (permits subclasses) | switch expressions stable | LTS
Java 18- UTF-8 default charset | Simple Web Server jwebserver | @snippet in Javadoc
Java 19- Virtual Threads preview | Record Patterns preview | Structured Concurrency preview
Java 21- Virtual Threads final | Record Patterns final | Pattern Matching switch | Sequenced Collections | LTS
```

---

## Records (Java 16+)

```
What-           special class | immutable | no boilerplate | auto: fields+constructor+getters+equals+hashCode+toString
Syntax-         public record Person(String name, int age) {}
Restrictions-   no extra instance fields in body | no extends (extends Record implicitly) | all fields final
Allowed-        static fields/methods | custom methods | implement interfaces | compact constructor
Compact ctor-   validation/normalization | no param list needed | compact form { if condition throw }
vs Lombok-      Record=no dependency Java 16+ immutable | Lombok=any Java mutable optional
Use cases-      DTO | value objects | map keys | multiple return values | pattern matching
Stream.toList()- Java 16+ immutable | collect(toList())=mutable any Java
```

---

## Virtual Threads (Java 21)

```
What-           lightweight threads managed by JVM | not OS threads | millions simultaneously
Problem-        platform threads=OS threads expensive ~1MB | blocking IO wastes OS thread
Solution-       virtual thread blocks → JVM parks → carrier thread reused for others
Use for-        IO bound: DB queries | API calls | Kafka | file processing
Not for-        CPU bound: sorting | image processing | encryption | ML computations
Synchronized-   pins virtual thread to carrier thread | prefer ReentrantLock instead
ReentrantLock-  JVM can unmount virtual thread waiting | carrier thread free for others
Usage-          Thread.ofVirtual().start(runnable) | Executors.newVirtualThreadPerTaskExecutor()
Don't pool-     create new per task | pooling defeats the purpose
```

---

## Transaction Isolation

```
READ_UNCOMMITTED- dirty reads possible | fastest | use for non-critical
READ_COMMITTED-   no dirty reads | non-repeatable reads possible | default most DBs
REPEATABLE_READ-  no dirty/non-repeatable | phantom reads possible | row locked
SERIALIZABLE-     no dirty/non-repeatable/phantom | slowest | financial transactions
```

---

## Transaction Propagation

```
REQUIRED-      default | use existing or create new
REQUIRES_NEW-  always new transaction | suspend existing | audit logs
NESTED-        savepoint within existing | rollback to savepoint on failure
MANDATORY-     must have existing | exception if none
SUPPORTS-      use if exists otherwise no tx
NOT_SUPPORTED- suspend existing | run without tx
NEVER-         exception if tx exists
```

---

## Saga Pattern

```
Choreography-  services emit events | others listen | loosely coupled | hard to track | Kafka
Orchestration- central coordinator | tells each service | easy to track | Temporal/Step Functions
Compensation-  undo previous step on failure | reverse transaction
Idempotency-   check processedEventId before processing | no duplicate processing
Outbox-        save + event same @Transactional | scheduler publishes | no lost events
```

---

## JVM Memory Segments

```
Heap-          new objects | GC managed | young(eden+survivor) + old generation
Stack-         method calls | local variables | per thread | LIFO
Metaspace-     class metadata | method info | replaces PermGen Java 8+
Code Cache-    JIT compiled native code
Native Memory- off-heap | direct buffers | NIO
GC types-      Serial | Parallel | G1(default Java 9+) | ZGC(Java 11+ low latency) | Shenandoah
```

---

## RestClient (Modern - Spring 6+)

```
What-          fluent API | replaces RestTemplate | synchronous | Spring 6+
vs RestTemplate- RestClient=modern fluent | RestTemplate=maintenance mode legacy
vs WebClient-   RestClient=sync blocking | WebClient=async non-blocking reactive
Usage-         RestClient.create(baseUrl) | .get().uri().retrieve().body(Type.class)
Error handling- onStatus(status, (req,res) -> throw exception)
```

---

## Feign Client

```
What-          declarative REST client | no boilerplate | interface based | Spring Cloud
Setup-         @EnableFeignClients | @FeignClient(name="service", url="http://...")
Usage-         @GetMapping on interface method | Spring generates implementation
vs RestTemplate- Feign=declarative interface | RestTemplate=manual boilerplate
Interceptors-  RequestInterceptor | add auth headers to all requests
```

---

## gRPC

```
What-          Google RPC | HTTP/2 + Protobuf | faster than REST | strongly typed
vs REST-       gRPC=binary faster smaller | REST=JSON human readable browser friendly
4 patterns-    Unary=one req one res | Server streaming=one req many res
               Client streaming=many req one res | Bidirectional=many req many res
StreamObserver- onNext() | onError() | onCompleted()
Use when-      microservice to microservice | low latency | streaming | polyglot
```

---

## WebFlux (Reactive)

```
What-          non-blocking reactive | event loop model | Spring 5+
vs MVC-        MVC=thread per request blocking | WebFlux=event loop non-blocking
Mono-          0 or 1 element
Flux-          0 to N elements
Operators-     map=sync transform | flatMap=async transform | filter | zip=combine two Monos
Backpressure-  consumer controls producer | onBackpressureBuffer | limitRate | onBackpressureDrop
Error-         onErrorReturn | onErrorResume | timeout | retry
Use when-      high concurrency | streaming | reactive DB R2DBC
```

---

## Performance Improvements

```
DB-            EXPLAIN ANALYZE | indexes | JOIN FETCH N+1 | readOnly=true read replica | pagination
               HikariCP tuning | only required columns | slow query log
App-           @Cacheable | CompletableFuture parallel | @Async | Virtual Threads Java 21
               right data structures | circuit breaker timeout | ZGC generational
Infra-         ECS task CPU/memory | horizontal scaling | CDN static content
Debug tools-   distributed tracing X-Ray/Jaeger | /actuator/heapdump Eclipse MAT | threaddump deadlock
```

---

## JPA Advanced

```
findById()-        hits DB immediately + Optional | use when need entity data
getReferenceById()- proxy no DB hit | use for FK only | EntityNotFoundException on access
@EntityGraph-      attributePaths={"items"} | avoids N+1 | one query fetch relations
Dirty checking-    inside @Transactional change field → auto UPDATE on commit | no save() needed
First level cache- per session default always on | same session=same object
Second level cache- application wide | configure EhCache/Redis | @Cache annotation
flush vs commit-   flush=SQL sent to DB not permanent | commit=permanent no rollback
```

---

## Resilience4j

```
Circuit Breaker-  CLOSED→OPEN(threshold)→HALF_OPEN(test)→CLOSED | fallbackMethod
Config-           failureRateThreshold=50% | waitDurationInOpenState=60s | permittedCallsInHalfOpen=5
Retry-            @Retryable | maxAttempts | @Backoff delay | multiplier=exponential | random=jitter
Rate Limiter-     @RateLimiter | limitForPeriod | limitRefreshPeriod | 429 on exceed
Bulkhead-         separate thread pools | payment pool separate from order pool
TimeLimiter-      timeout on operation | cancel if exceeded
```

---

## JUnit 5 + Mockito

```
@Mock-           pure Mockito | no Spring | fast | with @InjectMocks | unit tests
@MockBean-       Spring managed | replaces real bean | with @WebMvcTest | integration tests
@ExtendWith-     MockitoExtension for unit | SpringExtension for Spring tests
Assertions-      assertEquals | assertThrows | assertNotNull | assertThat
Mockito-         when().thenReturn() | verify() | times() | any() | doThrow()
@Spy-            partial mock | real methods by default | override specific
@Captor-         ArgumentCaptor | capture argument passed to mock
@ParameterizedTest- run same test with different inputs | @ValueSource | @CsvSource
```

---

## Spring Batch

```
Components-    Job=entire batch | Step=unit of work | ItemReader=read | ItemProcessor=transform | ItemWriter=write
Chunk-         read N → process N → write N → commit | chunk-size=commit interval
Meta tables-   BATCH_JOB_INSTANCE | BATCH_JOB_EXECUTION | BATCH_STEP_EXECUTION | restart from checkpoint
Listeners-     job level | step level | item level | monitor events
Skip policy-   skip bad records | continue processing | log skipped
Retry policy-  retry failed records | max retry count
Scheduler-     @Scheduled cron | every Friday | first Monday of month
```

---

## Spring Cloud

```
Config Server-    centralized config | Git backed | dev/stage/prod configs | @RefreshScope
Eureka-           service discovery | register on startup | heartbeat 30s | client-side
API Gateway-      Spring Cloud Gateway | routing + filters | rate limiting | circuit breaker
Feign Client-     declarative REST | @FeignClient | no RestTemplate boilerplate
Sleuth-           distributed tracing | trace ID across services | find slow service
Bus-              /actuator/busrefresh | broadcast refresh to all pods via Kafka
```

---

## Design Patterns

```
Creational-    Singleton=one instance | Factory=create without new | Builder=step by step | Prototype=clone existing
Structural-    Adapter=incompatible interfaces | Decorator=add behavior | Proxy=control access | Facade=simplify
Behavioral-    Strategy=swap algorithm | Observer=notify listeners | Command=encapsulate request | Chain of Responsibility=filters
Spring usage-  Singleton=@Component beans | Factory=BeanFactory | Proxy=@Transactional AOP | Observer=ApplicationEvent
               Template Method=JpaRepository | Strategy=multiple @Service implementations | Decorator=filters
```

---

## CQRS

```
What-          Command Query Responsibility Segregation | separate read write models
Command-       create/update/delete → writer endpoint → Aurora writer
Query-         read → readOnly=true → Aurora reader endpoint
Benefits-      independent scaling | optimize each side | clean domain
Implementation- AbstractRoutingDataSource | readOnly=true→reader | write→writer
Event Sourcing- store events not state | replay to rebuild | full audit trail
```

---

## Collections

```
HashMap-          O(1) | null key allowed | NOT thread safe | fail fast
LinkedHashMap-    insertion order | LRU cache removeEldestEntry | O(1)
TreeMap-          sorted O(log n) | Comparable/Comparator | no null key
ConcurrentHashMap- thread safe bucket level lock | NO null key/value | CAS Java 8+
ArrayList-        dynamic array O(1) get | O(n) insert | not thread safe
LinkedList-       doubly linked O(1) add/remove | O(n) get | more memory
HashSet-          no duplicates | backed by HashMap | null allowed | O(1)
PriorityQueue-    min-heap | natural order | O(log n) add/poll
ArrayDeque-       FIFO queue or LIFO stack | faster than LinkedList | no null
```

---

## Micrometer

```
What-          metrics facade | vendor neutral | Prometheus Grafana CloudWatch Datadog
Counters-      increment only | count events | payment.count
Timers-        measure duration | latency | p50/p95/p99
Gauges-        current value snapshot | queue size | active connections
Distribution-  histogram + percentiles | SLA boundaries
Spring Boot-   auto configured | /actuator/metrics | /actuator/prometheus
Custom-        registry.counter("name", "tag", "value").increment()
               registry.timer("name").record(() -> method())
```

---

## HikariCP

```
What-          fastest Java connection pool | Spring Boot default
Config-        minimum-idle | maximum-pool-size | connection-timeout | idle-timeout | leak-detection-threshold
Pool size-     = (core_count * 2) + effective_spindle_count
Monitoring-    /actuator/metrics/hikaricp.connections | pool.active | pool.idle | pool.pending
Leak detection- leak-detection-threshold=30000ms | logs warning if connection not returned
```

---

## How Spring Boot Works Internally

```
Startup-       @SpringBootApplication = @SpringBootConfig + @EnableAutoConfig + @ComponentScan
Auto config-   reads META-INF/spring/AutoConfiguration.imports | evaluates @Conditional | register if pass
SpringApplication.run()- creates ApplicationContext | registers beans | starts embedded Tomcat
Bean lifecycle- instantiate → @Autowired → @PostConstruct → use → @PreDestroy → destroy
Embedded server- Tomcat default | Jetty/Undertow alternative | no WAR needed runs as JAR
```

---

## AtomicInteger & Volatile

```
AtomicInteger-  thread safe counter | compareAndSet CAS | no synchronized needed | incrementAndGet
Volatile-       visibility guarantee | not atomicity | changes visible to all threads immediately
                prevents CPU caching of variable | use for flags/status not compound operations
vs synchronized- volatile=visibility only | synchronized=visibility+atomicity | AtomicInteger=atomic ops
```

---

## CompletableFuture vs Future

```
Future-              blocking get() | no callback | cannot combine | Java 5
CompletableFuture-   non-blocking | callbacks | chain operations | combine | Java 8
thenApply()-         transform result sync Function
thenCompose()-       transform result async returns CompletableFuture
thenCombine()-       combine two independent futures
allOf()-             wait for all to complete
anyOf()-             wait for first to complete
exceptionally()-     handle exception fallback
parallel calls-      CompletableFuture.supplyAsync(task1) + supplyAsync(task2) + allOf().join()
```

---

## API Versioning

```
URI versioning-    /api/v1/orders | /api/v2/orders | most common visible
Header versioning- Accept: application/vnd.api.v1+json | clean URL
Param versioning-  /api/orders?version=1 | simple
@RequestMapping-   can combine version prefix at class level
```

---

## Quick Reference

```
AOP-            @Around most powerful | self invoke bypasses proxy | separate class fix
SOLID-          SRP=one job | OCP=extend not modify | LSP=subtype substitutable | ISP=small interface | DIP=depend abstraction
Records-        Java 16+ | immutable | no boilerplate | no extra instance fields | compact constructor
Virtual Threads- Java 21 | IO bound | millions | block=park+reuse carrier | ReentrantLock not synchronized
12-Factor-      config=env vars | stateless | stdout logs | port binding | dev/prod parity
Saga-           choreography=events | orchestration=coordinator | compensation=undo | outbox=guaranteed
Circuit Breaker- CLOSED→OPEN→HALF_OPEN | fallback | Resilience4j
Kafka-          partition key=ordering | acks=all safest | idempotence=no dup | outbox=consistent
CQRS-           command→writer | query→reader readOnly=true
```
