# AWS — Interview Q&A
> 10 Questions with Correct Answers & Code Snippets

---

## Q1. Difference between EC2 and ECS? When to choose Fargate?

### Answer
```
EC2:
→ virtual machine ✅
→ you manage everything ✅
→ OS patching ✅
→ scaling manual ✅
→ pay for instance always ✅
→ GPU support ✅
→ ML, image processing ✅
→ IAM on instance level ✅
→ predictable traffic ✅

ECS on Fargate:
→ serverless containers ✅
→ AWS manages infra ✅
→ no patching ✅
→ auto scales ✅
→ pay per task only ✅
→ per task IAM role ✅
→ minimal privileges ✅
→ microservices ✅

Choose Fargate:
→ microservices ✅
→ variable traffic ✅
→ no ops overhead ✅
→ fast deployment ✅

Choose EC2:
→ GPU/ML workloads ✅
→ predictable traffic ✅
→ cost optimization ✅
→ custom OS needed ✅
```

| | EC2 | ECS Fargate |
|---|---|---|
| **Management** | Self managed ❌ | AWS managed ✅ |
| **Scaling** | Manual | Auto ✅ |
| **Billing** | Per instance | Per task ✅ |
| **IAM** | Instance level | Per task ✅ |
| **GPU** | ✅ Yes | ❌ No |
| **Use for** | ML/Heavy compute | Microservices ✅ |

---

## Q2. Difference between SQS, SNS and EventBridge?

### Answer
```
SQS (Simple Queue Service):
→ P2P queue ✅
→ pull based ✅
→ message persisted 14 days ✅
→ retry + DLQ ✅
→ Standard = max throughput ✅
→ Standard = ordering not guaranteed ✅
→ Standard = duplicate possible ✅
→ Standard = cheaper ✅
→ FIFO = ordering guaranteed ✅
→ FIFO = no duplicate ✅
→ FIFO = financial transactions ✅

SNS (Simple Notification Service):
→ pub/sub ✅
→ push based ✅
→ fan-out ✅
→ no retention ❌
→ subscribers: SQS, Lambda,
  HTTP, email, SMS ✅

EventBridge:
→ event bus ✅
→ routing rules ✅
→ AWS service events ✅
→ scheduled events (cron) ✅
→ SaaS integrations ✅
→ moderate throughput ✅
```

### Fan-out pattern
```
Order placed
    ↓
SNS topic
    ↓
SQS-payment   → Payment Service ✅
SQS-inventory → Inventory Service ✅
SQS-notify    → Notification Service ✅
```

| | SQS | SNS | EventBridge |
|---|---|---|---|
| **Pattern** | P2P ✅ | Pub/Sub ✅ | Event routing ✅ |
| **Direction** | Pull | Push ✅ | Push ✅ |
| **Retention** | 14 days ✅ | None ❌ | None ❌ |
| **Use for** | Task queue | Fan-out | AWS events/cron ✅ |

---

## Q3. What is AWS Lambda? Triggers and Cold Start?

### Answer
```
Lambda:
→ serverless computing ✅
→ AWS manages servers ✅
→ max 15 minutes timeout ✅
→ max RAM 10GB ✅
→ temp storage 10GB ✅
→ env variables 4KB ✅
→ 1000 concurrent default ✅
→ pay per invocation ✅

Triggers used:
→ API Gateway ✅
→ SQS ✅
→ S3 ✅
→ EventBridge (cron) ✅
→ Kinesis ✅
→ SNS ✅
→ DynamoDB streams ✅

Cold Start:
→ no recent invocation ✅
→ new instance needed ✅
→ JVM startup slow ❌
→ 2-5 seconds delay ❌

Cold Start fixes:
→ Provisioned concurrency ✅
→ SnapStart (JVM snapshot) ✅
→ Thin JAR ✅
→ Lambda Layers ✅
→ EventBridge ping ✅
→ GraalVM native image ✅
```

