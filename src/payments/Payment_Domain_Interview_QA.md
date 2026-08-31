# Payment Domain — Interview Q&A
> 10 Questions based on real project experience at Gap/ECOM

---

## Q1. End-to-end Payment Authorization Flow?

### Answer
```
Step 1 — Customer places order:
→ ECOM (US/CA) sends card details to Vault Service
→ card number, expiry, CVV, billing address ✅

Step 2 — Tokenization (Bluefin):
→ Vault Service → sends to Bluefin
→ Bluefin encrypts card number ✅
→ Bluefin returns:
   → VaultID ✅
   → Token Extension ✅
→ Raw card number NEVER stored anywhere ✅
→ Only VaultID stored in Order DB ✅
→ This is PCI compliance ✅

Step 3 — Payment Authorization (PAS):
→ Payment Authorization Service receives /authorize-cards
→ PAS sends VaultID + Token Extension to Bluefin
→ Bluefin decrypts → returns real card number ✅
→ PAS sends to Chase Gateway ✅
→ Chase sends to Card Issuer (Visa/MC/Discover)
→ Card Issuer responds:
   ✅ Approved → Auth Code returned
   ❌ Declined → Insufficient balance / Invalid CVV / Expired

Step 4 — Store auth result in DB:
→ Order Number ✅
→ Customer Details ✅
→ VaultID ✅
→ Auth Amount ✅
→ Auth Code ✅
→ Status ✅

CVV — first-time auth only. Never stored anywhere ✅
```

```
Source Systems:
ECOM (US/CA), SFCC-JP (Japan), OMS, OIS, CCR, ReSA/RA, NGCC

Payment Processors:
Chase, Adyen, TSYS, AfterPay, SVS, Klarna,
PayPal/BrainTree, ACI (Fraud)

Complete flow:
ECOM → Vault Service → Bluefin (encrypt) → VaultID returned
PAS  → Bluefin (decrypt) → Chase → Card Issuer → Auth Code
```

### Interview answer
```
"Customer places order on ECOM →
card details sent to Vault Service →
Vault sends to Bluefin for encryption →
Bluefin returns VaultID + Token Extension →
raw card never stored — PCI compliant ✅

For authorization — PAS receives request →
sends VaultID to Bluefin to decrypt →
gets real card number →
sends to Chase Gateway →
Chase contacts card issuer (Visa/MC/Discover) →
approved → auth code stored in DB ✅

CVV sent only on first-time authorization —
never stored anywhere — PCI requirement ✅"
```

---

## Q2. What is PCI Compliance? How did your system ensure it?

### Answer
```
PCI DSS = Payment Card Industry Data Security Standard
Rules to protect cardholder data ✅

What our system did for PCI compliance:

1. Card number NEVER stored ✅
   → Raw card → Vault Service → Bluefin encrypts
   → Only VaultID stored in Order DB ✅
   → Actual card lives only in Bluefin encrypted ✅

2. CVV NEVER stored anywhere ✅
   → CVV sent only on first-time auth
   → Never persisted in any DB ✅
   → PCI rule: CVV cannot be stored ever ✅

3. Tokenization via Bluefin ✅
   → Raw card → VaultID ✅
   → Even if DB hacked → VaultID useless ✅

4. Only VaultID + last 4 digits in Order DB ✅

5. All communication HTTPS/REST ✅

6. ACI Fraud validation every transaction ✅

7. Liability shift for CC transactions ✅

8. Blue-Green with CM ticket for prod ✅
   → PCI audit trail ✅

9. Secrets in Azure Key Vault ✅
   → No hardcoded passwords ✅
```

```
Without PCI:
→ DB stores: 4111-1111-1111-1111 ❌
→ DB hacked → all cards exposed ❌

With PCI (our system):
→ DB stores: VaultID = "VAULT-123-XYZ" ✅
→ DB hacked → VaultID useless to hacker ✅
→ Real card only in Bluefin (encrypted) ✅
→ Only Bluefin can decrypt ✅
```

