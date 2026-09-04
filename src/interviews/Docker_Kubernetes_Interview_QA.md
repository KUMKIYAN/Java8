# Docker & Kubernetes — Interview Q&A
> 10 Questions + DaemonSet with Correct Answers & Code Snippets

---

## Q1. What is Docker? How is it different from a Virtual Machine?

### Answer
```
Docker = containerization platform
→ package application + dependencies + runtime ✅
→ runs consistently on any machine ✅
→ solves "works on my machine" problem ✅
→ lightweight + fast start up✅
→ shares host OS kernel ✅

Problem it solves:
→ developer machine = works ✅
→ prod server = fails ❌ (different Java, libs)
→ Docker = same container everywhere ✅

Docker vs VM:
VM:
→ full OS per VM (GBs) ❌
→ slow startup (minutes) ❌
→ heavy resource usage ❌

Docker:
→ shares host OS kernel ✅
→ fast startup (seconds) ✅
→ lightweight (MBs) ✅
→ more containers per machine ✅
```

```dockerfile
# Dockerfile — Spring Boot ✅
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/order-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseZGC", "-Xmx768m", "-jar", "app.jar"]
```

```bash
# Docker commands ✅
docker build -t order-service:1.0 .    # build image
docker run -p 8080:8080 order-service  # run container
docker push myregistry/order-service   # push to registry
docker pull myregistry/order-service   # pull from registry
docker ps                              # list running containers
docker stop <container-id>             # stop container
docker images                          # list images
docker logs <container-id>             # view logs
```

| | Docker Container | Virtual Machine |
|---|---|---|
| **OS** | Shares host kernel ✅ | Full OS per VM ❌ |
| **Size** | MBs ✅ | GBs ❌ |
| **Startup** | Seconds ✅ | Minutes ❌ |
| **Resources** | Lightweight ✅ | Heavy ❌ |
| **Use for** | Microservices ✅ | Legacy apps |

---

## Q2. What is a Dockerfile? Key instructions?

### Answer
```
Dockerfile = text file with build instructions
→ tells Docker HOW to build image ✅
→ each instruction = one layer ✅
→ layers cached → faster rebuild ✅

Key instructions:
FROM       → base image ✅
WORKDIR    → set working directory ✅
COPY       → copy files from host ✅
RUN        → execute command at BUILD time ✅
ENV        → set environment variables ✅
ARG        → build time variable ✅
EXPOSE     → document port (not open) ✅
ENTRYPOINT → main command ✅
CMD        → default args (overridable) ✅
HEALTHCHECK→ container health check ✅
```

```dockerfile
# Complete Spring Boot Dockerfile ✅
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="kiyan@example.com"

WORKDIR /app

COPY target/order-service.jar app.jar

RUN mkdir -p /app/logs

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx768m"

ARG BUILD_VERSION=1.0

EXPOSE 8080

VOLUME ["/app/logs"]

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=prod"]
```

```dockerfile
# Multi-stage build — optimized ✅
# Stage 1 — BUILD
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline        # cache deps ✅
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 — RUNTIME (smaller image) ✅
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/order-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseZGC", "-jar", "app.jar"]
# builder image discarded → only JRE + JAR ✅
```

| Instruction | When runs | Purpose |
|---|---|---|
| `FROM` | Build time | Base image ✅ |
| `WORKDIR` | Build time | Set directory ✅ |
| `COPY` | Build time | Copy files ✅ |
| `RUN` | Build time | Execute command ✅ |
| `ENV` | Build + Run | Environment var ✅ |
| `EXPOSE` | Documentation | Document port ✅ |
| `ENTRYPOINT` | Run time | Main command ✅ |
| `CMD` | Run time | Default args ✅ |
| `HEALTHCHECK` | Run time | Health check ✅ |

---

## Q3. What is Docker Compose? How used for local development?

### Answer
```
Docker Compose:
→ define multiple containers in one file ✅
→ docker-compose.yml ✅
→ start/stop all containers with one command ✅
→ startup order with depends_on ✅
→ networking between containers ✅
→ volumes for data persistence ✅
→ perfect for local development ✅

Without Compose:
→ start MySQL manually ❌
→ start Kafka manually ❌
→ manage ports manually ❌

With Compose:
→ docker compose up → everything starts ✅
→ docker compose down → everything stops ✅
```

