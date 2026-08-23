# React / Node.js — Interview Q&A for Java Developer
> 10 Questions mapped to Java/Spring Boot concepts

---

## Q1. React component architecture vs Spring Boot layered architecture?

### Answer
```
Spring Boot (backend):
→ layered architecture ✅
   Controller → Service → Repository → DB
→ handles business logic ✅
→ REST APIs ✅
→ @Component, @Service, @Repository ✅

React (frontend):
→ component based architecture ✅
→ each component = UI piece ✅
→ useState  → manages local data ✅
→ useEffect → side effects (API calls) ✅
→ props     → pass data between components ✅

Virtual DOM:
→ in-memory copy of real DOM ✅
→ diff identified between virtual and real DOM ✅
→ only changed parts re-rendered ✅
→ faster than updating entire DOM ✅

How they work together:
React (UI) → Axios → Spring Boot REST API → DB ✅
```

```jsx
// React component calling Spring Boot API ✅
const OrderList = () => {
    const [orders,  setOrders]  = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        setLoading(true);
        try {
            const response = await axios.get(
                "http://localhost:8080/api/orders");
            setOrders(response.data);
        } catch (error) {
            console.error("Error:", error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            {loading && <p>Loading...</p>}
            {orders.map(order => (
                <OrderCard key={order.id} order={order} />
            ))}
        </div>
    );
};
```

```java
// Spring Boot REST API ✅
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @GetMapping
    public List<Order> getOrders() {
        return orderService.getOrders();
    }
}
```

| | Spring Boot | React |
|---|---|---|
| **Architecture** | Layered ✅ | Component based ✅ |
| **Language** | Java | JavaScript/TypeScript |
| **Purpose** | Backend API | Frontend UI |
| **State** | DB/Session | useState/Redux |
| **DI** | @Autowired | props/Context |
| **Lifecycle** | @PostConstruct | useEffect ✅ |

---

## Q2. What is Node.js? How does it differ from Spring Boot?

### Answer
```
Node.js = JavaScript runtime on SERVER side
→ run JavaScript outside browser ✅
→ build backend REST APIs ✅
→ same language frontend + backend ✅
→ built on Chrome V8 engine ✅
→ non-blocking event driven I/O ✅
→ single threaded event loop ✅

Node.js:
→ JavaScript based ✅
→ non-blocking async by default ✅
→ lightweight + fast startup ✅
→ great for real-time apps ✅
→ less enterprise features ❌

Spring Boot:
→ Java based ✅
→ multi-threaded ✅
→ heavy enterprise features ✅
→ strong typing ✅
→ better for complex business logic ✅
→ slower startup ⚠️
```

```javascript
// Node.js + Express — REST API
const express = require("express");
const app = express();
app.use(express.json());

// GET /api/orders — like @GetMapping ✅
app.get("/api/orders", async (req, res) => {
    try {
        const orders = await orderService.getAll();
        res.json(orders);            // like ResponseEntity.ok() ✅
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// POST /api/orders — like @PostMapping ✅
app.post("/api/orders", async (req, res) => {
    try {
        const order = await orderService.create(req.body);
        res.status(201).json(order); // like ResponseEntity.created() ✅
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
});

app.listen(3000, () => console.log("Server on 3000"));
```

| | Node.js | Spring Boot |
|---|---|---|
| **Language** | JavaScript | Java |
| **Threading** | Single event loop | Multi-thread ✅ |
| **I/O** | Non-blocking ✅ | Blocking (default) |
| **Startup** | ✅ Fast | ⚠️ Slower |
| **Enterprise** | ❌ Less | ✅ Rich |
| **Type safety** | ⚠️ Weak | ✅ Strong |
| **Use for** | APIs, real-time | Enterprise, complex ✅ |

---

## Q3. What is REST API? How does React consume Spring Boot REST API?

