# Architecture & Design — Interview Q&A
> 4 Common Interview Questions with Complete Answers

---

## Q1. How do you develop any application?

### Answer
```
Step 1 — Requirement Understanding:
→ meet with business/manager ✅
→ understand what to build ✅
→ clarify edge cases ✅
→ document in Confluence ✅
→ technical architecture diagram ✅
→ sequence diagrams ✅
→ brainstorm sessions ✅
→ manager sign-off ✅

Step 2 — Design:
→ identify microservices needed ✅
→ define API contracts (OpenAPI 3.0) - API Design First Approach ✅
→ choose DB (SQL vs NoSQL) ✅
→ choose messaging (Kafka vs REST) ✅
→ define data models ✅
→ identify third party integrations ✅

Step 3 — JIRA stories:
→ break into user stories ✅
→ estimate story points ✅
→ sprint planning ✅
→ assign to developers ✅

Step 4 — Development:
→ ask developer approach before coding ✅
→ Spring Boot +  microservices + Java Feature ✅
→ REST APIs for sync communication ✅
→ Kafka for async communication ✅
→ constructor injection ✅
→ proper exception handling ✅
→ logging at right levels ✅
→ follow design patterns ✅

Step 5 — Testing:
→ JUnit + Mockito unit tests ✅
→ Cucumber integration tests ✅
→ code coverage ✅
→ SonarQube analysis ✅ -Defects and code smells

Step 6 — Code Review:
→ check requirements met ✅
→ check performance (N+1, indexes) ✅
→ check security (no hardcoded creds) ✅
→ check design patterns ✅

Step 7 — CI/CD:
→ Jenkins pipeline ✅
→ build → test → sonar → deploy ✅
→ Blue-Green deployment ✅
→ zero downtime ✅

Step 8 — Monitoring:
→ CloudWatch + Splunk alerts ✅
→ PagerDuty on-call ✅
→ health endpoints ✅
→ metrics dashboards ✅
```

### Complete flow
```
Requirements → Design → Stories
→ Development → Testing
→ Code Review → CI/CD
→ UAT → Production → Monitoring ✅
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

## Q2. How do you make your application secure?

### Answer
```
Security layers:

1. Authentication ✅
→ JWT token validation ✅
→ API Gateway rate limiting ✅
→ Spring Security filter chain ✅
→ BCrypt password encoding ✅

2. Authorization ✅
→ @PreAuthorize role based ✅
→ @hasRole('ADMIN') ✅
→ method level security ✅
→ least privilege principle ✅

3. Data Security ✅
→ never store plain passwords ✅
→ BCrypt hashing ✅
→ PCI DSS — card never stored ✅
→ Bluefin tokenization ✅
→ secrets in AWS Secrets Manager ✅
→ no hardcoded credentials ✅
→ KMS encryption ✅

4. Transport Security ✅
→ HTTPS only ✅
→ TLS 1.2+ ✅
→ SSL certificates ✅

5. Input Validation ✅
→ @Valid + @NotNull ✅
→ @Size, @Pattern ✅
→ prevent SQL injection ✅
→ prevent XSS ✅
→ sanitize inputs ✅

6. API Security ✅
→ rate limiting ✅
→ 429 Too Many Requests ✅
→ CORS configuration -> Cross-Origin Resource Sharing - which domains are allowed to make API call ✅.
→ CSRF disabled for JWT -> Cross-Site Request Forgery - Attacker trick uses logged-in user's browser make to make unwanted calls ✅. 
→ no sensitive data in logs ✅
→ no sensitive data in URLs ✅

7. Infrastructure Security ✅
→ IAM roles minimal permissions ✅
→ VPC private subnets ✅
→ security groups ✅
→ WAF on API Gateway ✅
→ secrets rotation ✅

8. Code Security ✅
→ SonarQube scans ✅
→ dependency vulnerability check ✅
→ OWASP guidelines ✅
→ no secrets in Git ✅
```

### Code
```java
// Spring Security ✅
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
                .requestMatchers("/api/auth/**")
                    .permitAll()        // public ✅
                .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")   // admin ✅
                .anyRequest()
                    .authenticated())   // JWT ✅
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter
                        .class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // ✅
    }
}

