# Spring Boot + AWS Services — Complete Integration Guide
> Code Snippets for Interview & Development Quick Reference

---

## 1. Dependencies — pom.xml

```xml
<properties>
    <spring-cloud-aws.version>3.1.0</spring-cloud-aws.version>
    <aws-sdk.version>2.21.0</aws-sdk.version>
</properties>

<dependencies>
    <!-- Spring Boot Core -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Cloud AWS -->
    <dependency>
        <groupId>io.awspring.cloud</groupId>
        <artifactId>spring-cloud-aws-starter</artifactId>
        <version>${spring-cloud-aws.version}</version>
    </dependency>

    <!-- Aurora PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>

    <!-- DynamoDB -->
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>dynamodb-enhanced</artifactId>
        <version>${aws-sdk.version}</version>
    </dependency>

    <!-- Secrets Manager -->
    <dependency>
        <groupId>io.awspring.cloud</groupId>
        <artifactId>spring-cloud-aws-starter-secrets-manager</artifactId>
        <version>${spring-cloud-aws.version}</version>
    </dependency>

    <!-- SQS -->
    <dependency>
        <groupId>io.awspring.cloud</groupId>
        <artifactId>spring-cloud-aws-starter-sqs</artifactId>
        <version>${spring-cloud-aws.version}</version>
    </dependency>

    <!-- SNS -->
    <dependency>
        <groupId>io.awspring.cloud</groupId>
        <artifactId>spring-cloud-aws-starter-sns</artifactId>
        <version>${spring-cloud-aws.version}</version>
    </dependency>

    <!-- S3 -->
    <dependency>
        <groupId>io.awspring.cloud</groupId>
        <artifactId>spring-cloud-aws-starter-s3</artifactId>
        <version>${spring-cloud-aws.version}</version>
    </dependency>

    <!-- Kafka (MSK) -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## 2. application.yml

```yaml
spring:
  application:
    name: order-service

  # Aurora PostgreSQL — loaded from Secrets Manager
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 3000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  # Secrets Manager — auto-inject secrets
  config:
    import: "aws-secretsmanager:/prod/order-service/db-credentials,
             aws-secretsmanager:/prod/order-service/app-secrets"

  # Kafka (MSK)
  kafka:
    bootstrap-servers: ${MSK_BOOTSTRAP_SERVERS}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        enable.idempotence: true
        acks: all
    consumer:
      group-id: order-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      properties:
        spring.json.trusted.packages: "*"

cloud:
  aws:
    region:
      static: us-east-1
    credentials:
      # uses IAM role automatically in ECS ✅
      # no access key needed when running on AWS

# DynamoDB
aws:
  dynamodb:
    endpoint: https://dynamodb.us-east-1.amazonaws.com
    table-name: Orders

# S3
  s3:
    bucket-name: ${S3_BUCKET_NAME}

# SNS
  sns:
    order-events-topic: ${SNS_ORDER_EVENTS_ARN}

# API Gateway
  api-gateway:
    base-url: ${API_GATEWAY_URL}

# Environments
server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus
  endpoint:
    health:
      show-details: always
```

---

## 3. Aurora PostgreSQL

### Entity
```java
@Entity
@Table(name = "orders",
       indexes = {
           @Index(name = "idx_orders_customer", columnList = "customer_id"),
           @Index(name = "idx_orders_status",   columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "order_seq")
    @SequenceGenerator(name = "order_seq",
                       sequenceName = "order_sequence",
                       allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Repository
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order> {

    List<Order> findByCustomerId(String customerId);

    @Query("SELECT o FROM Order o WHERE o.status = :status")
    Page<Order> findByStatus(@Param("status") OrderStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") OrderStatus status);
}
```

### Aurora Read/Write Split Config
```java
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
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put("writer", writerDataSource());
        dataSources.put("reader", readerDataSource());

        AbstractRoutingDataSource routing = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return TransactionSynchronizationManager
                        .isCurrentTransactionReadOnly() ? "reader" : "writer";
            }
        };
        routing.setTargetDataSources(dataSources);
        routing.setDefaultTargetDataSource(writerDataSource());
        return routing;
    }
}
```

---

## 4. AWS Secrets Manager

### Auto-inject via spring.config.import
```yaml
# application.yml
spring:
  config:
    import: "aws-secretsmanager:/prod/order-service/db-credentials"

