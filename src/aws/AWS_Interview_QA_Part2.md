# AWS — Interview Q&A (Part 2)
> 5 Questions with Correct Answers & Code Snippets

---

## Q1. What is AWS Secrets Manager? How used in Spring Boot?

### Answer
```
Secrets Manager:
→ stores secrets — DB passwords, API keys ✅
→ no hardcoded secrets in code ✅
→ auto rotation of secrets ✅
→ encryption at rest (KMS) ✅ -> Key Management Service - nobody can read.
→ audit trail (CloudTrail) ✅
→ versioning of secrets ✅
→ IAM role based access — no access keys needed ✅

Three ways to use in Spring Boot:
1. Task definition secrets → ECS injects as env vars ✅
2. spring.config.import   → auto inject into properties ✅
3. SDK manual fetch       → runtime dynamic secret ✅

Best practices:
→ never hardcode secrets in code or yml ✅
→ use IAM role on ECS task ✅
→ enable auto rotation for DB passwords ✅
→ use /prod/service/secret naming convention ✅
→ separate secrets per environment ✅
→ never commit secrets to Git ❌
→ never log secret values ❌
```

```json
// Way 1 — taskDefinition.json ✅
{
  "secrets": [
    {
      "name": "DB_PASSWORD",
      "valueFrom": "arn:aws:secretsmanager:us-east-1:123:secret:/prod/db-password"
    },
    {
      "name": "JWT_SECRET",
      "valueFrom": "arn:aws:secretsmanager:us-east-1:123:secret:/prod/jwt-secret"
    }
  ]
}
```

```yaml
# Way 2 — spring.config.import ✅
spring:
  config:
    import: "aws-secretsmanager:/prod/order-service/db-credentials,
             aws-secretsmanager:/prod/order-service/app-secrets"

# Secrets Manager stores JSON:
# {
#   "DB_HOST":     "aurora.cluster.rds.amazonaws.com",
#   "DB_NAME":     "orderdb",
#   "DB_USERNAME": "admin",
#   "DB_PASSWORD": "secret123"
# }

# injected automatically ✅
spring:
  datasource:
    url:      jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

```java
// Way 3 — SDK manual fetch ✅
@Service
@RequiredArgsConstructor
public class SecretsService {

    private final SecretsManagerClient secretsClient;

    public String getSecret(String secretName) {
        GetSecretValueResponse response =
                secretsClient.getSecretValue(
                    GetSecretValueRequest.builder()
                            .secretId(secretName)
                            .build());
        return response.secretString();
    }

    public Map<String, String> getSecretAsMap(String name) {
        String json = getSecret(name);
        return objectMapper.readValue(json, Map.class);
    }
}

@Configuration
public class SecretsManagerConfig {

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(
                    DefaultCredentialsProvider.create()) // IAM role ✅
                .build();
    }
}
```

| Method | Use when |
|---|---|
| Task definition secrets | ECS deployment ✅ |
| spring.config.import | Spring Boot auto inject ✅ |
| SDK manual fetch | Runtime dynamic secret ✅ |

---

## Q2. What is AWS CloudWatch? How used for monitoring and alerting?

### Answer
```
CloudWatch has three parts:

1. CloudWatch Logs:
→ centralized logs from ECS, Lambda ✅
→ log groups per service ✅
→ log streams per container ✅
→ search with filter patterns + regex ✅
→ retention 1 day to forever ✅

Log Group: /ecs/order-service
    ↓
    ├── Log Stream: container-1 ✅
    │   → 10:00 Order created ORD001
    │   → 10:01 Payment processed
    │
    ├── Log Stream: container-2 ✅
    │   → 10:00 Order created ORD002
    │   → 10:01 DB connection failed
    │
    └── Log Stream: container-3 ✅
        → 10:00 Order created ORD003
        → 10:01 Kafka published

2. CloudWatch Metrics:
→ CPU, memory, request count ✅
→ custom metrics from Spring Boot ✅
→ p50/p95/p99 latency ✅
→ error rate ✅

