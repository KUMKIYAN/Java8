# Banking / Java / AWS — Interview Q&A
> 5 Questions based on Job Description
> Java + AWS + Terraform + Banking + Splunk + JUnit + Cucumber

---

## Q1. Walk through a complex banking/payment system you have built?

### Answer
```
Gap Payment System — 3 years, 25+ Spring Boot microservices ✅

Architecture:
→ event driven microservices on AWS ✅
→ Kafka for async communication ✅
→ Aurora DB for persistence ✅
→ ECS Fargate for deployment ✅
→ PCI DSS compliant ✅

Source systems:
→ ECOM (US/CA), OMS, NGCC, SFCC-JP ✅

Payment flow:
→ ECOM sends card details to Vault Service ✅
→ Vault → Bluefin encryption ✅
→ VaultID + Token Extension returned ✅
→ Raw card NEVER stored — PCI compliant ✅
→ PAS → Bluefin decrypt → Chase Gateway ✅
→ Chase → Card Issuer → auth code ✅
→ auth + expiry + hold amount stored ✅

Payment methods:
→ Credit cards (Visa/MC/Discover) ✅
→ Gift cards (SVS) ✅
→ Apple Pay (Virtual PAN) ✅
→ BNPL — AfterPay + Klarna ✅
→ PayPal via BrainTree ✅

Key features built:
→ Re-Auth scheduler (every 2-3 hours) ✅
→ Account Updater ✅
→ Payment capture on shipment ✅
→ Idempotency — no double charging ✅
→ ACI fraud validation (SOAP/XML) ✅

Monitoring:
→ Splunk + CloudWatch + New Relic alerts ✅
→ on-call support ✅

CI/CD:
→ Jenkins Blue-Green deployment ✅
→ CM ticket for PCI compliance ✅
→ zero downtime deployment ✅
```

### Interview answer
```
"I worked in Gap payment system for 3 years
maintaining 25+ Spring Boot microservices ✅

Payment flow:
ECOM/OMS sends card details →
Vault Service → Bluefin tokenization →
VaultID stored — PCI DSS compliant ✅
raw card NEVER stored ✅

PAS → Bluefin decrypt → Chase Gateway →
Chase → Card Issuer → auth code ✅

Payment methods:
Credit cards, Gift cards, Apple Pay,
AfterPay, Klarna, PayPal ✅

Key features:
Re-Auth scheduler ✅
Account Updater ✅
Idempotency — no double charging ✅
ACI fraud validation — SOAP/XML ✅

Monitoring:
Splunk + CloudWatch + PagerDuty ✅
on-call immediate response ✅

CI/CD:
Jenkins Blue-Green — zero downtime ✅
CM ticket for PCI audit trail ✅"
```

---

## Q2. How have you used Terraform in your projects?

### Answer
```
Terraform = Infrastructure as Code ✅
→ all AWS resources defined in .tf files ✅
→ versioned in Git ✅
→ reviewed via Pull Request ✅
→ same code → dev/stage/prod ✅

Resources provisioned:
ECS:
→ ECS cluster ✅
→ ECS service + task definition ✅
→ auto scaling policies ✅

Networking:
→ VPC + subnets ✅
→ security groups ✅
→ ALB + target groups ✅

Database:
→ Aurora cluster ✅
→ subnet groups ✅

Security:
→ IAM roles + policies ✅
→ Secrets Manager ✅

Monitoring:
→ CloudWatch alarms ✅
→ SNS topics for alerts ✅

Key concepts:
Provider  = AWS/Azure connection ✅
Resource  = what to create ✅
Module    = reusable component ✅
State     = tracks what exists ✅
           stored in S3 + DynamoDB lock ✅
Plan      = show what will change ✅
Apply     = create/update resources ✅
Workspace = separate state per env ✅
```

