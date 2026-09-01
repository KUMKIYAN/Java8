# Architecture & Design — Short Notes

---

## Q1. How to develop application?

```
Requirements- meet business | clarify edge cases | Confluence | sequence diagrams | sign-off
Design-       Microservices | Define API contract | SQL/NoSQL |CQRS | DCP | communication pattern 
                            | third party | resources
Stories-      JIRA stories | story points | sprint planning | assign
Development-  Ask approach first | SpringBoot + Java + Microservices features | Kafka | constructor injection 
                           | exception | logs
Testing-      JUnit+Mockito | Cucumber | 90% coverage | SonarQube
Code Review-  requirements | N+1+indexes | no hardcoded creds | design patterns | Demo | DS
CI/CD-        Jenkins | build→test→sonar→deploy | Blue-Green | zero downtime
Monitoring-   CloudWatch+Splunk | PagerDuty | health endpoints | dashboards
```
### Tech stack choices
```
Backend:   Java 21 + Spring Boot ✅
Messaging: Kafka (async) / REST (sync) ✅
DB:        Aurora PostgreSQL / MySQL ✅
Cache:     Caffeine / Redis ✅
Cloud:     AWS ECS + Fargate ✅
IaC:       Terraform ✅
CI/CD:     Jenkins Blue-Green ✅
Monitor:   CloudWatch + Splunk ✅
```

---

## Q2. How to secure application?

```
Authentication-   Spring Security | JWT | BCrypt 
Authorization-    @PreAuthorize | @hasRole | method level | least privilege
Data Security-    BCrypt hash | PCI DSS | Bluefin token | Secrets Manager | KMS
Transport-        HTTPS | TLS 1.2+ | SSL cert | ACM
Input Validation- @Valid | @NotNull | @Pattern | no SQL inject | no XSS
API Security-     rate limit→429 | CORS=domain allow | CSRF=fake req | do not log sensitive
Infrastructure-   IAM minimal | VPC  | security group | WAF | rotate secrets
Code Security-    SonarQube | no secrets Git | dependency check
```

---

## Q3. Kafka vs REST?

```
Kafka-   async | no wait | millions TPS | fan-out | replay | audit | decouple | casecade
REST-    sync | immediate | CRUD  | tight coupled | casecade | Auth NOW | Stock | Fraud

Kafka real- order→payment+inventory+notify | shipment→capture | Outbox
REST real-  Bluefin VaultID | Chase auth | ACI fraud | GET order
```

---

## Q4. End-to-end API ownership?

```
Design-      OpenAPI first | req/res models | error codes | auth | share frontend | parallel dev
Development- endpoints | @Valid | @ControllerAdvice | logging | idempotency | pagination
Testing-     unit+integration+Cucumber | happy+error+boundary condition | load+security
Docs-        Swagger UI | OpenAPI annotations | Confluence | Postman collection
Deploy-      Jenkins | dev→stage→UAT→prod | Blue-Green | zero downtime
Production-  CloudWatch+Splunk | PagerDuty | first on call | RCA→fix→prevent
Versioning-  /v1 /v2 | backward compat | @deprecated | sunset old
```

---

## WAF protects-
```
SQL inject | XSS | DDoS | bots | Bad IP block | geo block | sits front of API Gateway | Terraform
```

# AWS — Short Notes (All 3 Parts Combined)

---

## Compute

```
EC2-          virtual machine | self managed | OS patch | GPU/ML | predictable traffic | IAM instance level
ECS Fargate-  serverless containers | AWS managed | auto scale | pay per task | per task IAM | microservices
ECS EC2-      you manage servers | cheaper steady traffic | spot instances | more control
ECS concepts- Cluster=group | TaskDef=blueprint | Task=running container | Service=manages tasks | ECR=registry
```

---

## Lambda

```
What-         serverless | pay per invocation | 15min max | 10GB RAM | 1000 concurrent | stateless
Triggers-     API Gateway | SQS | S3 | EventBridge cron | Kinesis | SNS | DynamoDB streams
Cold start-   JVM slow start | 2-5s delay | fix: SnapStart | provisioned concurrency | thin JAR | GraalVM
SnapStart-    JVM snapshot after init | faster restart | free | apply on PublishedVersions
Limits-       15min | 10GB RAM | 6MB payload | 1000 concurrent | no stateful storage
```

