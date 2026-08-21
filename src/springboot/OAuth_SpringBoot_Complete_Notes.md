# OAuth 2.0 + Spring Boot — Complete Notes
> Quick Reference for Interview & Development

---

## 1. What is OAuth 2.0?

```
OAuth 2.0 = Authorization framework
→ allows third-party apps to access resources
  WITHOUT sharing passwords

Example:
"Login with Google" on any website
→ you never give your Google password to that website
→ Google issues a token → website uses token ✅
```

---

## 2. Key Roles

```
Resource Owner  → the USER (you)
Client          → the APP wanting access (Spring Boot app)
Authorization Server → issues tokens (Google, Okta, Keycloak)
Resource Server → holds protected data (your API)
```

---

## 3. OAuth 2.0 Grant Types

### Grant Type 1 — Authorization Code (most common, most secure)
```
Used for: web apps, mobile apps where USER is present

Flow:
1. User clicks "Login with Google"
2. App redirects to Google login page
3. User logs in → Google asks "Allow this app?"
4. User approves → Google redirects back with AUTH CODE
5. App exchanges AUTH CODE for ACCESS TOKEN (server-to-server)
6. App uses ACCESS TOKEN to call APIs ✅

Why secure:
→ token never exposed in browser URL
→ exchange happens server-to-server ✅
```

### Grant Type 2 — Client Credentials (machine to machine)
```
Used for: microservice to microservice, no user involved

Flow:
1. Service A sends client_id + client_secret to Auth Server
2. Auth Server validates → issues ACCESS TOKEN
3. Service A calls Service B with ACCESS TOKEN
4. Service B validates token → allows request ✅

No user login needed — service authenticates itself
```

### Grant Type 3 — Password (legacy, avoid)
```
Used for: trusted first-party apps only

Flow:
1. User enters username + password in YOUR app
2. App sends credentials to Auth Server
3. Auth Server issues token

Problem:
→ user shares password with your app ❌
→ not recommended — use Authorization Code instead
```

### Grant Type 4 — Implicit (deprecated)
```
Old mobile/SPA flow — token returned directly in URL
→ deprecated — use Authorization Code + PKCE instead ❌
```

---

## 4. Tokens

### Access Token
```
→ short-lived (15 mins - 1 hour)
→ used to access protected resources
→ JWT format (most common) or opaque string
```

### Refresh Token
```
→ long-lived (days/weeks)
→ used to get new access token when expired
→ stored securely (never in browser) ✅
```

### ID Token (OpenID Connect)
```
→ JWT containing user info (name, email, sub)
→ OIDC extension of OAuth 2.0
→ used for authentication (who the user is)
```

### JWT Structure
```
eyJhbGc.eyJzdWI.SflKxwRJ

Header.Payload.Signature

Header:  { "alg": "HS256", "typ": "JWT" }
Payload: { "sub": "user123", "email": "user@gmail.com",
           "roles": ["USER"], "exp": 1234567890 }
Signature: HMAC-SHA256(header + payload, SECRET)
```

---

## 5. Dependencies

```xml
<!-- OAuth2 Resource Server (protect your API) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- OAuth2 Client (login with Google/Okta) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

## 6. OAuth2 Client — Login with Google

### application.yml
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
```

### Security Config
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .failureUrl("/login?error")
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
            );
        return http.build();
    }
}
```

### Get logged-in user info
```java
@RestController
public class UserController {

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(
            @AuthenticationPrincipal OAuth2User principal) {
        return Map.of(
            "name",  principal.getAttribute("name"),
            "email", principal.getAttribute("email"),
            "sub",   principal.getAttribute("sub")
        );
    }
}
```

---

## 7. OAuth2 Resource Server — Protect your API with JWT

### application.yml
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://accounts.google.com        # Google
          # OR
          issuer-uri: https://your-domain.okta.com       # Okta
          # OR
          jwk-set-uri: http://localhost:8080/realms/myrealm/protocol/openid-connect/certs  # Keycloak
```

### Security Config
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // enables @PreAuthorize
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthConverter())
                )
            );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter converter =
                new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix("ROLE_");           // prefix roles
        converter.setAuthoritiesClaimName("roles");      // claim name in JWT

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }
}
```

### Access JWT claims in controller
```java
@RestController
public class OrderController {

    @GetMapping("/api/orders")
    public List<Order> getOrders(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();           // user id
        String email  = jwt.getClaim("email");      // email claim
        List<String> roles = jwt.getClaim("roles"); // roles claim
        return orderService.getOrdersByUser(userId);
    }

    @GetMapping("/api/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")               // method level security
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }
}
```

---

## 8. Client Credentials — Microservice to Microservice

### application.yml (calling service)
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          order-service:
            client-id: order-service
            client-secret: ${CLIENT_SECRET}
            authorization-grant-type: client_credentials
            scope: payment:read payment:write
        provider:
          order-service:
            token-uri: https://auth-server/oauth/token
```

