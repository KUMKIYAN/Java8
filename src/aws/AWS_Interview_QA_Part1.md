# AWS — Interview Q&A
> 4 Questions with Correct Answers & Code Snippets

---

## Q1. Difference between SQS and SNS? When to use together?

### Answer
```
SQS (Simple Queue Service):
→ queue system — pull based ✅
→ one consumer at a time ✅
→ message deleted after consumed ✅
→ message retention up to 14 days ✅
→ DLQ for failed messages ✅
→ visibility timeout ✅
    Consumer reads message → Message hidden for 30 sec → Processing done → delete message
    What if processing takes longer than 30 sec?
    Visibility timeout expires → message becomes visible again → another consumer picks it up → processed twice!
    Set visibility timeout longer than your processing time
→ at-least-once delivery ✅
→ Standard (unordered) + FIFO (ordered) ✅

SNS (Simple Notification Service):
→ pub/sub — push based ✅
→ multiple subscribers ✅
→ publishes to ALL subscribers simultaneously ✅
→ NO message retention ❌
→ subscribers: SQS, Lambda, Email, SMS, HTTP ✅
→ message filtering per subscriber ✅

Fan-out pattern (both together):
→ one SNS publish → multiple SQS queues ✅
→ order placed → SNS
  → SQS payment service ✅
  → SQS inventory service ✅
  → SQS notification service ✅
```

```java
// publish to SNS → fans out to all SQS ✅
@Service
public class OrderEventPublisher {

    @Value("${aws.sns.order-topic-arn}")
    private String topicArn;

    public void publishOrderPlaced(Order order) {
        snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .message(toJson(order))
                .subject("ORDER_PLACED")
                .messageAttributes(Map.of(
                    "eventType", MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue("ORDER_PLACED")
                            .build()
                ))
                .build()); // all SQS subscribers receive ✅
    }
}

// SQS consumer — receives from SNS fan-out ✅
@SqsListener("${aws.sqs.payment-queue-url}")
public void processPayment(OrderEvent event) {
    paymentService.process(event);
}
```

| | SQS | SNS |
|---|---|---|
| **Model** | Queue (pull) | Pub/Sub (push) |
| **Receivers** | One consumer | Multiple subscribers ✅ |
| **Retention** | 14 days ✅ | None ❌ |
| **DLQ** | ✅ Yes | ✅ On subscription |
| **Types** | Standard + FIFO | Standard + FIFO |
| **Use for** | Task queue | Fan-out, broadcast |

---

## Q2. What is AWS Lambda? Spring Boot integration and limitations?

### Answer
```
Lambda = serverless compute
→ run code without managing servers ✅
→ triggered by events ✅
→ auto scales automatically ✅
→ pay per invocation ✅
→ default 1000 concurrent executions ✅
→ max execution time = 15 minutes ✅

Lambda triggers:
→ API Gateway → HTTP request ✅
→ SQS         → process messages ✅
→ SNS         → notifications ✅
→ S3          → file uploaded ✅
→ EventBridge → scheduled cron ✅
→ DynamoDB    → stream changes ✅
→ Kafka (MSK) → consume events ✅
```

```java
// Option 1 — Spring Cloud Function ✅
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-function-adapter-aws</artifactId>
</dependency>

@SpringBootApplication
public class OrderLambdaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderLambdaApplication.class, args);
    }

    @Bean
    public Function<OrderEvent, OrderResponse> processOrder() {
        return event -> {
            log.info("Processing: {}", event.getOrderId());
            return orderService.process(event); // ✅
        };
    }
}

// Option 2 — implement RequestHandler ✅
public class OrderHandler
        implements RequestHandler<SQSEvent, Void> {

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        event.getRecords().forEach(record -> {
            String body = record.getBody();
            OrderEvent order = fromJson(body, OrderEvent.class);
            orderService.process(order); // ✅
        });
        return null;
    }
}

// S3 trigger — file uploaded ✅
public class S3EventHandler
        implements RequestHandler<S3Event, Void> {

    @Override
    public Void handleRequest(S3Event event, Context context) {
        event.getRecords().forEach(record -> {
            String bucket = record.getS3().getBucket().getName();
            String key    = record.getS3().getObject().getKey();
            processFile(bucket, key); // ✅
        });
        return null;
    }
}
```