```hcl
# Provider ✅
provider "aws" {
  region = "us-east-1"
}

# ECS cluster ✅
resource "aws_ecs_cluster" "main" {
  name = "payment-cluster"
}

# Aurora DB ✅
resource "aws_rds_cluster" "aurora" {
  cluster_identifier = "payment-db"
  engine             = "aurora-postgresql"
  database_name      = "paymentdb"
  master_username    = var.db_username
  master_password    = var.db_password
}

# Remote state — S3 + DynamoDB locking ✅
terraform {
  backend "s3" {
    bucket         = "my-terraform-state"
    key            = "payment/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-lock" # ✅
  }
}

# HPA auto scaling ✅
resource "aws_appautoscaling_policy" "cpu" {
  policy_type = "TargetTrackingScaling"
  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type =
        "ECSServiceAverageCPUUtilization"
    }
    target_value = 70.0  # scale at 70% CPU ✅
  }
}
```

### Interview answer
```
"All AWS infrastructure managed via Terraform ✅

Infrastructure as Code:
→ .tf files versioned in Git ✅
→ PR review for infra changes ✅
→ same code → dev/stage/prod
  using workspaces ✅

We provisioned:
ECS cluster + services ✅
Aurora PostgreSQL ✅
ALB + security groups ✅
IAM roles + Secrets Manager ✅
CloudWatch alarms + SNS ✅

Remote state in S3 +
DynamoDB locking for team collaboration ✅

terraform plan  → review changes ✅
terraform apply → create resources ✅

Banking benefit:
→ infra changes auditable in Git ✅
→ no manual AWS console changes ✅
→ compliance and audit trail ✅"
```

---

## Q3. JUnit and Cucumber testing — how used in projects?

### Answer
```
JUnit + Mockito — unit testing:
→ @ExtendWith(MockitoExtension.class) ✅
→ @Mock — mock dependencies ✅
→ @InjectMocks — class under test ✅
→ when().thenReturn() — stub ✅
→ verify() — confirm called ✅
→ assertThat() — assertions ✅

Lifecycle:
→ @BeforeAll  — once before all ✅
→ @BeforeEach — before each test ✅
→ @AfterEach  — cleanup after each ✅
→ @Test       — actual test ✅

TDD approach:
→ write failing test first ✅
→ write code to make it pass ✅
→ refactor ✅

Cucumber — integration testing:
→ Gherkin language ✅
→ Given/When/Then/And ✅
→ .feature files ✅
→ Step Definitions = Java code ✅
→ business readable ✅
→ generates HTML reports ✅
→ non-technical stakeholders validate ✅
```

```java
// JUnit + Mockito ✅
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepo;

    @Mock
    private ChaseGateway chaseGateway;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() { }

    @Test
    void testAuthorizePayment_success() {
        // Arrange
        PaymentRequest request =
            PaymentRequest.builder()
                .vaultId("VAULT-123")
                .amount(BigDecimal.TEN)
                .build();

        when(chaseGateway.authorize(request))
                .thenReturn(
                    AuthResponse.builder()
                        .authCode("AUTH-456")
                        .status("APPROVED")
                        .build());

        // Act
        AuthResponse result =
            paymentService.authorize(request);

        // Assert
        assertThat(result.getStatus())
                .isEqualTo("APPROVED"); ✅
        verify(chaseGateway, times(1))
                .authorize(request); ✅
    }

    @Test
    void testAuthorizePayment_declined() {
        when(chaseGateway.authorize(any()))
                .thenReturn(
                    AuthResponse.builder()
                        .status("DECLINED")
                        .build());

        AuthResponse result =
            paymentService.authorize(request);

        assertThat(result.getStatus())
                .isEqualTo("DECLINED"); ✅
        verify(kafkaTemplate, times(1))
                .send(eq("payment-failed"), any()); ✅
    }

    @Test
    void testAuthorizePayment_exception() {
        when(chaseGateway.authorize(any()))
                .thenThrow(
                    new RuntimeException("Down"));

        assertThrows(RuntimeException.class, () ->
            paymentService.authorize(request)); ✅
    }
}
```

```gherkin
# payment.feature ✅
Feature: Payment Authorization

  Scenario: Successful credit card authorization
    Given a customer with valid card details
    And the order amount is 100 dollars
    When the payment authorization is requested
    Then the authorization should be approved
    And the auth code should be stored in DB

  Scenario: Declined payment
    Given a customer with insufficient balance
    When the payment authorization is requested
    Then the authorization should be declined
    And OMS should be notified via Kafka

  Scenario Outline: Multiple payment methods
    Given a customer pays with <payment_method>
    When the payment is processed
    Then the payment should be <status>

    Examples:
      | payment_method | status   |
      | credit_card    | approved |
      | gift_card      | approved |
      | afterpay       | approved |
```

