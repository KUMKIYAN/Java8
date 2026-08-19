# GraphQL — Interview Q&A
> 10 Questions with Correct Answers & Code Snippets

---

## Q1. What is GraphQL? How is it different from REST API?

### Answer
```
GraphQL:
→ query language for APIs ✅
→ single endpoint /graphql ✅
→ client requests EXACTLY what it needs ✅
→ no over-fetching ✅
→ no under-fetching ✅
→ strongly typed schema ✅
→ developed by Facebook 2012 ✅
→ open sourced 2015 ✅

REST problems:
→ multiple endpoints ❌
→ over-fetching — extra data returned ❌
→ under-fetching — need multiple calls ❌
→ fixed response structure ❌
```

```
REST — 3 calls needed ❌
GET /orders/1
GET /orders/1/customer
GET /orders/1/items

GraphQL — 1 call ✅
POST /graphql
query {
  order(id: 1) {
    id
    status
    customer { name }
    items { name price }
  }
}
```

| | REST | GraphQL |
|---|---|---|
| Endpoints | Multiple ❌ | Single /graphql ✅ |
| Response | Fixed | Client defined ✅ |
| Over-fetching | Yes ❌ | No ✅ |
| Real-time | Polling ❌ | Subscriptions ✅ |

---

## Q2. What are Query, Mutation and Subscription?

### Answer
```
Query      = READ  → like GET ✅
Mutation   = WRITE → like POST/PUT/DELETE ✅
Subscription = REAL-TIME → like WebSocket ✅
```

```graphql
# Query — READ ✅
query GetOrder {
  order(id: "1") {
    id
    status
    amount
    customer { name email }
  }
}

# Mutation — CREATE ✅
mutation CreateOrder {
  createOrder(input: {
    customerId: "C001"
    amount: 100.50
  }) {
    id
    status
  }
}

# Mutation — UPDATE ✅
mutation UpdateStatus {
  updateOrderStatus(id: "1", status: "PAID") {
    id
    status
  }
}

# Subscription — REAL-TIME ✅
subscription OrderStatusUpdate {
  orderStatusChanged(orderId: "1") {
    id
    status
    updatedAt
  }
}
```

| Operation | REST equivalent | Purpose |
|---|---|---|
| Query | GET | Fetch data ✅ |
| Mutation | POST/PUT/DELETE | Write data ✅ |
| Subscription | WebSocket | Real-time ✅ |

---

## Q3. What is GraphQL Schema? How to define it?

### Answer
```
Schema:
→ contract between client and server ✅
→ defines available types ✅
→ defines available operations ✅
→ strongly typed ✅
→ SDL = Schema Definition Language ✅

! = non-nullable (required) ✅
[] = list ✅
```

```graphql
# Types ✅
type Order {
  id:       ID!           # required ✅
  status:   String!
  amount:   Float!
  customer: Customer!     # nested ✅
  items:    [OrderItem!]  # list ✅
}

type Customer {
  id:    ID!
  name:  String!
  email: String!
}

# Input for mutation ✅
input CreateOrderInput {
  customerId: String!
  amount:     Float!
}

# Query type ✅
type Query {
  order(id: ID!):  Order
  orders:          [Order!]!
}

# Mutation type ✅
type Mutation {
  createOrder(input: CreateOrderInput!): Order!
  updateOrderStatus(id: ID!, status: String!): Order!
  deleteOrder(id: ID!): Boolean!
}

# Subscription type ✅
type Subscription {
  orderStatusChanged(orderId: ID!): Order!
}
```

---

## Q4. How to implement GraphQL in Spring Boot?

### Answer
```
Two libraries:
1. Spring for GraphQL (official) ✅
   → @QueryMapping ✅
   → @MutationMapping ✅
   → @SubscriptionMapping ✅

2. Netflix DGS ✅
   → @DgsComponent ✅
   → @DgsQuery ✅
   → @DgsMutation ✅
   → enterprise grade ✅

Schema file:
→ resources/graphql/schema.graphqls ✅
```

