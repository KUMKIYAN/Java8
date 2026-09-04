# Terraform — Short Notes (All Files Combined)

---

## What is Terraform
```
What-          IaC tool by HashiCorp | provision cloud infra using code | declarative
Language-      HCL = HashiCorp Configuration Language
Multi-cloud-   AWS | Azure | GCP | 1000+ providers | cloud agnostic
vs Manual-     no manual clicks | version controlled | repeatable | identical dev/prod
vs CloudFormation- Terraform=multi-cloud+open source | CloudFormation=AWS only
vs Ansible-    Terraform=infra provisioning | Ansible=config management
```

---

## Core Concepts
```
Provider-      which cloud to use | aws_region | azurerm | google
Resource-      infra component | resource "aws_s3_bucket" "name" { }
Variable-      input param | var.db_password | sensitive=true hides from logs
Output-        expose values after apply | output "db_endpoint" { value = ... }
Data source-   read EXISTING infra not managed by Terraform | data "aws_vpc" "existing"
Local-         internal computed values | local.service_name | local.common_tags
Module-        reusable group of resources | like function | write once use many times
```

---

## Commands
```
init-          download providers + modules ✅
plan-          dry run preview changes | nothing created ✅
plan -out-     save plan to file ✅
apply-         create/update infra ✅
apply -auto-approve- no confirmation prompt ✅
destroy-       delete all infra ✅
destroy -target- delete specific resource ✅
fmt-           format .tf files ✅
validate-      check syntax ✅
show-          current state ✅
state list-    list all resources ✅
state rm-      remove from state without destroying ✅
import-        bring existing resource under Terraform ✅
output-        show output values ✅
workspace-     manage multiple environments ✅
```

---

## State File
```
What-          terraform.tfstate | JSON | tracks all managed resources
Purpose-       compares desired(code) vs actual(real infra) | determines changes
Remote state-  S3 bucket | shared across team | no conflicts
State locking- DynamoDB table | prevents concurrent apply | first gets lock others wait
Never-         never edit manually ❌ | never commit secrets ❌
```

---

## Variables vs Locals vs Outputs
```
Variables-     INPUT | from outside | var.name | parameterize config | sensitive=true
Locals-        INTERNAL | computed inside | local.name | common_tags reuse
Outputs-       RETURN values | after apply | share between modules | alb_dns
tfvars-        dev.tfvars | prod.tfvars | apply -var-file="prod.tfvars"
```

---

## Modules
```
What-          reusable group of resources | like function | parameterize with variables
Structure-     modules/ecs-service/main.tf | variables.tf | outputs.tf
Use-           module "order_service" { source = "./modules/ecs-service" }
Public-        terraform-aws-modules/vpc/aws | Terraform Registry
Benefits-      write once | use for order+payment+inventory services ✅
```

---

## VPC + Networking
```
VPC-           Virtual Private Cloud | private network in AWS | isolated
CIDR-          Classless Inter-Domain Routing | IP range notation
/16-           65536 IPs | 2^16 | 10.0.x.x range | most common VPC ✅
/24-           256 IPs | 2^8 | subnet size ✅
Public subnet- internet facing | ALB lives here ✅
Private subnet- no internet | ECS tasks + DB live here ✅
Multi VPC-     same CIDR ok if isolated | conflict when peered → use different CIDRs
```

---

## ECS on Fargate — Key Resources
```
aws_ecs_cluster-         logical group of services ✅
aws_ecs_task_definition- blueprint | CPU | memory | docker image | env vars | secrets
aws_ecs_service-         running tasks | desired_count | load balancer | network config
aws_ecr_repository-      Docker image registry ✅
aws_lb-                  Application Load Balancer | public subnets ✅
aws_lb_target_group-     routes traffic to ECS tasks ✅
aws_security_group-      firewall rules | ingress 443 | egress all ✅
```

---