# Secrets Manager stores:
# {
#   "DB_HOST": "cluster.rds.amazonaws.com",
#   "DB_NAME": "orderdb",
#   "DB_USERNAME": "admin",
#   "DB_PASSWORD": "secret123"
# }
# → auto-injected as ${DB_HOST}, ${DB_USERNAME} etc ✅
```

### Manual fetch via SDK
```java
@Service
@RequiredArgsConstructor
public class SecretsService {

    private final SecretsManagerClient secretsClient;

    public String getSecret(String secretName) {
        GetSecretValueResponse response = secretsClient.getSecretValue(
            GetSecretValueRequest.builder()
                .secretId(secretName)
                .build()
        );
        return response.secretString();
    }

    public Map<String, String> getSecretAsMap(String secretName) {
        String secretString = getSecret(secretName);
        return objectMapper.readValue(secretString, Map.class);
    }
}

@Configuration
public class SecretsManagerConfig {

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
```

---

## 5. DynamoDB

### Model
```java
@DynamoDbBean
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSession {

    private String sessionId;         // Partition Key
    private String customerId;        // Sort Key
    private String status;
    private Map<String, String> cart;
    private Long ttl;                 // Time To Live (auto-delete)
}
```

### Repository
```java
@Repository
public class OrderSessionRepository {

    private final DynamoDbTable<OrderSession> table;

    public OrderSessionRepository(DynamoDbEnhancedClient client) {
        this.table = client.table("OrderSessions",
                TableSchema.fromBean(OrderSession.class));
    }

    // PUT item
    public void save(OrderSession session) {
        table.putItem(session);
    }

    // GET item
    public Optional<OrderSession> findById(String sessionId, String customerId) {
        Key key = Key.builder()
                .partitionValue(sessionId)
                .sortValue(customerId)
                .build();
        return Optional.ofNullable(table.getItem(key));
    }

    // DELETE item
    public void delete(String sessionId, String customerId) {
        Key key = Key.builder()
                .partitionValue(sessionId)
                .sortValue(customerId)
                .build();
        table.deleteItem(key);
    }

    // QUERY — all items for partition key
    public List<OrderSession> findBySessionId(String sessionId) {
        QueryConditional query = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(sessionId).build());
        return table.query(query).items().stream().toList();
    }

    // SCAN with filter
    public List<OrderSession> findByStatus(String status) {
        Expression filter = Expression.builder()
                .expression("#s = :status")
                .putExpressionName("#s", "status")
                .putExpressionValue(":status", AttributeValue.fromS(status))
                .build();

        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .filterExpression(filter)
                .build();

        return table.scan(request).items().stream().toList();
    }
}

@Configuration
public class DynamoDbConfig {

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }
}
```

---

## 6. SQS

### Producer
```java
@Service
@RequiredArgsConstructor
public class OrderQueueProducer {

    private final SqsTemplate sqsTemplate;

    @Value("${aws.sqs.order-queue-url}")
    private String queueUrl;

    public void sendOrder(OrderEvent event) {
        sqsTemplate.send(queueUrl, event);
    }

    // with message attributes
    public void sendWithAttributes(OrderEvent event) {
        sqsTemplate.send(to -> to
                .queue(queueUrl)
                .payload(event)
                .header("eventType", "ORDER_PLACED")
                .header("priority", "HIGH")
                .delaySeconds(0)
        );
    }
}
```

### Consumer
```java
@Service
@RequiredArgsConstructor
public class OrderQueueConsumer {

    private final OrderService orderService;

    @SqsListener("${aws.sqs.order-queue-url}")
    public void processOrder(OrderEvent event) {
        orderService.process(event);
    }

    // with full message metadata
    @SqsListener("${aws.sqs.order-queue-url}")
    public void processOrderWithMetadata(
            @Payload OrderEvent event,
            @Header("eventType") String eventType,
            @Header(SqsHeaders.SQS_RECEIPT_HANDLE_HEADER) String receiptHandle) {
        log.info("Processing event type: {}", eventType);
        orderService.process(event);
    }