### Interview answer
```
"PCI DSS = Payment Card Industry Data Security Standard.

In our system:
Tokenization — raw card sent to Vault Service →
Vault sends to Bluefin → encrypted →
VaultID returned. Raw card never stored ✅

CVV never stored — sent once on first auth only ✅

Only VaultID + last 4 digits in Order DB ✅

All communication over HTTPS ✅

Secrets in Azure Key Vault — never hardcoded ✅

Change Management ticket required for
every production deployment — PCI audit trail ✅"
```

---

## Q3. Difference between Authorization, Capture, and Refund?

### Answer
```
Authorization:
→ order placed → hold funds ✅
→ money NOT charged yet ✅
→ just a hold on card ✅
→ expires in 7 days ✅
→ auth code stored in DB ✅

Capture:
→ order shipped → invoice generated ✅
→ actually charge customer ✅
→ can capture 30% MORE than auth ✅
→ to cover shipping charges at shipment ✅
→ auth = $100 → capture up to $130 ✅

Refund:
→ Reference Refund = return with original order ✅
  → money collected → refund to original method ✅
→ Blind Refund = no original order ref ✅
  → gift return without receipt ✅

Cancellation (different from refund):
→ order cancelled before shipment ✅
→ VOID the authorization ✅
→ no money ever moved ✅
```

```
All payment operations:

Operation        When                    What happens
──────────────────────────────────────────────────────────
Authorization    Order placed            Hold funds — not charged
Re-Authorization Auth expiring           Re-hold before 7 day expiry
Capture          Order shipped           Actually charge customer
Refund (Ref)     Return with order       Refund to original method
Blind Refund     No order reference      Refund without transaction ref
Cancellation     Order cancelled         Void auth — no money moved
Credit Memo      Price match             Partial refund for price diff
Return Recharge  Exchange (not returned) Item exchanged — recharged
```

### Interview answer
```
"Authorization = order placed →
hold funds on card via Chase.
Money not charged yet — just a hold.
Auth expires in 7 days ✅

Capture = order ships + invoice generated →
actually charge customer.
Can capture up to 30% more than original auth
to cover shipping charges ✅

Refund = on return.
Reference refund uses original transaction ID →
refund to original payment method ✅
Blind refund = no original order ref
e.g. gift return without receipt ✅

Cancellation = different from refund.
Before shipment → VOID authorization.
No money ever moved ✅

Every capture uses idempotency —
order number + invoice number + amount —
prevents double charging on retry ✅"
```

---

## Q4. What is Re-Authorization? How did the scheduler handle it?

### Answer
```
Why re-auth needed:
→ auth expires in 7 days ✅
→ some orders take 30 days (backorder, furniture) ✅
→ if auth expired → capture FAILS ❌
→ re-auth before expiry → capture succeeds ✅

How scheduler works:
→ runs every 2-3 hours ✅
→ queries DB for auths expiring soon ✅
→ re-authorize using SAME VaultID ✅
→ no need to ask customer for card again ✅
→ new auth code stored in DB ✅
→ new 7-day window starts ✅

Skip re-auth when:
→ order already captured ✅
→ order cancelled ✅
→ order already refunded ✅
```

```java
// Re-Authorization Scheduler ✅
@Component
@Slf4j
@RequiredArgsConstructor
public class ReAuthorizationScheduler {

    @Scheduled(fixedDelay = 7200000) // every 2 hours ✅
    @Async("taskExecutor")
    public void reAuthorizeExpiringAuths() {

        // find auths expiring in next 24 hours ✅
        LocalDateTime threshold = LocalDateTime.now().plusHours(24);
        List<Authorization> expiring =
                authRepo.findExpiringAuths(threshold);

        expiring.forEach(auth -> {
            // skip if already captured/cancelled ✅
            if (auth.isCaptured() || auth.isCancelled()) return;

            // re-auth using same VaultID ✅
            AuthRequest request = AuthRequest.builder()
                    .vaultId(auth.getVaultId())
                    .amount(auth.getAuthAmount())
                    .orderNumber(auth.getOrderNumber())
                    .build();

            AuthResponse response = authService.authorize(request);

            if (response.isApproved()) {
                auth.setAuthCode(response.getAuthCode());
                auth.setExpiryDate(LocalDateTime.now().plusDays(7));
                authRepo.save(auth); // new 7-day window ✅
            }
        });
    }
}
```