---

## SQS vs SNS vs EventBridge

```
SQS-          queue | pull based | 14 days retention | DLQ | one consumer | retry
SQS Standard- max throughput | no ordering guarantee | duplicate possible | cheaper
SQS FIFO-     ordering guaranteed | no duplicate | financial transactions | 300msg/s
SNS-          pub/sub | push | fan-out | no retention | subscribers: SQS/Lambda/email/SMS
EventBridge-  event bus | routing rules | AWS service events | cron schedule | SaaS | moderate throughput
Fan-out-      SNS → SQS-payment + SQS-inventory + SQS-notify (all receive same event)
```

---

## Aurora vs RDS

```
Aurora-       AWS managed | shared storage 6 copies | 15 read replicas | failover <30s | 128TB auto | 5x MySQL
RDS-          standard MySQL/PG | own storage per instance | 5 replicas | manual storage | cheaper simple
Routing-      readOnly=true → reader endpoint | write → writer endpoint | AbstractRoutingDataSource
Priority-     0-15 for failover promotion | 0 = highest | auto promoted on writer failure
```

---

## CloudWatch

```
Logs-         centralized | log groups per service | log streams per container | regex search | 45 days retention
Metrics-      CPU/memory/custom | Micrometer → CloudWatch | HikariCP pool | Kafka lag | payment latency
Alarms-       threshold → SNS → PagerDuty | CPU>80% | 5XX>10/min | SQS depth>100
Log Insights- SQL-like queries | filter ERROR | stats count by bin(1h)
```

---

## API Gateway

```
HTTP API-     70% cheaper | JWT built-in | CORS built-in | no transform | no cache | most use cases
REST API-     transform | cache | API keys | WAF | usage plans | enterprise | costlier
WebSocket-    bidirectional | persistent | live chat | stock prices | real-time
vs ALB-       API GW = auth+rate limit+WAF public | ALB = internal routing cheaper faster
Best practice- API GW (public) → ALB (internal) → ECS
```

---

## Secrets Manager

```
What-         stores DB pass/API keys | KMS encrypted | auto rotation 30 days | CloudTrail audit | versioning
3 ways-       taskDef ARN injection | spring.config.import | SDK manual fetch
Best practice- /prod/service/secret naming | never in code/yml/Git | IAM role not access keys | separate per env
vs SSM-       Secrets Mgr = passwords + auto rotation | SSM = config values + cheaper
```

---

## IAM

```
Components-   Users | Groups | Roles | Policies
User types-   programmatic=access key | console=username+pass | role=temporary no password
Task Exec Role- ECS agent | pull ECR | read Secrets Manager | write CloudWatch logs | infra level
Task Role-    your app | access SQS/S3/DynamoDB | minimal privilege | per task
Least privilege- only what service needs | separate exec role + task role
```

---

## Auto Scaling

```
Metrics-      CPU>70% scale out | Memory>80% scale out | ALB>1000 req/task | SQS>100 msgs
Cooldown-     scale out=60s | scale in=300s | prevents thrashing
Scheduled-    Black Friday cron | Nov 6am scale up | Nov 10pm scale down
Target-       min=2 | max=10 | desired count | ECS service
```

---

## Blue-Green Deployment

```
What-         Blue=current live | Green=new version | ALB switches traffic | instant rollback | zero downtime
Flow-         deploy green | validate logs+smoke test | approve | ALB → green | rollback → blue in seconds
vs Rolling-   rolling=one env cheaper | blue-green=two envs instant rollback production
buildspec-    pre_build: test+sonar+ECR login | build: mvn+docker+ECR push | post_build: imagedefinitions.json
```

---

## DynamoDB

```
What-         NoSQL key-value | fully managed | auto scale | pay per request | ms latency
Keys-         Partition Key=distributes data | Sort Key=range within partition | PK+SK=composite
GSI-          different PK | create anytime | max 20 | query by any attribute
LSI-          same PK diff SK | must create at table creation | max 5
TTL-          auto delete expired items | set ttl attribute | free
vs Aurora-    DynamoDB=sessions/carts/simple lookup | Aurora=complex joins/ACID/reporting
```

---

## CodePipeline CI/CD