## Auto Scaling
```
Target-        aws_appautoscaling_target | min=2 | max=10 | ECS service
CPU policy-    ECSServiceAverageCPUUtilization | target_value=70% ✅
Memory policy- ECSServiceAverageMemoryUtilization | target_value=80% ✅
ALB policy-    ALBRequestCountPerTarget | 1000 req/task ✅
Scheduled-     cron | Black Friday scale up | Nov 6am → scale down 10pm ✅
Custom-        KafkaConsumerLag | SQS depth | PaymentService namespace ✅
```

---

## Aurora RDS
```
aws_rds_cluster-         cluster config | engine | database_name | master credentials
aws_rds_cluster_instance- each = one node | instance_class = compute | count for readers
promotion_tier-          0=highest priority | promoted on writer failure ✅
storage_encrypted-       true | KMS encryption ✅
deletion_protection-     true in prod ✅
```

---

## Secrets Manager
```
aws_secretsmanager_secret-         creates empty container | name + KMS key
aws_secretsmanager_secret_version- actual value | secret_string
Inject ECS-    secrets=[{name="DB_PASSWORD", valueFrom=secret.arn}] ✅
Two resources- secret=locker(empty) | version=value inside locker ✅
```

---

## Security Group
```
ingress-       inbound rules | who can come IN | port 443 HTTPS from 0.0.0.0/0
egress-        outbound rules | ALB can send anywhere | protocol=-1 all
ALB SG-        ingress 443 internet | egress all ✅
ECS SG-        ingress 8080 from ALB only | egress all ✅
```

---

## Workspaces + Environments
```
Workspace-     dev | staging | prod | same code different state
Commands-      terraform workspace new dev | select prod | show current
Use in code-   name = "cluster-${terraform.workspace}" ✅
tfvars-        dev.tfvars | prod.tfvars | separate configs per env ✅
```

---

## CI/CD Pipeline
```
GitHub Actions- checkout → setup terraform → init → plan → apply ✅
Secrets-        AWS_ACCESS_KEY_ID | AWS_SECRET_ACCESS_KEY | TF_VAR_db_password
Jenkins-        similar stages | credentials plugin for secrets ✅
Best practice-  plan before apply | auto-approve only in CD ✅
```

---

## File Structure
```
main.tf-       main resources ✅
variables.tf-  input variables ✅
outputs.tf-    output values ✅
providers.tf-  provider config ✅
terraform.tf-  backend + version config ✅
locals.tf-     computed values ✅
dev.tfvars-    dev environment values ✅
prod.tfvars-   prod environment values ✅
modules/-      reusable modules ✅
```

---

## Interview Q&A
```
What is Terraform-        IaC tool | HCL | declarative | multi-cloud ✅
State file-               JSON tracks managed resources ✅
Remote state-             S3 shared | DynamoDB locking | team collaboration ✅
Plan vs Apply-            plan=preview | apply=execute ✅
Module-                   reusable resources | write once use many ✅
Idempotent-               apply 10 times = same result ✅
Import-                   bring existing resource under Terraform ✅
Data source-              read existing infra not managed by Terraform ✅
Sensitive variable-       sensitive=true hides from logs ✅
Workspace-                multiple envs from same code ✅
State locking-            DynamoDB prevents concurrent conflicts ✅
Terraform vs Ansible-     Terraform=provision infra | Ansible=configure servers ✅
```

---

## Best Practices
```
DO-    remote state S3+DynamoDB | modules | workspaces | plan before apply ✅
DO-    sensitive=true passwords | tag all resources | version pin providers ✅
DO-    fmt+validate in CI/CD | never hardcode secrets ✅
DONT-  edit state manually ❌ | commit tfvars secrets to Git ❌
DONT-  apply without plan ❌ | store state locally in team ❌
```

---

## Quick Reference
```
HCL-           HashiCorp Configuration Language ✅
tfstate-       tracks all managed resources ✅
/16-           65536 IPs ✅
/24-           256 IPs ✅
CIDR-          Classless Inter-Domain Routing ✅
plan-          dry run no changes ✅
apply-         execute changes ✅
module-        reusable code ✅
workspace-     multiple environments ✅
state lock-    DynamoDB prevents conflict ✅
sensitive-     hides from logs ✅
data source-   read existing infra ✅
local-         internal computed value ✅
output-        return value after apply ✅
```