```yaml
# docker-compose.yml — complete local setup ✅
version: "3.8"

services:

  order-service:
    build: .
    container_name: order-service
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DB_HOST: mysql
      DB_PASSWORD: secret
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    depends_on:
      mysql:
        condition: service_healthy     # wait for healthy ✅
      kafka:
        condition: service_started
    networks:
      - app-network

  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: secret
      MYSQL_DATABASE: orderdb
    volumes:
      - mysql-data:/var/lib/mysql      # persist data ✅
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - app-network

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    networks:
      - app-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - app-network

  mongodb:
    image: mongo:7.0
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db
    networks:
      - app-network

volumes:
  mysql-data:
  mongo-data:

networks:
  app-network:
    driver: bridge
```

```bash
# Docker Compose commands ✅
docker compose up              # start all ✅
docker compose up -d           # background ✅
docker compose down            # stop + remove ✅
docker compose down -v         # stop + remove volumes ✅
docker compose logs -f order-service # follow logs ✅
docker compose ps              # list services ✅
docker compose restart order-service # restart ✅
docker compose build           # rebuild images ✅
docker compose exec mysql bash # exec into container ✅
```

---

## Q4. What is Kubernetes? Problems it solves over Docker?

### Answer
```
Kubernetes = container orchestration platform
→ manages containers at scale ✅
→ auto scaling ✅
→ self healing ✅
→ load balancing ✅
→ rolling deployments ✅
→ service discovery ✅

Problems Docker alone cannot solve:
→ container crashes → no auto restart ❌
→ high traffic → no auto scaling ❌
→ new version deploy → downtime ❌
→ multiple hosts → no coordination ❌
→ service discovery → no built-in ❌

Kubernetes solves all ✅

Key concepts:
Cluster    = group of nodes ✅
Node       = single machine ✅
Pod        = smallest unit ✅
Deployment = manages pods ✅
Service    = exposes pods ✅
ConfigMap  = store config ✅
Secret     = store secrets ✅
Ingress    = HTTP routing ✅
Namespace  = logical isolation ✅
```

```yaml
# Deployment ✅
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
  strategy:
    type: RollingUpdate        # zero downtime ✅
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    spec:
      containers:
        - name: order-service
          image: myregistry/order-service:latest
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 60
```

| | Docker alone | Kubernetes |
|---|---|---|
| **Auto restart** | ❌ No | ✅ Yes |
| **Auto scaling** | ❌ No | ✅ Yes |
| **Rolling deploy** | ❌ No | ✅ Yes |
| **Load balancing** | ❌ No | ✅ Yes |
| **Self healing** | ❌ No | ✅ Yes |
| **Multi-host** | ❌ No | ✅ Yes |

---

## Q5. What is a Pod? Difference between Pod and Container?

### Answer
```
Container:
→ running instance of Docker image ✅
→ one process typically ✅
→ isolated environment ✅

Pod:
→ wrapper around one or more containers ✅
→ containers share same network (localhost) ✅
→ containers share same storage volumes ✅
→ containers share same IP address ✅
→ smallest deployable unit in K8s ✅
→ ephemeral — can die and restart ✅

Usually one container per pod ✅
Sidecar pattern = multiple containers in pod ✅

Pod lifecycle:
→ Pending   → being scheduled
→ Running   → at least one container running ✅
→ Succeeded → all containers completed ✅
→ Failed    → containers failed ❌
```

```yaml
# Single container pod ✅
apiVersion: v1
kind: Pod
metadata:
  name: order-service-pod
spec:
  containers:
    - name: order-service
      image: myregistry/order-service:latest
      ports:
        - containerPort: 8080

---
# Multi-container pod — sidecar ✅
apiVersion: v1
kind: Pod
metadata:
  name: order-service-pod
spec:
  containers:
    # main container ✅
    - name: order-service
      image: myregistry/order-service:latest
      ports:
        - containerPort: 8080

    # sidecar — log collector ✅
    - name: log-collector
      image: fluentd:latest
      volumeMounts:
        - name: log-volume
          mountPath: /var/log

  volumes:
    - name: log-volume
      emptyDir: {}
```

