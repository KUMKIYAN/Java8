# Azure Integration Developer — Interview Q&A
> 20 Questions based on Job Description
> Java + Spring Boot + Azure APIM + Kafka + OAuth + mTLS + Batch + CI/CD

---

## Q1. Azure API Management vs AWS API Gateway?

```
Azure API Management (APIM):
→ full API lifecycle management ✅
→ API versioning (v1, v2) via path/query/header ✅
→ developer portal ✅
→ subscriptions per consumer ✅
→ policy engine (XML based) ✅
→ throttling per subscription ✅
→ monitoring + analytics ✅
→ OAuth + JWT validation ✅

AWS API Gateway:
→ routing requests to Lambda/ECS ✅
→ JWT auth built-in (HTTP API) ✅
→ rate limiting ✅
→ caching ✅
→ three types: HTTP, REST, WebSocket ✅

Key difference:
→ APIM = enterprise API management platform ✅
→ AWS Gateway = routing + auth ✅
→ APIM has developer portal ✅
→ APIM has subscription management ✅
```

| | Azure APIM | AWS API Gateway |
|---|---|---|
| **Versioning** | ✅ Built-in | Manual |
| **Developer portal** | ✅ Yes | ❌ No |
| **Subscriptions** | ✅ Yes | API keys |
| **Policy engine** | ✅ Rich | Limited |
| **Cloud** | Azure ✅ | AWS ✅ |

---

## Q2. OAuth 2.0 vs OpenID Connect vs JWT?

```
OAuth 2.0 = Authorization ✅
→ answers "what can you DO?" ✅
→ gives permission to access resources ✅
→ issues ACCESS TOKEN ✅

OpenID Connect = Authentication ✅
→ answers "WHO are you?" ✅
→ built ON TOP of OAuth 2.0 ✅
→ issues ID TOKEN ✅
→ contains user info: name, email, userId ✅

JWT = Token FORMAT ✅
→ both access token + ID token use JWT ✅
→ Header = algorithm (HS256) ✅
→ Payload = userId, roles, expiry ✅
→ Signature = tamper proof ✅

Simple rule:
→ OAuth 2.0  = authorization (what) ✅
→ OpenID     = authentication (who) ✅
→ JWT        = the token format ✅

Real flow:
→ OpenID authenticates WHO you are ✅
→ OAuth authorizes WHAT you can access ✅
→ JWT issued → client sends on every request ✅
→ API validates JWT ✅
```

### Simple analogy
```
OAuth 2.0    = hotel key card (access) ✅
OpenID Connect = passport (identity) ✅
JWT          = physical card/passport format ✅
```

---

## Q3. SFTP and PGP file integration?

```
SFTP (Secure File Transfer Protocol):
→ encrypted file transfer ✅
→ port 22 ✅
→ username + password or SSH key ✅
→ poll SFTP on schedule ✅
→ download → process → archive → delete ✅

PGP (Pretty Good Privacy):
→ asymmetric encryption ✅
→ same concept as SSL ✅
→ public key = sender encrypts ✅
→ private key = receiver decrypts ✅

Real project (DHL):
→ finance team has our PUBLIC key ✅
→ encrypts file with public key ✅
→ uploads to SFTP ✅
→ our app polls SFTP ✅
→ downloads encrypted file ✅
→ reads PRIVATE key from Secrets Manager ✅
→ decrypts PGP → gets CSV ✅
→ processes CSV ✅
→ archives + deletes ✅

Keys stored:
→ Secrets Manager (NOT properties file) ✅
→ shared with counterparts securely ✅
```

---

## Q4. Spring Batch and ETL?

```
Spring Batch key concepts:
→ Job = entire batch process ✅
→ Step = one unit of work ✅
→ ItemReader = read from CSV/DB/file ✅
→ ItemProcessor = transform/validate ✅
→ ItemWriter = write to DB ✅
→ Chunk = process N records at once ✅

Real project:
→ weekly report = every Friday ✅
→ monthly report = first Monday of month ✅
→ daily report = every day ✅
→ PGP decrypt → CSV ✅
→ ItemReader reads CSV ✅
→ skip lines + comma delimiter ✅
→ ItemProcessor converts to business object ✅
→ ItemWriter saves to DB ✅

Meta tables:
→ BATCH_JOB_INSTANCE ✅
→ BATCH_JOB_EXECUTION ✅
→ BATCH_STEP_EXECUTION ✅
→ tracks start/end/status ✅
→ restart from last checkpoint on failure ✅

Listeners:
→ job level listener ✅
→ step level listener ✅
→ item level listener ✅

Chunk flow:
→ Read 100 records → Process → Write → Commit ✅
→ repeat until file complete ✅
```

