# Terraform — Complete Reference Guide
> ECS + Aurora + ALB + IAM + Secrets Manager + CloudWatch

---

## What is Terraform?
```
Terraform = Infrastructure as Code ✅
→ create AWS resources via code ✅
→ versioned in Git ✅
→ reviewed via Pull Request ✅
→ same code → dev/stage/prod ✅
→ no manual AWS console clicks ✅

Key commands:
terraform init   → initialize ✅
terraform plan   → show what will change ✅
terraform apply  → create/update resources ✅
terraform destroy→ delete resources ✅
```

---

## Remote State — S3 + DynamoDB Lock

### Why needed
```
Without remote state:
→ state on each developer laptop ❌
→ Dev 1 creates resource ✅
→ Dev 2 does not know ❌
→ duplicate resources created ❌

With S3 remote state:
→ ONE shared state file ✅
→ all developers read same state ✅
→ no conflicts ✅

Without DynamoDB lock:
→ Dev 1 + Dev 2 apply same time ❌
→ state file corrupted ❌

With DynamoDB lock:
→ Dev 1 applies → lock acquired ✅
→ Dev 2 waits ⏳
→ Dev 1 done → lock released ✅
→ Dev 2 applies ✅

Simple analogy:
S3       = shared Google Doc ✅
DynamoDB = edit lock on Doc ✅
```

```hcl
# Remote State ✅
terraform {
  backend "s3" {
    bucket         = "my-terraform-state"
    key            = "payment/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-lock" # lock ✅
  }
}
```

---

## Provider

```hcl
provider "aws" {
  region = "us-east-1"
}
```

---

## ECS — Elastic Container Service

### Concepts
```
ECS Cluster         = group of services ✅
ECS Task Definition = container config (image, CPU, memory) ✅
ECS Service         = runs + manages tasks ✅

taskDefinition.json = JSON format of task def ✅
aws_ecs_task_definition = same in Terraform HCL ✅

serviceDefinition.json = JSON format of service ✅
aws_ecs_service        = same in Terraform HCL ✅
```

```hcl
# ECS Cluster ✅
resource "aws_ecs_cluster" "main" {
  name = "payment-cluster"
}

# Task Definition — container config ✅
# Same as taskDefinition.json — just HCL format
resource "aws_ecs_task_definition" "payment" {
  family                   = "payment-service"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name  = "payment-service"
    image = "myregistry/payment-service:latest"
    portMappings = [{
      containerPort = 8080
    }]
    environment = [{
      name  = "SPRING_PROFILES_ACTIVE"
      value = "prod"
    }]
    secrets = [
      {
        name      = "DB_PASSWORD"    # env var name ✅
        valueFrom = "${aws_secretsmanager_secret
                        .db_password.arn}:DB_PASSWORD::"
      },
      {
        name      = "JWT_SECRET"
        valueFrom = "${aws_secretsmanager_secret
                        .jwt_secret.arn}:JWT_SECRET::"
      }
    ]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        awslogs-group         = "/ecs/payment-service"
        awslogs-region        = "us-east-1"
        awslogs-stream-prefix = "ecs"
      }
    }
    healthCheck = {
      command  = ["CMD-SHELL",
        "curl -f http://localhost:8080/actuator/health || exit 1"]
      interval = 30
      timeout  = 5
      retries  = 3
    }
  }])
}

# ECS Service — runs + manages tasks ✅
# Same as serviceDefinition.json — just HCL format
resource "aws_ecs_service" "payment" {
  name            = "payment-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.payment.arn
  desired_count   = 2
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = ["subnet-abc", "subnet-def"]
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.payment.arn
    container_name   = "payment-service"
    container_port   = 8080
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true # auto rollback on failure ✅
  }

  deployment_controller {
    type = "ECS" # rolling deployment ✅
  }
}

# Auto Scaling ✅
resource "aws_appautoscaling_target" "ecs" {
  max_capacity       = 10
  min_capacity       = 2
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.payment.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "cpu" {
  name               = "payment-cpu-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value       = 70.0  # scale at CPU > 70% ✅
    scale_in_cooldown  = 300
    scale_out_cooldown = 60
  }
}
```

---

## ALB — Load Balancer + Security Groups

### Concepts
```
Security Group = firewall rules ✅
ALB            = load balancer — routes traffic ✅
Target Group   = group of ECS tasks ✅
               = health check config ✅

Flow:
Internet → ALB → Target Group → ECS Tasks ✅
```

