# AWS — Short Notes

---

## AWS - Aurora

```
AWS's managed RD
5X and 3X
separates Compute and Storage
Compute = CPU + RAM (database engine that processes queries)
Storage = actual data on disk
primary + replicas share same storage layer
10GB → 128TB
15 read replicas - Priority tiers from 0-15
2 copies × 3 AZs = 6 copies total
Replica lag is typically under 100ms
Replica promoted to primary under 30 seconds
Multi-region Aurora - Replication under 1 second.
Region failure - secondary region comes under 1 min.
V1, V2, ACU - 2GB of RAM + matching CPU + networking - based on traffic 0.5 ACU to 128 ACU
pay for what you use
high availability, large scale, global apps - fast failover
RDS for lower cost, simpler needs, smaller workloads.
```

## AWS - Lambda
```
No servers to manage
You pay only when code runs — pay per invocation.
0 to 1000 function instantly
Pay per request + duration
Max timeout — 15 minutes
Max memory — 10GB
Stateless — no data persisted between runs
Cold Start -Not called for while - AWS spins up a new container - 100ms to seconds latency.
Provisioned Concurrency - pre-warms a fixed number of Lambda containers - we need to pay this.
SnapStart - memory snapshot of fully initialized JVM after first deploy - will be reused on cold start.
Reduce package size - spring-boot-thin-launcher - code in JAR - Dependencies download separately
Lambda Layers - ZIP of shared libraries - resued across - attached at run time.
GraalVM Native Image - native binary. 
ProGuard - scan -  Remove Dead Code - smaller jar
EventBridge - dummy request to Lambda - but only keeps one container warm - not suitable
Remove Unused Dependencies & Auto Configurations
Unreserved Concurrency - Reserved Concurrency - Provisioned Concurrency

Max timeout                     15 minutes
Max memory                      10 GB
Max deployment package          50 MB (zip) / 250 MB unzipped
Max /tmp storage                10 GB
Default concurrency per region  1000
Max environment variables       4 KB
Max layers per function         5
```

## AWS - DynamoDB
```
AWS managed Serverless - NoSQL key-value + document database.  
Auto scales up/down, Multi-AZ by default
Single-digit millisecond performance at any scale
RCU = Read Capacity Unit (4KB read/sec)  
WCU = Write Capacity Unit (1KB write/sec)

DynamoDB = Table (Amazon naming) ✅
rows inside = Items ✅
each Item = JSON document ✅
schema-less like MongoDB ✅

Partition Key -> Primary unique identifier
Sort Key -> Secondary key within partition

Every item MUST have a Partition Key. Sort Key is optional.
Partition Key only Example: Must be unique per item -> userId - get userIds
Partition Key + Sort Key : Combination must be unique -> Sort key allows range queries  
customerId (PK) + orderId (SK) => get all orders for a customer

PutItem, GetItem, UpdateItem, DeleteItem,
Query (Fetch items by PK (+ optional SK filter) — efficient)
Scan,
BatchGetItem -> Fetch up to 100 items in one call,
TransactWrite -> Atomic write across multiple items

GSI — Global Secondary Index - an alternate index - query the same data from multiple access patterns

With PK we only get userId(100) -> PK = one record ✅
With GSI we can get list. getUserList(active) -> GSI = many records ✅

Completely different PK + SK
Can be added anytime
Up to 20 GSIs per table
Has its own read/write capacity => RCU and WCU
Eventually consistent reads only

LSI — Local Secondary Index
Same PK, different SK
Must be created at table creation
Shares capacity with main table
Supports strongly consistent reads
Up to 5 LSIs per table
LSI query:
→ can only query within ONE customer (same PK) ✅
→ cannot query across ALL customers ❌
(that needs GSI — different PK)

"Local" in LSI = local to one partition ✅
"Global" in GSI = across all partitions ✅
```

## AWS - API Gateway
```
AWS managed service - secure, and monitor APIs
Routes all client requests
REST API = full features - Request/response transform - Caching support - Usage plans + API keys - Default choice
HTTP API = simple + cheap - 70% cheaper - No caching & transform - Best for Lambda + simple routes
WebSocket =  Persistent connection - real-time 2W communication - Chat apps, live updates  - Push from server.
Routing - Authentication - Rate Limiting - Throttling - Caching - Transform - Termination - Monitoring
Authorizer
Cognito - JWT token
Lambda - Custom auth logic - API GW call - Lambda allow/deny - cached 300 sec - custom tokens (team token)
API Key - Best for B2B - Not for end users - Usage plans per key - x-api-key

Programmatic user - Console user - BOTH - IAM Role.

Rate Limiting + Throttling  - Defualt - 10,000 req/sec - 5,000 concurrent requests - 429 - overload
API key limits - Usage Plans - Different tiers - Throttle/Quota  per client
Caching - default 300 sec - 0 to 3600 sec (1Hr)- 0.5GB to 237GB - only REST API.
```