3. CloudWatch Alarms:
→ threshold on any metric ✅
→ trigger SNS → PagerDuty/email ✅
→ trigger auto scaling ✅
→ composite alarms ✅
```

```json
// taskDefinition.json — log config ✅
{
  "logConfiguration": {
    "logDriver": "awslogs",
    "options": {
      "awslogs-group":         "/ecs/order-service",
      "awslogs-region":        "us-east-1",
      "awslogs-stream-prefix": "ecs"
    }
  }
}
```

```java
// custom metrics via Micrometer ✅
@Service
@RequiredArgsConstructor
public class OrderService {

    private final MeterRegistry meterRegistry;

    public Order createOrder(OrderRequest req) {
        // count orders created ✅
        meterRegistry.counter("orders.created",
                "status", "success").increment();

        // measure processing time ✅
        return meterRegistry.timer("orders.processing.time")
                .record(() -> processOrder(req));
    }
}
```

```yaml
# push metrics to CloudWatch ✅
management:
  metrics:
    export:
      cloudwatch:
        namespace: OrderService
        step: 1m
```

```hcl
# Terraform — CloudWatch alarm + SNS alert ✅
resource "aws_cloudwatch_metric_alarm" "high_error_rate" {
  alarm_name          = "order-service-high-errors"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "5XXError"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Sum"
  threshold           = 10         # > 10 errors/min ✅
  alarm_actions       = [aws_sns_topic.alerts.arn]
}

resource "aws_cloudwatch_metric_alarm" "high_cpu" {
  alarm_name  = "order-service-high-cpu"
  metric_name = "CPUUtilization"
  namespace   = "AWS/ECS"
  threshold   = 80                 # CPU > 80% ✅
  alarm_actions = [aws_sns_topic.alerts.arn]
  dimensions = {
    ClusterName = "production-cluster"
    ServiceName = "order-service"
  }
}

# log metric filter — alert on ERROR logs ✅
resource "aws_cloudwatch_log_metric_filter" "error_filter" {
  name           = "order-service-errors"
  log_group_name = "/ecs/order-service"
  pattern        = "[timestamp, level=ERROR, ...]"

  metric_transformation {
    name      = "ErrorCount"
    namespace = "OrderService"
    value     = "1"
  }
}
```

| Feature | Purpose |
|---|---|
| **Logs** | Centralized logging + search ✅ |
| **Metrics** | CPU, memory, custom metrics ✅ |
| **Alarms** | Threshold → SNS → PagerDuty ✅ |
| **Log Insights** | SQL-like query on logs ✅ |
| **Dashboards** | Visual metrics + logs ✅ |
| **Log retention** | 1 day to forever ✅ |

---

## Q3. What is AWS API Gateway? How does it differ from ALB?

### Answer
```
API Gateway:
→ single entry point for all clients ✅
→ request routing ✅
→ authentication (JWT/Cognito/API Key) ✅
→ rate limiting + usage plans ✅
→ request/response transformation ✅
→ HTTPS termination ✅
→ WAF integration ✅
→ API versioning ✅
→ more expensive ⚠️

ALB (Load Balancer):
→ Layer 7 routing ✅
→ simple path/host routing ✅
→ no auth or rate limiting ❌
→ cheaper ✅
→ faster (less overhead) ✅
→ use for internal service routing ✅

Best practice — use both:
API Gateway (public) → ALB (internal) → ECS ✅
```

```hcl
# Terraform — API Gateway ✅

resource "aws_api_gateway_rest_api" "order_api" {
  name = "order-service-api"
}

resource "aws_api_gateway_resource" "orders" {
  rest_api_id = aws_api_gateway_rest_api.order_api.id
  parent_id   = aws_api_gateway_rest_api.order_api.root_resource_id
  path_part   = "orders"
}

resource "aws_api_gateway_method" "get_orders" {
  rest_api_id      = aws_api_gateway_rest_api.order_api.id
  resource_id      = aws_api_gateway_resource.orders.id
  http_method      = "GET"
  authorization    = "COGNITO_USER_POOLS"  # auth ✅
  authorizer_id    = aws_api_gateway_authorizer.cognito.id
  api_key_required = true                  # API key ✅
}