### Answer
```
REST API:
→ GET    → fetch data ✅
→ POST   → create resource ✅
→ PUT    → full update ✅
→ PATCH  → partial update ✅
→ DELETE → delete resource ✅
→ stateless — no session on server ✅
→ JSON request/response ✅

React → Spring Boot flow:
→ Axios configured with base URL ✅
→ interceptor adds JWT token ✅
→ handles errors centrally ✅
→ CORS must be enabled on Spring Boot ✅
```

```jsx
// Axios setup ✅
import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080/api",
    timeout: 5000,
    headers: { "Content-Type": "application/json" }
});

// add JWT to every request ✅
api.interceptors.request.use(config => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// handle 401 globally ✅
api.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            window.location.href = "/login";
        }
        return Promise.reject(error);
    }
);

// all HTTP methods ✅
const fetchOrders  = async () => {
    const res = await api.get("/orders");
    setOrders(res.data);
};

const createOrder  = async (data) => {
    const res = await api.post("/orders", data);
    setOrders(prev => [...prev, res.data]);
};

const updateOrder  = async (id, data) =>
    await api.put(`/orders/${id}`, data);

const updateStatus = async (id, status) =>
    await api.patch(`/orders/${id}`, { status });

const deleteOrder  = async (id) =>
    await api.delete(`/orders/${id}`);
```

```java
// Spring Boot — enable CORS ✅
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "https://myapp.com"
        ));
        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

| HTTP | Axios | Spring Boot | Purpose |
|---|---|---|---|
| GET | `api.get("/orders")` | `@GetMapping` | Fetch ✅ |
| POST | `api.post("/orders", data)` | `@PostMapping` | Create ✅ |
| PUT | `api.put("/orders/1", data)` | `@PutMapping` | Update ✅ |
| PATCH | `api.patch("/orders/1", data)` | `@PatchMapping` | Partial ✅ |
| DELETE | `api.delete("/orders/1")` | `@DeleteMapping` | Delete ✅ |

---

## Q4. What is CORS? Why does it occur and how to fix it?

### Answer
```
CORS = Cross Origin Resource Sharing
→ security mechanism ✅
→ controls which origins can access API ✅
→ prevents unauthorized cross-domain requests ✅

Why occurs:
React runs on:  http://localhost:3000
Spring Boot on: http://localhost:8080
→ different ports = different origin ❌
→ browser blocks request by default ❌

How CORS works:
→ browser sends OPTIONS preflight first ✅
→ Spring Boot responds with allowed origins ✅
→ browser sends actual request ✅
→ server side fix — NOT client side ✅

Three ways to fix in Spring Boot:
1. @CrossOrigin on controller ✅
2. Global CorsConfigurationSource ✅ (recommended)
3. Spring Security CORS config ✅
```

```java
// Fix 1 — @CrossOrigin ✅
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController { }

// Fix 2 — Global config ✅ recommended
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "https://myapp.com"
        ));
        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

// Fix 3 — Spring Security ✅
http.cors(cors -> cors
    .configurationSource(corsConfigurationSource()));
```

| CORS Fix | Where | Use when |
|---|---|---|
| `@CrossOrigin` | Controller | Quick fix ✅ |
| `CorsConfigurationSource` | Global | All controllers ✅ |
| Spring Security CORS | Security config | With Spring Security ✅ |

---

## Q5. What is JWT? How does it work between React and Spring Boot?

### Answer
```
JWT = JSON Web Token (not JavaScript Web Token)
→ three parts: Header.Payload.Signature ✅
→ Header    → algorithm (HS256)
→ Payload   → claims (sub, roles, exp)
→ Signature → HMAC-SHA256 with secret ✅

Flow:
1. React → POST /api/auth/login { email, password }
2. Spring Boot validates → generates JWT ✅
3. React stores token in localStorage ✅
4. Every request → Authorization: Bearer <token> ✅
5. JwtAuthFilter validates token ✅
6. Extracts username + roles → SecurityContext ✅

