# Terraform — Complete Interview Notes
> Infrastructure as Code — Quick Reference

---

## 1. What is Terraform?

```
Terraform = Infrastructure as Code (IaC) tool by HashiCorp

→ provision and manage cloud infrastructure using code
→ declarative — describe WHAT you want, Terraform figures out HOW
→ multi-cloud — AWS, Azure, GCP, 1000+ providers
→ open source (2014) + enterprise version available
→ language: HCL (HashiCorp Configuration Language)
```

---

## 2. Why Terraform?

```
Without Terraform (manual):
→ click AWS console → create EC2 → create RDS → create S3
→ not repeatable ❌
→ not version controlled ❌
→ different between dev/prod ❌
→ slow ❌

With Terraform:
→ write .tf files → terraform apply → done ✅
→ version controlled (Git) ✅
→ identical across dev/staging/prod ✅
→ fast and repeatable ✅
→ disaster recovery — recreate entire infra in minutes ✅
```

---

## 3. Core Concepts

### Provider
```hcl
# tells Terraform which cloud to use
provider "aws" {
  region = "us-east-1"
}

provider "azurerm" {
  features {}
}

provider "google" {
  project = "my-project"
  region  = "us-central1"
}
```

### Resource
```hcl
# infrastructure component to create
resource "aws_s3_bucket" "my_bucket" {   # type + name
  bucket = "my-order-service-bucket"
  tags = {
    Environment = "prod"
  }
}

resource "aws_instance" "web_server" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t3.medium"
}
```

### Variable
```hcl
# input variables — parameterize config
variable "db_password" {
  type        = string
  sensitive   = true    # hide from logs ✅
  description = "DB password"
}

variable "environment" {
  type    = string
  default = "dev"
}

# use variable
resource "aws_db_instance" "db" {
  password = var.db_password
  tags = {
    Env = var.environment
  }
}
```

### Output
```hcl
# expose values after apply
output "db_endpoint" {
  value     = aws_db_instance.db.endpoint
  sensitive = false
}

output "s3_bucket_name" {
  value = aws_s3_bucket.my_bucket.id
}
```

### Data Source
```hcl
# read existing infrastructure (not created by Terraform)
data "aws_vpc" "existing" {
  id = "vpc-12345678"
}

# use in resource
resource "aws_subnet" "my_subnet" {
  vpc_id = data.aws_vpc.existing.id
}
```

### Local
```hcl
# local computed values
locals {
  service_name = "order-service"
  common_tags = {
    Project     = "ecommerce"
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

resource "aws_ecs_cluster" "cluster" {
  name = local.service_name
  tags = local.common_tags
}
```

---

## 4. Terraform Commands

```bash
# initialize — download providers and modules
terraform init

# preview changes (dry run — does NOT create anything)
terraform plan

# preview and save plan to file
terraform plan -out=tfplan

# apply — create/update infrastructure
terraform apply

# apply saved plan
terraform apply tfplan

# apply without confirmation prompt
terraform apply -auto-approve

# destroy all infrastructure
terraform destroy

# destroy specific resource
terraform destroy -target=aws_instance.web_server

# format .tf files
terraform fmt

# validate syntax
terraform validate

# show current state
terraform show

# list resources in state
terraform state list

# show specific resource in state
terraform state show aws_s3_bucket.my_bucket

# remove resource from state (without destroying)
terraform state rm aws_s3_bucket.my_bucket

# import existing resource into state
terraform import aws_s3_bucket.my_bucket my-existing-bucket

# show output values
terraform output

# refresh state from real infrastructure
terraform refresh

# graph dependencies
terraform graph
```

---

## 5. State File

```
terraform.tfstate = tracks what Terraform manages

→ JSON file recording all resources created
→ compares desired state (code) vs actual state (real infra)
→ determines what needs to be created/updated/deleted

Never edit manually ❌
Always store remotely in production ✅
```

### Remote State (S3 backend)
```hcl
# terraform.tf — store state in S3
terraform {
  backend "s3" {
    bucket         = "my-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-lock"  # state locking ✅
  }
}
```