# integration → ALB → ECS ✅
resource "aws_api_gateway_integration" "orders" {
  rest_api_id             = aws_api_gateway_rest_api.order_api.id
  resource_id             = aws_api_gateway_resource.orders.id
  http_method             = "GET"
  type                    = "HTTP_PROXY"
  integration_http_method = "GET"
  uri = "http://${aws_lb.main.dns_name}/api/orders"
}

# throttling ✅
resource "aws_api_gateway_stage" "prod" {
  stage_name = "prod"
  default_route_settings {
    throttling_rate_limit  = 1000
    throttling_burst_limit = 2000
  }
}

# usage plan per client ✅
resource "aws_api_gateway_usage_plan" "basic" {
  throttle_settings {
    rate_limit  = 100   # 100 req/sec ✅
    burst_limit = 200
  }
  quota_settings {
    limit  = 10000      # 10000 req/day ✅
    period = "DAY"
  }
}

# Cognito authorizer ✅
resource "aws_api_gateway_authorizer" "cognito" {
  name          = "cognito-authorizer"
  type          = "COGNITO_USER_POOLS"
  provider_arns = [aws_cognito_user_pool.main.arn]
}
```

```
Complete flow:
Client
  ↓ HTTPS
API Gateway
  → validate JWT (Cognito) ✅
  → check API key ✅
  → rate limit ✅
  → WAF rules ✅
  ↓ HTTP
ALB
  → health check ✅
  → round robin ✅
  ↓
ECS Tasks (Spring Boot) ✅
```

| | API Gateway | ALB |
|---|---|---|
| **Auth** | ✅ JWT/Cognito/API Key | ❌ No |
| **Rate limiting** | ✅ Yes | ❌ No |
| **Transformation** | ✅ Yes | ❌ No |
| **WAF** | ✅ Yes | ✅ Yes |
| **Cost** | Higher ⚠️ | Lower ✅ |
| **Use for** | Public APIs | Internal routing ✅ |

---

## Q4. What is AWS DynamoDB? When to use over Aurora? Spring Boot integration?

### Answer
```
DynamoDB:
→ NoSQL — document + key-value ✅
→ fully managed + auto scalable ✅
→ pay per request ✅
→ millisecond latency ✅
→ highly available ✅

Key concepts:
→ Partition Key (PK) → distributes data ✅
→ Sort Key (SK)      → range within partition ✅
→ PK alone           → simple primary key ✅
→ PK + SK            → composite primary key ✅

Indexes:
→ GSI (Global Secondary Index):
   → different PK from table ✅
   → create at ANY time ✅
   → max 20 per table ✅

→ LSI (Local Secondary Index):
   → same PK, different SK ✅
   → must define AT CREATION TIME ✅
   → max 5 per table ✅

When to use DynamoDB over Aurora:
→ simple key-value lookups ✅
→ high throughput needed ✅
→ no complex joins ✅
→ auto scaling required ✅
→ sessions, carts, caching ✅

When to use Aurora over DynamoDB:
→ complex queries + joins ✅
→ ACID transactions needed ✅
→ reporting/analytics ✅
→ relational data ✅
```

```java
// Entity ✅
@DynamoDbBean
@Data
@NoArgsConstructor
@Builder
public class OrderSession {

    private String sessionId;   // Partition Key ✅
    private String customerId;  // Sort Key ✅
    private String status;
    private Map<String, String> cart;
    private Long ttl;           // auto delete ✅
}

// Config ✅
@Configuration
public class DynamoDbConfig {

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(
                    DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(
            DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }
}

// Repository ✅
@Repository
public class OrderSessionRepository {

    private final DynamoDbTable<OrderSession> table;

    public OrderSessionRepository(DynamoDbEnhancedClient client) {
        this.table = client.table("OrderSessions",
                TableSchema.fromBean(OrderSession.class));
    }

    // PUT ✅
    public void save(OrderSession session) {
        table.putItem(session);
    }

    // GET ✅
    public Optional<OrderSession> findById(
            String sessionId, String customerId) {
        Key key = Key.builder()
                .partitionValue(sessionId)
                .sortValue(customerId)
                .build();
        return Optional.ofNullable(table.getItem(key));
    }

    // QUERY — all items for PK ✅
    public List<OrderSession> findBySessionId(String sessionId) {
        QueryConditional query = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(sessionId).build());
        return table.query(query).items().stream().toList();
    }

