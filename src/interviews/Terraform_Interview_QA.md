# Terraform — Interview Q&A
> 3 Questions with Answers and Code Examples

---

## Q1. Difference between terraform plan, terraform apply and terraform state?

### Answer
```
terraform plan:
→ compares state vs code ✅
→ shows what will change ✅
→ + created, ~ updated, - destroyed ✅
→ no actual resource created ❌
→ like dry run ✅
→ safe to run anytime ✅

terraform apply:
→ executes the plan ✅
→ creates/updates/destroys resources ✅
→ updates state file after ✅
→ actual resource allocation ✅

terraform state:
→ JSON file tracking all resources ✅
→ resource ID + properties ✅
→ without state → duplicates created ❌
→ with state → knows what exists ✅
→ stored in S3 (remote) ✅
→ DynamoDB for locking ✅
→ one dev works → others locked ✅
```

```hcl
# terraform plan output ✅
# + = will be CREATED
# ~ = will be UPDATED
# - = will be DESTROYED

# Plan: 3 to add, 1 to change, 0 to destroy

# + resource "aws_ecs_cluster" "main" {
#     name = "payment-cluster"
#   }

# ~ resource "aws_ecs_service" "payment" {
#     desired_count = 2 → 3
#   }

# Remote state ✅
terraform {
  backend "s3" {
    bucket         = "my-terraform-state"
    key            = "payment/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-lock" # lock ✅
  }
}
```

```bash
# Commands ✅
terraform init                    # initialize ✅
terraform plan                    # preview ✅
terraform plan -out=tfplan        # save plan ✅
terraform apply                   # create ✅
terraform apply tfplan            # apply saved ✅
terraform apply -auto-approve     # no prompt ✅
terraform destroy                 # delete all ✅

# State commands ✅
terraform state list              # list resources ✅
terraform state show aws_ecs_cluster.main  # details ✅
terraform state rm aws_ecs_cluster.main    # remove ✅
terraform import aws_ecs_cluster.main <id> # import ✅
```

| | terraform plan | terraform apply |
|---|---|---|
| **Creates resources** | ❌ No | ✅ Yes |
| **Updates state** | ❌ No | ✅ Yes |
| **Safe to run** | ✅ Always | ⚠️ Careful |
| **Purpose** | Preview ✅ | Execute ✅ |

---

## Q2. What is a Terraform Module? Why use it?

### Answer
```
Module = reusable Terraform code ✅
→ like a Java method ✅
→ write ONCE ✅
→ reuse many times ✅
→ avoid code duplication ✅

Without module:
→ same ECS code copy-pasted
  for each service ❌
→ change one → update all ❌

With module:
→ write ECS code ONCE ✅
→ call module for each service ✅
→ change module → all updated ✅
```

```hcl
# ── Module definition ─────────────────────────────────────────
# modules/ecs-service/main.tf ✅

# input variables ✅
variable "service_name"   { type = string }
variable "cluster_id"     { type = string }
variable "desired_count"  { type = number }
variable "docker_image"   { type = string }
variable "container_port" { type = number }

# ECS Task Definition ✅
resource "aws_ecs_task_definition" "this" {
  family                   = var.service_name
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "512"
  memory                   = "1024"

  container_definitions = jsonencode([{
    name  = var.service_name
    image = var.docker_image       # dynamic ✅
    portMappings = [{
      containerPort = var.container_port
    }]
  }])
}

# ECS Service ✅
resource "aws_ecs_service" "this" {
  name            = var.service_name    # dynamic ✅
  cluster         = var.cluster_id
  task_definition = aws_ecs_task_definition.this.arn
  desired_count   = var.desired_count   # dynamic ✅
  launch_type     = "FARGATE"
}

# output ✅
output "service_name" {
  value = aws_ecs_service.this.name
}
```

```hcl
# ── Use module 3 times — main.tf ──────────────────────────────

# payment-service ✅
module "payment_service" {
  source         = "./modules/ecs-service" # ✅
  service_name   = "payment-service"
  cluster_id     = aws_ecs_cluster.main.id
  desired_count  = 3
  docker_image   = "myregistry/payment:latest"
  container_port = 8080
}

# order-service ✅
module "order_service" {
  source         = "./modules/ecs-service" # same ✅
  service_name   = "order-service"
  cluster_id     = aws_ecs_cluster.main.id
  desired_count  = 2
  docker_image   = "myregistry/order:latest"
  container_port = 8081
}

# notification-service ✅
module "notification_service" {
  source         = "./modules/ecs-service" # same ✅
  service_name   = "notification-service"
  cluster_id     = aws_ecs_cluster.main.id
  desired_count  = 1
  docker_image   = "myregistry/notification:latest"
  container_port = 8082
}
```

```
Folder structure:
terraform/
├── main.tf              # use modules ✅
├── variables.tf         # input vars ✅
├── outputs.tf           # outputs ✅
└── modules/
    ├── ecs-service/     # ECS module ✅
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── aurora/          # DB module ✅
    │   ├── main.tf
    │   └── variables.tf
    └── alb/             # ALB module ✅
        ├── main.tf
        └── variables.tf
```