```java
// Step Definitions ✅
@SpringBootTest
public class PaymentStepDefinitions {

    @Autowired
    private PaymentService paymentService;

    private PaymentRequest request;
    private AuthResponse   response;

    @Given("a customer with valid card details")
    public void customerWithValidCard() {
        request = PaymentRequest.builder()
                .vaultId("VAULT-123")
                .amount(BigDecimal.TEN)
                .build();
    }

    @When("the payment authorization is requested")
    public void requestAuthorization() {
        response = paymentService.authorize(request);
    }

    @Then("the authorization should be approved")
    public void verifyApproved() {
        assertThat(response.getStatus())
                .isEqualTo("APPROVED"); ✅
    }
}
```

### Interview answer
```
"Unit tests using JUnit 5 + Mockito ✅

@Mock for dependencies ✅
@InjectMocks for class under test ✅
when().thenReturn() for stubbing ✅
verify() to confirm method called ✅

TDD:
write failing test → write code →
refactor ✅

Cucumber for integration:
→ Gherkin — Given/When/Then ✅
→ .feature files ✅
→ Step Definitions = Java ✅
→ non-technical readable ✅
→ HTML reports generated ✅
→ used for payment flows:
  authorization, capture, refund ✅"
```

---

## Q4. APM tools — how used Splunk and CloudWatch?

### Answer
```
Tools used:
→ Splunk ✅
→ CloudWatch (Logs + Metrics + Alarms) ✅
→ New Relic ✅
→ Grafana ✅
→ PagerDuty integration ✅

Splunk:
→ centralized log aggregation ✅
→ SPL queries to search logs ✅
→ search by order number, exception ✅
→ 45 days log retention ✅
→ saved searches + dashboards ✅
→ count based alert evaluation ✅
→ PagerDuty → call + email ✅

CloudWatch:
→ ECS container logs ✅
→ Log Insights — SQL like queries ✅
→ custom metrics via Micrometer ✅
→ JVM metrics — heap, threads, GC ✅
→ alarms on CPU > 80% ✅
→ alarms on 5XX errors > 10/min ✅
→ SNS → PagerDuty ✅

Metrics tracked:
→ payment count ✅
→ authorization latency p99 ✅
→ error rate ✅
→ JVM heap usage ✅
→ thread count ✅
→ GC pause time ✅
```

```
Splunk queries used:

// search payment errors ✅
index="payment-service"
sourcetype="spring-boot"
"PaymentDeclinedException"
| timechart count by host

// find specific order ✅
index="payment-service"
orderNumber="ORD-12345"
| table _time, level, message

// error rate last 1 hour ✅
index="payment-service" level=ERROR
| timechart span=5m count as errors

// alert rule ✅
index="payment-service"
"ChaseGateway connection failed"
| stats count as failures
| where failures > 3
→ trigger PagerDuty ✅
```

```java
// Micrometer → CloudWatch ✅
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final MeterRegistry meterRegistry;

    public AuthResponse authorize(
            PaymentRequest request) {

        meterRegistry.counter(
            "payment.authorization.count",
            "status", "initiated").increment(); ✅

        return meterRegistry
            .timer("payment.authorization.latency")
            .record(() ->
                chaseGateway.authorize(request)); ✅
    }
}
```

```yaml
# CloudWatch config ✅
management:
  metrics:
    export:
      cloudwatch:
        namespace: PaymentService
        step: 1m
  endpoints:
    web:
      exposure:
        include: health, metrics,
                 loggers, heapdump,
                 threaddump
```

