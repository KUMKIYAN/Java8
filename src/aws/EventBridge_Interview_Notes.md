# AWS EventBridge — Interview Notes
> Complete reference for EventBridge concepts and usage

---

## What is EventBridge?

```
EventBridge = AWS serverless event bus ✅
→ connects different AWS services ✅
→ routes events from source to target ✅
→ like a smart router ✅
→ formerly called CloudWatch Events ✅

Three types:
1. Default Event Bus   → AWS service events ✅
2. Custom Event Bus    → your app events ✅
3. Partner Event Bus   → SaaS integrations ✅
```

---

## Key Components

```
Event:
→ JSON message describing what happened ✅
→ who sent it + what changed ✅

Event Bus:
→ receives events ✅
→ routes to targets ✅

Rules:
→ filter which events to route ✅
→ pattern matching ✅

Targets:
→ where to send matched events ✅
→ Lambda, SQS, SNS, ECS, API Gateway ✅

Schedule:
→ cron or rate based ✅
→ trigger Lambda periodically ✅
```

---

## EventBridge vs SQS vs SNS vs Kafka

```
EventBridge:
→ event routing + filtering ✅
→ AWS service integration ✅
→ scheduled triggers ✅
→ SaaS integrations ✅
→ moderate throughput ✅
→ NOT for millions of events ❌

SQS:
→ P2P queue ✅
→ message persistence ✅
→ retry + DLQ ✅
→ pull based ✅

SNS:
→ fan-out pub/sub ✅
→ push based ✅
→ no persistence ❌

Kafka:
→ millions of events ✅
→ event replay ✅
→ high throughput ✅
→ retention 14 days ✅
```

| | EventBridge | SQS | SNS | Kafka |
|---|---|---|---|---|
| **Throughput** | Moderate | High ✅ | High ✅ | Millions ✅ |
| **Routing** | ✅ Rules | ❌ No | Filter ✅ | Consumer group |
| **Schedule** | ✅ Yes | ❌ No | ❌ No | ❌ No |
| **Replay** | ❌ No | ❌ No | ❌ No | ✅ Yes |
| **SaaS** | ✅ Yes | ❌ No | ❌ No | ❌ No |

---

## Use Cases

```
1. Scheduled jobs (cron) ✅
→ currency rates every 4 hours ✅
→ re-auth scheduler ✅
→ report generation ✅
→ DB cleanup ✅

2. AWS service events ✅
→ EC2 instance starts/stops ✅
→ S3 file uploaded ✅
→ RDS snapshot completed ✅
→ CodePipeline state change ✅

3. Application events ✅
→ order created → notify team ✅
→ payment failed → alert ✅
→ custom business events ✅

4. SaaS integrations ✅
→ Zendesk ✅
→ Salesforce ✅
→ PagerDuty ✅

5. Keep Lambda warm ✅
→ ping every 5 minutes ✅
→ prevent cold start ✅
```

---

## Scheduled Events (Cron)

```
Two schedule formats:

Rate expression:
→ rate(5 minutes) ✅
→ rate(1 hour) ✅
→ rate(1 day) ✅

Cron expression:
→ cron(0 8 * * ? *)  = every day 8am ✅
→ cron(0 */4 * * ? *)= every 4 hours ✅
→ cron(0 0 1 * ? *)  = 1st of month ✅
→ cron(0 9 ? * MON *)= every Monday 9am ✅
```

---

## Event Pattern (Filtering)

```json
// Filter S3 events ✅
{
  "source": ["aws.s3"],
  "detail-type": ["Object Created"],
  "detail": {
    "bucket": {
      "name": ["payment-bucket"]
    }
  }
}

// Filter custom app events ✅
{
  "source": ["com.kiyan.payment"],
  "detail-type": ["OrderCreated"],
  "detail": {
    "status": ["PAID"]
  }
}
```

---

## Targets

```
Lambda ✅       → process event
SQS ✅          → queue for processing
SNS ✅          → fan-out notification
ECS ✅          → run container task
API Gateway ✅  → call REST endpoint
Step Functions ✅→ orchestration
CloudWatch ✅   → log event
Kinesis ✅      → stream processing
```

---

## Code Examples

```java
// ── Lambda triggered by EventBridge cron ✅ ───────────────────
public class CurrencyRateHandler
        implements RequestHandler<
                ScheduledEvent, Void> {

    private final CurrencyService currencyService;

    @Override
    public Void handleRequest(
            ScheduledEvent event,
            Context context) {

        log.info("EventBridge triggered: {}",
                event.getTime());

        // fetch + store currency rates ✅
        currencyService.fetchAndStore();
        return null;
    }
}

// ── Publish custom event to EventBridge ✅ ────────────────────
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final EventBridgeClient
            eventBridgeClient;

    public void publishOrderCreated(Order order) {

        // build event ✅
        PutEventsRequestEntry entry =
            PutEventsRequestEntry.builder()
                .source("com.kiyan.payment") // ✅
                .detailType("OrderCreated")  // ✅
                .detail(toJson(order))       // ✅
                .eventBusName("payment-bus") // ✅
                .build();

        // publish ✅
        PutEventsResponse response =
            eventBridgeClient.putEvents(
                PutEventsRequest.builder()
                    .entries(entry)
                    .build());

        // check failures ✅
        if (response.failedEntryCount() > 0) {
            log.error("EventBridge publish failed");
        }
    }
}

// ── Consume EventBridge via SQS ✅ ────────────────────────────
// EventBridge → SQS → Lambda/Service
@SqsListener("order-events-queue")
public void handleOrderEvent(
        String eventJson) {

    OrderEvent event = fromJson(eventJson);
    log.info("Received: {}",
            event.getOrderId());
    orderService.process(event); // ✅
}
```