```
Stages-       Source(GitHub) → Build(CodeBuild) → Approve(manual) → Deploy(ECS)
buildspec-    pre_build: ECR login+mvn test+SonarQube | build: mvn package+docker+ECR push | post_build: imagedefinitions.json
Two files-    imagedefinitions.json=image URI only | taskDefinition.json=full config CPU/mem/env
Auto rollback- ECS circuit breaker on health check fail | no manual intervention
Artifacts-    S3 bucket between stages | ECR for Docker images
```

---

## Quick Reference

```
EC2-          self managed VM | GPU/ML | predictable
Fargate-      serverless | per task IAM | variable traffic
Lambda-       event driven | 15min | cold start fix=SnapStart
SQS FIFO-     ordering + no duplicate | financial
SNS-          fan-out to multiple SQS
EventBridge-  cron + AWS events + SaaS
Aurora-       shared storage | 30s failover | 5x MySQL
CloudWatch-   logs + metrics + alarms
HTTP API-     70% cheaper | JWT built-in
REST API-     transform + cache + WAF
Blue-Green-   instant rollback | zero downtime
Secrets Mgr-  KMS + auto rotate | task def injection
IAM-          exec role=infra | task role=app
CPU scale-    70% threshold | 60s cooldown out
Memory scale- 80% threshold | 300s cooldown in
DynamoDB-     sessions/carts | GSI anytime | TTL free
```

# Spring Boot / Microservices — Short Notes (All 3 Parts Combined)

---

## Stereotypes & Beans

```
@Component-       generic bean | auto detected via @ComponentScan
@Service-         business logic layer | same as @Component
@Repository-      DB layer | translates SQL → Spring DataAccessException
@Controller-      web layer | returns view name | needs ViewResolver
@RestController-  @Controller + @ResponseBody | returns JSON | REST APIs
@Bean-            method level | inside @Configuration | third party classes | full control before return
@Component vs @Bean- @Component=your class | @Bean=third party (RestTemplate/KafkaTemplate/ObjectMapper)
```

---

## Dependency Injection

```
Field injection-       uses reflection | breaks without Spring | hard to test | refactor | hides dependencies
Constructor injection- recommended | final fields | works without Spring | Lombok @RequiredArgsConstructor | refactor
Setter injection-      optional deps only | @Autowired(required=false)
@Primary-              default bean when multiple same type | one per type only
@Qualifier-            specific bean by name | overrides @Primary | on injection point
```

---

## @Transactional

```
What-             atomicity | all succeed or all fail | RuntimeException=rollback | checked=no rollback
rollbackFor-      @Transactional(rollbackFor=PaymentException.class) for checked exceptions
Self invocation-  internal this.method() bypasses CGLIB proxy | @Transactional ignored | fix=separate class
readOnly=true-    read replica routing | FlushMode.NEVER | no dirty check | no snapshots | faster
Propagation-      REQUIRED=default(use existing or new) | REQUIRES_NEW=always new | NESTED=savepoint
                  MANDATORY=must have existing | SUPPORTS=use if exists | NOT_SUPPORTED=suspend | NEVER=no tx
```

---

## Auto Configuration

```
@SpringBootApplication-  @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan | entry point | root package | one per app
Auto config flow-        reads META-INF/spring/AutoConfiguration.imports | evaluates @Conditional | pass=register | fail=skip
@Conditional types-      @ConditionalOnClass | @ConditionalOnMissingBean | @ConditionalOnProperty | @ConditionalOnWebApplication
@Configuration-          bean factory | @Bean methods | CGLIB proxy=singleton guaranteed | multiple allowed
```

---

## Properties & Config

```
@Value-                  single property | SpEL support | default value ${prop:default} | field level
@ConfigurationProperties- group of properties | prefix binding | type safe | @Valid support | nested objects | recommended
                          app.order -> maxRetry, timeout, Db.host.port
@Profile-                load beans per env | @Profile("dev") @Profile("prod") @Profile("!prod")
Profiles-                application-dev.yml | application-prod.yml | SPRING_PROFILES_ACTIVE=prod
@RefreshScope-           refresh bean without restart | /actuator/refresh | /actuator/busrefresh / Spring Cloud Bus = broadcast all pods
@ConditionalOnProperty-  bean created only if property matches | havingValue | matchIfMissing
```

---