### Lambda limitations
```
1. Cold start ❌
   → LC not used recently - stopped -  invocation → JVM starts → 2-5 seconds slow
   → Spring Boot context load → even slower ❌
   → fix: provisioned concurrency ✅
   → fix: SnapStart ✅ - memory snapshot of fully initialized JVM
   → fix: spring-boot-thin-launcher ✅ - Dependencies download separately
   → fix: Lambda Layers ✅ - ZIP of shared libraries - resued across - attached at run time.
   → fix: GraalVM native image ✅
   → fix: Remove Unused Dependencies & Auto Configurations

2. Max execution time = 15 minutes ❌
   → long running tasks not suitable ❌
   → use ECS or Step Functions instead ✅

3. Max memory = 10GB ❌
   → heavy processing not suitable ❌

4. Stateless ❌
   → no in-memory state between invocations
   → use DynamoDB or ElastiCache for state ✅

5. Max payload = 6MB ❌
   → large files → use S3 ✅

6. 1000 concurrent limit ❌
   → high traffic → throttling ❌
   → request limit increase from AWS ✅

7. Spring Boot too heavy ⚠️
   → large JAR → slow cold start ❌
   → use Spring Cloud Function ✅
```

| | Lambda | ECS Fargate |
|---|---|---|
| **Server mgmt** | ✅ None | ✅ None |
| **Cold start** | ❌ Yes | ✅ No |
| **Max runtime** | 15 min ❌ | Unlimited ✅ |
| **Cost** | Per invocation | Per hour |
| **Use for** | Short event driven | Long running ✅ |

---

## Q3. What is AWS Aurora? Difference from RDS? Spring Boot integration?

### Answer
```
Aurora:
→ AWS proprietary — MySQL + PostgreSQL compatible ✅
→ separates compute and storage ✅
→ shared storage (6 copies across 3 AZs) ✅
→ up to 15 read replicas ✅
→ less than 100 millisecond replication ✅
→ failover < 30 seconds ✅
→ auto scales to 128TB ✅
→ pay per usage not fixed size ✅
→ multi-region support ✅
→ 5x faster than MySQL ✅
→ 3x faster than PostgreSQL ✅
→ serverless option ✅

RDS:
→ standard MySQL/PostgreSQL/Oracle/SQL Server
→ each instance has its OWN storage ❌
→ max 5 read replicas ❌
→ storage scaling - manual ❌
→ slower than Aurora ❌
→ cheaper for simple workloads ✅
```

```yaml
# application.yml — writer + reader endpoints
spring:
  datasource:
    writer:
      url: jdbc:postgresql://aurora-cluster.cluster-xxx.rds.amazonaws.com:5432/orderdb
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
    reader:
      url: jdbc:postgresql://aurora-cluster.cluster-ro-xxx.rds.amazonaws.com:5432/orderdb
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
```

```java
// route reads to replica + writes to primary ✅
@Configuration
public class AuroraDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.writer")
    public DataSource writerDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.reader")
    public DataSource readerDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public DataSource routingDataSource() {
        Map<Object, Object> sources = new HashMap<>();
        sources.put("writer", writerDataSource());
        sources.put("reader", readerDataSource());

        AbstractRoutingDataSource routing =
                new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return TransactionSynchronizationManager
                    .isCurrentTransactionReadOnly()
                    ? "reader"  // read → replica ✅
                    : "writer"; // write → primary ✅
            }
        };
        routing.setTargetDataSources(sources);
        routing.setDefaultTargetDataSource(writerDataSource());
        return routing;
    }
}

// readOnly → routed to reader automatically ✅
@Transactional(readOnly = true)
public List<Order> getAllOrders() {
    return orderRepository.findAll(); // → reader ✅
}

@Transactional // → writer ✅
public Order createOrder(OrderRequest req) {
    return orderRepository.save(toEntity(req));
}
```

| | Aurora | RDS |
|---|---|---|
| **Engine** | AWS proprietary | Standard MySQL/PG |
| **Storage** | Shared 6 copies ✅ | Per instance |
| **Read replicas** | 15 ✅ | 5 |
| **Failover** | < 30 seconds ✅ | Minutes |
| **Storage scaling** | Auto ✅ | Manual |
| **Speed** | 5x MySQL ✅ | Standard |
| **Cost** | Higher | Lower |
| **Use for** | Production HA | Dev/simple |

---

## Q4. What is ECS? EC2 vs Fargate? How to deploy Spring Boot?

### Answer
```
ECS = Elastic Container Service
→ run Docker containers on AWS ✅
→ no Kubernetes complexity ✅
→ integrates with ALB, ECR, CloudWatch ✅

ECS concepts:
→ Cluster        = group of tasks ✅
→ Task Definition= blueprint (image, CPU, memory, env) ✅
→ Task           = running container ✅
→ Service        = manages tasks (desired count, scaling) ✅
→ ECR            = Docker image registry ✅

Fargate (serverless):
→ no EC2 to manage ✅
→ AWS manages servers ✅
→ pay per task (CPU + memory) ✅
→ recommended for most workloads ✅
→ dynamic scaling ✅

EC2 launch type:
→ you manage EC2 instances ✅
→ more control ✅
→ cheaper for steady high traffic ✅
→ spot instances for cost saving ✅
→ use when traffic is predictable ✅

Auto scaling:
→ min/max tasks configured ✅
→ scale on CPU utilization ✅
→ scale on request count ✅

Deployment flow:
GitHub → CodePipeline → CodeBuild
→ mvn test → mvn package
→ docker build → push to ECR
→ update ECS service ✅
→ rolling deployment → zero downtime ✅
→ circuit breaker → auto rollback ✅
```