```java
// Controller ✅
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Query ✅
    @QueryMapping
    public Order order(@Argument String id) {
        return orderService.getOrder(id);
    }

    @QueryMapping
    public List<Order> orders() {
        return orderService.getAllOrders();
    }

    // Mutation ✅
    @MutationMapping
    public Order createOrder(
            @Argument CreateOrderInput input) {
        return orderService.createOrder(input);
    }

    @MutationMapping
    public Order updateOrderStatus(
            @Argument String id,
            @Argument String status) {
        return orderService.updateStatus(id, status);
    }

    // Subscription ✅
    @SubscriptionMapping
    public Flux<Order> orderStatusChanged(
            @Argument String orderId) {
        return orderService.getOrderStatusStream(orderId);
    }
}
```

```yaml
# application.yml ✅
spring:
  graphql:
    graphiql:
      enabled: true   # UI at /graphiql ✅
    path: /graphql    # endpoint ✅
```

---

## Q5. What is N+1 problem in GraphQL? Fix with DataLoader?

### Answer
```
N+1 problem:
→ fetch 10 orders ✅
→ for each order → fetch customer ✅
→ 10 separate DB calls ❌
→ 1 + 10 = 11 queries ❌

DataLoader fix:
→ batches all customer IDs ✅
→ one DB call for all ✅
→ 1 + 1 = 2 queries ✅
→ like JOIN FETCH in JPA ✅
```

```java
// Without DataLoader — N+1 ❌
@SchemaMapping(typeName = "Order", field = "customer")
public Customer customer(Order order) {
    // called for EACH order ❌
    return customerRepository
            .findById(order.getCustomerId());
}

// With DataLoader — batch ✅
@Bean
public BatchLoaderRegistry batchLoaderRegistry() {
    return BatchLoaderRegistry.newRegistry()
            .forTypePair(String.class, Customer.class)
            .registerBatchLoader((ids, env) -> {
                // ONE call for ALL IDs ✅
                return customerRepository.findAllById(ids);
            });
}

@SchemaMapping(typeName = "Order", field = "customer")
public CompletableFuture<Customer> customer(
        Order order,
        DataLoader<String, Customer> loader) {
    return loader.load(order.getCustomerId()); // ✅
}
```

---

## Q6. What is GraphQL Fragment?

### Answer
```
Fragment:
→ reusable piece of query ✅
→ avoid repeating fields ✅
→ DRY principle ✅
→ like a method in Java ✅
```

```graphql
# Without fragment — repeated ❌
query {
  order(id: "1") {
    id
    status
    customer { name email } # repeated ❌
  }
  orders {
    id
    status
    customer { name email } # repeated ❌
  }
}

# With fragment — reusable ✅
fragment CustomerFields on Customer {
  name
  email
}

fragment OrderFields on Order {
  id
  status
  customer {
    ...CustomerFields  # reuse ✅
  }
}

query GetOrder {
  order(id: "1") {
    ...OrderFields  # reuse ✅
  }
}

query GetOrders {
  orders {
    ...OrderFields  # reuse ✅
  }
}
```

---

## Q7. What are GraphQL Directives?

### Answer
```
Directives:
→ modify behavior of query ✅
→ like annotations in Java ✅
→ start with @ ✅

Built-in:
→ @include(if: Boolean) ✅
→ @skip(if: Boolean) ✅
→ @deprecated(reason: String) ✅

Custom:
→ @auth ✅
→ @hasRole ✅
→ @rateLimit ✅
```

```graphql
# @include — conditionally include ✅
query GetOrder($showItems: Boolean!) {
  order(id: "1") {
    id
    status
    items @include(if: $showItems) {
      name
      price
    }
  }
}

# @skip — conditionally skip ✅
query GetOrder($skipCustomer: Boolean!) {
  order(id: "1") {
    id
    customer @skip(if: $skipCustomer) {
      name
    }
  }
}

# @deprecated ✅
type Order {
  id:        ID!
  status:    String!
  orderCode: String @deprecated(reason: "Use id instead")
}
```

---

## Q8. How to handle errors in GraphQL?

### Answer
```
GraphQL errors:
→ always returns 200 HTTP ✅
→ errors in "errors" array ✅
→ partial data possible ✅

Different from REST:
REST  → error = 404/500 HTTP ❌
GraphQL → error = 200 + errors[] ✅
```

```json
{
  "data": { "order": null },
  "errors": [
    {
      "message": "Order not found: 1",
      "extensions": {
        "code": "ORDER_NOT_FOUND",
        "status": 404
      }
    }
  ]
}
```