```java
// Lambda — SQS trigger ✅
public class OrderHandler
        implements RequestHandler<SQSEvent, Void> {
    @Override
    public Void handleRequest(
            SQSEvent event, Context context) {
        event.getRecords().forEach(record ->
            processOrder(fromJson(record.getBody())));
        return null;
    }
}

// Lambda — EventBridge cron ✅
// currency rates every 4 hours
public class CurrencyHandler
        implements RequestHandler<ScheduledEvent, Void> {
    @Override
    public Void handleRequest(
            ScheduledEvent event, Context context) {
        currencyService.fetchAndStore();
        return null;
    }
}
```

```hcl
# Terraform — Lambda with SnapStart ✅
resource "aws_lambda_function" "order" {
  function_name = "order-processor"
  runtime       = "java21"
  handler       = "com.kiyan.OrderHandler"
  timeout       = 300
  memory_size   = 1024

  snap_start {
    apply_on = "PublishedVersions" # ✅
  }
}

# Provisioned concurrency ✅
resource "aws_lambda_provisioned_concurrency_config" "order" {
  provisioned_concurrent_executions = 5
}

# EventBridge cron ✅
resource "aws_cloudwatch_event_rule" "currency" {
  schedule_expression = "rate(4 hours)"
}
```

| Trigger | Use case |
|---|---|
| API Gateway | REST endpoint ✅ |
| SQS | Queue processing ✅ |
| S3 | File processing ✅ |
| EventBridge | Cron jobs ✅ |
| Kinesis | Streaming ✅ |

| Cold Start Fix | Cost |
|---|---|
| Provisioned concurrency | 💰 Extra |
| SnapStart | ✅ Free |
| Thin JAR | ✅ Free |
| Lambda Layers | ✅ Free |

---

## Q4. What is AWS Aurora? How different from RDS?

### Answer
```
Aurora:
→ AWS managed relational DB ✅
→ separates compute + storage ✅
→ replicas share same storage ✅
→ replication lag < 10ms ✅
→ writer fails → reader promoted ✅
→ failover < 30 seconds ✅
→ priority 0-15 for promotion ✅
→ cross region lag < 1 second ✅
→ cross region failover < 1 minute ✅
→ auto scales storage ✅
→ 128TB max storage ✅
→ 5x faster than MySQL ✅
→ 3x faster than PostgreSQL ✅

RDS:
→ separate storage per instance ✅
→ manual storage ✅
→ slower failover ✅
→ smaller workloads ✅

Endpoints:
→ writer endpoint → primary ✅
→ reader endpoint → replicas ✅
→ readOnly=true → reader ✅
→ AbstractRoutingDataSource ✅
```

```java
// AbstractRoutingDataSource ✅
@Bean
@Primary
public DataSource routingDataSource() {
    AbstractRoutingDataSource routing =
        new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return TransactionSynchronizationManager
                    .isCurrentTransactionReadOnly()
                    ? "reader"  // ✅
                    : "writer"; // ✅
            }
        };
    Map<Object, Object> sources = new HashMap<>();
    sources.put("writer", writerDataSource());
    sources.put("reader", readerDataSource());
    routing.setTargetDataSources(sources);
    routing.setDefaultTargetDataSource(writerDataSource());
    return routing;
}

// readOnly → reader ✅
@Transactional(readOnly = true)
public List<Order> findOrders() {
    return orderRepo.findAll();
}
```

```hcl
# Terraform — Aurora ✅
resource "aws_rds_cluster" "aurora" {
  engine                  = "aurora-postgresql"
  backup_retention_period = 7
  deletion_protection     = true
  storage_encrypted       = true  # KMS ✅
}

resource "aws_rds_cluster_instance" "writer" {
  instance_class = "db.r6g.large"
  promotion_tier = 0  # highest priority ✅
}

resource "aws_rds_cluster_instance" "reader" {
  instance_class = "db.r6g.large"
  promotion_tier = 1
}
```