// Field validation ✅
public record PaymentRequest(
    @NotBlank
    @Size(max = 50)
    String orderId,

    @NotNull
    @Positive
    BigDecimal amount,

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9]{16}$")
    String cardToken
) {}

// No sensitive data in logs ✅
log.info("Processing order: {}",
        order.getOrderId()); // ✅
// NEVER:
log.info("Card: {}", cardNumber); // ❌
```

### Security checklist
```
✅ JWT authentication
✅ Role based authorization
✅ BCrypt password hashing
✅ HTTPS/TLS
✅ Input validation
✅ Rate limiting
✅ No hardcoded secrets
✅ Secrets Manager
✅ IAM least privilege
✅ VPC private subnets
✅ SonarQube scans
✅ No sensitive data in logs
✅ CORS configured
✅ WAF on API Gateway
```

---

## Q3. When do you choose Kafka over REST API?

### Answer
```
Choose Kafka when:
→ async communication needed ✅
→ producer should not wait ✅
→ high throughput ✅
→ millions of messages ✅
→ multiple consumers same event ✅
→ fan-out needed ✅
→ message replay needed ✅
→ event sourcing ✅
→ audit trail ✅
→ decoupling services ✅
→ prevent cascade failures ✅
→ downstream can be slow ✅

Choose REST when:
→ immediate response needed ✅
→ synchronous call ✅
→ simple CRUD ✅
→ client needs instant answer ✅
→ payment authorization ✅
   (need auth code NOW) ✅
→ small payload ✅
→ request-response pattern ✅
```

### Real examples from projects
```
Used Kafka:
→ order created → notify payment
  + inventory + notification ✅
→ shipment event → capture payment ✅
→ OMS → payment team ✅
→ return request → refund ✅
→ Outbox pattern events ✅

Used REST:
→ Bluefin tokenization ✅
   (need VaultID immediately) ✅
→ Chase authorization ✅
   (need auth code now) ✅
→ ACI fraud validation ✅
   (need accept/deny now) ✅
→ GET order details ✅
```

### Decision table
```
Scenario                    Choice
──────────────────────────────────────
Need immediate response   → REST ✅
Can process later         → Kafka ✅
Multiple consumers        → Kafka ✅
Single consumer           → REST/SQS ✅
High throughput           → Kafka ✅
Simple CRUD               → REST ✅
Event replay needed       → Kafka ✅
Audit trail               → Kafka ✅
Tight coupling ok         → REST ✅
Loose coupling needed     → Kafka ✅
```

| | Kafka | REST |
|---|---|---|
| **Communication** | Async ✅ | Sync ✅ |
| **Response** | 202 Accepted | Immediate ✅ |
| **Throughput** | Millions ✅ | Limited |
| **Consumers** | Multiple ✅ | One ✅ |
| **Replay** | ✅ Yes | ❌ No |
| **Coupling** | Loose ✅ | Tight |
| **Use for** | Events | CRUD/Auth ✅ |

---

## Q4. How do you take end-to-end ownership of an API?

### Answer
```
Phase 1 — Design:
→ understand requirements ✅
→ define API contract (OpenAPI 3.0) ✅
→ request/response models ✅
→ error codes + messages ✅
→ authentication method ✅
→ rate limiting rules ✅
→ share with frontend team ✅
→ parallel development ✅

Phase 2 — Development:
→ implement endpoints ✅
→ proper validation ✅
→ exception handling ✅
→ @ControllerAdvice ✅
→ meaningful error messages ✅
→ logging ✅
→ idempotency ✅
→ pagination ✅

Phase 3 — Testing:
→ unit tests — all scenarios ✅
→ happy path ✅
→ error scenarios ✅
→ boundary conditions ✅
→ integration tests ✅
→ Cucumber tests ✅
→ load testing ✅
→ security testing ✅

Phase 4 — Documentation:
→ Swagger UI ✅
→ OpenAPI 3.0 annotations ✅
→ request/response examples ✅
→ error code documentation ✅
→ Confluence page ✅
→ Postman collection ✅