### Interview answer
```
"Auth holds expire in 7 days at Chase level.
Most orders ship within 7 days — no problem.
But backorder, furniture, custom items
take up to 30 days.

If auth expires before capture →
capture FAILS ❌

Re-Auth scheduler runs every 2-3 hours ✅
Queries DB for auths expiring within 24 hours.

For each expiring auth:
→ skip if already captured or cancelled ✅
→ re-auth using same VaultID ✅
→ Bluefin decrypts card → Chase re-authorizes ✅
→ no customer action needed ✅
→ new auth code + new 7-day window ✅
→ continues until order ships ✅"
```

---

## Q5. ACI Fraud Validation — three responses and handling?

### Answer
```
ACI = fraud validation processor ✅
Also called RedProxy / RedShield ✅
Integration: RestTemplate + SOAP/XML ✅
Content-Type: XML (not REST) ✅

Mandatory fields sent to ACI:
→ Customer IP ✅
→ Credit Card Number ✅
→ CVV (first-time only) ✅
→ Card Expiry ✅
→ Billing Address ✅
→ Shipping Address ✅
→ Item Details ✅

Three responses:
ACCEPT   → proceed to Chase authorization ✅
DENY     → order rejected immediately ❌
CHALLENGE→ contact customer ⚠️
           "Did you place this order?"
           YES → proceed to auth ✅
           NO  → cancel order ❌
           OMS updated via Kafka ✅

Why fraud AFTER auth in our system:
→ too many ACI challenges slowed process ❌
→ business decision → auth first ✅
```

```java
// ACI — SOAP/XML integration ✅
@Service
public class AciFraudService {

    public FraudDecision validateFraud(PaymentRequest req) {
        String xmlRequest = """
            <FraudRequest>
                <CustomerIP>%s</CustomerIP>
                <CardNumber>%s</CardNumber>
                <CVV>%s</CVV>
                <CardExpiry>%s</CardExpiry>
                <BillingAddress>%s</BillingAddress>
                <ShippingAddress>%s</ShippingAddress>
                <Items>%s</Items>
            </FraudRequest>
            """.formatted(
                req.getCustomerIp(),
                req.getCardNumber(),
                req.getCvv(),
                req.getCardExpiry(),
                req.getBillingAddress(),
                req.getShippingAddress(),
                buildItemsXml(req.getItems())
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML); // XML ✅

        ResponseEntity<String> response = restTemplate.exchange(
                aciUrl, HttpMethod.POST,
                new HttpEntity<>(xmlRequest, headers),
                String.class);

        return parseAciResponse(response.getBody());
    }
}

// Handle decisions ✅
switch (decision.getResult()) {
    case ACCEPT    -> authService.authorize(orderNumber);
    case DENY      -> kafkaTemplate.send("fraud-events",
                          FraudEvent.denied(orderNumber));
    case CHALLENGE -> kafkaTemplate.send("fraud-challenge-events",
                          FraudEvent.challenge(orderNumber));
}
```

```
ACI Decision   Action                    OMS Update
────────────────────────────────────────────────────
ACCEPT    → proceed to Chase auth ✅     ORDER_CONFIRMED
DENY      → reject order ❌             ORDER_CANCELLED
CHALLENGE → contact customer ⚠️        pending
           YES → auth ✅               ORDER_CONFIRMED
           NO  → cancel ❌             ORDER_CANCELLED
           → OMS via Kafka ✅
```