| | Aurora | RDS |
|---|---|---|
| **Storage** | Shared ✅ | Separate |
| **Speed** | 5x MySQL ✅ | Standard |
| **Failover** | < 30 sec ✅ | Minutes |
| **Scale** | 128TB auto ✅ | Manual |
| **Global** | ✅ Yes | Limited |

---

## Q5. What is AWS CloudWatch? How used for monitoring?

### Answer
```
Three main features:

1. CloudWatch Logs:
→ centralized log storage ✅
→ log groups per service ✅
→ log streams per container ✅
→ Log Insights SQL queries ✅
→ 45 days retention ✅

2. CloudWatch Metrics:
→ CPU, memory, custom ✅
→ JVM metrics via Micrometer ✅
→ HikariCP pool metrics ✅
→ payment count, latency ✅

3. CloudWatch Alarms:
→ threshold → SNS → PagerDuty ✅
→ CPU > 80% ✅
→ 5XX errors > 10/min ✅
→ SQS depth > 100 ✅
```

```java
// Custom metrics ✅
@Service
public class PaymentService {
    private final MeterRegistry registry;

    public AuthResponse authorize(PaymentRequest req) {
        registry.counter("payment.count",
            "status", "initiated").increment(); // ✅
        return registry.timer("payment.latency")
                .record(() -> chaseGateway.authorize(req));
    }
}
```

```yaml
# application.yml ✅
management:
  metrics:
    export:
      cloudwatch:
        namespace: PaymentService
        step: 1m
  endpoints:
    web:
      exposure:
        include: health, metrics, heapdump,
                 threaddump, loggers, prometheus
```

```hcl
# CloudWatch Alarms ✅
resource "aws_cloudwatch_metric_alarm" "cpu" {
  alarm_name          = "payment-cpu-high"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  threshold           = 80        # CPU > 80% ✅
  alarm_actions       = [aws_sns_topic.alerts.arn]
}

resource "aws_cloudwatch_metric_alarm" "errors" {
  alarm_name  = "payment-5xx"
  metric_name = "5XXError"
  namespace   = "AWS/ApplicationELB"
  threshold   = 10                # > 10/min ✅
  alarm_actions = [aws_sns_topic.alerts.arn]
}

resource "aws_cloudwatch_log_group" "payment" {
  name              = "/ecs/payment-service"
  retention_in_days = 45          # ✅
}

resource "aws_sns_topic_subscription" "pagerduty" {
  protocol  = "https"
  endpoint  = var.pagerduty_url   # ✅
}
```

```
# Log Insights queries ✅
fields @timestamp, @message
| filter @message like /ERROR/
| sort @timestamp desc

fields @timestamp
| filter @message like /Exception/
| stats count() by bin(1h)
```

---

## Q6. What is AWS API Gateway? Three types?

### Answer
```
API Gateway:
→ single point of entry ✅
→ routing to services ✅
→ authentication ✅
→ rate limiting ✅
→ caching ✅
→ request/response transformation ✅

Three types:

HTTP API:
→ 70% cheaper than REST ✅
→ faster ✅
→ JWT auth built-in ✅
→ CORS built-in ✅
→ no transformation ❌
→ no caching ❌
→ recommended for most ✅

REST API:
→ transformation ✅
→ caching ✅
→ API keys + usage plans ✅
→ WAF integration ✅
→ HTTPS termination ✅
→ costlier ⚠️
→ enterprise features ✅

WebSocket API:
→ bidirectional ✅
→ persistent connection ✅
→ live chat ✅
→ stock prices ✅
→ order tracking ✅
→ real-time notifications ✅
```