Phase 5 — Deployment:
→ Jenkins CI/CD ✅
→ deploy to dev/stage ✅
→ UAT support ✅
→ performance testing ✅
→ Blue-Green to prod ✅
→ zero downtime ✅

Phase 6 — Production support:
→ CloudWatch metrics ✅
→ Splunk log monitoring ✅
→ PagerDuty alerts ✅
→ first on production calls ✅
→ root cause analysis ✅
→ fix + prevent recurrence ✅

Phase 7 — Versioning:
→ backward compatible changes ✅
→ @deprecated old endpoints ✅
→ /v1 /v2 versioning ✅
→ sunset old versions ✅
```

### Code
```java
// OpenAPI documented API ✅
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment API")
public class PaymentController {

    @Operation(summary = "Process payment")
    @ApiResponses({
        @ApiResponse(responseCode = "202",
            description = "Payment accepted"),
        @ApiResponse(responseCode = "400",
            description = "Invalid request"),
        @ApiResponse(responseCode = "409",
            description = "Duplicate payment")
    })
    @PostMapping
    public ResponseEntity<PaymentResponse>
            processPayment(
                @RequestHeader("X-Idempotency-Key")
                String idempotencyKey,
                @RequestBody @Valid
                PaymentRequest request) {

        // idempotency check ✅
        if (paymentRepo.existsByKey(
                idempotencyKey)) {
            return ResponseEntity.ok(
                paymentRepo.findByKey(
                    idempotencyKey));
        }

        // async via Kafka ✅
        kafkaTemplate.send(
            "payment-events", request);

        return ResponseEntity
                .accepted()
                .body(PaymentResponse
                    .pending(idempotencyKey));
    }
}

// Global exception handler ✅
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
        MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse>
            handleValidation(
                MethodArgumentNotValidException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                    "VALIDATION_ERROR",
                    e.getMessage())); // ✅
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
            handleGeneral(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity
                .internalServerError()
                .body(new ErrorResponse(
                    "INTERNAL_ERROR",
                    "Something went wrong")); // ✅
    }
}
```

### End-to-end ownership checklist
```
Design:
✅ API contract defined
✅ Shared with consumers
✅ Authentication decided
✅ Error codes documented

Development:
✅ Validation added
✅ Exception handling
✅ Idempotency
✅ Logging
✅ Pagination

Testing:
✅ Unit tests
✅ Integration tests
✅ Load tests
✅ Security tests

Deployment:
✅ CI/CD pipeline
✅ Blue-Green
✅ Zero downtime

Production:
✅ Monitoring alerts
✅ On-call support
✅ Root cause analysis
✅ Prevent recurrence
```

---

## Quick Reference — All Key Points

| Question | Key Points |
|---|---|
| How develop app | Requirements → Design → Code → Test → Deploy → Monitor ✅ |
| Make secure | JWT + BCrypt + HTTPS + Validation + Secrets Manager ✅ |
| Kafka vs REST | Async/fan-out/replay → Kafka, Sync/immediate → REST ✅ |
| End-to-end ownership | Design → Code → Test → Deploy → Support ✅ |
| Security layers | Auth + AuthZ + Data + Transport + Input + Infra ✅ |
| Choose Kafka | High throughput + multiple consumers + replay ✅ |
| Choose REST | Immediate response + simple CRUD ✅ |
| API ownership | OpenAPI + idempotency + monitoring + on-call ✅ |

## WAF Protect From 
```
→ SQL Injection ✅
attacker sends: ' OR 1=1 --
WAF blocks ✅

→ XSS (Cross Site Scripting) ✅
attacker sends: <script>alert()</script>
WAF blocks ✅

→ DDoS attacks ✅
thousands of requests ✅
WAF rate limits ✅

→ Bot attacks ✅
automated scraping ✅
WAF detects + blocks ✅

→ IP blocking ✅
known bad IPs blocked ✅

→ Geo blocking ✅
block specific countries ✅
```

```
This is configured using Terraform and attach to API gateway.

WAF = security guard
that reads request content ✅
blocks SQL injection, XSS (cross site script),
bots, DDoS attacks ✅
sits in front of API Gateway ✅
```