---

## Q5. Azure APIM Policies?

```
Policy = XML configuration in APIM ✅
Four sections:
→ inbound  = before request reaches backend ✅
→ backend  = how to call backend ✅
→ outbound = before response to client ✅
→ on-error = error handling ✅

Common policies:
Inbound:
→ validate-jwt = JWT validation ✅
→ rate-limit-by-key = throttling ✅
→ ip-filter = whitelist IPs ✅
→ set-header = add headers ✅
→ rewrite-uri = URL transform ✅

Outbound:
→ set-header = add response headers ✅
→ json-to-xml / xml-to-json ✅
→ remove-header = hide internal headers ✅

Subscriptions:
→ each consumer gets API key ✅
→ throttle per subscription ✅
→ monitor per subscriber ✅
```

---

## Q6. OpenAPI/Swagger Documentation?

```
OpenAPI 3.0 = industry standard ✅
→ renamed from Swagger 2.0 in 2017 ✅
→ YAML or JSON format ✅
→ auto generates Swagger UI ✅
→ available at /swagger-ui.html ✅

Contract first approach (our approach):
→ design API contract BEFORE development ✅
→ share with consumers + PDMs ✅
→ get sign off ✅
→ parallel frontend + backend development ✅
→ frontend mocks from contract ✅
→ integrate after development ✅

Documents:
→ endpoints ✅
→ HTTP methods ✅
→ request body ✅
→ response body ✅
→ error codes ✅
→ auth requirements ✅

Annotations:
→ @OpenAPIDefinition ✅
→ @Operation ✅
→ @ApiResponse ✅
→ @Schema ✅
→ @Tag ✅
```

---

## Q7. Kafka Producers and Consumers?

```
Kafka usage in projects:
→ majority of microservices = Kafka ✅
→ some = producer only ✅
→ some = consumer only ✅
→ some = both producer + consumer ✅

Producer:
→ KafkaTemplate to publish ✅
→ overloaded methods: topic + message ✅
→ topic + key + message ✅
→ ProducerRecord ✅

Consumer:
→ @KafkaListener ✅
→ consumer group assigned ✅
→ concurrency config ✅
→ container factory ✅
→ retry config ✅
→ DLT on max retries ✅

Patterns used:
→ @RetryableTopic = auto retry topics ✅
→ @DltHandler = DLT processing ✅
→ Outbox pattern = consistency ✅
→ Saga choreography ✅

Real example (Gap):
→ OMS publishes order event ✅
→ payment service consumes ✅
→ validates + processes ✅
→ publishes result to EDI ✅

Kafka delay logic (real problem solved):
→ order has multiple events ✅
→ if previous event not acknowledged
  AND within one hour ✅
→ introduce delay ✅
→ sequence maintained ✅
```

---

## Q8. CI/CD Pipeline Experience?

```
Git:
→ GitHub for repositories ✅
→ feature branches ✅
→ PR review + approve ✅
→ merge to main ✅

Jenkins (Expert):
→ Jenkinsfile declarative pipeline ✅
→ shared libraries for 40+ apps ✅
→ HashiCorp Vault for secrets ✅
→ built from scratch ✅

GitHub Actions:
→ YAML based ✅
→ Azure Key Vault secrets ✅
→ migrated from Jenkins ✅

Pipeline stages:
→ install dependencies ✅
→ pre-build: unit tests + SonarQube ✅
→ build: mvn package + Docker image ✅
→ post-build: push to JFrog/ACR ✅
→ deploy dev → approval → stage → approval → prod ✅

Registries:
→ JFrog Artifactory ✅
→ Azure Container Registry ✅

Complete flow:
→ Code push → PR → Merge → Pipeline triggers ✅
→ Test → Build → Push → Deploy → Monitor ✅
```

---

## Q9. Monitoring and Logging?