```yaml
# buildspec.yml — full deployment flow ✅
version: 0.2

phases:
  pre_build:
    commands:
      - mvn test
      - aws ecr get-login-password | docker login --username AWS --password-stdin $ECR_REPO

  build:
    commands:
      - mvn clean package -DskipTests
      - IMAGE_TAG=$CODEBUILD_RESOLVED_SOURCE_VERSION
      - docker build -t $ECR_REPO:$IMAGE_TAG .
      - docker push $ECR_REPO:$IMAGE_TAG

  post_build:
    commands:
      - sed -i "s|IMAGE_PLACEHOLDER|$ECR_REPO:$IMAGE_TAG|g" taskDefinition.json
      - printf '[{"name":"order-service","imageUri":"%s"}]'
        $ECR_REPO:$IMAGE_TAG > imagedefinitions.json

artifacts:
  files:
    - imagedefinitions.json
    - taskDefinition.json
```

```json
// taskDefinition.json ✅
{
  "family": "order-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [{
    "name": "order-service",
    "image": "IMAGE_PLACEHOLDER",
    "portMappings": [{ "containerPort": 8080 }],
    "environment": [
      { "name": "SPRING_PROFILES_ACTIVE", "value": "prod" }
    ],
    "secrets": [
      { "name": "DB_PASSWORD",
        "valueFrom": "arn:aws:secretsmanager:..." }
    ],
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": {
        "awslogs-group": "/ecs/order-service",
        "awslogs-region": "us-east-1",
        "awslogs-stream-prefix": "ecs"
      }
    },
    "healthCheck": {
      "command": ["CMD-SHELL",
        "curl -f http://localhost:8080/actuator/health || exit 1"],
      "interval": 30,
      "timeout": 5,
      "retries": 3
    }
  }]
}
```

```hcl
# Terraform — ECS service with auto scaling ✅
resource "aws_ecs_service" "order_service" {
  name            = "order-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.order.arn
  desired_count   = 2
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = module.vpc.private_subnets
    security_groups = [aws_security_group.ecs.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.order.arn
    container_name   = "order-service"
    container_port   = 8080
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true  # auto rollback on failure ✅
  }
}

# auto scaling ✅
resource "aws_appautoscaling_policy" "cpu" {
  policy_type = "TargetTrackingScaling"

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value       = 70.0  # scale when CPU > 70% ✅
    scale_in_cooldown  = 300
    scale_out_cooldown = 60
  }
}
```

| | EC2 Launch Type | Fargate |
|---|---|---|
| **Server mgmt** | ❌ You manage | ✅ AWS manages |
| **Cost** | ✅ Cheaper steady | Higher per task |
| **Control** | ✅ More | Less |
| **Scaling** | Manual EC2 + task | ✅ Task only |
| **Use for** | Known steady traffic | Dynamic workloads ✅ |
| **Recommended** | Cost optimization | Most workloads ✅ |

---

## Quick Reference — All 4 Key Points

| Topic | Key Point |
|---|---|
| SQS | Queue pull based. one consumer. 14 days retention. DLQ ✅ |
| SNS | Pub/Sub push. multiple subscribers. no retention ✅ |
| Fan-out | SNS → multiple SQS queues simultaneously ✅ |
| SQS FIFO | Ordered + exactly once. 300 msg/sec limit ✅ |
| Lambda | Serverless. pay per call. 15 min max. cold start ⚠️ |
| Lambda cold start | Fix: provisioned concurrency or GraalVM ✅ |
| Lambda limits | 1000 concurrent. 6MB payload. 10GB memory ✅ |
| Aurora | Shared storage. 15 replicas. auto scale. failover 30s ✅ |
| Aurora vs RDS | Aurora = faster + auto scale. RDS = cheaper simple ✅ |
| Aurora routing | readOnly=true → reader. readOnly=false → writer ✅ |
| ECS Fargate | Serverless containers. AWS manages servers ✅ |
| ECS EC2 | You manage servers. cheaper for steady traffic ✅ |
| Task Definition | Blueprint — image, CPU, memory, env, secrets ✅ |
| Auto scaling | CPU > 70% → scale out. cooldown 300s scale in ✅ |
| CodePipeline | GitHub → build → ECR → ECS rolling deploy ✅ |