### Interview answer
```
"Fraud validation via ACI processor
(RedProxy/RedShield).

Integrated via RestTemplate + SOAP/XML —
Content-Type XML — not REST ✅

We send: customer IP, card number,
CVV (first-time only), card expiry,
billing address, shipping address, item details ✅

ACI checks fraud history and returns:

ACCEPT → proceed to Chase authorization ✅

DENY → order rejected immediately ❌
       OMS notified via Kafka ✅

CHALLENGE → customer contacted by bot/agent ⚠️
            'Did you place this order?'
            YES → proceed to auth ✅
            NO  → cancel ✅
            OMS updated via Kafka ✅

Important — in our system fraud check
runs AFTER auth not before.
ACI was generating too many challenges
slowing the process.
Business decision to auth first ✅"
```

---

## Q6. What is Account Updater? Why important?

### Answer
```
Account Updater handles expiring credit cards
automatically — no customer action needed ✅

Flow:
→ CC expires next month ✅
→ Bank issues new card (same last 4) ✅
→ New expiry date ✅
→ Bank connects to card network (Visa/MC) ✅
→ Account Updater service receives notification ✅
→ Gap customer profile updated ✅
→ Vault (Bluefin) updated with new card ✅
→ VaultID stays SAME — just details updated ✅
→ Customer never knew card expired ✅

Without Account Updater:
→ next order fails ❌
→ customer must update card manually ❌
→ bad experience → lost sales ❌

With Account Updater:
→ seamless ✅
→ no failed transactions ✅
→ especially important for:
   → backorders ✅
   → subscriptions ✅
   → 30-day delivery orders ✅
```

```java
// Account Updater ✅
@Service
@RequiredArgsConstructor
public class AccountUpdaterService {

    @KafkaListener(topics = "account-update-events")
    @Transactional
    public void handleAccountUpdate(AccountUpdateEvent event) {

        CustomerProfile profile = profileRepo
                .findByCustomerId(event.getCustomerId())
                .orElseThrow();

        // update Vault (Bluefin) — same VaultID ✅
        vaultService.updateCard(VaultUpdateRequest.builder()
                .vaultId(profile.getVaultId())   // same VaultID ✅
                .newCardNumber(event.getNewCardNumber())
                .newExpiryDate(event.getNewExpiryDate())
                .build());

        // update customer profile ✅
        profile.setCardExpiry(event.getNewExpiryDate());
        if (event.getNewCardNumber() != null) {
            profile.setCardLast4(
                event.getNewCardNumber()
                     .substring(
                         event.getNewCardNumber().length() - 4));
        }
        profileRepo.save(profile);
    }
}
```

```
Complete flow:

CC expires 12/2024
     ↓
Bank issues new card → same last 4, new expiry 12/2026
     ↓
Bank → Card Network (Visa/MC)
     ↓
Account Updater receives notification ✅
     ↓
Update Gap profile + Vault (Bluefin) ✅
VaultID stays same ✅
     ↓
Next transaction:
same VaultID → Bluefin decrypts → new details ✅
Chase authorizes → success ✅
Customer never knew card expired ✅
```

### Interview answer
```
"Account Updater handles expiring credit cards
automatically — no customer action needed ✅

When CC expires:
Bank automatically issues new card
with same last 4 digits, new expiry date →
Bank connects to card network (Visa/MC) →
Account Updater service receives notification ✅

We update two things:
1. Gap customer profile — new expiry ✅
2. Vault (Bluefin) — new card details ✅
   VaultID stays the SAME ✅

Next order:
same VaultID → Bluefin decrypts → new card ✅
Chase authorizes → success ✅
Customer never knew card expired ✅

Especially important for backorders
where customer placed order months ago ✅"
```

---

## Q7. Payment methods supported? How did BNPL work?