---

## Terraform — EventBridge Setup

```hcl
# ── Custom Event Bus ✅ ───────────────────────────────────────
resource "aws_cloudwatch_event_bus" "payment" {
  name = "payment-bus" # custom bus ✅
}

# ── Scheduled Rule — every 4 hours ✅ ────────────────────────
resource "aws_cloudwatch_event_rule" "currency" {
  name                = "currency-rate-fetch"
  description         = "Fetch currency rates"
  schedule_expression = "rate(4 hours)" # ✅
}

# Target — Lambda ✅
resource "aws_cloudwatch_event_target" "currency_lambda" {
  rule      = aws_cloudwatch_event_rule.currency.name
  target_id = "CurrencyLambda"
  arn       = aws_lambda_function.currency.arn
}

# Lambda permission ✅
resource "aws_lambda_permission" "currency" {
  statement_id  = "AllowEventBridge"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.currency.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.currency.arn
}

# ── Pattern Rule — S3 event ✅ ────────────────────────────────
resource "aws_cloudwatch_event_rule" "s3_upload" {
  name = "payment-file-upload"

  event_pattern = jsonencode({
    source      = ["aws.s3"]
    detail-type = ["Object Created"]
    detail = {
      bucket = {
        name = ["payment-bucket"] # ✅
      }
    }
  })
}

# Target — SQS ✅
resource "aws_cloudwatch_event_target" "s3_sqs" {
  rule      = aws_cloudwatch_event_rule.s3_upload.name
  target_id = "PaymentQueue"
  arn       = aws_sqs_queue.payment.arn
}

# ── Custom event rule ✅ ──────────────────────────────────────
resource "aws_cloudwatch_event_rule" "order_created" {
  name         = "order-created"
  event_bus_name = aws_cloudwatch_event_bus.payment.name

  event_pattern = jsonencode({
    source      = ["com.kiyan.payment"]
    detail-type = ["OrderCreated"]
    detail = {
      status = ["PAID"] # filter ✅
    }
  })
}

# Multiple targets for same event ✅
resource "aws_cloudwatch_event_target" "notify_lambda" {
  rule           = aws_cloudwatch_event_rule.order_created.name
  event_bus_name = aws_cloudwatch_event_bus.payment.name
  target_id      = "NotifyLambda"
  arn            = aws_lambda_function.notify.arn
}

resource "aws_cloudwatch_event_target" "inventory_sqs" {
  rule           = aws_cloudwatch_event_rule.order_created.name
  event_bus_name = aws_cloudwatch_event_bus.payment.name
  target_id      = "InventorySQS"
  arn            = aws_sqs_queue.inventory.arn
}

# ── Keep Lambda warm — ping every 5 min ✅ ───────────────────
resource "aws_cloudwatch_event_rule" "warm_lambda" {
  name                = "keep-lambda-warm"
  schedule_expression = "rate(5 minutes)" # ✅
}

resource "aws_cloudwatch_event_target" "warm" {
  rule      = aws_cloudwatch_event_rule.warm_lambda.name
  target_id = "WarmLambda"
  arn       = aws_lambda_function.payment.arn
}
```

---

## Real Project Examples

```
1. Currency Consumer Service:
→ EventBridge cron every 4 hours ✅
→ triggers Lambda ✅
→ Lambda fetches rates from API ✅
→ stores in RDS MySQL ✅
→ downstream services read rates ✅

2. Re-Authorization Scheduler:
→ EventBridge cron every 2-3 hours ✅
→ triggers Lambda/ECS task ✅
→ finds expiring auths ✅
→ re-authorizes via Chase ✅

3. Keep Lambda warm:
→ EventBridge ping every 5 min ✅
→ Lambda stays warm ✅
→ no cold start ✅

4. S3 file processing:
→ file uploaded to S3 ✅
→ EventBridge detects ✅
→ routes to Lambda ✅
→ Lambda processes file ✅

5. CodePipeline notifications:
→ pipeline fails ✅
→ EventBridge captures ✅
→ routes to SNS ✅
→ team notified ✅
```

---

## Quick Reference — Key Points

| Topic | Key Point |
|---|---|
| What is EventBridge | Serverless event bus — routes events ✅ |
| Event Bus types | Default, Custom, Partner ✅ |
| Rules | Filter events by pattern ✅ |
| Targets | Lambda, SQS, SNS, ECS ✅ |
| Schedule | rate() or cron() ✅ |
| vs SQS | EventBridge = routing, SQS = queue ✅ |
| vs Kafka | Kafka = millions TPS, EventBridge = moderate ✅ |
| Real use | Currency rates cron ✅ |
| Real use | Re-auth scheduler ✅ |
| Real use | Keep Lambda warm ✅ |
| Real use | S3 file processing ✅ |
| SaaS | Zendesk, Salesforce, PagerDuty ✅ |
| Cold start fix | EventBridge ping every 5 min ✅ |
| Terraform | aws_cloudwatch_event_rule ✅ |
| Terraform | aws_cloudwatch_event_target ✅ |