```
Tools used:
→ Splunk = log search + alerts ✅
→ CloudWatch = AWS metrics + alarms ✅
→ Grafana = visual dashboards ✅
→ New Relic = full APM ✅
→ GCP logging ✅
→ PagerDuty = on-call alerts ✅

Monitoring layers:
1. Logs: Splunk log search ✅
2. Metrics: CloudWatch + Grafana ✅
3. Alerts: threshold → SNS → PagerDuty ✅
4. Tracing: Sleuth + Zipkin ✅
   trace ID across services ✅

Custom metrics:
→ Micrometer + Spring Actuator ✅
→ payment count + latency ✅
→ HikariCP pool metrics ✅
→ Kafka consumer lag ✅

Real example:
→ Chase gateway timeout → alert ✅
→ > 2 exceptions → PagerDuty fires ✅
→ jump on call immediately ✅
→ check CloudWatch logs ✅
→ circuit breaker status ✅
→ fix + monitor ✅
```

---

## Q10. Mutual TLS (mTLS)?

```
TLS vs mTLS:
→ TLS = one way ✅
   server proves identity only ✅
   client = anonymous ✅
→ mTLS = two way ✅
   server proves identity ✅
   client ALSO proves identity ✅
   fraud client rejected ❌

Certificate storage in Java:
→ Keystore = your certificate + private key ✅
→ Truststore = trusted CA certificates ✅

When to use mTLS:
→ microservice to microservice ✅
→ payment gateway calls ✅
→ banking internal systems ✅
→ B2B high security integrations ✅

Real example (Gap):
→ Gap → Chase Gateway mTLS ✅
→ Chase gives Gap client certificate ✅
→ Gap presents on every call ✅
→ Chase validates ✅
→ only Gap can call Chase ✅
→ fraud caller rejected ❌

mTLS flow:
→ Client → sends certificate → Server ✅
→ Server validates client cert ✅
→ Server → sends certificate → Client ✅
→ Client validates server cert ✅
→ both trusted → encrypted connection ✅
```

---

## Q11. Secrets Management?

```
AWS Secrets Manager:
→ store API keys + DB passwords ✅
→ SFTP keys + passwords ✅
→ KMS encrypted ✅
→ auto rotation ✅
→ CloudTrail audit ✅
→ inject into ECS task definition ✅

Azure Key Vault:
→ same concept on Azure ✅
→ secrets, keys, certificates ✅
→ managed identity access ✅
→ used in Azure DevOps pipeline ✅

HashiCorp Vault:
→ cloud agnostic ✅
→ used in Jenkins pipeline ✅

Best practices:
→ NEVER in properties file ❌
→ NEVER in Git ❌
→ NEVER in logs ❌
→ rotate regularly ✅
→ minimal IAM access ✅
→ audit who accessed ✅
→ read via SDK at startup ✅
→ injected as env variables ✅
→ ${DB_PASSWORD} in yml ✅
```

---

## Q12. Error Handling Patterns?

```
Spring Boot error handling layers:

1. @RestControllerAdvice ✅
→ global exception handler ✅
→ @ExceptionHandler per exception ✅
→ consistent error response ✅

2. Custom exceptions ✅
→ OrderNotFoundException → 404 ✅
→ PaymentDeclinedException → 402 ✅
→ DuplicateRequestException → 409 ✅

3. Validation errors ✅
→ @Valid on request body ✅
→ field level error messages ✅

4. Circuit breaker fallback ✅
→ downstream fails → fallback ✅

5. Kafka error handling ✅
→ @RetryableTopic ✅
→ DLT for poison messages ✅

Error response DTO:
→ timestamp ✅
→ status code ✅
→ error message ✅
→ endpoint path ✅
→ HTTP method ✅
→ request body received ✅
→ correlation/trace ID ✅

HTTP status codes:
→ 400 = bad request ✅
→ 401 = unauthorized ✅
→ 403 = forbidden ✅
→ 404 = not found ✅
→ 409 = conflict ✅
→ 429 = too many requests ✅
→ 500 = internal server error ✅
```

---

## Q13. Documentation and Runbooks?

```
Types of documentation:

1. API specs (OpenAPI 3.0) ✅
→ endpoints, methods ✅
→ request/response ✅
→ error codes ✅
→ share before development ✅
→ get sign off ✅

2. Runbooks ✅
→ step by step operational guide ✅
→ how to handle incidents ✅
→ how to restart service ✅
→ how to rollback deployment ✅
→ on-call engineer guide ✅
→ stored in Confluence ✅

3. Architecture docs ✅
→ sequence diagrams ✅
→ component diagrams ✅
→ data flow diagrams ✅

4. Postman collections ✅
→ all endpoints ✅
→ share with QA + frontend ✅

5. README files ✅
→ how to run locally ✅
→ environment setup ✅

Tools:
→ Confluence ✅
→ Swagger UI ✅
→ Postman ✅
→ JIRA ✅
→ GitHub README ✅
```