### Answer
```
All payment methods:

Credit Cards:
→ Visa, Mastercard, Discover ✅
→ via Chase processor ✅
→ Bluefin tokenization ✅

Digital Wallets:
→ Apple Pay → Virtual PAN (device token) ✅
→ Google Pay ✅
→ PayPal → encrypted token via BrainTree ✅

BNPL (Buy Now Pay Later):
→ AfterPay ✅
→ Klarna ✅

Gift Cards (SVS — Stored Value Service):
→ Physical Gift Card ✅
→ Virtual Gift Card ✅
→ Merchandise Return Card ✅
Operations: Issue, Redeem, Void Redeem, Balance Check ✅

AfterPay:
→ customer pays 4 installments ✅
→ AfterPay pays Gap IMMEDIATELY ✅
→ AfterPay collects from customer ✅
→ HTTPS/REST integration ✅

Klarna:
→ customer pays in installments ✅
→ Klarna LIABLE — Gap gets full amount upfront ✅
→ Klarna collects from customer ✅
→ Gap has ZERO credit risk ✅
→ via Klarna processor ✅

Apple Pay:
→ Virtual PAN (device token) ✅
→ real card number NEVER shared with Gap ✅
→ Apple creates device token ✅
→ Gap sends token to Chase ✅
→ Chase maps token → real card ✅

PayPal:
→ encrypted username/password token ✅
→ PayPal/BrainTree processor ✅
```

### Interview answer
```
"Our system supported multiple payment methods:

Credit Cards — Visa, Mastercard, Discover
via Chase + Bluefin tokenization ✅

Digital Wallets:
Apple Pay — Virtual PAN device token
real card never shared with Gap ✅
Google Pay — similar ✅
PayPal — encrypted token via BrainTree ✅

Gift Cards via SVS (Stored Value Service):
Physical, Virtual, Merchandise Return Card
Issue, Redeem, Void Redeem, Balance Check ✅

BNPL — Buy Now Pay Later:
AfterPay — 4 installments
AfterPay pays Gap IMMEDIATELY ✅
Gap has no credit risk ✅

Klarna — installments
Klarna LIABLE — Gap gets full amount upfront ✅
Klarna collects from customer ✅
Gap has ZERO credit risk ✅

Key benefit:
Gap paid immediately in BOTH cases ✅
Credit risk stays with AfterPay/Klarna ✅"
```

---

## Q8. What is Idempotency in capture? How to prevent double charging?

### Answer
```
Idempotency = prevent double charging
on retry or network failure ✅

Idempotency key in our system:
→ order number + invoice number + amount ✅
→ NO separate idempotency table ✅
→ uses payment settlement transaction table ✅

Already exists → skip → no double charge ✅

Two levels of protection:
1. Internal settlement table check ✅
2. Vendor idempotency key at Chase level ✅

Scenarios:
Retry → internal check → skip ✅
Network fail → retry → Chase idempotency key ✅
Customer charged ONCE ✅
```

```java
// Idempotency in Capture Service ✅
@Transactional
public CaptureResponse capture(CaptureRequest request) {

    // idempotency check — settlement table ✅
    // order number + invoice number + amount
    boolean exists = settlementRepo
            .existsByOrderNumberAndInvoiceNumberAndAmount(
                    request.getOrderNumber(),
                    request.getInvoiceNumber(),
                    request.getAmount());

    if (exists) {
        log.info("Duplicate — skipping: {}",
                request.getOrderNumber());
        return existingResult(); // no double charge ✅
    }

    // vendor idempotency key at Chase level ✅
    ChaseRequest chaseReq = ChaseRequest.builder()
            .authCode(request.getAuthCode())
            .amount(request.getAmount())
            .idempotencyKey(
                request.getOrderNumber() + "-" +
                request.getInvoiceNumber()) // ✅
            .build();

    ChaseResponse response = chaseGateway.capture(chaseReq);

    // save to settlement table ✅
    settlementRepo.save(PaymentSettlement.builder()
            .orderNumber(request.getOrderNumber())
            .invoiceNumber(request.getInvoiceNumber())
            .amount(request.getAmount())
            .status(response.getStatus())
            .build());

    return response.toCaptureResponse();
}
```