    // DELETE ✅
    public void delete(String sessionId, String customerId) {
        table.deleteItem(Key.builder()
                .partitionValue(sessionId)
                .sortValue(customerId)
                .build());
    }
}
```

```hcl
# Terraform — DynamoDB table ✅
resource "aws_dynamodb_table" "order_sessions" {
  name         = "OrderSessions"
  billing_mode = "PAY_PER_REQUEST"  # auto scale ✅
  hash_key     = "sessionId"        # PK ✅
  range_key    = "customerId"       # SK ✅

  attribute { name = "sessionId";  type = "S" }
  attribute { name = "customerId"; type = "S" }
  attribute { name = "status";     type = "S" }

  # GSI — query by status ✅
  global_secondary_index {
    name            = "status-index"
    hash_key        = "status"
    projection_type = "ALL"
  }

  # TTL ✅
  ttl {
    attribute_name = "ttl"
    enabled        = true
  }
}
```

| | DynamoDB | Aurora |
|---|---|---|
| **Type** | NoSQL key-value | Relational SQL |
| **Scaling** | ✅ Auto | Manual replicas |
| **Joins** | ❌ No | ✅ Yes |
| **ACID** | Limited | ✅ Full |
| **Latency** | ✅ Single ms | Low |
| **Use for** | Sessions, carts | Complex queries |

---

## Q5. What is AWS CodePipeline? Walk through complete CI/CD pipeline?

### Answer
```
CodePipeline:
→ automates complete deployment process ✅
→ triggered on code push ✅
→ Source → Build → Approve → Deploy ✅

CodePipeline stages:
1. Source   → GitHub/CodeCommit ✅
2. Build    → CodeBuild (buildspec.yml) ✅
             → unit tests, integration tests, SonarQube ✅
             → build JAR, Docker image, push to ECR ✅
3. Approve  → manual approval for prod ✅
4. Deploy   → ECS rolling deployment ✅

buildspec.yml phases:
→ pre_build  → ECR login, run tests, SonarQube ✅
→ build      → mvn package, docker build, docker push ✅
→ post_build → update taskDefinition, imagedefinitions ✅

Two key files:
→ imagedefinitions.json → only image URI → uses existing task def ✅
→ taskDefinition.json   → full config (CPU, memory, env) ✅
```

```yaml
# buildspec.yml — complete ✅
version: 0.2

env:
  secrets-manager:
    DB_PASSWORD: /prod/order-service/db:DB_PASSWORD
    SONAR_TOKEN: /prod/sonar:token

phases:
  install:
    runtime-versions:
      java: corretto21

  pre_build:
    commands:
      - aws ecr get-login-password --region $AWS_REGION |
        docker login --username AWS --password-stdin $ECR_REPO
      - mvn test                        # unit tests ✅
      - mvn sonar:sonar
        -Dsonar.host.url=$SONAR_URL
        -Dsonar.login=$SONAR_TOKEN      # SonarQube ✅

  build:
    commands:
      - mvn clean package -DskipTests   # build JAR ✅
      - IMAGE_TAG=$CODEBUILD_RESOLVED_SOURCE_VERSION
      - docker build -t $ECR_REPO:$IMAGE_TAG .
      - docker push $ECR_REPO:$IMAGE_TAG
      - docker tag $ECR_REPO:$IMAGE_TAG $ECR_REPO:latest
      - docker push $ECR_REPO:latest    # push to ECR ✅

  post_build:
    commands:
      - sed -i "s|IMAGE_PLACEHOLDER|$ECR_REPO:$IMAGE_TAG|g"
        taskDefinition.json
      - printf '[{"name":"order-service","imageUri":"%s"}]'
        $ECR_REPO:$IMAGE_TAG > imagedefinitions.json ✅

artifacts:
  files:
    - imagedefinitions.json
    - taskDefinition.json