### WebClient with OAuth2 token (auto-inject token)
```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient paymentWebClient(
            OAuth2AuthorizedClientManager authorizedClientManager) {

        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                    authorizedClientManager);
        oauth2.setDefaultClientRegistrationId("order-service");

        return WebClient.builder()
                .filter(oauth2)  // auto-attaches token to every request ✅
                .baseUrl("https://payment-service")
                .build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRepo,
            OAuth2AuthorizedClientRepository authorizedClientRepo) {

        OAuth2AuthorizedClientProvider provider =
            OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        DefaultOAuth2AuthorizedClientManager manager =
            new DefaultOAuth2AuthorizedClientManager(clientRepo, authorizedClientRepo);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }
}
```

### Usage — auto token injection
```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final WebClient paymentWebClient;

    public PaymentResponse processPayment(PaymentRequest request) {
        return paymentWebClient.post()
                .uri("/api/v1/payments")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .block();
        // OAuth2 token automatically attached ✅
        // token refreshed automatically when expired ✅
    }
}
```

---

## 9. Keycloak Setup (popular Auth Server)

### application.yml
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: my-app
            client-secret: ${KEYCLOAK_SECRET}
            scope: openid, profile, email
            authorization-grant-type: authorization_code
        provider:
          keycloak:
            issuer-uri: http://localhost:8080/realms/myrealm
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/myrealm
```

### Extract Keycloak roles (nested in token)
```java
@Component
public class KeycloakJwtConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Keycloak puts roles in realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> roles = (List<String>) realmAccess.get("roles");

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());

        return new JwtAuthenticationToken(jwt, authorities);
    }
}

// register in SecurityConfig
.oauth2ResourceServer(oauth2 -> oauth2
    .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtConverter()))
)
```

---

## 10. Custom JWT (without external Auth Server)

### Generate JWT
```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private static final long EXPIRY = 86400000; // 24 hours

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRY))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
```

### JWT Filter
```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
                                    throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token    = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        if (username != null &&
            SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(request, response);
    }
}
```

### Login endpoint
```java
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody LoginRequest request) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.username(), request.password()
            )
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(Map.of("token", token));
    }
}

public record LoginRequest(String username, String password) {}
```

---

## 11. OpenID Connect (OIDC)

```
OAuth 2.0  = Authorization (what you can do)
OIDC       = Authentication (who you are) — built on top of OAuth 2.0

OIDC adds:
→ ID Token (JWT with user info)
→ /userinfo endpoint
→ standard claims: sub, name, email, picture

scope = "openid" → triggers OIDC → get ID Token
scope = "profile" → get name, picture
scope = "email"   → get email
```

---

## 12. Common Interview Questions

| Question | Answer |
|---|---|
| **OAuth vs OIDC** | OAuth = authorization (access), OIDC = authentication (identity) built on OAuth |
| **Access vs Refresh token** | Access = short-lived API token, Refresh = long-lived, gets new access token |
| **Authorization Code vs Client Credentials** | Auth Code = user present (web/mobile), Client Credentials = machine to machine |
| **Where to store token** | HttpOnly cookie (web) or memory (SPA) — never localStorage |
| **JWT vs Opaque token** | JWT = self-contained (no DB lookup), Opaque = requires introspection endpoint |
| **What is PKCE** | Proof Key Code Exchange — prevents auth code interception in mobile/SPA |
| **Token expiry** | Access token 15min-1hr, Refresh token days-weeks |
| **What is scope** | Permissions requested — `read:orders`, `write:payments` |
| **Stateless vs Stateful** | JWT = stateless (no session), Session = stateful (server stores session) |
| **How to revoke JWT** | Blacklist in Redis or use short expiry + refresh token rotation |

---

## 13. Security Best Practices

```
✅ Store secrets in AWS Secrets Manager — never hardcode
✅ Use HTTPS always — never HTTP for OAuth flows
✅ Short access token expiry (15-60 mins)
✅ Rotate refresh tokens on use
✅ Use PKCE for mobile and SPA apps
✅ Validate JWT signature + expiry + issuer on every request
✅ Use HttpOnly cookie for tokens in browser — never localStorage
✅ Scope tokens — give minimum permissions needed
✅ Log all auth events for audit trail
❌ Never log tokens or secrets
❌ Never send tokens in URL query params
```

---

## 14. Flow Summary

### Authorization Code Flow
```
User → App → Auth Server (login) → Auth Code
Auth Code → App → Auth Server (exchange) → Access Token + Refresh Token
Access Token → Resource Server → Protected Data ✅
```

### Client Credentials Flow
```
Service A → Auth Server (client_id + secret) → Access Token
Access Token → Service B → Protected API ✅
```

### Token Refresh Flow
```
Access Token expired → App sends Refresh Token → Auth Server
Auth Server → new Access Token (+ new Refresh Token) ✅
```