```
Scenario — network failure:

Attempt 1:
→ Chase processes → customer charged ✅
→ network fails → response LOST ❌
→ settlement NOT saved ❌

Retry (Attempt 2):
→ internal check → not in settlement ❌
→ sends to Chase again
→ Chase sees same idempotency key ✅
→ returns same result — no double charge ✅
→ settlement saved ✅

Customer charged ONCE ✅
Two layers protect:
→ Level 1: settlement table ✅
→ Level 2: Chase idempotency key ✅
```

### Interview answer
```
"Idempotency prevents double charging
customer on retry or network failure ✅

We implement at two levels:

Level 1 — Internal check:
Before sending to Chase we check
payment settlement table using
order number + invoice number + amount ✅

No separate idempotency table —
we use settlement transaction table itself ✅

If combination exists → skip →
return existing result → no double charge ✅

Level 2 — Vendor idempotency key:
order number + invoice number sent to Chase ✅
Chase sees same key → returns same response
without charging again ✅

Real scenario:
Capture → Chase charges → network fails →
retry → internal check: not in settlement →
sends to Chase → Chase sees same key →
returns same response → settlement saved ✅
Customer charged ONCE ✅"
```

---

## Q9. How did CI/CD pipeline work? What is Blue-Green deployment?

### Answer
```
Jenkins declarative pipeline (built 2017) ✅
Jenkins Shared Library — reused across all pipelines ✅
Common build, test, deploy logic written ONCE ✅

Pipeline stages:

1. Prepare
   → detect git push vs manual trigger ✅
   → manual → dropdown to choose start stage ✅
   → skip straight to deploy if needed ✅

2. Build (Docker agent)
   → ./gradlew clean build ✅
   → produces artifact ✅

3. Test
   → integration + contract tests ✅
   → encrypt key from Azure Key Vault ✅
   → secrets NEVER hardcoded ✅

4. Sonar
   → code quality scan ✅
   → milestone → kills older concurrent builds ✅

5. Publish JAR (master only)
   → timestamp version: 2024-07-14-10-30 ✅
   → uploaded to Artifactory ✅

6. Deploy Test → deploy to test env ✅
7. Deploy Stage → human approval → confirm version ✅
8. Deploy Prod
   → operator enters VERSION + CM TICKET ✅
   → PCI DSS compliance requirement ✅
   → deploys to INACTIVE Blue-Green slot ✅
   → live traffic NOT touched ✅

9. BlueGreen Swap
   → PAUSE → wait for human approval ✅
   → test new slot manually ✅
   → Approve → traffic switches ✅
   → zero downtime ✅

Post: Teams notification after every run ✅
```

```groovy
// Jenkins Shared Library pipeline ✅
@Library('XYZ-jenkins-shared-library') _

pipeline {
    stages {
        // Prepare — detect trigger ✅
        stage('Prepare') {
            steps {
                script { selectDeploymentType(this) }
            }
        }

        // Test — Azure Key Vault secrets ✅
        stage('Test') {
            options {
                azureKeyVault([
                    credentialID: 'azure-mi',
                    keyVaultURL: 'https://xyz.vault.azure.net',
                    secrets: [[
                        envVariable: 'ENCRYPT_KEY',
                        name: 'encrypt-key-secret',
                        secretType: 'Secret'
                    ]]
                ])
            }
            steps { executeTest([:]) }
        }

        // Sonar — milestone kills old builds ✅
        stage('Sonar') {
            steps {
                publishSonar([:])
                milestone 0 // abort older builds ✅
            }
        }

        // Publish — timestamp version ✅
        stage('Publish Jar') {
            when { branch 'master' }
            steps {
                script {
                    publishVersion(this, now)
                    // version: 2024-07-14-10-30 ✅
                    milestone 1
                }
            }
        }

        // Prod — CM ticket required (PCI) ✅
        stage('Deploy to Production') {
            steps {
                script {
                    deployToAzureProd(this)
                    // version + CM ticket ✅
                    // inactive slot only ✅
                    milestone 5
                }
            }
        }

        // BlueGreen Swap — human approval ✅
        stage('BlueGreen Swap') {
            steps {
                script {
                    swapBlueGreenInAzure(this)
                    // PAUSES — waits for approval ✅
                    // Approve → traffic switches ✅
                    milestone 6
                }
            }
        }
    }
    post { always { notify [:] } } // Teams ✅
}
```