React token storage:
→ localStorage (simple but XSS risk) ⚠️
→ HttpOnly cookie (safer) ✅
→ memory (safest but lost on refresh) ✅
```

```jsx
// React — login ✅
const handleLogin = async (e) => {
    e.preventDefault();
    const response = await axios.post(
        "http://localhost:8080/api/auth/login",
        { email, password }
    );
    localStorage.setItem("token", response.data.token);
    navigate("/dashboard");
};

// Axios interceptor — add token ✅
api.interceptors.request.use(config => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Protected Route ✅
const ProtectedRoute = ({ children }) => {
    const token = localStorage.getItem("token");
    return token
        ? children
        : <Navigate to="/login" />;
};
```

```java
// Spring Boot — generate token ✅
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(
        @RequestBody LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.email(), request.password())
    );
    UserDetails user = userService
            .loadUserByUsername(request.email());
    String token = jwtService.generateToken(user);
    return ResponseEntity.ok(new AuthResponse(token));
}

// JWT Filter — validate every request ✅
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        if (jwtService.isTokenValid(token)) {
            String username = jwtService.extractUsername(token);
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    username, null,
                    jwtService.extractRoles(token));
            SecurityContextHolder.getContext()
                    .setAuthentication(auth); // ✅
        }
        chain.doFilter(request, response);
    }
}
```

| Step | React | Spring Boot |
|---|---|---|
| **Login** | POST credentials | Validate + generate JWT ✅ |
| **Store** | localStorage ✅ | Stateless — no session |
| **Send** | Axios interceptor ✅ | JwtAuthFilter reads |
| **Validate** | Check 401 response | Verify signature + expiry ✅ |
| **Logout** | Clear localStorage ✅ | Nothing (stateless) |

---

## Q6. What is useState and useEffect? Spring Boot equivalents?

### Answer
```
useState:
→ stores data that changes ✅
→ triggers UI re-render on change ✅
→ Spring Boot equivalent: instance variable / DTO ✅

useEffect:
→ runs side effects ✅
→ API calls, subscriptions, timers ✅
→ []        = @PostConstruct (run once) ✅
→ [dep]     = @EventListener (react to change) ✅
→ cleanup   = @PreDestroy (cleanup) ✅
```

```jsx
const OrderList = () => {

    // useState = holds data ✅
    const [orders,  setOrders]  = useState([]);
    const [loading, setLoading] = useState(false);
    const [page,    setPage]    = useState(0);

    // @PostConstruct — run once on mount ✅
    useEffect(() => {
        fetchOrders();
    }, []);

    // @EventListener — run when page changes ✅
    useEffect(() => {
        fetchOrders();
    }, [page]);

    // @PreDestroy — cleanup on unmount ✅
    useEffect(() => {
        const interval = setInterval(() => {
            fetchOrders();
        }, 30000);
        return () => clearInterval(interval); // cleanup ✅
    }, []);

    const fetchOrders = async () => {
        setLoading(true);
        try {
            const res = await api.get(
                `/orders?page=${page}&size=10`);
            setOrders(res.data.content);
        } finally {
            setLoading(false);
        }
    };
};
```

```java
// Spring Boot equivalents ✅
@Service
public class OrderService {

    private List<Order> cachedOrders = new ArrayList<>(); // useState

    @PostConstruct                    // useEffect([])
    public void init() {
        cachedOrders = orderRepository.findAll();
    }

    @EventListener                    // useEffect([dep])
    public void onOrderCreated(OrderCreatedEvent event) {
        cachedOrders.add(event.getOrder());
    }

    @PreDestroy                       // useEffect cleanup
    public void cleanup() {
        cachedOrders.clear();
    }
}
```

| React | Spring Boot | Purpose |
|---|---|---|
| `useState([])` | `List<Order> orders` | Store data ✅ |
| `setOrders(data)` | `orders = data` | Update data ✅ |
| `useEffect(fn, [])` | `@PostConstruct` | Run once ✅ |
| `useEffect(fn, [dep])` | `@EventListener` | React to change ✅ |
| `return () => cleanup` | `@PreDestroy` | Cleanup ✅ |

---

## Q7. What is React Router? Compare to Spring Boot URL mapping?

### Answer
```
React Router:
→ client side routing ✅
→ navigate between pages without refresh ✅
→ SPA (Single Page Application) ✅
→ URL changes in browser ✅