```java
// Lambda Authorizer ✅
public class AuthorizerHandler
        implements RequestHandler<
                APIGatewayCustomAuthorizerRequest,
                APIGatewayCustomAuthorizerResponse> {
    @Override
    public APIGatewayCustomAuthorizerResponse handleRequest(
            APIGatewayCustomAuthorizerRequest req,
            Context context) {
        String token = req.getAuthorizationToken();
        if (jwtService.isValid(token)) {
            return allowPolicy(req.getMethodArn()); // ✅
        }
        return denyPolicy(req.getMethodArn()); // ❌
        // cached 300 seconds ✅
    }
}
```

| | HTTP API | REST API | WebSocket |
|---|---|---|---|
| **Cost** | 70% cheaper ✅ | Higher | Per message |
| **Transformation** | ❌ No | ✅ Yes | ❌ No |
| **Caching** | ❌ No | ✅ Yes | ❌ No |
| **JWT auth** | ✅ Built-in | Lambda | Lambda |
| **WAF** | ❌ No | ✅ Yes | ❌ No |
| **Use for** | Simple APIs ✅ | Enterprise | Real-time ✅ |

---

## Q7. ECS Deployment and Blue-Green?

### Answer
```
ECS deployment via CodePipeline:
→ buildspec.yml phases ✅
→ install → pre_build → build → post_build ✅

install:
→ install Java ✅

pre_build:
→ unit tests ✅
→ SonarQube ✅
→ ECR login ✅

build:
→ mvn clean package ✅
→ docker build ✅

post_build:
→ push to ECR ✅
→ imagedefinitions.json ✅
→ manager approval ✅
→ CodeDeploy deploys ✅

Blue-Green:
→ Blue = current live ✅
→ Green = new version ✅
→ ALB switches traffic ✅
→ instant rollback ✅
→ zero downtime ✅
```

```yaml
# buildspec.yml ✅
version: 0.2
phases:
  install:
    runtime-versions:
      java: corretto21
  pre_build:
    commands:
      - mvn test
      - mvn sonar:sonar
      - aws ecr get-login-password | docker login
  build:
    commands:
      - mvn clean package -DskipTests
      - docker build -t payment-service .
      - docker tag payment-service:latest $ECR_URI:$BUILD_NUM
  post_build:
    commands:
      - docker push $ECR_URI:$BUILD_NUM
      - printf '[{"name":"payment-service","imageUri":"%s"}]'
          $ECR_URI:$BUILD_NUM > imagedefinitions.json
artifacts:
  files:
    - imagedefinitions.json
    - appspec.yaml
    - taskdef.json
```

```
Blue-Green flow:

BEFORE:
Blue (v1.0) ← ALB ← ALL traffic ✅
Green (empty)

DEPLOY:
Blue (v1.0) ← still live ✅
Green (v2.0) ← new deployed ✅

VALIDATE:
→ check logs ✅
→ smoke test ✅

APPROVE → switch:
Green (v2.0) ← ALB ← ALL traffic ✅

ROLLBACK:
→ click switch ✅
→ Blue instantly ✅
→ seconds not minutes ✅
```

| | Rolling | Blue-Green |
|---|---|---|
| **Environments** | One ✅ | Two |
| **Rollback** | Slow ❌ | Instant ✅ |
| **Downtime** | Minimal | Zero ✅ |
| **Use for** | Dev/Stage | Production ✅ |

---

## Q8. AWS Secrets Manager — how used?

### Answer
```
Secrets Manager:
→ stores API keys + passwords ✅
→ encrypted with KMS ✅
→ even AWS cannot read ✅
→ auto rotation ✅
→ new password every 30 days ✅
→ old password valid for days ✅
→ CloudTrail audit ✅
→ who accessed what + when ✅

Integration:
→ task definition secrets ✅
→ Terraform ✅
→ SDK at runtime ✅
→ Spring Boot ${ENV_VAR} ✅
```