## AWS - ECS

```
AWS Managed COS - manages Docker on cluster - Define WTR - ECS handles rest.
Key Features - Health check - Auto restart - Auto scaling - LB Integration - CloudWatch & Deep AWS integration
Cluster - Image - Task Definition - Task - Service - Container
Launch Types - EC2 - manage - patch - ECS places containers - Cheaper-GPU-1000 cores
Fargate - Server less - AWS manages - pay - scales
Task Definition - Blueprint - docker image - cpu - memory - port - IAM Roles - Log - health - volume mounts
ECS Service - desired count - restart - rolling deployment - autoscaling rules.
IAM Role - entire EC2 - all Containers will have same role
IAM Role - per Task -  role specfic access - payment, Order and User - different access - Principle of Least Privilege
```

## AWS - ECR

```
AWS DockerHub - build - push - pull - Run 
Vulnerabilties - IAM controll push/pull - ImageTag - latest,V2.0,V1.0
Scalling triggers - CPU utilization - Memory utilization - ALB request count
Network - Fargate - awsvpc - Own -> IP - SecurityGroup per service - network interface - Just like EC2
Network - EC2 - bridge/host - host network
ALB - registers/deregisters - unhealthy instances - Path-based routing - SSL Termination (decrypt it and send HTTP).

Deployment Strategies - Rolling Update - default - some old and some new - gradually.
bluegreen - New version - Test green fully - Switch traffic - rollback to blue
Canary - % traffic new version - errors - increase - decrease to 0.

SSM Parameter Store - non-sensitive information like urls and db urls - SDK call need to refresh.

executionRoleArn - ECS Agent permission - pull image - write logs
taskRoleArn - app permission to call AWS service like DynamoDB, S3, SQS, etc - App logic.

```

## AWS Secrets Manager

```
AWS service to securely store, manage, and automatically rotate
Problem Without Secrets Manager
With Secrets Manager - fetch at runtime - Password never in code - Encrypted in AWS

Enable Rotation - Lambda does everything - Secrets Manager calls Rotation Lambda - update DB + SM
password updated automatically
App fetches new password automatically
No manual work — no downtime

Store secrets - Encrypt - secrets encrypted with KMS - no plain text - Auto rotation - x days - Versioning - CloudTrail - IAM controls
```

## AWS SQS - Simple Queue Service

```
AWS managed message queue service - communicate asynchronously - Producer put - Consumer reads and process.
Advantages - Traffic spike - queue absorbs burst - message waits safely. 2 Queue Types.
Standard Q - unlimited - At-least-once - Best-effor - twice - Cheaper - notification, logs, emails
FIFO Q - 300 TPS - 3000 with batching - Exactly-once - order guaranteed - No duplicate - expensive - payments, orders, banking
Visibility - Message hidden. Delay Queue - Message invisible for x seconds. Retention - 1 min 14 days - 4 days.
Long/Short Polling. Batch Size  - 10 messages. Max Message Size - 256 KB - DLQ - failed messages after retries
SNS + SQS Fan-Out Pattern => Order Placed Event → SNS Topic
Queue 1 → Payment Service - Queue 2 → Notification Service - Queue 3 → Inventory Service
```

## AWS SQS - S3 - Simple Storage Service

S3 - Simple Storage Service - Object - image/video/JSON/JAR backup - Infinitely/Unlimited - highly durable 11 -
nines - almost imposible to loose data - accessible via URL (Pre-Signed URL) - without needing AWS credentials - Object storage - not a file system - 0 bytes - 5TB. Global service - Replication - CRR - Disaster recovery - SRR - Versioning must be enabled.
Standard - Standard-IA  - One Zone-IA - Intelligent (auto move) - Glacier Instant (quarter) - Glacier Flexible
(rarely accessed) - Glacier Deep (7+ years)
Versioning - Protects - Delete - Restore - cannot fully disable - only suspend
Day 0 — Upload (Standard) → Day 30 — Move to Standard-IA → Day 90 — Move to Glacier → Day 365 — Delete
automated cost saving. Old data automatically moves to cheaper storage. No manual work needed.
S3 (static files) + CloudFront (CDN) + Route 53 (custom domain)
Static website hosting - Image / video storage - Backup and disaster recovery - Data lake for analytics
Application logs - CodePipeline artifacts - Docker image layers
Use Lifecycle Rules to move objects automatically between classes.