### State Locking
```
Multiple developers run terraform apply simultaneously:
→ both read same state → conflict ❌

DynamoDB lock:
→ first apply → acquires lock ✅
→ second apply → waits for lock ✅
→ no conflicts ✅
```

---

## 6. Modules

```
Module = reusable group of Terraform resources
→ like a function in programming
→ write once, use many times ✅
→ parameterize with variables
```

### Create module
```
modules/
  ecs-service/
    main.tf       ← resources
    variables.tf  ← input variables
    outputs.tf    ← output values
```

```hcl
# modules/ecs-service/main.tf
resource "aws_ecs_service" "service" {
  name            = var.service_name
  cluster         = var.cluster_id
  task_definition = var.task_definition
  desired_count   = var.desired_count
}

# modules/ecs-service/variables.tf
variable "service_name"    { type = string }
variable "cluster_id"      { type = string }
variable "task_definition" { type = string }
variable "desired_count"   { type = number; default = 2 }

# modules/ecs-service/outputs.tf
output "service_id" {
  value = aws_ecs_service.service.id
}
```

### Use module
```hcl
# main.tf — use module
module "order_service" {
  source = "./modules/ecs-service"

  service_name    = "order-service"
  cluster_id      = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.order.arn
  desired_count   = 3
}

module "payment_service" {
  source = "./modules/ecs-service"

  service_name    = "payment-service"
  cluster_id      = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.payment.arn
  desired_count   = 2
}

# same module, different config ✅
```

### Public modules (Terraform Registry)
```hcl
# use community modules
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.0.0"

  name = "my-vpc"
  cidr = "10.0.0.0/16"

  azs             = ["us-east-1a", "us-east-1b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]
}
```

---

## 7. Complete AWS Example — Spring Boot on ECS

```hcl
# variables.tf
variable "environment"  { default = "prod" }
variable "db_password"  { sensitive = true }
variable "docker_image" { type = string }

# main.tf

# Provider
provider "aws" {
  region = "us-east-1"
}

# VPC
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.0.0"
  name    = "order-vpc"
  cidr    = "10.0.0.0/16"
  azs     = ["us-east-1a", "us-east-1b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]
}

# ECR Repository
resource "aws_ecr_repository" "order_service" {
  name = "order-service"
}

# ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = "production-cluster"
}

# Aurora PostgreSQL
resource "aws_rds_cluster" "aurora" {
  cluster_identifier = "order-db"
  engine             = "aurora-postgresql"
  engine_version     = "15.2"
  database_name      = "orderdb"
  master_username    = "admin"
  master_password    = var.db_password
  skip_final_snapshot = true
}

# Secrets Manager
resource "aws_secretsmanager_secret" "db_password" {
  name = "/prod/order-service/db-password"
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = var.db_password
}

# ECS Task Definition
resource "aws_ecs_task_definition" "order_service" {
  family                   = "order-service"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"

  container_definitions = jsonencode([{
    name  = "order-service"
    image = var.docker_image
    portMappings = [{
      containerPort = 8080
    }]
    environment = [{
      name  = "SPRING_PROFILES_ACTIVE"
      value = var.environment
    }]
    secrets = [{
      name      = "DB_PASSWORD"
      valueFrom = aws_secretsmanager_secret.db_password.arn
    }]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = "/ecs/order-service"
        "awslogs-region"        = "us-east-1"
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])
}

# Application Load Balancer
resource "aws_lb" "main" {
  name               = "order-service-alb"
  internal           = false
  load_balancer_type = "application"
  subnets            = module.vpc.public_subnets
}

# ECS Service
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
}

# Auto Scaling
resource "aws_appautoscaling_target" "ecs" {
  max_capacity       = 10
  min_capacity       = 2
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.order_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "cpu" {
  name               = "cpu-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value = 70.0  # scale when CPU > 70%
  }
}

# Outputs
output "alb_dns" {
  value = aws_lb.main.dns_name
}

output "ecr_url" {
  value = aws_ecr_repository.order_service.repository_url
}
```

---

## 8. Workspaces — Multiple Environments

