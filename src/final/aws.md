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