---

## Q14. Third Party System Integration?

```
Real integrations (Gap payment):
→ Chase payment gateway ✅
→ ACI fraud detection (SOAP/XML) ✅
→ Bluefin tokenization ✅
→ SVS gift cards ✅
→ AfterPay/Klarna BNPL ✅
→ Account Updater ✅
→ Finance team (SFTP + PGP) ✅

Integration tools:
→ RestTemplate (legacy) ✅
→ RestClient (modern) ✅
→ WebClient (reactive) ✅
→ Feign Client (declarative) ✅

Authentication types:
→ HMAC ✅
→ OAuth 2.0 ✅
→ API key ✅
→ mTLS ✅
→ Basic auth ✅

Resilience:
→ Retry (3 attempts) ✅
→ Circuit breaker ✅
→ Timeout configuration ✅
→ Fallback response ✅
→ log + alert on final failure ✅
```

---

## Q15. Cross-functional Team Collaboration?

```
Teams collaborated with:
→ OMS team (upstream) ✅
→ EDI/EDA team (downstream) ✅
→ Infrastructure team ✅
→ Unix/Server team ✅
→ IAM/Security team ✅
→ QA team ✅
→ Product/PDM team ✅
→ Finance team (SFTP files) ✅
→ Frontend team ✅

Collaboration approach:
→ share API contract early ✅
→ get sign off before coding ✅
→ define clear boundaries ✅
→ agree on error codes ✅
→ agree on retry strategy ✅
→ regular sync calls ✅
→ document decisions in Confluence ✅

Real example:
→ IAM team called to migrate
  header → token based auth ✅
→ collaborated + implemented ✅
→ got sign off ✅
```

---

## Q16. JSON and XML?

```
JSON:
→ REST APIs ✅
→ lightweight + human readable ✅
→ Jackson library ✅
→ ObjectMapper ✅
→ @JsonProperty, @JsonIgnore ✅

XML:
→ SOAP services ✅
→ ACI fraud = SOAP/XML ✅
→ legacy enterprise systems ✅
→ JAXB marshalling ✅
→ @XmlRootElement, @XmlElement ✅
→ XSD schema validation ✅

Content negotiation:
→ Accept: application/json ✅
→ Accept: application/xml ✅
→ Spring returns correct format ✅

XML schema (XSD):
→ defines structure ✅
→ validates XML against schema ✅
→ contract between systems ✅
→ no contract violation ✅

Real example:
→ ACI fraud = SOAP/XML ✅
→ build XML → validate XSD ✅
→ send to ACI ✅
→ parse XML response ✅
→ convert to Java object ✅
```

---

## Q17. Rate Limiting vs Throttling?

```
Rate limiting = the RULE ✅
→ max 100 calls per minute ✅
→ defines the limit ✅
→ like speed limit sign ✅

Throttling = ENFORCING the rule ✅
→ blocking when limit exceeded ✅
→ slowing down requests ✅
→ like police enforcing speed limit ✅
→ returns 429 Too Many Requests ✅

Implementation:
1. Resilience4j = single instance ✅
2. Redis = distributed per user ✅
3. API Gateway = per subscription ✅
4. Azure APIM = per subscription ✅
5. Bucket4j = token bucket ✅

Response headers:
→ X-RateLimit-Limit ✅
→ X-RateLimit-Remaining ✅
→ Retry-After ✅
→ 429 status code ✅
```

---

## Q18. Testing Strategy?

```
TDD approach:
→ write failing test first ✅
→ write minimum code to pass ✅
→ refactor ✅
→ repeat ✅
→ 90%+ code coverage ✅

Unit testing:
→ JUnit 5 ✅
→ Mockito ✅
→ @Mock + @InjectMocks ✅
→ @ExtendWith(MockitoExtension) ✅
→ fast — no Spring context ✅
→ test one class at a time ✅

Integration testing:
→ @SpringBootTest ✅
→ @WebMvcTest = web layer only ✅
→ @DataJpaTest = JPA + H2 ✅
→ Testcontainers = real DB ✅
→ MockMvc ✅
→ @MockBean ✅

Cucumber E2E:
→ .feature files ✅
→ Given/When/Then ✅
→ Gherkin language ✅
→ PDM can understand ✅
→ step definitions in Java ✅
→ HTML reports ✅

Code coverage:
→ JaCoCo ✅
→ 90%+ target ✅
→ SonarQube enforces ✅
→ fails build if below ✅
```