```

```hcl
# Terraform — complete CodePipeline ✅
resource "aws_codepipeline" "order_pipeline" {
  name     = "order-service-pipeline"
  role_arn = aws_iam_role.codepipeline.arn

  artifact_store {
    location = aws_s3_bucket.artifacts.bucket
    type     = "S3"
  }

  # Stage 1 — Source ✅
  stage {
    name = "Source"
    action {
      name             = "Source"
      category         = "Source"
      owner            = "AWS"
      provider         = "CodeStarSourceConnection"
      version          = "1"
      output_artifacts = ["source_output"]
      configuration = {
        ConnectionArn    = var.github_connection_arn
        FullRepositoryId = "myorg/order-service"
        BranchName       = "main"
      }
    }
  }

  # Stage 2 — Build ✅
  stage {
    name = "Build"
    action {
      name             = "Build"
      category         = "Build"
      owner            = "AWS"
      provider         = "CodeBuild"
      version          = "1"
      input_artifacts  = ["source_output"]
      output_artifacts = ["build_output"]
      configuration = {
        ProjectName = aws_codebuild_project.order.name
      }
    }
  }

  # Stage 3 — Manual Approval ✅
  stage {
    name = "Approve"
    action {
      name     = "ManualApproval"
      category = "Approval"
      owner    = "AWS"
      provider = "Manual"
      version  = "1"
      configuration = {
        NotificationArn = aws_sns_topic.approvals.arn
        CustomData      = "Please review and approve"
      }
    }
  }

  # Stage 4 — Deploy to ECS ✅
  stage {
    name = "Deploy"
    action {
      name            = "Deploy"
      category        = "Deploy"
      owner           = "AWS"
      provider        = "ECS"
      version         = "1"
      input_artifacts = ["build_output"]
      configuration = {
        ClusterName = aws_ecs_cluster.main.name
        ServiceName = aws_ecs_service.order_service.name
        FileName    = "imagedefinitions.json" # ✅
      }
    }
  }
}
```

```
Complete flow:

Developer pushes to GitHub (main)
         ↓
CodePipeline triggered automatically ✅
         ↓
Stage 1 — Source
→ pull latest code ✅
         ↓
Stage 2 — Build (CodeBuild)
pre_build:  login ECR + mvn test + SonarQube ✅
build:      mvn package + docker build + ECR push ✅
post_build: taskDefinition.json + imagedefinitions.json ✅
         ↓
Stage 3 — Manual Approval
→ notify team via SNS ✅
→ reviewer approves ✅
         ↓
Stage 4 — Deploy to ECS
→ rolling deployment ✅
→ new tasks start with new image ✅
→ health checks pass ✅
→ old tasks terminated ✅
→ circuit breaker → auto rollback on failure ✅
```

| Stage | Tool | Purpose |
|---|---|---|
| **Source** | GitHub + CodeStar | Pull code ✅ |
| **Build** | CodeBuild | Test + JAR + Docker + ECR ✅ |
| **Approve** | Manual | Production gate ✅ |
| **Deploy** | ECS | Rolling zero downtime ✅ |

---

## Quick Reference — All 5 Key Points

| Topic | Key Point |
|---|---|
| Secrets Manager | task def ARN + spring.config.import + SDK ✅ |
| Secrets naming | /prod/service/secret convention ✅ |
| Secrets rotation | Auto rotation for DB passwords ✅ |
| CloudWatch Logs | Centralized + search + regex ✅ |
| CloudWatch Metrics | Custom via Micrometer → CloudWatch ✅ |
| CloudWatch Alarms | Threshold → SNS → PagerDuty ✅ |
| API Gateway | Public APIs — auth + rate limit + WAF ✅ |
| ALB | Internal routing — cheaper + faster ✅ |
| API Gateway + ALB | Gateway → ALB → ECS (best practice) ✅ |
| DynamoDB PK + SK | Partition Key + Sort Key ✅ |
| DynamoDB GSI | Any time. max 20. different PK ✅ |
| DynamoDB LSI | At creation only. max 5. same PK ✅ |
| DynamoDB vs Aurora | DynamoDB = sessions/carts. Aurora = complex queries ✅ |
| CodePipeline stages | Source → Build → Approve → Deploy ✅ |
| buildspec phases | pre_build → build → post_build ✅ |
| imagedefinitions.json | Only image URI → uses existing task def ✅ |
| Auto rollback | ECS circuit breaker on health check fail ✅ |