## JPA / Hibernate

```
Fetch defaults-     @OneToMany=LAZY | @ManyToMany=LAZY | @ManyToOne=EAGER(override!) | @OneToOne=EAGER(override!)
N+1 problem-        1 query parent + N queries each child | fix: JOIN FETCH | @EntityGraph | @BatchSize
LAZY risk-          LazyInitializationException outside transaction | fix: JOIN FETCH or @EntityGraph
mappedBy-           on PARENT side OneToMany | points to FIELD NAME in child | no extra join table
@JoinColumn-        on CHILD side ManyToOne | creates FK column in child table
@ManyToMany extra-  create explicit join entity with @EmbeddedId for extra columns
save()-             null ID=INSERT(persist) | has ID=UPDATE(merge) | dirty check=auto UPDATE inside @Transactional
                    persistence context | NOT sent to DB | flush | commit at the end of transaction
saveAndFlush()-     SQL sent immediately | ID generated now | still rollbackable | use when need ID next step
flush()-            sends pending SQL to DB | not committed | call save() first
Optimistic lock-    @Version field | no DB lock | exception on conflict | retry with @Retryable
Pessimistic lock-   SELECT FOR UPDATE | others wait | high contention | risk deadlock
@OneToOne-          FK in one table | mappedBy on inverse | default EAGER override LAZY
@Modifying-         required for UPDATE/DELETE @Query | must use with @Transactional | returns int rows affected
clearAutomatically- prevents stale data after bulk update | flushAutomatically=flush before query
nativeQuery=true-   table/column names | DB specific functions | complex joins | bulk ops faster
@EntityGraph-       attributePaths={"items"} | one query fetch relations | avoids N+1
```

---

## Caching

```
@Cacheable-    cache result on first call | key="#id" | unless="#result==null" | no DB hit next call
@CacheEvict-   remove from cache on update/delete | allEntries=true clears all
@CachePut-     always update cache + DB | no skip on cache hit
@EnableCaching- required on main class
Stale cache-   check @Cacheable log | check TTL | check @CacheEvict missing | Redis multi-instance issue
Stampede fix-  mutex lock | staggered TTL | background refresh scheduler
```

---

## Testing

```
@SpringBootTest-   full context | all beans | slow | end-to-end | needs real DB or @MockBean
@WebMvcTest-       web layer only | no service/repo | fast | MockMvc auto | service must be @MockBean
@DataJpaTest-      JPA layer only | H2 default | @Transactional rollback after each test
@JsonTest-         JSON serialization only | JacksonTester
@RestClientTest-   REST clients | MockRestServiceServer
@Mock-             Mockito | no Spring | fast | with @InjectMocks | unit tests
@MockBean-         Spring managed | replaces real bean | slower | with @WebMvcTest | integration tests
```

---

## Security

```
Filter chain-         SecurityContextPersistenceFilter | UsernamePasswordAuthFilter | JwtAuthFilter | ExceptionTranslationFilter | FilterSecurityInterceptor
SecurityContextHolder- stores auth per request ThreadLocal | cleared after request
CORS-                 Cross Origin Resource Sharing | browser blocks different domain | server must allow origins
CSRF-                 Cross Site Request Forgery | attacker tricks logged-in user | disabled in REST JWT
@EnableWebSecurity-   optional Spring Boot 3 | recommended for clarity
@EnableMethodSecurity- needed for @PreAuthorize | @PostAuthorize | @Secured
@PreAuthorize-        method level security | hasRole('ADMIN') | hasAuthority
JWT filter-           extract token | validate | set SecurityContextHolder | call chain.doFilter
```

---

## REST API

```
@PathVariable-    URL path /orders/{id} | mandatory | resource identification
@RequestParam-    query string ?status=PAID | optional | defaultValue | filtering+pagination
PUT-              replace entire object | idempotent | missing fields=null | send ALL fields
PATCH-            partial update | only changed fields | not guaranteed idempotent | Map<String,Object>
@RequestBody-     JSON → Java object | @Valid validation | POST/PUT/PATCH
@ResponseBody-    Java → JSON response | @RestController = @Controller + @ResponseBody
ResponseEntity-   full control status+headers+body | 201 created(uri) | 204 noContent | 202 accepted
@ControllerAdvice- global exception handler | all controllers | limit by package or class
@RestControllerAdvice- @ControllerAdvice + @ResponseBody | returns JSON | recommended REST
@ExceptionHandler- maps specific exception | custom status + error DTO
```