```hcl
# Public modules from Terraform Registry ✅
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.0.0"
  name    = "payment-vpc"
  cidr    = "10.0.0.0/16"
}
```

---

## Q3. Difference between Terraform variables, locals and outputs?

### Answer
```
variables = INPUT to Terraform ✅
            like method parameters in Java ✅
            pass from outside ✅

locals    = INTERNAL calculation ✅
            like local variables in Java method ✅
            calculate inside Terraform ✅

outputs   = RETURN values ✅
            like method return value in Java ✅
            share with others ✅
```

```hcl
# ── variables.tf — INPUT ──────────────────────────────────────
variable "environment" {
  type        = string
  description = "Environment name"
  default     = "dev"      # optional default ✅
}

variable "db_password" {
  type      = string
  sensitive = true          # hidden in logs ✅
}

variable "desired_count" {
  type    = number
  default = 2
}

variable "allowed_ports" {
  type    = list(number)
  default = [8080, 8081]
}

# use variable ✅
resource "aws_ecs_service" "payment" {
  name          = "payment-${var.environment}" # ✅
  desired_count = var.desired_count            # ✅
}
```

```bash
# pass variable values ✅
terraform apply -var="environment=prod"
terraform apply -var-file="prod.tfvars"
```

```hcl
# prod.tfvars ✅
environment   = "prod"
desired_count = 5
db_password   = "supersecret"
```

```hcl
# ── locals.tf — INTERNAL ──────────────────────────────────────
locals {
  # combine values ✅
  service_name = "payment-${var.environment}"
  # payment-prod ✅
  # payment-dev ✅

  # common tags — reuse everywhere ✅
  common_tags = {
    Environment = var.environment
    Team        = "payment"
    ManagedBy   = "terraform"
  }

  # calculation ✅
  max_capacity = var.desired_count * 2

  # condition ✅
  is_production = var.environment == "prod" ? true : false
}

# use locals ✅
resource "aws_ecs_service" "payment" {
  name = local.service_name   # ✅
  tags = local.common_tags    # ✅
}

resource "aws_rds_cluster" "aurora" {
  cluster_identifier = local.service_name # ✅
  tags               = local.common_tags  # ✅
}
```

```hcl
# ── outputs.tf — RETURN ───────────────────────────────────────

# ALB DNS name ✅
output "alb_dns_name" {
  value       = aws_lb.main.dns_name
  description = "Load balancer DNS name"
}

# Aurora endpoints ✅
output "aurora_writer_endpoint" {
  value = aws_rds_cluster.aurora.endpoint
}

output "aurora_reader_endpoint" {
  value = aws_rds_cluster.aurora.reader_endpoint
}

# sensitive output ✅
output "db_password" {
  value     = var.db_password
  sensitive = true # hidden in logs ✅
}
```

```bash
# see outputs after apply ✅
terraform output
# alb_dns_name = "payment-alb-123.us-east-1.elb.amazonaws.com"
# aurora_writer_endpoint = "aurora.cluster.rds.amazonaws.com"

# specific output ✅
terraform output alb_dns_name
```

```hcl
# ── All together — real example ───────────────────────────────

# variables.tf — INPUT ✅
variable "environment"   { default = "prod" }
variable "desired_count" { default = 2 }
variable "db_password"   { sensitive = true }

# locals.tf — INTERNAL ✅
locals {
  service_name = "payment-${var.environment}"
  common_tags  = {
    Environment = var.environment
    Team        = "payment"
  }
  max_capacity = var.desired_count * 3
}

# main.tf — USE ✅
resource "aws_ecs_service" "payment" {
  name          = local.service_name   # local ✅
  desired_count = var.desired_count    # variable ✅
  tags          = local.common_tags    # local ✅
}

# outputs.tf — RETURN ✅
output "service_name" {
  value = aws_ecs_service.payment.name
}
output "alb_url" {
  value = aws_lb.main.dns_name
}
```

| | Variables | Locals | Outputs |
|---|---|---|---|
| **Purpose** | Input ✅ | Internal calc ✅ | Return value ✅ |
| **Like Java** | Parameters | Local variables | Return ✅ |
| **Defined in** | variables.tf | locals.tf | outputs.tf |
| **Access** | `var.name` | `local.name` | `output` command |
| **Sensitive** | ✅ Yes | ❌ No | ✅ Yes |
| **Default** | ✅ Optional | N/A | N/A |

---

## Quick Reference — All Terraform Concepts

| Concept | Purpose |
|---|---|
| `terraform plan` | Preview changes — no creation ✅ |
| `terraform apply` | Create/update/destroy resources ✅ |
| `terraform state` | Track what exists in AWS ✅ |
| `S3 backend` | Remote shared state ✅ |
| `DynamoDB lock` | One apply at a time ✅ |
| `Module` | Reusable code — write once use many ✅ |
| `variable` | INPUT — pass from outside ✅ |
| `local` | INTERNAL — calculate inside ✅ |
| `output` | RETURN — share with others ✅ |
| `sensitive = true` | Hidden in logs ✅ |
| `count` | Create multiple resources ✅ |
| `var.name` | Access variable ✅ |
| `local.name` | Access local ✅ |
| `-var-file` | Pass values from file ✅ |
| `tfvars` | Variable values file ✅ |