```bash
kubectl get pods                          # list pods ✅
kubectl describe pod order-service-xyz    # details ✅
kubectl logs order-service-xyz            # logs ✅
kubectl logs order-service-xyz -f         # follow ✅
kubectl exec -it order-service-xyz -- bash # exec in ✅
kubectl delete pod order-service-xyz      # delete ✅
```

| | Container | Pod |
|---|---|---|
| **Definition** | Running image | Wrapper around containers |
| **Network** | Own network | Shared with pod containers ✅ |
| **IP** | Container IP | One IP per pod ✅ |
| **Managed by** | Docker | Kubernetes ✅ |

---

## Q6. Deployment vs ReplicaSet vs StatefulSet?

### Answer
```
Deployment:
→ manages STATELESS applications ✅
→ creates ReplicaSet automatically ✅
→ rolling updates + rollback ✅
→ use for: Spring Boot, APIs ✅

ReplicaSet:
→ ensures N pods always running ✅
→ you rarely create directly ❌
→ Deployment creates it for you ✅
→ Deployment → ReplicaSet → Pods ✅

StatefulSet:
→ manages STATEFUL applications ✅
→ pods have stable names (pod-0, pod-1) ✅
→ pods have stable storage ✅
→ ordered startup/shutdown ✅
→ each pod has OWN persistent volume ✅
→ use for: MySQL, Kafka, Zookeeper ✅
```

```yaml
# Deployment — stateless ✅
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
  template:
    spec:
      containers:
        - name: order-service
          image: order-service:v2.0

---
# StatefulSet — stateful ✅
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
spec:
  serviceName: mysql
  replicas: 3
  template:
    spec:
      containers:
        - name: mysql
          image: mysql:8.0
          volumeMounts:
            - name: mysql-data
              mountPath: /var/lib/mysql

  # each pod gets OWN volume ✅
  volumeClaimTemplates:
    - metadata:
        name: mysql-data
      spec:
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: 10Gi

# pods created in order ✅
# mysql-0 → mysql-1 → mysql-2
# stable DNS: mysql-0.mysql.default.svc.cluster.local ✅
```

| | Deployment | ReplicaSet | StatefulSet |
|---|---|---|---|
| **Use for** | Stateless ✅ | Managed by Deploy | Stateful ✅ |
| **Pod names** | Random (pod-xyz) | Random | Stable (pod-0) ✅ |
| **Storage** | Shared | Shared | Own per pod ✅ |
| **Startup** | Any order | Any order | Ordered ✅ |
| **Example** | Spring Boot ✅ | — | MySQL, Kafka ✅ |

---

## Q7. What is a Kubernetes Service? Types?

### Answer
```
Service = stable endpoint to access pods ✅

Problem:
→ pods die → restart with NEW IP ❌
→ caller cannot track changing IPs ❌

Service solves:
→ fixed IP + DNS name ✅
→ load balances across pods ✅
→ pods come and go — service stays stable ✅

Four types:
1. ClusterIP   → internal only (default) ✅
2. NodePort    → node IP + port (dev/test) ✅
3. LoadBalancer→ external internet (prod) ✅
4. ExternalName→ external DNS ✅
```

```yaml
# ClusterIP — internal only ✅
apiVersion: v1
kind: Service
metadata:
  name: order-service
spec:
  type: ClusterIP
  selector:
    app: order-service
  ports:
    - port: 80
      targetPort: 8080
# http://order-service:80 inside cluster ✅

---
# NodePort — dev/testing ✅
apiVersion: v1
kind: Service
spec:
  type: NodePort
  ports:
    - port: 80
      targetPort: 8080
      nodePort: 30080     # range 30000-32767
# http://node-ip:30080 ✅

---
# LoadBalancer — production public ✅
apiVersion: v1
kind: Service
spec:
  type: LoadBalancer      # creates cloud ALB ✅
  ports:
    - port: 80
      targetPort: 8080
# accessible from internet ✅

---
# Ingress — HTTP routing ✅
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: order-ingress
spec:
  rules:
    - host: api.myapp.com
      http:
        paths:
          - path: /orders
            pathType: Prefix
            backend:
              service:
                name: order-service
                port:
                  number: 80
          - path: /payments
            pathType: Prefix
            backend:
              service:
                name: payment-service
                port:
                  number: 80
```