```hcl
# Security Group — ALB ✅
resource "aws_security_group" "alb" {
  name   = "alb-sg"
  vpc_id = "vpc-12345"

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # HTTPS from internet ✅
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Security Group — ECS ✅
resource "aws_security_group" "ecs" {
  name   = "ecs-sg"
  vpc_id = "vpc-12345"

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id] # ALB only ✅
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# ALB ✅
resource "aws_lb" "main" {
  name               = "payment-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = ["subnet-abc", "subnet-def"]
}

# Target Group + Health Check ✅
resource "aws_lb_target_group" "payment" {
  name        = "payment-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = "vpc-12345"
  target_type = "ip"

  health_check {
    path                = "/actuator/health" # ✅
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
}

# ALB Listener ✅
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.main.arn
  port              = 443
  protocol          = "HTTPS"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.payment.arn
  }
}
```

---

## Aurora PostgreSQL

### Concepts
```
Aurora = AWS managed PostgreSQL ✅
→ writer endpoint → writes ✅
→ reader endpoint → reads ✅
→ auto scales storage ✅
→ failover < 30 seconds ✅
```

```hcl
# Aurora Cluster ✅
resource "aws_rds_cluster" "aurora" {
  cluster_identifier      = "payment-db"
  engine                  = "aurora-postgresql"
  engine_version          = "15.3"
  database_name           = "paymentdb"
  master_username         = var.db_username
  master_password         = var.db_password
  backup_retention_period = 7           # 7 days ✅
  skip_final_snapshot     = false
  deletion_protection     = true        # prod safety ✅
  storage_encrypted       = true        # encrypted ✅
}

# Writer Instance ✅
resource "aws_rds_cluster_instance" "writer" {
  identifier         = "payment-db-writer"
  cluster_identifier = aws_rds_cluster.aurora.id
  instance_class     = "db.r6g.large"
  engine             = "aurora-postgresql"
}

# Reader Instance ✅
resource "aws_rds_cluster_instance" "reader" {
  identifier         = "payment-db-reader"
  cluster_identifier = aws_rds_cluster.aurora.id
  instance_class     = "db.r6g.large"
  engine             = "aurora-postgresql"
}
```

---

## IAM Roles

### Concepts
```
IAM Role = permissions for ECS task ✅
→ allows ECS to pull image from ECR ✅
→ allows ECS to read secrets ✅
→ allows ECS to write logs ✅
→ no access keys needed ✅
```

```hcl
# ECS Task Execution Role ✅
resource "aws_iam_role" "ecs_task" {
  name = "ecs-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

# Attach standard ECS policy ✅
resource "aws_iam_role_policy_attachment" "ecs" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Allow access to Secrets Manager ✅
resource "aws_iam_role_policy" "secrets" {
  name = "secrets-access"
  role = aws_iam_role.ecs_task.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["secretsmanager:GetSecretValue"]
      Resource = ["arn:aws:secretsmanager:*:*:secret:payment/*"]
    }]
  })
}
```

---

## Secrets Manager

### Concepts
```
aws_secretsmanager_secret
→ creates the secret container ✅
→ like creating empty locker ✅

aws_secretsmanager_secret_version
→ stores actual value inside ✅
→ like putting password in locker ✅

ECS task definition secrets
→ injects secret as env var ✅
→ Spring Boot reads via ${VAR_NAME} ✅

Never hardcode passwords ✅
PCI DSS compliance requirement ✅
```

```hcl
# DB Credentials ✅
resource "aws_secretsmanager_secret" "db" {
  name        = "/prod/payment/db-credentials"
  description = "Aurora DB credentials"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "db" {
  secret_id = aws_secretsmanager_secret.db.id

  secret_string = jsonencode({
    DB_HOST     = "aurora.cluster.rds.amazonaws.com"
    DB_PORT     = "5432"
    DB_NAME     = "paymentdb"
    DB_USERNAME = var.db_username
    DB_PASSWORD = var.db_password
  })
}

# JWT Secret ✅
resource "aws_secretsmanager_secret" "jwt" {
  name = "/prod/payment/jwt-secret"
}

resource "aws_secretsmanager_secret_version" "jwt" {
  secret_id     = aws_secretsmanager_secret.jwt.id
  secret_string = jsonencode({
    JWT_SECRET = var.jwt_secret
    JWT_EXPIRY = "86400000"
  })
}

# Chase Gateway credentials ✅
resource "aws_secretsmanager_secret" "chase" {
  name = "/prod/payment/chase-credentials"
}

resource "aws_secretsmanager_secret_version" "chase" {
  secret_id     = aws_secretsmanager_secret.chase.id
  secret_string = jsonencode({
    CHASE_API_KEY    = var.chase_api_key
    CHASE_API_SECRET = var.chase_api_secret
    CHASE_URL        = var.chase_url
  })
}
```