    // manual acknowledgment
    @SqsListener(value = "${aws.sqs.order-queue-url}",
                 acknowledgementMode = SqsAcknowledgementMode.MANUAL)
    public void processManual(OrderEvent event, Acknowledgement ack) {
        try {
            orderService.process(event);
            ack.acknowledge(); // delete from SQS only on success ✅
        } catch (Exception e) {
            log.error("Processing failed — message will be retried");
            // do NOT ack → message becomes visible again after visibility timeout
        }
    }
}
```

---

## 7. SNS

### Publisher
```java
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final SnsTemplate snsTemplate;
    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.order-events-topic}")
    private String orderEventsTopic;

    // simple publish
    public void publish(OrderEvent event) {
        snsTemplate.sendNotification(orderEventsTopic, event, "ORDER_PLACED");
    }

    // publish with message attributes (for filtering)
    public void publishWithAttributes(OrderEvent event, String eventType) {
        snsClient.publish(PublishRequest.builder()
                .topicArn(orderEventsTopic)
                .message(toJson(event))
                .subject(eventType)
                .messageAttributes(Map.of(
                    "eventType", MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(eventType)
                            .build()
                ))
                .build());
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
```

---

## 8. S3

### Service
```java
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    // upload file
    public String uploadFile(String key, byte[] content, String contentType) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build(),
            RequestBody.fromBytes(content)
        );
        return "s3://" + bucketName + "/" + key;
    }

    // download file
    public byte[] downloadFile(String key) {
        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
            GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()
        );
        return response.asByteArray();
    }

    // generate presigned URL (temporary access)
    public String generatePresignedUrl(String key, Duration expiry) {
        S3Presigner presigner = S3Presigner.builder()
                .region(Region.US_EAST_1).build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .getObjectRequest(r -> r.bucket(bucketName).key(key))
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    // delete file
    public void deleteFile(String key) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()
        );
    }

    // list files
    public List<String> listFiles(String prefix) {
        ListObjectsV2Response response = s3Client.listObjectsV2(
            ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build()
        );
        return response.contents().stream()
                .map(S3Object::key)
                .toList();
    }
}

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
```

---

## 9. AWS API Gateway Integration

### RestClient calling API Gateway
```java
@Configuration
public class ApiGatewayConfig {

    @Value("${aws.api-gateway.base-url}")
    private String apiGatewayUrl;

    @Bean("apiGatewayClient")
    public RestClient apiGatewayRestClient() {
        // connection pool
        PoolingHttpClientConnectionManager cm =
                new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(20);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .build();

        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .baseUrl(apiGatewayUrl)
                .defaultHeader("x-api-key", "${aws.api-gateway.api-key}")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

@Service
@RequiredArgsConstructor
public class ExternalApiService {

    @Qualifier("apiGatewayClient")
    private final RestClient restClient;

    public OrderResponse getOrder(String orderId) {
        return restClient.get()
                .uri("/orders/{id}", orderId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new OrderNotFoundException("Order not found: " + orderId);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ServiceUnavailableException("API Gateway error");
                })
                .body(OrderResponse.class);
    }

    public OrderResponse createOrder(OrderRequest request) {
        return restClient.post()
                .uri("/orders")
                .body(request)
                .retrieve()
                .body(OrderResponse.class);
    }
}
```

---

## 10. ECS — Health Check and Metadata

```java
// Spring Boot health endpoint used by ECS health checks
// ECS calls /actuator/health to check container health

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Health health() {
        try {
            orderRepository.count(); // simple DB check
            return Health.up()
                    .withDetail("database", "Aurora PostgreSQL UP")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "DOWN: " + e.getMessage())
                    .build();
        }
    }
}

// application.yml — ECS health check endpoint
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true  # liveness and readiness probes ✅

// ECS task definition health check:
// command: ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
// interval: 30s
// timeout: 5s
// retries: 3
```

---

## 11. ECR — Docker Build and Push

```bash
# Dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/order-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java",
            "-XX:+UseZGC",
            "-XX:+ZGenerational",
            "-Xmx768m",
            "-jar", "app.jar"]

# build and push to ECR
AWS_ACCOUNT=123456789012
REGION=us-east-1
ECR_REPO=$AWS_ACCOUNT.dkr.ecr.$REGION.amazonaws.com/order-service

# login to ECR
aws ecr get-login-password --region $REGION | \
    docker login --username AWS --password-stdin $ECR_REPO

# build image
docker build -t order-service:latest .