| Type | Access | Use case |
|---|---|---|
| **ClusterIP** | Internal only | Microservice to microservice ✅ |
| **NodePort** | Node IP + port | Dev/testing ✅ |
| **LoadBalancer** | External internet | Production public API ✅ |
| **ExternalName** | External DNS | External DB/API ✅ |
| **Ingress** | HTTP routing | Multiple services ✅ |

---

## Q8. What is ConfigMap and Secret in Kubernetes?

### Answer
```
ConfigMap:
→ store non-sensitive config ✅
→ env vars, config files ✅
→ update without rebuild ✅
→ NOT encrypted ❌

Secret:
→ store sensitive data ✅
→ base64 encoded ✅
→ use Azure Key Vault / AWS Secrets Manager ✅

Two ways to use:
1. environment variables ✅
2. mounted as files ✅
```

```yaml
# ConfigMap ✅
apiVersion: v1
kind: ConfigMap
metadata:
  name: order-service-config
data:
  SPRING_PROFILES_ACTIVE: "prod"
  SERVER_PORT: "8080"
  KAFKA_BOOTSTRAP_SERVERS: "kafka:9092"
  application.yml: |
    spring:
      application:
        name: order-service

---
# Secret ✅
apiVersion: v1
kind: Secret
metadata:
  name: order-service-secret
type: Opaque
data:
  DB_PASSWORD: bXlzZWNyZXRwYXNzd29yZA==  # base64 ✅
  JWT_SECRET:  c2VjcmV0a2V5MTIz

---
# Deployment — use both ✅
spec:
  template:
    spec:
      containers:
        - name: order-service
          # specific env from ConfigMap ✅
          env:
            - name: SPRING_PROFILES_ACTIVE
              valueFrom:
                configMapKeyRef:
                  name: order-service-config
                  key: SPRING_PROFILES_ACTIVE
            # specific env from Secret ✅
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: order-service-secret
                  key: DB_PASSWORD
          # all ConfigMap as env ✅
          envFrom:
            - configMapRef:
                name: order-service-config
            - secretRef:
                name: order-service-secret
```

```java
// Spring Boot reads env vars ✅
// application.yml
spring:
  datasource:
    password: ${DB_PASSWORD}     // from Secret ✅
    url: ${DB_URL}               // from ConfigMap ✅
server:
  port: ${SERVER_PORT:8080}      // default 8080 ✅
```

```bash
kubectl create configmap order-config \
    --from-literal=SPRING_PROFILES_ACTIVE=prod ✅
kubectl get configmaps ✅
kubectl edit configmap order-config   # live edit ✅
kubectl create secret generic order-secret \
    --from-literal=DB_PASSWORD=mysecret ✅
kubectl get secrets ✅
```

| | ConfigMap | Secret |
|---|---|---|
| **Data** | Non-sensitive | Sensitive ✅ |
| **Encoding** | Plain text | Base64 ✅ |
| **Encrypted** | ❌ No | ❌ Default no |
| **Use for** | Config, env | Passwords, keys ✅ |

---

## Q9. What is HPA (Horizontal Pod Autoscaler)?

### Answer
```
HPA = Horizontal Pod Autoscaler
→ automatically scales pod count ✅
→ based on CPU, memory, custom metrics ✅
→ checks metrics every 15 seconds ✅
→ minReplicas → always keep this many ✅
→ maxReplicas → never exceed this ✅

Scale OUT → add pods when high load ✅
Scale IN  → remove pods when low load ✅

Requirements:
→ metrics-server installed ✅
→ resource requests defined in pod ✅
→ without requests → HPA cannot calculate % ❌
```