```
Blue-Green flow:

BEFORE swap:
Blue slot  → v1.0 ← LIVE traffic ✅
Green slot → v2.0 ← new (no traffic) ✅

Test green → all good ✅
Human approves ✅

AFTER swap:
Blue slot  → v1.0 ← standing by
Green slot → v2.0 ← LIVE traffic ✅

Something wrong?
→ click Blue-Green job ✅
→ traffic reverts to Blue instantly ✅
→ seconds not minutes ✅
→ no redeployment needed ✅
```

### Interview answer
```
"Jenkins declarative pipeline built in 2017
for payment service.

Jenkins Shared Library —
common logic written once,
reused across all payment pipelines ✅

Stages:
Prepare → detect git push or manual ✅
Build → Gradle clean build ✅
Test → integration tests + Azure Key Vault
       for encryption key — never hardcoded ✅
Sonar → code quality + milestone step
        kills concurrent older builds ✅
Publish → timestamp version to Artifactory ✅
Deploy Test/Stage → progressive deployment ✅
Deploy Prod → VERSION + CM ticket required ✅
              PCI DSS compliance ✅
              deploys to INACTIVE slot ✅
              live traffic NOT touched ✅
BlueGreen Swap → PAUSES for human approval ✅
                 test new slot ✅
                 Approve → switch ✅
                 zero downtime ✅

Rollback:
Something wrong → click Blue-Green job →
traffic reverts to old version instantly ✅
seconds — not minutes ✅

Teams notification after every run ✅"
```

---

## Q10. Order Cancelled — how to handle cancellation vs refund?

### Answer
```
Cancellation (before shipment):
→ order cancelled ✅
→ shipment NOT started ✅
→ VOID the authorization ✅
→ no money ever moved ✅
→ hold released on customer card ✅

Return/Refund (after shipment):
→ order shipped → captured → money collected ✅
→ customer initiates return ✅
→ OMS sends return request to payment team ✅

Reference Refund:
→ return WITH original order ✅
→ original transaction ID available ✅
→ refund to ORIGINAL payment method ✅

Blind Refund:
→ return WITHOUT original order ✅
→ gift return without receipt ✅
→ no transaction reference ✅

Credit Memo:
→ price match after purchase ✅
→ partial refund for price difference ✅

Return Recharge:
→ exchange — item NOT returned ✅
→ recharged for exchange item ✅
```

```java
// Cancellation — void auth ✅
@KafkaListener(topics = "order-cancelled-events")
@Transactional
public void handleCancellation(OrderCancelledEvent event) {

    Authorization auth = authRepo
            .findByOrderNumber(event.getOrderNumber())
            .orElseThrow();

    // already captured → refund instead ✅
    if (auth.isCaptured()) {
        initiateRefund(event.getOrderNumber());
        return;
    }

    // void auth — no money moved ✅
    VoidResponse response = chaseGateway.voidAuth(
            VoidRequest.builder()
                    .authCode(auth.getAuthCode())
                    .orderNumber(auth.getOrderNumber())
                    .build());

    if (response.isSuccess()) {
        auth.setStatus("CANCELLED");
        authRepo.save(auth); // ✅
    }
}

// Return — refund ✅
@KafkaListener(topics = "return-request-events")
@Transactional
public void handleReturn(ReturnRequestEvent event) {

    if (event.hasOriginalTransactionId()) {
        // Reference Refund ✅
        chaseGateway.refund(RefundRequest.builder()
                .originalTransactionId(
                    event.getOriginalTransactionId())
                .amount(event.getRefundAmount())
                .build());
    } else {
        // Blind Refund ✅
        chaseGateway.blindRefund(BlindRefundRequest.builder()
                .amount(event.getRefundAmount())
                .orderNumber(event.getOrderNumber())
                .build());
    }

    settlementRepo.save(PaymentSettlement.builder()
            .orderNumber(event.getOrderNumber())
            .amount(event.getRefundAmount().negate())
            .status("REFUNDED")
            .build());
}
```