```java
// Custom exception ✅
public class OrderNotFoundException
        extends RuntimeException {
    public OrderNotFoundException(String id) {
        super("Order not found: " + id);
    }
}

// Global error handler ✅
@Component
public class GraphQLExceptionHandler
        implements DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(
            Throwable ex, DataFetchingEnvironment env) {

        if (ex instanceof OrderNotFoundException) {
            return GraphQLError.newError()
                    .message(ex.getMessage())
                    .errorType(ErrorType.NOT_FOUND)
                    .extensions(Map.of(
                        "code", "ORDER_NOT_FOUND",
                        "status", 404))
                    .build(); // ✅
        }
        return null;
    }
}

// Resolver ✅
@QueryMapping
public Order order(@Argument String id) {
    return orderRepository.findById(id)
            .orElseThrow(() ->
                new OrderNotFoundException(id)); // ✅
}
```

---

## Q9. How to secure GraphQL APIs?

### Answer
```
Security approaches:

1. Spring Security + JWT ✅
2. Field level @PreAuthorize ✅
3. Query depth limiting ✅
4. Query complexity limiting ✅
5. Rate limiting ✅
```

```java
// Spring Security ✅
@Bean
public SecurityFilterChain filterChain(
        HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/graphql")
                .authenticated() // ✅
            .requestMatchers("/graphiql")
                .permitAll()
        )
        .addFilterBefore(jwtAuthFilter,
            UsernamePasswordAuthenticationFilter.class);
    return http.build();
}

// Field level security ✅
@QueryMapping
@PreAuthorize("hasRole('ADMIN')")
public List<Order> allOrders() {
    return orderService.getAllOrders();
}

@MutationMapping
@PreAuthorize("hasRole('USER')")
public Order createOrder(@Argument CreateOrderInput input) {
    return orderService.createOrder(input);
}

// Query depth limiting ✅
@Bean
public GraphQlSource graphQlSource() {
    return GraphQlSource.builder()
            .instrumentation(
                new MaxQueryDepthInstrumentation(5)) // max 5 levels ✅
            .build();
}
```

---

## Q10. GraphQL vs REST — When to choose each?

### Answer
```
Choose GraphQL when:
→ multiple clients — mobile, web, 3rd party ✅
→ complex nested data ✅
→ over-fetching problem ✅
→ real-time subscriptions needed ✅
→ microservices aggregation ✅

Choose REST when:
→ simple CRUD ✅
→ file uploads ✅
→ HTTP caching important ✅
→ team not familiar ✅
→ simple public APIs ✅
```

```
Real example:

Mobile: needs name, avatar only
Web:    needs name, avatar, orders, address
3rd party: needs id, status only

REST → 3 different endpoints ❌
       or one over-fetches ❌

GraphQL → one endpoint ✅
          each client gets exactly what it needs ✅
```

| | GraphQL | REST |
|---|---|---|
| Endpoints | Single ✅ | Multiple |
| Response | Client defined ✅ | Fixed |
| Real-time | Subscriptions ✅ | Polling |
| File upload | Complex ⚠️ | Simple ✅ |
| Caching | Complex ⚠️ | Simple ✅ |
| Use for | Complex/multi-client ✅ | Simple CRUD ✅ |

---

## Quick Reference — All Key Points

| Topic | Key Point |
|---|---|
| GraphQL | Single endpoint — client defines response ✅ |
| Query | READ — like GET ✅ |
| Mutation | WRITE — like POST/PUT/DELETE ✅ |
| Subscription | REAL-TIME — like WebSocket ✅ |
| Schema | Contract — types + operations ✅ |
| ! | Non-nullable field ✅ |
| [] | List type ✅ |
| N+1 fix | DataLoader — batch loading ✅ |
| Fragment | Reusable field sets ✅ |
| @include | Include field conditionally ✅ |
| @skip | Skip field conditionally ✅ |
| @deprecated | Mark field deprecated ✅ |
| Error | Always 200 + errors array ✅ |
| Security | JWT + @PreAuthorize + depth limit ✅ |
| Spring lib | spring-boot-starter-graphql ✅ |
| Netflix DGS | @DgsComponent @DgsQuery ✅ |
| GraphiQL | Built-in UI for testing ✅ |
| Over-fetching | REST problem — GraphQL solves ✅ |
| Choose GraphQL | Multi-client + complex + real-time ✅ |
| Choose REST | Simple CRUD + file upload ✅ |