# tag for ECR
docker tag order-service:latest $ECR_REPO:latest
docker tag order-service:latest $ECR_REPO:$GIT_COMMIT

# push to ECR
docker push $ECR_REPO:latest
docker push $ECR_REPO:$GIT_COMMIT
```

---

## 12. CodePipeline — buildspec.yml

```yaml
# buildspec.yml — CodeBuild instructions
version: 0.2

env:
  secrets-manager:
    DB_PASSWORD: /prod/order-service/db-credentials:DB_PASSWORD

phases:
  install:
    runtime-versions:
      java: corretto21

  pre_build:
    commands:
      # login to ECR
      - aws ecr get-login-password --region $AWS_REGION |
        docker login --username AWS --password-stdin $ECR_REPO
      # run tests
      - mvn test

  build:
    commands:
      # build JAR
      - mvn clean package -DskipTests
      # build Docker image
      - IMAGE_TAG=$CODEBUILD_RESOLVED_SOURCE_VERSION
      - docker build -t $ECR_REPO:$IMAGE_TAG .
      - docker push $ECR_REPO:$IMAGE_TAG
      - docker tag $ECR_REPO:$IMAGE_TAG $ECR_REPO:latest
      - docker push $ECR_REPO:latest

  post_build:
    commands:
      # update task definition with new image
      - sed -i "s|IMAGE_PLACEHOLDER|$ECR_REPO:$IMAGE_TAG|g" taskDefinition.json
      # create imagedefinitions.json for CodeDeploy
      - printf '[{"name":"order-service","imageUri":"%s"}]'
        $ECR_REPO:$IMAGE_TAG > imagedefinitions.json

artifacts:
  files:
    - imagedefinitions.json
    - taskDefinition.json
    - appspec.yml
```

---

## 13. Terraform — Complete Infrastructure

```hcl
# main.tf

provider "aws" {
  region = var.aws_region
}

# ── VPC ─────────────────────────────────────────────────
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.0.0"
  name    = "${var.app_name}-vpc"
  cidr    = "10.0.0.0/16"
  azs     = ["us-east-1a", "us-east-1b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]
  enable_nat_gateway = true
}

# ── ECR ─────────────────────────────────────────────────
resource "aws_ecr_repository" "order_service" {
  name                 = "order-service"
  image_tag_mutability = "MUTABLE"
  image_scanning_configuration {
    scan_on_push = true
  }
}

# ── ECS Cluster ─────────────────────────────────────────
resource "aws_ecs_cluster" "main" {
  name = "${var.app_name}-cluster"
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

# ── Aurora PostgreSQL ────────────────────────────────────
resource "aws_rds_cluster" "aurora" {
  cluster_identifier      = "${var.app_name}-db"
  engine                  = "aurora-postgresql"
  engine_version          = "15.4"
  database_name           = "orderdb"
  master_username         = "admin"
  manage_master_user_password = true  # AWS manages password in Secrets Manager ✅
  db_subnet_group_name    = aws_db_subnet_group.aurora.name
  vpc_security_group_ids  = [aws_security_group.aurora.id]
  backup_retention_period = 7
  deletion_protection     = true
  skip_final_snapshot     = false
}

resource "aws_rds_cluster_instance" "writer" {
  identifier         = "${var.app_name}-db-writer"
  cluster_identifier = aws_rds_cluster.aurora.id
  instance_class     = "db.r6g.large"
  engine             = aws_rds_cluster.aurora.engine
}

resource "aws_rds_cluster_instance" "reader" {
  identifier         = "${var.app_name}-db-reader"
  cluster_identifier = aws_rds_cluster.aurora.id
  instance_class     = "db.r6g.large"
  engine             = aws_rds_cluster.aurora.engine
}

# ── DynamoDB ─────────────────────────────────────────────
resource "aws_dynamodb_table" "order_sessions" {
  name           = "OrderSessions"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "sessionId"
  range_key      = "customerId"

  attribute {
    name = "sessionId"
    type = "S"
  }
  attribute {
    name = "customerId"
    type = "S"
  }
  attribute {
    name = "status"
    type = "S"
  }

  global_secondary_index {
    name            = "status-index"
    hash_key        = "status"
    projection_type = "ALL"
  }

  ttl {
    attribute_name = "ttl"
    enabled        = true
  }
}

# ── Secrets Manager ──────────────────────────────────────
resource "aws_secretsmanager_secret" "app_secrets" {
  name        = "/prod/order-service/app-secrets"
  description = "Order service application secrets"
}

resource "aws_secretsmanager_secret_version" "app_secrets" {
  secret_id = aws_secretsmanager_secret.app_secrets.id
  secret_string = jsonencode({
    API_KEY        = var.api_key
    JWT_SECRET     = var.jwt_secret
  })
}

# ── SQS ──────────────────────────────────────────────────
resource "aws_sqs_queue" "order_queue" {
  name                       = "order-events"
  message_retention_seconds  = 86400  # 1 day
  visibility_timeout_seconds = 30
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.order_dlq.arn
    maxReceiveCount     = 3
  })
}