### Interview answer
```
"Used Splunk + CloudWatch + New Relic
+ Grafana for monitoring ✅

Splunk:
→ centralized log aggregation ✅
→ SPL queries — search by order number,
  exception, payment status ✅
→ 45 days retention ✅
→ alert when pattern appears ✅
   e.g. ChaseGateway timeout > 3 times ✅
→ PagerDuty → call + email ✅

CloudWatch:
→ ECS container logs ✅
→ Log Insights — SQL like queries ✅
→ custom metrics via Micrometer ✅
   payment count, latency, error rate ✅
→ alarms: CPU > 80%, 5XX > 10/min ✅
→ SNS → PagerDuty ✅

JVM monitoring:
→ heap usage ✅
→ thread count ✅
→ GC pause time ✅
→ p99 latency ✅

Banking benefit:
→ immediate alert on payment failures ✅
→ on-call engineer notified in seconds ✅
→ MTTR reduced significantly ✅"
```

---

## Q5. Subject matter expert — requirements gathering and delivery?

### Answer
```
Requirement gathering process:

Step 1 — Understand requirement:
→ meeting with client/manager ✅
→ ask clarifying questions ✅
→ understand business impact ✅
→ understand edge cases ✅

Step 2 — Document in Confluence:
→ requirement summary ✅
→ technical approach ✅
→ architectural diagrams ✅
→ sequence diagrams for flows ✅
→ assumptions and risks ✅
→ out of scope items ✅
→ 5 days brainstorming ✅
→ review with manager ✅
→ debate unclear points ✅

Step 3 — Track in JIRA:
→ break into user stories ✅
→ estimate story points ✅
→ sprint planning (Agile/Scrum) ✅
→ 2 week sprints ✅
→ daily standups ✅
→ track progress ✅

Step 4 — Involve QA early:
→ QA in initial discussions ✅
→ define acceptance criteria ✅
→ write Cucumber test cases ✅
→ support QA during testing ✅

Step 5 — Deliver on time:
→ deliver what I promise ✅
→ escalate blockers early ✅
→ no surprises at deadline ✅

Real example — Account Updater feature:
→ requirement: auto update expiring cards ✅
→ documented flow in Confluence ✅
→ architectural diagram ✅
→ JIRA stories created ✅
→ delivered in 2 sprints ✅
→ zero defects in production ✅

AI tools achievement:
→ legacy migration estimated 45 days ✅
→ completed in 5 days with AI ✅
→ backend in less than 1 day ✅
```

### Interview answer
```
"I have been doing this for 12+ years ✅

Step 1 — Understand:
→ meeting with client/manager ✅
→ clarifying questions ✅
→ understand business impact ✅

Step 2 — Document in Confluence:
→ requirement summary ✅
→ architectural + sequence diagrams ✅
→ assumptions + risks ✅
→ 5 days brainstorming ✅
→ manager sign-off ✅

Step 3 — Track in JIRA:
→ user stories + story points ✅
→ Agile/Scrum — 2 week sprints ✅
→ daily standups ✅

Step 4 — QA early:
→ QA in initial discussions ✅
→ acceptance criteria together ✅
→ Cucumber test cases ✅

Step 5 — Deliver:
→ always deliver on time ✅
→ escalate blockers early ✅

Real example:
Account Updater → Confluence docs →
JIRA stories → 2 sprints → zero defects ✅

AI achievement:
Legacy migration: 45 days estimated →
completed in 5 days ✅"
```

---

## Quick Reference — This JD Key Points

| Requirement | Your Experience |
|---|---|
| Java/J2EE 10+ years | 12+ years ✅ |
| AWS cloud | ECS, Aurora, Lambda, SQS, SNS ✅ |
| Agile environment | Scrum, 2 week sprints, standups ✅ |
| JUnit testing | @Mock, @InjectMocks, TDD ✅ |
| Cucumber testing | Gherkin, feature files, step defs ✅ |
| APM + Splunk | Splunk + CloudWatch + New Relic ✅ |
| JIRA + Confluence | Daily use ✅ |
| Terraform + infra | ECS, Aurora, ALB, IAM via Terraform ✅ |
| Banking domain | Payment system — Gap 3 years ✅ |
| Client interaction | Daily with manager + team ✅ |
| Requirements docs | Confluence + diagrams ✅ |
| On time delivery | Always ✅ |
| Subject matter expert | Payment domain — PCI, auth, capture ✅ |
| Team guidance | Senior — guide junior devs ✅ |