```yaml
# HPA ✅
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: order-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service

  minReplicas: 2             # always keep 2 ✅
  maxReplicas: 10            # max 10 ✅

  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70  # scale when CPU > 70% ✅

    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80  # scale when mem > 80% ✅

---
# Deployment MUST have resource requests ✅
spec:
  containers:
    - name: order-service
      resources:
        requests:
          cpu:    "250m"    # HPA uses for % calculation ✅
          memory: "512Mi"
        limits:
          cpu:    "500m"
          memory: "1024Mi"
```

```
HPA flow:

Normal: 2 pods, CPU = 30% → no scaling
High:   traffic spikes → CPU = 80%
        HPA: 2 × (80/70) = ~3 pods → scales to 3 ✅
        CPU drops to 50% per pod ✅
Peak:   CPU = 90% → scales to 5 → max 10 ✅
Low:    CPU = 20% → 5min cooldown → scales to 2 ✅
```

```bash
kubectl get hpa                        # list ✅
kubectl describe hpa order-service-hpa # details ✅
kubectl top pods                       # CPU/memory ✅
kubectl get hpa -w                     # watch changes ✅
```

| | HPA | VPA | KEDA |
|---|---|---|---|
| **Scales** | Pod count | Pod resources | Pod count ✅ |
| **Based on** | CPU/Memory | CPU/Memory | Events/Queues ✅ |
| **Scale to zero** | ❌ No | ❌ No | ✅ Yes |

---

## Q10. kubectl apply vs kubectl create? Most used commands?

### Answer
```
kubectl create:
→ creates resource ONLY if not exists ✅
→ FAILS if already exists ❌
→ imperative approach ❌

kubectl apply:
→ creates if not exists ✅
→ UPDATES if already exists ✅
→ declarative + idempotent ✅
→ safe to run multiple times ✅
→ ALWAYS use apply in production ✅
```

```bash
# kubectl create — fails if exists ❌
kubectl create -f deployment.yml
# Error: already exists ❌

# kubectl apply — safe always ✅
kubectl apply -f deployment.yml
# created   (first time) ✅
# configured (update) ✅
# unchanged  (no change) ✅
```

```bash
# ── Pods ──────────────────────────────────────────────────────
kubectl get pods                          # list ✅
kubectl get pods -n production            # namespace ✅
kubectl get pods -o wide                  # more details ✅
kubectl describe pod order-service-xyz    # details ✅
kubectl logs order-service-xyz            # logs ✅
kubectl logs order-service-xyz -f         # follow logs ✅
kubectl logs order-service-xyz --previous # crashed pod ✅
kubectl exec -it order-service-xyz -- bash # exec in ✅
kubectl delete pod order-service-xyz      # delete ✅

# ── Deployments ───────────────────────────────────────────────
kubectl get deployments                   # list ✅
kubectl apply -f deployment.yml           # create/update ✅
kubectl delete -f deployment.yml          # delete ✅
kubectl rollout status deployment/order-service  # watch ✅
kubectl rollout history deployment/order-service # history ✅
kubectl rollout undo deployment/order-service    # rollback ✅
kubectl set image deployment/order-service \
    order-service=myregistry/order-service:v2   # update ✅
kubectl scale deployment order-service \
    --replicas=5                          # scale ✅

# ── Services ──────────────────────────────────────────────────
kubectl get services                      # list ✅
kubectl get svc                           # short ✅
kubectl port-forward svc/order-service 8080:80 # local ✅

# ── ConfigMap + Secret ────────────────────────────────────────
kubectl get configmaps                    # list ✅
kubectl get secrets                       # list ✅
kubectl edit configmap order-config       # live edit ✅

# ── Namespace ─────────────────────────────────────────────────
kubectl get namespaces                    # list ✅
kubectl create namespace production       # create ✅
kubectl config set-context --current \
    --namespace=production               # switch ✅

# ── Cluster info ──────────────────────────────────────────────
kubectl get nodes                         # nodes ✅
kubectl top pods                          # CPU/memory ✅
kubectl top nodes                         # node usage ✅
kubectl get all                           # everything ✅
kubectl get events                        # events ✅

# ── Debug ─────────────────────────────────────────────────────
kubectl describe pod order-service-xyz    # events + status ✅
kubectl logs order-service-xyz --previous # before crash ✅
kubectl get events --sort-by='.lastTimestamp' # recent ✅
kubectl exec -it order-service-xyz \
    -- curl http://localhost:8080/actuator/health # health ✅

# ── Apply multiple ────────────────────────────────────────────
kubectl apply -f ./k8s/                   # entire folder ✅
```