```hcl
# Create secret ✅
resource "aws_secretsmanager_secret" "db" {
  name       = "/prod/payment/db-credentials"
  kms_key_id = aws_kms_key.payment.arn  # encrypted ✅
  rotation_rules {
    automatically_after_days = 30       # auto rotate ✅
  }
}

resource "aws_secretsmanager_secret_version" "db" {
  secret_id     = aws_secretsmanager_secret.db.id
  secret_string = jsonencode({
    DB_HOST     = "aurora.cluster.rds.amazonaws.com"
    DB_PASSWORD = var.db_password
    CHASE_KEY   = var.chase_api_key
  })
}

# Inject into ECS task ✅
container_definitions = jsonencode([{
  secrets = [
    {
      name      = "DB_PASSWORD"
      valueFrom = "${aws_secretsmanager_secret.db.arn}:DB_PASSWORD::"
    }
  ]
}])
```

```yaml
# Spring Boot reads automatically ✅
spring:
  datasource:
    password: ${DB_PASSWORD}   # from secret ✅
    url: jdbc:postgresql://${DB_HOST}:5432/paymentdb
```

| | Secrets Manager | SSM Parameter Store |
|---|---|---|
| **Auto rotation** | ✅ Yes | ❌ No |
| **Encryption** | ✅ KMS | ✅ KMS |
| **Cost** | Higher | Cheaper ✅ |
| **Use for** | Passwords ✅ | Config values ✅ |

---

## Q9. AWS IAM — Roles, Policies and usage?

### Answer
```
IAM components:
→ Users ✅
→ Groups ✅
→ Roles ✅
→ Policies ✅

User types:
→ Programmatic = access key + secret ✅
→ Console = username + password ✅
→ Both = programmatic + console ✅
→ IAM Role = temporary, no password ✅

Two ECS roles:

Task Execution Role:
→ used by ECS AGENT ✅
→ pull Docker image from ECR ✅
→ read Secrets Manager ✅
→ write CloudWatch logs ✅
→ infra level ✅

Task Role:
→ used by YOUR APPLICATION ✅
→ access SQS ✅
→ access S3 ✅
→ access DynamoDB ✅
→ minimal privileges ✅
→ per task IAM ✅
```

```hcl
# Task Execution Role ✅
resource "aws_iam_role" "ecs_execution" {
  name = "ecs-execution-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs" {
  role       = aws_iam_role.ecs_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
  # → pull ECR ✅
  # → CloudWatch logs ✅
}

resource "aws_iam_role_policy" "secrets" {
  policy = jsonencode({
    Statement = [{
      Effect   = "Allow"
      Action   = ["secretsmanager:GetSecretValue"]
      Resource = ["arn:aws:secretsmanager:*:*:secret:/prod/payment/*"]
    }]
  })
}

# Task Role — app permissions ✅
resource "aws_iam_role_policy" "app" {
  policy = jsonencode({
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["sqs:SendMessage","sqs:ReceiveMessage","sqs:DeleteMessage"]
        Resource = ["arn:aws:sqs:*:*:payment-*"]
      },
      {
        Effect   = "Allow"
        Action   = ["s3:GetObject","s3:PutObject"]
        Resource = ["arn:aws:s3:::payment-bucket/*"]
      }
    ]
  })
}
```

| Role | Purpose |
|---|---|
| Task Execution Role | ECS infra — ECR + logs + secrets ✅ |
| Task Role | App — SQS, S3, DynamoDB ✅ |

---

## Q10. AWS Auto Scaling — how configured for ECS?

### Answer
```
Auto Scaling:
→ increases/decreases task count ✅
→ min capacity ✅
→ max capacity ✅
→ desired count ✅

Scaling metrics:
→ CPU > 70% → scale out ✅
→ Memory > 80% → scale out ✅
→ ALB > 1000 req/task → scale out ✅
→ SQS depth > 100 → scale out ✅
→ Schedule (cron) → pre-scale ✅

Cooldown:
→ scale out cooldown = 60s ✅
→ scale in cooldown = 300s ✅
→ prevents thrashing ✅
```