Spring Boot:
→ server side routing ✅
→ each URL = server request ✅
→ @RequestMapping maps URLs ✅

Key hooks:
→ useNavigate  → programmatic navigation ✅
→ useParams    → get URL parameters ✅
→ useLocation  → current URL ✅
→ Link         → navigation link ✅
```

```jsx
// React Router setup ✅
const App = () => (
    <BrowserRouter>
        <nav>
            <Link to="/">Home</Link>
            <Link to="/orders">Orders</Link>
        </nav>

        <Routes>
            <Route path="/"              element={<Home />} />
            <Route path="/orders"        element={<OrderList />} />
            <Route path="/orders/:id"    element={<OrderDetail />} />
            <Route path="/orders/new"    element={<CreateOrder />} />
            <Route path="/admin"         element={
                <ProtectedRoute role="ADMIN">
                    <AdminPanel />
                </ProtectedRoute>
            } />
            <Route path="*"              element={<NotFound />} />
        </Routes>
    </BrowserRouter>
);

// useParams — like @PathVariable ✅
const OrderDetail = () => {
    const { id } = useParams(); // /orders/42 → id=42 ✅
    const [order, setOrder] = useState(null);

    useEffect(() => {
        api.get(`/orders/${id}`)
           .then(res => setOrder(res.data));
    }, [id]);

    return <div>{order?.status}</div>;
};

// useNavigate — like redirect() ✅
const handleLogin = async () => {
    await login(credentials);
    navigate("/dashboard");           // go to ✅
    navigate(-1);                     // go back ✅
    navigate("/login", { replace: true }); // replace ✅
};

// Protected Route — like @PreAuthorize ✅
const ProtectedRoute = ({ children, role }) => {
    const token = localStorage.getItem("token");
    if (!token) return <Navigate to="/login" />;
    if (role && getUserRole(token) !== role)
        return <Navigate to="/unauthorized" />;
    return children;
};
```

| React Router | Spring Boot | Purpose |
|---|---|---|
| `<Route path="/orders">` | `@RequestMapping("/orders")` | URL mapping ✅ |
| `<Route path="/orders/:id">` | `@GetMapping("/{id}")` | URL with param ✅ |
| `useParams()` | `@PathVariable` | Get URL param ✅ |
| `useNavigate()` | `return redirect()` | Navigate ✅ |
| `<Link to="/orders">` | `<a href="/orders">` | Link ✅ |
| `ProtectedRoute` | `@PreAuthorize` | Auth guard ✅ |
| `<Route path="*">` | Default handler | 404 fallback ✅ |

---

## Q8. What is async/await? Compare to CompletableFuture in Java?

### Answer
```
async/await:
→ syntactic sugar over Promises ✅
→ makes async code look synchronous ✅
→ await = wait for Promise to resolve ✅
→ try/catch for error handling ✅

Promise = JavaScript equivalent of CompletableFuture ✅

Key difference:
→ JavaScript = single threaded event loop ✅
→ Java       = multi-threaded ✅
→ both achieve non-blocking behavior ✅

Comparison:
Promise.all()  = CompletableFuture.allOf() ✅
Promise.race() = CompletableFuture.anyOf() ✅
await          = .get() or .join() ✅
.then()        = .thenApply() ✅
.catch()       = .exceptionally() ✅
```

```javascript
// sequential async/await ✅
const fetchData = async () => {
    try {
        const ordersRes    = await api.get("/orders");
        const customersRes = await api.get("/customers");
        setOrders(ordersRes.data);
        setCustomers(customersRes.data);
    } catch (error) {
        setError(error.message);
    }
};