---

## Q19. Problem Solving and Ownership?

```
Problem solving approach:
→ understand problem fully ✅
→ reproduce locally ✅
→ brainstorm alternatives ✅
→ document pros/cons ✅
→ choose best solution ✅
→ discuss with team + manager ✅
→ implement + test ✅
→ document solution ✅
→ prevent recurrence ✅

Real examples:
→ Kafka delay logic ✅
   order events out of sequence ❌
   previous event not acked + within 1hr ✅
   introduced delay → sequence fixed ✅

→ OOM fix with @Qualifier ✅
   KafkaTemplate × 5000 objects ❌
   heap dump + Eclipse MAT ✅
   added @Qualifier → singleton ✅

→ DHL infinite loop ✅
   division vs multiplication bug ❌
   code review found it ✅
   fixed → never seen again ✅

→ Chase gateway timeout ✅
   circuit breaker OPEN ✅
   orders held → Chase recovered ✅
   zero data loss ✅

Ownership skills:
→ SME of payment domain ✅
→ first person on production calls ✅
→ built 40+ Jenkins pipelines ✅
→ mentored 4 developers ✅
→ end to end ownership ✅
→ requirements to production ✅
```

---

## Q20. API Key vs JWT vs Certificate Authentication?

```
API Key:
→ simple string token ✅
→ passed in X-API-Key header ✅
→ no expiry by default ✅
→ no user info ✅
→ revoke anytime ✅
→ use for: B2B, server to server ✅
→ Azure APIM subscription key ✅

JWT:
→ stateless ✅
→ contains user info ✅
→ Header = algorithm ✅
→ Payload = username, roles, expiry ✅
→ Signature = tamper proof ✅
→ expires automatically ✅
→ FilterSecurityInterceptor ✅
→ URL pattern permissions ✅
→ role based access ✅
→ use for: user authentication ✅

Certificate (mTLS):
→ strongest security ✅
→ both sides present certificate ✅
→ issued by CA ✅
→ use for: microservice to microservice ✅
→ payment gateways ✅
→ banking internal systems ✅
```

| | API Key | JWT | Certificate |
|---|---|---|---|
| **Complexity** | ✅ Simple | Medium | Complex |
| **User info** | ❌ No | ✅ Yes | ❌ No |
| **Expiry** | Manual | ✅ Auto | CA managed |
| **Security** | Basic | Good ✅ | ✅ Strongest |
| **Use for** | B2B ✅ | User auth ✅ | Microservices ✅ |

---

## Quick Reference — All Key Points

| Requirement | Your Experience |
|---|---|
| Azure APIM | Versioning + policies + subscriptions ✅ |
| OAuth 2.0 | Authorization (what) ✅ |
| OpenID Connect | Authentication (who) ✅ |
| JWT | Token format — header.payload.signature ✅ |
| SFTP + PGP | Finance team file integration ✅ |
| Spring Batch | Weekly/monthly/daily reports ✅ |
| Kafka | 5+ years — producer/consumer/saga ✅ |
| CI/CD | Jenkins 40+ apps + GitHub Actions ✅ |
| Monitoring | Splunk/CloudWatch/Grafana/NewRelic ✅ |
| mTLS | Chase gateway — both certs ✅ |
| Secrets | AWS Secrets Manager + Azure Key Vault ✅ |
| Error handling | @RestControllerAdvice + DLT ✅ |
| Documentation | OpenAPI + Confluence + Runbooks ✅ |
| Third party | Chase/ACI/Bluefin/SFTP ✅ |
| Collaboration | OMS/EDI/IAM/Infra teams ✅ |
| JSON/XML | Jackson/JAXB/XSD validation ✅ |
| Rate limiting | Resilience4j/Redis/APIM ✅ |
| Testing | JUnit/Mockito/Cucumber/TDD ✅ |
| Ownership | SME + first on call + 40+ pipelines ✅ |
| API Key/JWT/Cert | All three — different use cases ✅ |