```hcl
# Auto Scaling Target ✅
resource "aws_appautoscaling_target" "ecs" {
  max_capacity       = 10
  min_capacity       = 2
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.payment.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

# CPU Scaling ✅
resource "aws_appautoscaling_policy" "cpu" {
  policy_type = "TargetTrackingScaling"
  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value       = 70.0  # CPU > 70% ✅
    scale_in_cooldown  = 300
    scale_out_cooldown = 60
  }
}

# Memory Scaling ✅
resource "aws_appautoscaling_policy" "memory" {
  policy_type = "TargetTrackingScaling"
  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageMemoryUtilization"
    }
    target_value = 80.0        # Memory > 80% ✅
  }
}

# ALB Request Count ✅
resource "aws_appautoscaling_policy" "alb" {
  policy_type = "TargetTrackingScaling"
  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ALBRequestCountPerTarget"
      resource_label         = "${aws_lb.main.arn_suffix}/${aws_lb_target_group.payment.arn_suffix}"
    }
    target_value = 1000        # 1000 req/task ✅
  }
}

# Scheduled — Black Friday ✅
resource "aws_appautoscaling_scheduled_action" "scale_up" {
  name     = "black-friday-scale-up"
  schedule = "cron(0 6 * 11 ? *)"  # Nov 6am ✅
  scalable_target_action {
    min_capacity = 10
    max_capacity = 50
  }
}

resource "aws_appautoscaling_scheduled_action" "scale_down" {
  name     = "black-friday-scale-down"
  schedule = "cron(0 22 * 11 ? *)" # Nov 10pm ✅
  scalable_target_action {
    min_capacity = 2
    max_capacity = 10
  }
}

# SQS depth scaling ✅
resource "aws_cloudwatch_metric_alarm" "sqs" {
  metric_name = "ApproximateNumberOfMessagesVisible"
  namespace   = "AWS/SQS"
  threshold   = 100             # > 100 msgs ✅
  dimensions  = { QueueName = "payment-queue" }
}
```

| Metric | Threshold | Action |
|---|---|---|
| CPU | > 70% | Scale out ✅ |
| Memory | > 80% | Scale out ✅ |
| ALB requests | > 1000/task | Scale out ✅ |
| SQS depth | > 100 msgs | Scale out ✅ |
| Schedule | Cron | Pre-scale ✅ |

---

## Quick Reference — All AWS Key Points

| Service | Key Point |
|---|---|
| EC2 | Virtual machine — self managed ✅ |
| ECS Fargate | Serverless containers — per task IAM ✅ |
| SQS Standard | High throughput — no ordering guarantee ✅ |
| SQS FIFO | Ordering + exactly once — financial ✅ |
| SNS | Fan-out pub/sub ✅ |
| EventBridge | Cron + AWS service events ✅ |
| Lambda | Serverless — 15min max ✅ |
| SnapStart | JVM snapshot — cold start fix ✅ |
| Aurora | Shared storage — 30s failover ✅ |
| Aurora vs RDS | 5x faster — 128TB auto scale ✅ |
| CloudWatch | Logs + Metrics + Alarms ✅ |
| PagerDuty | SNS → on-call alert ✅ |
| HTTP API | 70% cheaper — JWT built-in ✅ |
| REST API | Transform + cache + WAF ✅ |
| WebSocket | Real-time bidirectional ✅ |
| Blue-Green | Instant rollback — zero downtime ✅ |
| Secrets Manager | KMS + auto rotation ✅ |
| SSM | Config values — cheaper ✅ |
| Task Exec Role | ECR + logs + secrets ✅ |
| Task Role | SQS + S3 + DynamoDB ✅ |
| CPU scaling | 70% threshold ✅ |
| Memory scaling | 80% threshold ✅ |
| Scheduled | Black Friday pre-scale ✅ |