```hcl
# use same code for dev/staging/prod
terraform workspace new dev
terraform workspace new staging
terraform workspace new prod

# switch workspace
terraform workspace select prod

# use workspace in config
resource "aws_ecs_cluster" "main" {
  name = "cluster-${terraform.workspace}"  # cluster-prod, cluster-dev
}
```

---

## 9. terraform.tfvars — Environment Config

```hcl
# dev.tfvars
environment   = "dev"
docker_image  = "123.dkr.ecr.us-east-1.amazonaws.com/order:dev"
desired_count = 1

# prod.tfvars
environment   = "prod"
docker_image  = "123.dkr.ecr.us-east-1.amazonaws.com/order:latest"
desired_count = 3

# apply with specific vars file
terraform apply -var-file="prod.tfvars"
```

---

## 10. Terraform in CI/CD Pipeline

```yaml
# GitHub Actions — Terraform pipeline
name: Terraform Deploy

on:
  push:
    branches: [main]

jobs:
  terraform:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v2

      - name: Terraform Init
        run: terraform init

      - name: Terraform Plan
        run: terraform plan -var-file="prod.tfvars"

      - name: Terraform Apply
        run: terraform apply -auto-approve -var-file="prod.tfvars"
        env:
          AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
          AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          TF_VAR_db_password: ${{ secrets.DB_PASSWORD }}
```

---

## 11. Terraform vs Others

| | Terraform | CloudFormation | Ansible | Pulumi |
|---|---|---|---|---|
| **By** | HashiCorp | AWS | Red Hat | Pulumi |
| **Language** | HCL | JSON/YAML | YAML | Python/JS/Go |
| **Multi-cloud** | ✅ | ❌ AWS only | ✅ | ✅ |
| **State** | tfstate file | AWS managed | No state | State backend |
| **Config mgmt** | ❌ | ❌ | ✅ | ❌ |
| **Open source** | ✅ | ❌ | ✅ | ✅ |
| **Best for** | IaC multi-cloud | AWS native | Config mgmt | Code-first IaC |

---

## 12. File Structure

```
project/
├── main.tf           ← main resources
├── variables.tf      ← input variables
├── outputs.tf        ← output values
├── providers.tf      ← provider config
├── terraform.tf      ← terraform + backend config
├── locals.tf         ← local values
├── dev.tfvars        ← dev environment values
├── prod.tfvars       ← prod environment values
└── modules/
    ├── ecs-service/
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    └── rds/
        ├── main.tf
        ├── variables.tf
        └── outputs.tf
```

---

## 13. Common Interview Questions

| Question | Answer |
|---|---|
| **What is Terraform** | IaC tool to provision cloud infrastructure using declarative HCL code |
| **Terraform vs CloudFormation** | Terraform = multi-cloud + open source, CloudFormation = AWS only |
| **What is state file** | JSON file tracking all resources Terraform manages |
| **Why remote state** | Team collaboration — shared state, no conflicts |
| **What is state locking** | DynamoDB prevents concurrent applies causing conflicts |
| **What is plan** | Dry run — preview changes without creating anything |
| **What is module** | Reusable group of resources — write once, use many times |
| **Idempotent** | Run terraform apply 10 times → same result ✅ |
| **Import existing** | `terraform import` — bring existing resources under Terraform management |
| **Destroy specific** | `terraform destroy -target=resource_type.name` |
| **Sensitive variable** | `sensitive = true` — hides value from logs and output |
| **Data source** | Read existing infrastructure not managed by Terraform |
| **Workspace** | Multiple environments (dev/staging/prod) from same code |

---

## 14. Best Practices

```
✅ Store state remotely (S3 + DynamoDB lock)
✅ Use modules for reusable infrastructure
✅ Use workspaces or separate state per environment
✅ Never hardcode secrets — use variables + Secrets Manager
✅ Always run terraform plan before apply
✅ Version pin providers and modules
✅ Use terraform fmt and validate in CI/CD
✅ Tag all resources (environment, team, managed-by)
✅ Use sensitive = true for passwords
✅ Review plan output carefully before apply
❌ Never edit state file manually
❌ Never commit .tfvars with secrets to Git
❌ Never run terraform apply without plan first
```