```yaml
# Spring Boot reads automatically ✅
spring:
  datasource:
    password: ${DB_PASSWORD}   # from secret ✅
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
jwt:
  secret: ${JWT_SECRET}        # from secret ✅
```

---

## CloudWatch Alarms + SNS

### Concepts
```
SNS Topic    = notification channel ✅
SNS Sub      = who gets notified ✅
               PagerDuty, email, Lambda ✅
CW Alarm     = threshold → triggers SNS ✅
CW Log Group = where logs stored ✅
```

```hcl
# SNS Topic — alerts channel ✅
resource "aws_sns_topic" "alerts" {
  name = "payment-alerts"
}

# SNS → PagerDuty ✅
resource "aws_sns_topic_subscription" "pagerduty" {
  topic_arn = aws_sns_topic.alerts.arn
  protocol  = "https"
  endpoint  = var.pagerduty_url
}

# Alarm — CPU high ✅
resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  alarm_name          = "payment-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = 60
  statistic           = "Average"
  threshold           = 80          # CPU > 80% ✅
  alarm_actions       = [aws_sns_topic.alerts.arn]

  dimensions = {
    ClusterName = aws_ecs_cluster.main.name
    ServiceName = aws_ecs_service.payment.name
  }
}

# Alarm — 5XX errors ✅
resource "aws_cloudwatch_metric_alarm" "errors" {
  alarm_name          = "payment-5xx-errors"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "5XXError"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Sum"
  threshold           = 10          # > 10 errors/min ✅
  alarm_actions       = [aws_sns_topic.alerts.arn]
}

# Alarm — Memory high ✅
resource "aws_cloudwatch_metric_alarm" "memory" {
  alarm_name          = "payment-memory-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "MemoryUtilization"
  namespace           = "AWS/ECS"
  period              = 60
  statistic           = "Average"
  threshold           = 85          # Memory > 85% ✅
  alarm_actions       = [aws_sns_topic.alerts.arn]
}

# CloudWatch Log Group ✅
resource "aws_cloudwatch_log_group" "payment" {
  name              = "/ecs/payment-service"
  retention_in_days = 45 # 45 days ✅
}
```

---

## Variables

```hcl
# variables.tf ✅
variable "db_username" {
  type      = string
  sensitive = true  # hidden in logs ✅
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "jwt_secret" {
  type      = string
  sensitive = true
}

variable "chase_api_key" {
  type      = string
  sensitive = true
}

variable "pagerduty_url" {
  type = string
}
```

---

## Quick Reference — All Resources

| Resource | Purpose |
|---|---|
| `terraform backend s3` | Remote state storage ✅ |
| `aws_dynamodb_table` | State lock ✅ |
| `aws_ecs_cluster` | ECS cluster ✅ |
| `aws_ecs_task_definition` | Container config (= taskDefinition.json) ✅ |
| `aws_ecs_service` | Run + manage tasks (= serviceDefinition.json) ✅ |
| `aws_appautoscaling_policy` | Auto scale on CPU ✅ |
| `aws_security_group` | Firewall rules ✅ |
| `aws_lb` | Load balancer ✅ |
| `aws_lb_target_group` | Health check + routing ✅ |
| `aws_lb_listener` | HTTPS listener ✅ |
| `aws_rds_cluster` | Aurora DB ✅ |
| `aws_rds_cluster_instance` | Writer + Reader ✅ |
| `aws_iam_role` | ECS permissions ✅ |
| `aws_iam_role_policy` | Secrets access ✅ |
| `aws_secretsmanager_secret` | Secret container ✅ |
| `aws_secretsmanager_secret_version` | Secret value ✅ |
| `aws_sns_topic` | Alert channel ✅ |
| `aws_sns_topic_subscription` | PagerDuty/email ✅ |
| `aws_cloudwatch_metric_alarm` | CPU/Memory/Error alerts ✅ |
| `aws_cloudwatch_log_group` | Log storage 45 days ✅ |

---

## Key concepts — one line each

```
Remote State  = shared state in S3 — team collaboration ✅
DynamoDB Lock = one apply at a time — no corruption ✅
Task Def      = what container to run + config ✅
ECS Service   = how many tasks + networking ✅
Security Group= firewall — ALB → ECS only ✅
Target Group  = health check + routes to ECS ✅
IAM Role      = ECS permissions — no hardcoded keys ✅
Secret        = passwords stored — injected as env var ✅
SNS           = alert channel → PagerDuty ✅
CW Alarm      = threshold → triggers SNS ✅
```