resource "aws_sqs_queue" "order_dlq" {
  name = "order-events-dlq"
}

# ── SNS ──────────────────────────────────────────────────
resource "aws_sns_topic" "order_events" {
  name = "order-events"
}

resource "aws_sns_topic_subscription" "sqs_subscription" {
  topic_arn = aws_sns_topic.order_events.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.order_queue.arn

  filter_policy = jsonencode({
    eventType = ["ORDER_PLACED", "ORDER_CANCELLED"]
  })
}

# ── S3 ───────────────────────────────────────────────────
resource "aws_s3_bucket" "order_attachments" {
  bucket = "${var.app_name}-order-attachments-${var.environment}"
}

resource "aws_s3_bucket_versioning" "order_attachments" {
  bucket = aws_s3_bucket.order_attachments.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "order_attachments" {
  bucket = aws_s3_bucket.order_attachments.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "aws:kms"
    }
  }
}

# ── ECS Task Definition ──────────────────────────────────
resource "aws_ecs_task_definition" "order_service" {
  family                   = "order-service"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name      = "order-service"
    image     = "IMAGE_PLACEHOLDER"
    essential = true
    portMappings = [{ containerPort = 8080 }]
    environment = [
      { name = "SPRING_PROFILES_ACTIVE", value = var.environment },
      { name = "AWS_REGION",             value = var.aws_region }
    ]
    secrets = [
      { name = "DB_HOST",     valueFrom = "${aws_secretsmanager_secret.app_secrets.arn}:DB_HOST::" },
      { name = "DB_PASSWORD", valueFrom = "${aws_rds_cluster.aurora.master_user_secret[0].secret_arn}:password::" }
    ]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = "/ecs/order-service"
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }
    healthCheck = {
      command     = ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
      interval    = 30
      timeout     = 5
      retries     = 3
      startPeriod = 60
    }
  }])
}

# ── ALB ──────────────────────────────────────────────────
resource "aws_lb" "main" {
  name               = "${var.app_name}-alb"
  internal           = false
  load_balancer_type = "application"
  subnets            = module.vpc.public_subnets
  security_groups    = [aws_security_group.alb.id]
}

resource "aws_lb_target_group" "order_service" {
  name        = "order-service-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = module.vpc.vpc_id
  target_type = "ip"

  health_check {
    path                = "/actuator/health"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
  }
}

# ── ECS Service ──────────────────────────────────────────
resource "aws_ecs_service" "order_service" {
  name            = "order-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.order_service.arn
  desired_count   = 2
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = module.vpc.private_subnets
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.order_service.arn
    container_name   = "order-service"
    container_port   = 8080
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true  # auto rollback on failure ✅
  }
}

# ── Auto Scaling ─────────────────────────────────────────
resource "aws_appautoscaling_target" "ecs" {
  max_capacity       = 10
  min_capacity       = 2
  resource_id        = "service/${aws_ecs_cluster.main.name}/order-service"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "cpu_scaling" {
  name               = "cpu-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value       = 70.0
    scale_in_cooldown  = 300
    scale_out_cooldown = 60
  }
}

# ── API Gateway ───────────────────────────────────────────
resource "aws_api_gateway_rest_api" "order_api" {
  name        = "order-service-api"
  description = "Order Service REST API"
}

resource "aws_api_gateway_resource" "orders" {
  rest_api_id = aws_api_gateway_rest_api.order_api.id
  parent_id   = aws_api_gateway_rest_api.order_api.root_resource_id
  path_part   = "orders"
}