---

## Async & Scheduling

```
@Async-           separate thread | caller not blocked | @EnableAsync required | public method | different class only
@Scheduled-       fixedRate=every N ms | fixedDelay=N ms after done | cron=specific time | single thread default
Together-         @Scheduled + @Async = each task runs parallel thread | ThreadPoolTaskExecutor
@Retryable-       retry on exception | maxAttempts | @Backoff delay | exponential multiplier | jitter random | @Recover fallback
Backoff types-    fixed=2s,2s,2s | exponential=1s,2s,4s(multiplier=2) | random=1-3s jitter
```

---

## Repository Hierarchy

```
CrudRepository-               save | findById | findAll | delete | count | existsById
PagingAndSortingRepository-   adds: findAll(Pageable) | findAll(Sort)
JpaRepository-                adds: saveAll | deleteAllInBatch | getReferenceById | flush | saveAndFlush | recommended
Pagination-                   PageRequest.of(page,size,sort) | Page has: content+totalElements+totalPages+number
Page vs Slice-                Page=count query slower | Slice=no count faster | Slice for infinite scroll
@Specification-               dynamic filters | null=ignored | combine with .where().and() | JpaSpecificationExecutor
```

---

## Patterns

```
Circuit Breaker-  CLOSED(normal)→OPEN(threshold exceeded, fallback)→HALF_OPEN(test calls)→CLOSED
States-           failureRateThreshold=50% | waitDurationInOpenState=60s | permittedCallsInHalfOpen=5
Saga-             choreography=events distributed loosely coupled | orchestration=centralized Temporal/StepFunctions
Outbox-           save data + event SAME @Transactional | scheduler publishes pending | consistency guaranteed
CQRS-             command=write to writer | query=readOnly=true to reader | separate scale
Saga consistency- idempotency check | outbox pattern | @RetryableTopic | DLT | compensation on failure
@RetryableTopic-  auto creates retry topics + DLT | backoff | @DltHandler for poison messages
```

---

## Actuator & Monitoring

```
Endpoints-        /health | /metrics | /loggers | /threaddump | /heapdump | /circuitbreakers | /refresh | /mappings
Production use-   /health=ECS health check | /metrics=Prometheus+Grafana | /loggers=debug without restart
Change log level- POST /actuator/loggers/com.kiyan.order {configuredLevel: DEBUG} | no restart needed
HikariCP-         connection pool | min-idle | max-pool-size | connection-timeout | leak-detection-threshold
```

---

## DevTools & Misc

```
DevTools-         auto restart on code change | LiveReload browser | disables template cache | not in prod
@EnableAutoConfig- Spring Boot 3=AutoConfiguration.imports | Spring Boot 2=spring.factories
Service discovery- Eureka: register on startup | heartbeat 30s | query for instance | client-side
API Gateway-      single entry | routing | JWT auth | rate limit | throttling 429 | transform | WAF | versioning
ECS vs K8s-       ECS=AWS only simple | K8s=multi-cloud complex powerful industry standard
```

---

## Quick Reference

```
Constructor injection-  always recommended
@Transactional self-    separate class fix
readOnly=true-          read replica + no dirty check
@Primary-               default | @Qualifier=specific override
save() INSERT-          null ID=INSERT | has ID=UPDATE
saveAndFlush()-         SQL now + ID available immediately
N+1 fix-               JOIN FETCH or @EntityGraph
LAZY default-          @OneToMany @ManyToMany
EAGER override-        @ManyToOne @OneToOne → force LAZY
Circuit Breaker-        CLOSED→OPEN→HALF_OPEN
Saga choreography-      events distributed loosely coupled
Outbox-                 same @Transactional data+event
@RetryableTopic-        auto retry + DLT + @DltHandler
@Cacheable-             cache first call | @CacheEvict on update
@Async-                 public method | different class | @EnableAsync
Page vs Slice-          Page=count query | Slice=faster no count
@Specification-         dynamic null-safe filters combined
@ConditionalOnProperty- feature flags via config
@Retryable-             maxAttempts + backoff + @Recover fallback
```