// parallel — Promise.all ✅
// like CompletableFuture.allOf()
const fetchParallel = async () => {
    const [ordersRes, customersRes] = await Promise.all([
        api.get("/orders"),
        api.get("/customers")
    ]);
    setOrders(ordersRes.data);
    setCustomers(customersRes.data);
};

// Promise.race — first wins ✅
// like CompletableFuture.anyOf()
const result = await Promise.race([
    api.get("/orders/fast"),
    api.get("/orders/slow")
]);
```

```java
// Java CompletableFuture equivalent ✅

// sequential — like await
List<Order> result = CompletableFuture
    .supplyAsync(() -> orderService.getOrders())
    .get(); // like await ✅

// parallel — like Promise.all ✅
CompletableFuture<List<Order>> ordersFuture =
    CompletableFuture.supplyAsync(() ->
        orderService.getOrders());

CompletableFuture<List<Customer>> customersFuture =
    CompletableFuture.supplyAsync(() ->
        customerService.getCustomers());

CompletableFuture.allOf(ordersFuture, customersFuture).join();

// chaining — like .then() ✅
CompletableFuture
    .supplyAsync(() -> orderService.getOrders())
    .thenApply(orders -> orders.stream()
        .filter(o -> "PAID".equals(o.getStatus()))
        .toList())
    .thenAccept(paid -> log.info("Paid: {}", paid.size()));
```

| JavaScript | Java | Purpose |
|---|---|---|
| `async function` | `CompletableFuture.supplyAsync()` | Async task ✅ |
| `await` | `.get()` / `.join()` | Wait for result ✅ |
| `Promise.all()` | `CompletableFuture.allOf()` | Wait all ✅ |
| `Promise.race()` | `CompletableFuture.anyOf()` | First wins ✅ |
| `.then()` | `.thenApply()` | Transform ✅ |
| `.catch()` | `.exceptionally()` | Handle error ✅ |

---

## Q9. What is NPM and package.json? Compare to Maven and pom.xml?

### Answer
```
NPM = Node Package Manager
→ downloads dependencies ✅
→ like mvn install ✅
→ stores in node_modules ✅
→ like .m2 local repository ✅

package.json:
→ project name + version ✅
→ dependencies (production) ✅
→ devDependencies (dev only) ✅
→ scripts (start, build, test) ✅
→ like pom.xml ✅

package-lock.json:
→ exact versions locked ✅
→ like pom.xml with exact versions ✅
```

```json
// package.json — like pom.xml ✅
{
  "name": "order-frontend",
  "version": "1.0.0",

  "dependencies": {
    "react":            "^18.2.0",
    "react-dom":        "^18.2.0",
    "react-router-dom": "^6.8.0",
    "axios":            "^1.3.0",
    "typescript":       "^5.0.0"
  },

  "devDependencies": {
    "@types/react": "^18.0.0",
    "vite":         "^4.0.0",
    "vitest":       "^0.34.0",
    "eslint":       "^8.0.0"
  },

  "scripts": {
    "start": "vite",          // mvn spring-boot:run ✅
    "build": "vite build",    // mvn clean package ✅
    "test":  "vitest",        // mvn test ✅
    "lint":  "eslint src"     // mvn checkstyle:check ✅
  }
}
```

```bash
# NPM vs Maven commands

npm install           = mvn install           ✅
npm install axios     = add dependency pom.xml ✅
npm install -D vitest = add test scope dep    ✅
npm start             = mvn spring-boot:run   ✅
npm run build         = mvn clean package     ✅
npm test              = mvn test              ✅
npm update            = mvn versions:update   ✅
npm outdated          = mvn versions:display  ✅
```

| | NPM + package.json | Maven + pom.xml |
|---|---|---|
| **Config file** | package.json ✅ | pom.xml ✅ |
| **Lock file** | package-lock.json | exact versions |
| **Local store** | node_modules | .m2 repository |
| **Install** | `npm install` | `mvn install` |
| **Build** | `npm run build` | `mvn clean package` |
| **Test** | `npm test` | `mvn test` |
| **Registry** | npmjs.com | Maven Central |
| **Dev only** | `devDependencies` | `<scope>test</scope>` |

---

## Q10. How to position yourself as Java developer in React/Node.js interview?

### Confident answer
```
"I am a senior Java/Spring Boot developer with deep
backend expertise — REST APIs, microservices, Kafka,
AWS, JPA. I understand exactly how the backend works
and what the frontend needs.