resource "aws_api_gateway_method" "get_orders" {
  rest_api_id   = aws_api_gateway_rest_api.order_api.id
  resource_id   = aws_api_gateway_resource.orders.id
  http_method   = "GET"
  authorization = "NONE"
  api_key_required = true  # require API key ✅
}

# ── CodePipeline ─────────────────────────────────────────
resource "aws_codepipeline" "order_pipeline" {
  name     = "order-service-pipeline"
  role_arn = aws_iam_role.codepipeline.arn

  artifact_store {
    location = aws_s3_bucket.pipeline_artifacts.bucket
    type     = "S3"
  }

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
        ProjectName = aws_codebuild_project.order_service.name
      }
    }
  }

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
        FileName    = "imagedefinitions.json"
      }
    }
  }
}

# ── CloudWatch Alarms ────────────────────────────────────
resource "aws_cloudwatch_metric_alarm" "high_cpu" {
  alarm_name          = "order-service-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = 60
  statistic           = "Average"
  threshold           = 80
  alarm_actions       = [aws_sns_topic.alerts.arn]
  dimensions = {
    ClusterName = aws_ecs_cluster.main.name
    ServiceName = "order-service"
  }
}

# ── Outputs ───────────────────────────────────────────────
output "alb_dns_name" {
  value = aws_lb.main.dns_name
}
output "ecr_repository_url" {
  value = aws_ecr_repository.order_service.repository_url
}
output "aurora_writer_endpoint" {
  value = aws_rds_cluster.aurora.endpoint
}
output "aurora_reader_endpoint" {
  value = aws_rds_cluster.aurora.reader_endpoint
}
output "api_gateway_url" {
  value = aws_api_gateway_deployment.order_api.invoke_url
}
```

---

## 14. IAM Role for ECS Task

```hcl
# ECS Task Role — permissions for app to access AWS services
resource "aws_iam_role" "ecs_task" {
  name = "order-service-task-role"

  assume_role_policy = jsonencode({
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "ecs_task_policy" {
  role = aws_iam_role.ecs_task.id

  policy = jsonencode({
    Statement = [
      # DynamoDB access
      {
        Effect   = "Allow"
        Action   = ["dynamodb:PutItem", "dynamodb:GetItem",
                    "dynamodb:DeleteItem", "dynamodb:Query", "dynamodb:Scan"]
        Resource = aws_dynamodb_table.order_sessions.arn
      },
      # S3 access
      {
        Effect   = "Allow"
        Action   = ["s3:PutObject", "s3:GetObject", "s3:DeleteObject"]
        Resource = "${aws_s3_bucket.order_attachments.arn}/*"
      },
      # SQS access
      {
        Effect   = "Allow"
        Action   = ["sqs:SendMessage", "sqs:ReceiveMessage", "sqs:DeleteMessage"]
        Resource = aws_sqs_queue.order_queue.arn
      },
      # SNS publish
      {
        Effect   = "Allow"
        Action   = ["sns:Publish"]
        Resource = aws_sns_topic.order_events.arn
      },
      # Secrets Manager read
      {
        Effect   = "Allow"
        Action   = ["secretsmanager:GetSecretValue"]
        Resource = aws_secretsmanager_secret.app_secrets.arn
      }
    ]
  })
}
```

---

## 15. Complete Architecture Flow

```
Internet
    ↓
Route 53 (DNS)
    ↓
CloudFront CDN (optional)
    ↓
API Gateway (rate limit, auth, routing)
    ↓
ALB (load balancing across AZs)
    ↓              ↓
ECS Task 1    ECS Task 2    ← Spring Boot app (Fargate)
(AZ-1)        (AZ-2)        ← ECR image pulled
    ↓
├── Aurora PostgreSQL (writer + reader)
├── DynamoDB (sessions, caching)
├── Secrets Manager (credentials)
├── SQS (async task queue)
├── SNS (fan-out notifications)
├── S3 (file storage)
└── MSK / Kafka (event streaming)
    ↓
CloudWatch + X-Ray (monitoring + tracing)
    ↓
SNS Alert → Email/PagerDuty

CI/CD:
GitHub → CodePipeline → CodeBuild → ECR → ECS deploy
         (buildspec.yml)  (mvn + docker)
Terraform provisions all infrastructure ✅
```