```
Decision flow:

Order Cancelled event
         ↓
Already CAPTURED?
         ↓
NO → Void Auth ✅         YES → Initiate Refund ✅
   → no money moved            → money collected
   → hold released             → refund to original method

Return Request
         ↓
Has original transaction ID?
         ↓
YES → Reference Refund ✅  NO → Blind Refund ✅
   → normal return              → gift return ✅
```

```
All scenarios:

Scenario           When                   Action
──────────────────────────────────────────────────────────
Cancellation       Before shipment        Void auth ✅
Refund (Reference) After shipment/return  Refund to original ✅
Blind Refund       No order reference     Refund without ref ✅
Credit Memo        Price match            Partial refund ✅
Return Recharge    Exchange (kept old)    Recharged ✅
```

### Interview answer
```
"Two different scenarios:

Cancellation — before shipment:
Order cancelled → shipment not started →
VOID the authorization ✅
No money ever moved ✅
Hold released on customer card ✅
OMS sends cancel event via Kafka →
payment team consumes → void via Chase ✅

Refund — after shipment:
Order shipped → captured → money collected ✅
Customer initiates return ✅
OMS sends return request via Kafka →
payment team processes refund ✅

Reference refund — has original transaction ID →
refund to original payment method ✅

Blind refund — no original order reference
e.g. gift return without receipt →
refund without linking to original ✅

Credit Memo — price match →
partial refund for price difference ✅

Key difference:
Cancellation = VOID auth (no money moved) ✅
Refund = money already collected → return it ✅"
```

---

## Quick Reference — All 10 Key Points

| Topic | Key Point |
|---|---|
| Auth flow | ECOM → Vault → Bluefin(encrypt) → VaultID → PAS → Bluefin(decrypt) → Chase ✅ |
| Tokenization | Raw card → Bluefin → VaultID. Never stored in DB ✅ |
| PCI DSS | Card never stored. CVV never stored. VaultID only ✅ |
| Authorization | Hold funds. 7 day expiry. Not charged yet ✅ |
| Capture | On shipment. Up to 130% of auth amount ✅ |
| Re-Authorization | Scheduler every 2-3 hours. Same VaultID ✅ |
| ACI Fraud | SOAP/XML. Accept/Deny/Challenge. OMS via Kafka ✅ |
| Account Updater | Bank updates → profile + Vault updated. VaultID same ✅ |
| BNPL | Gap paid upfront. Credit risk with AfterPay/Klarna ✅ |
| SVS Gift Cards | Issue, Redeem, Void Redeem, Balance Check ✅ |
| Idempotency | order + invoice + amount in settlement table ✅ |
| Double charge | Two levels: settlement table + Chase idempotency key ✅ |
| Blue-Green | Deploy inactive slot → test → approve → swap ✅ |
| CM Ticket | PCI audit trail requirement for prod deploy ✅ |
| Cancellation | Before shipment = void auth (no money moved) ✅ |
| Refund | Reference (with order) or Blind (without order) ✅ |
| Credit Memo | Partial refund for price match ✅ |
| Return Recharge | Exchange without return → recharged ✅ |
| TYSIS → Chase | TCP/IP to HTTP/REST. Cheaper + more reliable ✅ |
| CVV rule | First-time auth only. Never stored. PCI requirement ✅ |