On the frontend side I understand:
→ how React components call REST APIs ✅
→ JWT token flow end to end ✅
→ CORS configuration ✅
→ network calls in browser DevTools ✅
→ localStorage/sessionStorage ✅
→ async/await similar to CompletableFuture ✅
→ npm/package.json similar to Maven/pom.xml ✅

My advantage over pure frontend developer:
→ I built the APIs they consume ✅
→ I know exactly what response format comes back ✅
→ I can debug both sides simultaneously ✅
→ I understand security end to end ✅
→ full stack thinking — no communication gap ✅"
```

### Complete mapping — React to Java/Spring Boot

| React concept | Java/Spring Boot equivalent |
|---|---|
| `useState` | instance variable / DTO ✅ |
| `useEffect([])` | `@PostConstruct` ✅ |
| `useEffect([dep])` | `@EventListener` ✅ |
| `useEffect cleanup` | `@PreDestroy` ✅ |
| Axios interceptor | Spring Security filter ✅ |
| React Router | `@RequestMapping` ✅ |
| `useParams` | `@PathVariable` ✅ |
| `ProtectedRoute` | `@PreAuthorize` ✅ |
| `Promise.all()` | `CompletableFuture.allOf()` ✅ |
| `async/await` | `CompletableFuture.get()` ✅ |
| `package.json` | `pom.xml` ✅ |
| `npm install` | `mvn install` ✅ |
| `npm run build` | `mvn clean package` ✅ |
| CORS config | `CorsConfigurationSource` ✅ |
| JWT in localStorage | `SecurityContextHolder` ✅ |
| Virtual DOM diff | dirty checking in Hibernate ✅ |
| Component props | method parameters / DTO ✅ |
| `axios.get()` | `restClient.get()` ✅ |
| `.then()` | `.thenApply()` ✅ |
| `.catch()` | `.exceptionally()` ✅ |

### Five talking points — say these confidently
```
1. Full stack understanding:
"I built Spring Boot APIs consumed by React frontends.
I understand both sides completely." ✅

2. Debugging:
"I use browser DevTools network tab to inspect
HTTP calls — same as reading Spring Boot logs.
I know exactly what request goes out and
what response comes back." ✅

3. Security:
"I implemented JWT end to end —
Spring Boot generates token,
React stores and sends it,
Spring Security validates it.
I understand the complete flow." ✅

4. Learning ability:
"React concepts map directly to Java concepts.
useState = variable, useEffect = @PostConstruct,
Axios = RestClient. I pick up new frameworks
quickly because I understand the fundamentals." ✅

5. Team value:
"As a senior backend developer on a fullstack team
I reduce communication gaps between frontend and
backend developers — I speak both languages." ✅
```

---

## Quick Reference — 60 seconds before interview

```
React         = component based UI ✅
Virtual DOM   = diff → only changed parts update ✅
useState      = local data storage ✅
useEffect     = API calls + @PostConstruct ✅
Axios         = HTTP client like RestClient ✅
CORS          = cross origin — fix in Spring Boot ✅
JWT           = Header.Payload.Signature ✅
React Router  = client side @RequestMapping ✅
async/await   = like CompletableFuture ✅
Promise.all() = CompletableFuture.allOf() ✅
package.json  = like pom.xml ✅
npm install   = like mvn install ✅
npm run build = like mvn clean package ✅
Node.js       = JavaScript on server side ✅
Express       = like Spring Boot @RestController ✅
```