---

## BONUS — DaemonSet

### What is DaemonSet?
```
DaemonSet = ensures ONE pod runs on EVERY node ✅

When new node added → pod automatically created ✅
When node removed  → pod automatically deleted ✅

When to use:
→ log collection (Fluentd, Logstash) ✅
→ monitoring agent (Prometheus Node Exporter) ✅
→ security scanning on every node ✅
→ network plugins (Calico, Flannel) ✅
→ anything that needs to run on ALL nodes ✅
```

```yaml
# DaemonSet — log collector on every node ✅
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: log-collector
  namespace: monitoring
spec:
  selector:
    matchLabels:
      app: log-collector
  template:
    metadata:
      labels:
        app: log-collector
    spec:
      containers:
        - name: fluentd
          image: fluentd:latest
          resources:
            requests:
              cpu:    "100m"
              memory: "200Mi"
            limits:
              cpu:    "200m"
              memory: "400Mi"
          volumeMounts:
            - name: var-log
              mountPath: /var/log
            - name: docker-logs
              mountPath: /var/lib/docker/containers

      volumes:
        - name: var-log
          hostPath:
            path: /var/log
        - name: docker-logs
          hostPath:
            path: /var/lib/docker/containers

      tolerations:              # run on master nodes too ✅
        - key: node-role.kubernetes.io/master
          effect: NoSchedule
          operator: Exists
```

```bash
kubectl get daemonsets          # list ✅
kubectl get ds                  # short form ✅
kubectl describe ds log-collector # details ✅
kubectl get pods -o wide        # see which node ✅
# every node shows one log-collector pod ✅
```

| | Deployment | DaemonSet | StatefulSet |
|---|---|---|---|
| **Pods** | N replicas anywhere | One per node ✅ | Ordered stable |
| **Use for** | Stateless apps | Node level agents ✅ | Databases |
| **Auto add** | No | When node added ✅ | No |
| **Example** | Spring Boot | Fluentd, Prometheus | MySQL, Kafka |

---

## Quick Reference — All Key Points

```
Docker         = package app + deps + runtime ✅
Dockerfile     = instructions to build image ✅
docker compose = run multiple containers locally ✅
Kubernetes     = orchestrate containers at scale ✅
Pod            = smallest K8s unit ✅
Deployment     = stateless + rolling update ✅
ReplicaSet     = ensures N pods running ✅
StatefulSet    = stateful + ordered + own storage ✅
DaemonSet      = one pod per node ✅
Service        = stable endpoint for pods ✅
ClusterIP      = internal only ✅
LoadBalancer   = external internet ✅
ConfigMap      = non-sensitive config ✅
Secret         = passwords, keys ✅
HPA            = auto scale pods on CPU/memory ✅
kubectl apply  = create OR update — use always ✅
kubectl create = create only — fails if exists ❌
```

| Topic | Key Point |
|---|---|
| Docker vs VM | Docker = MBs + seconds. VM = GBs + minutes ✅ |
| Dockerfile FROM | Base image — always first instruction ✅ |
| Multi-stage build | Small final image — discard builder ✅ |
| Compose depends_on | Control startup order ✅ |
| Pod vs Container | Pod = wrapper. containers share IP + storage ✅ |
| Deployment | Stateless. rolling update. most used ✅ |
| StatefulSet | Stateful. ordered. own volume per pod ✅ |
| DaemonSet | One pod per node. logs/monitoring ✅ |
| ClusterIP | Default. internal only ✅ |
| LoadBalancer | External. creates cloud LB ✅ |
| HPA minReplicas | Always keep this many pods ✅ |
| HPA maxReplicas | Never exceed this count ✅ |
| kubectl apply | Safe. idempotent. use always ✅ |
| kubectl rollout undo | Rollback deployment ✅ |
| kubectl top pods | Check CPU/memory usage ✅ |
