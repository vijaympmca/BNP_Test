# Simple Online Bookstore — Spring Boot REST API

A RESTful backend for a small online bookstore. Visitors browse books; logged-in users manage a shopping
cart (add, view, change quantity, remove) and place orders. Administrators also manage the catalog,
manage users and see sales reports.

**Tech stack:** Java 17 · Spring Boot 3.3 · Spring Web · Spring Data JPA · H2 · Spring Security 6 + JWT (jjwt 0.12)
· Bean Validation · Lombok · springdoc-openapi (Swagger UI) · JUnit 5 / Mockito / MockMvc · Maven

---

## 1. Roles

| Role | Display name | Can do |
|---|---|---|
| `USER` | User | Browse books, manage **own** cart, place / view / cancel **own** orders, view own profile |
| `ADMIN` | Administrator | Everything a User can, **plus** create / update / delete books, list users, change a user's role, enable / disable accounts, view **all** orders (and any single order), sales reports |

* Public registration always creates a **USER**. An **ADMIN** is created at startup, and an ADMIN can promote
  another user with `PATCH /api/admin/users/{id}/role`.
* An ADMIN cannot remove their own Administrator role or disable their own account (prevents lock-out).
* The role is re-read from the database on every request, so promotions, demotions and disabled accounts take
  effect immediately, even for tokens that were issued earlier.

---

## 2. Prerequisites

* **Java 17** or newer (`java -version`)
* **Maven 3.9+** — or use the included Maven wrapper (`./mvnw`), see step 3
* Git
* Internet access on the first build (Maven downloads the dependencies)

---

## 3. Get the code and run it

```bash
# 1. Clone
git clone <repo-url>
cd online-bookstore

# 2. (Optional, once) add the Maven wrapper if the repo does not contain mvnw yet
mvn -N wrapper:wrapper

# 3. Build and run all tests
./mvnw clean install          # Windows: mvnw.cmd clean install   (or: mvn clean install)

# 4. Start the application
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run (or: mvn spring-boot:run)

# Alternative: run the packaged jar
java -jar target/online-bookstore-1.0.0.jar
```

The API is now available at **http://localhost:8080**.

### Useful URLs

| What | URL |
|---|---|
| Swagger UI (try every endpoint) | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 console | http://localhost:8080/h2-console — JDBC URL `jdbc:h2:mem:bookstore`, user `sa`, empty password |
| Health check | http://localhost:8080/actuator/health → `200 UP` or `503 DOWN` |

### Configuration (environment variables)

| Variable | Default | Purpose |
|---|---|---|
| `JWT_SECRET` | dev value in `application.yml` | HMAC key for JWTs — **at least 32 characters; change it outside local development** |
| `JWT_EXPIRATION_MS` | `86400000` (24 h) | Token lifetime |
| `ADMIN_PASSWORD` | `Bookstore@2026` | Password of the seeded admin |
| `USER_PASSWORD` | `Reader@2026` | Password of the seeded user |

The database is in-memory, so all data resets on every restart. For a persistent file database, change
`spring.datasource.url` to `jdbc:h2:file:./data/bookstore`.

---

## 4. Seed data (loaded at startup)

| Username | Email | Password | Role |
|---|---|---|---|
| `admin` | admin@bookstore.com | `Bookstore@2026` | ADMIN (Administrator) |
| `john_doe` | john.doe@bookstore.com | `Reader@2026` | USER |

Plus **12 sample books** (one of them, *Kubernetes in Action*, has stock 0 to show the out-of-stock case).

---

## 5. Using the API (curl walkthrough)

All responses use one of two shapes.

Success:
```json
{ "success": true, "message": "Item added to cart", "data": { ... }, "timestamp": "2026-10-14T13:09:00" }
```
Error:
```json
{ "timestamp": "2026-10-14T13:09:00", "status": 404, "error": "Not Found",
  "message": "Book not found with id: 42", "path": "/api/books/42",
  "validationErrors": { "quantity": "quantity must be at least 1" } }
```

### 5.1 Register and log in (USER)

```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ravi Kumar","username":"ravi_k","email":"ravi@example.com",
       "password":"Secure#Pass1","confirmPassword":"Secure#Pass1","phone":"9876543210"}'
# → 201 Created

curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"ravi_k","password":"Secure#Pass1"}'
# → 200 OK, data.accessToken = "eyJhbGciOi..."
```

Copy the token into a variable (Linux/macOS):

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"ravi_k","password":"Secure#Pass1"}' | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')
```

Send it on every protected request: `-H "Authorization: Bearer $TOKEN"`.

### 5.2 Browse books (public)

```bash
curl "http://localhost:8080/api/books?page=0&size=10"
curl "http://localhost:8080/api/books?search=java&sortBy=price&direction=desc"
curl  http://localhost:8080/api/books/1
```

### 5.3 Cart

```bash
curl -X POST http://localhost:8080/api/cart/items -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"bookId":1,"quantity":2}'           # 201
curl http://localhost:8080/api/cart -H "Authorization: Bearer $TOKEN"           # 200
curl -X PUT http://localhost:8080/api/cart/items/1 -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"quantity":3}'                      # 200
curl -X DELETE http://localhost:8080/api/cart/items/1 -H "Authorization: Bearer $TOKEN"  # 204
curl -X DELETE http://localhost:8080/api/cart -H "Authorization: Bearer $TOKEN"          # 204 (clear)
```

### 5.4 Orders

```bash
curl -X POST http://localhost:8080/api/cart/items -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"bookId":2,"quantity":1}'
curl -X POST http://localhost:8080/api/orders -H "Authorization: Bearer $TOKEN"          # 201 checkout
curl http://localhost:8080/api/orders -H "Authorization: Bearer $TOKEN"                  # 200 my orders
curl http://localhost:8080/api/orders/1 -H "Authorization: Bearer $TOKEN"                # 200 / 403 / 404
curl -X POST http://localhost:8080/api/orders/1/cancel -H "Authorization: Bearer $TOKEN" # 200, stock returned
```

### 5.5 Administrator

```bash
ADMIN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"Bookstore@2026"}' | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')

# Books
curl -X POST http://localhost:8080/api/books -H "Authorization: Bearer $ADMIN" -H "Content-Type: application/json" \
  -d '{"title":"Java Performance","author":"Scott Oaks","isbn":"9781492056119","price":59.99,"stock":7}'   # 201
curl -X PUT http://localhost:8080/api/books/13 -H "Authorization: Bearer $ADMIN" -H "Content-Type: application/json" \
  -d '{"title":"Java Performance","author":"Scott Oaks","isbn":"9781492056119","price":49.99,"stock":10}'  # 200
curl -X DELETE http://localhost:8080/api/books/13 -H "Authorization: Bearer $ADMIN"                        # 204

# Users
curl http://localhost:8080/api/admin/users -H "Authorization: Bearer $ADMIN"
curl -X PATCH http://localhost:8080/api/admin/users/3/role -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" -d '{"role":"ADMIN"}'
curl -X PATCH http://localhost:8080/api/admin/users/3/status -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" -d '{"enabled":false}'

# All orders and reports
curl http://localhost:8080/api/admin/orders -H "Authorization: Bearer $ADMIN"
curl "http://localhost:8080/api/admin/reports/top-books?limit=5" -H "Authorization: Bearer $ADMIN"
curl http://localhost:8080/api/admin/reports/order-summary -H "Authorization: Bearer $ADMIN"
```

On Windows PowerShell, use `curl.exe` and put JSON bodies in single quotes, or simply use Swagger UI:
log in, copy `accessToken`, click **Authorize** and paste the token (without the word `Bearer`).

---

## 6. Endpoint reference

### Auth
| Method | Endpoint | Access | Success | Errors |
|---|---|---|---|---|
| POST | `/api/auth/register` | Public | 201 | 400 validation, 409 username/email exists |
| POST | `/api/auth/login` | Public | 200 (JWT) | 400, 401 bad credentials, 403 account disabled |
| GET | `/api/auth/me` | USER, ADMIN | 200 | 401 |

### Books
| Method | Endpoint | Access | Success | Errors |
|---|---|---|---|---|
| GET | `/api/books?page=0&size=10&search=java&sortBy=title&direction=asc` | Public | 200 | 400 bad paging / sort |
| GET | `/api/books/{id}` | Public | 200 | 404 |
| POST | `/api/books` | ADMIN | 201 | 400, 401, 403, 409 ISBN exists |
| PUT | `/api/books/{id}` | ADMIN | 200 | 400, 401, 403, 404, 409 |
| DELETE | `/api/books/{id}` | ADMIN | 204 | 401, 403, 404, 409 book is in existing orders |

`sortBy`: `id`, `title`, `author`, `price`, `stock`. `size`: 1–100.

### Cart (the cart always belongs to the user in the token)
| Method | Endpoint | Access | Success | Errors |
|---|---|---|---|---|
| GET | `/api/cart` | USER, ADMIN | 200 | 401 |
| GET | `/api/cart/summary` | USER, ADMIN | 200 | 401 |
| POST | `/api/cart/items` `{bookId, quantity}` | USER, ADMIN | 201 | 400, 404 book, 409 insufficient stock |
| PUT | `/api/cart/items/{bookId}` `{quantity}` | USER, ADMIN | 200 | 400, 404 not in cart, 409 stock |
| DELETE | `/api/cart/items/{bookId}` | USER, ADMIN | 204 | 404 |
| DELETE | `/api/cart` | USER, ADMIN | 204 | 401 |

### Orders
| Method | Endpoint | Access | Success | Errors |
|---|---|---|---|---|
| POST | `/api/orders` (checkout) | USER, ADMIN | 201 | 400 empty cart, 409 stock changed |
| GET | `/api/orders?page=0&size=10` | USER, ADMIN (own orders) | 200 | 401 |
| GET | `/api/orders/{id}` | Owner or ADMIN | 200 | 403 not your order, 404 |
| POST | `/api/orders/{id}/cancel` | Owner or ADMIN | 200 | 403, 404, 409 already cancelled |

### Administration (ADMIN only — a USER gets 403)
| Method | Endpoint | Success | Errors |
|---|---|---|---|
| GET | `/api/admin/users?page=0&size=10` | 200 | 401, 403 |
| GET | `/api/admin/users/{id}` | 200 | 404 |
| PATCH | `/api/admin/users/{id}/role` `{ "role": "USER" \| "ADMIN" }` | 200 | 400 (incl. demoting yourself), 404 |
| PATCH | `/api/admin/users/{id}/status` `{ "enabled": true \| false }` | 200 | 400 (incl. disabling yourself), 404 |
| GET | `/api/admin/orders?page=0&size=10` | 200 | 401, 403 |
| GET | `/api/admin/reports/top-books?limit=5` | 200 | 400 limit outside 1–50 |
| GET | `/api/admin/reports/order-summary` | 200 | 401, 403 |

### Health
| Method | Endpoint | Success |
|---|---|---|
| GET | `/actuator/health` | 200 `UP` / 503 `DOWN` (includes `database` and `bookCatalog` checks) |

### Status codes used
`200` OK · `201` Created · `204` No Content · `400` validation / bad input / empty cart · `401` missing, invalid
or expired token, wrong credentials · `403` wrong role, not your order, disabled account · `404` not found ·
`405` wrong HTTP method · `409` duplicate, insufficient stock, already cancelled · `500` unexpected error
(logged; generic message returned).

---

## 7. Registration rules

| Field | Rules |
|---|---|
| `fullName` | Required, 2–50 characters, letters and spaces only |
| `username` | Required, 4–20 characters, starts with a letter, only letters, digits, `_` or `.`; unique (case-insensitive) |
| `email` | Required, valid email format, max 100 characters; unique (case-insensitive, stored in lowercase) |
| `password` | Required, 8–64 characters, at least 1 uppercase, 1 lowercase, 1 digit, 1 special character, no spaces, must not contain the username |
| `confirmPassword` | Required, must equal `password` |
| `phone` (optional) | 10 digits, starting with 6–9 |

Validation runs in four layers: field annotations (400) → custom validators `@StrongPassword` / `@PasswordMatches`
(400) → business rules in `AuthServiceImpl` (409 duplicate) → database unique constraints (409, last safety net).
The login error is deliberately generic (*"Invalid username/email or password"*), so it doesn't reveal
whether a username exists. Users can log in with either username or email.

---

## 8. Running tests

```bash
./mvnw test
```

| Test class | Type | Covers |
|---|---|---|
| `AuthControllerTest` | MockMvc integration | 201 register, 400 blank/weak/mismatched/username-in-password, 409 duplicate, login by email, 401, admin role |
| `BookControllerTest` | MockMvc integration | public listing, search, 400 paging, 404, 401/403 for writes, admin create/delete |
| `CartAndOrderControllerTest` | MockMvc integration | add/merge/update/remove, totals, checkout, stock decrease, 403 ownership, cancel + restock, 409 stock, 400 empty cart |
| `AdminControllerTest` | MockMvc integration | 403 for USER, reports, promote USER→ADMIN, disable account → 403 login, health |
| `AuthServiceImplTest` | Mockito unit | duplicate username/email, encoded password, USER role |
| `CartServiceImplTest` | Mockito unit | 404 book, add/merge quantity, 409 stock, 404 not in cart |
| `StrongPasswordValidatorTest` | Unit | valid and invalid passwords |

Tests use a separate in-memory database (`src/test/resources/application-test.yml`).

---

## 9. Architecture

```
Client (Postman / Frontend)
  → RequestLoggingFilter (correlation ID) → JwtAuthenticationFilter        filter/
  → Spring Security route rules (USER / ADMIN)                              security/, configuration/
  → ExecutionTimeInterceptor, AuditInterceptor                              interceptor/
  → Controller (validates request objects, returns ResponseEntity)          controller/
  → Service interface → ServiceImpl (business logic), Mapper Entity↔Object service/, serviceimpl/, mapping/
  → Repository (Spring Data JPA + JPQL → DTO projections)                   repository/, dto/
  → H2 database (JPA entities)                                              entity/
Any exception → GlobalExceptionHandler → standard ErrorResponse + HTTP code exception/
```

Data model: `User 1—1 Cart 1—* CartItem *—1 Book` and `User 1—* Order 1—* OrderItem *—1 Book`.
`OrderItem.priceAtPurchase` is a snapshot, so later price changes don't alter old orders. `Book` has a
`@Version` column, so two simultaneous checkouts of the same book can't oversell it (the loser gets 409).

---

## 10. Project structure (18 folders, base package `com.bookstore`)

| # | Folder | Contents |
|---|---|---|
| 1 | `controller/` | REST endpoints: Auth, Book, Cart, Order, Report, Admin |
| 2 | `service/` | Service interfaces (contracts) |
| 3 | `serviceimpl/` | Business logic implementations |
| 4 | `repository/` | Spring Data JPA repositories + custom JPQL |
| 5 | `dto/` | Read-only projections of reporting queries (`TopSellingBookDto`, `OrderSummaryDto`, `CartTotalDto`) |
| 6 | `entity/` | JPA entities and enums (`Role` = USER / ADMIN, `OrderStatus`) |
| 7 | `object/` | API request (`object/request`) and response (`object/response`) bodies |
| 8 | `bean/` | Spring-managed config / scoped beans (`JwtProperties`, `AppProperties`, `RequestContext`) |
| 9 | `pojo/` | Plain internal models (`JwtClaims`, `PriceBreakdown`, `StockCheckResult`) |
| 10 | `configuration/` | Security, MVC (interceptors, CORS), Swagger, seed data |
| 11 | `mapping/` | Entity ↔ response object mappers |
| 12 | `security/` | JWT provider, user details, 401 / 403 JSON handlers |
| 13 | `filter/` | JWT authentication filter, request logging filter |
| 14 | `interceptor/` | Execution-time and audit interceptors |
| 15 | `utility/` | Constants, `SecurityUtils`, `PriceUtils`, `PaginationUtils`, `validator/` (custom password validators) |
| 16 | `health/` | Custom health indicators (`database`, `bookCatalog`) |
| 17 | `exception/` | Global exception handler, `ErrorResponse`, custom exceptions |
| 18 | `application/` | `BookstoreApplication` main class (scans all packages explicitly) |

Mappers are hand-written `@Component` classes rather than MapStruct, which keeps the build free of
annotation-processor ordering issues; swapping in MapStruct later only touches the `mapping/` folder.

---

## 11. Troubleshooting

| Problem | Fix |
|---|---|
| `app.jwt.secret must be at least 32 characters` on startup | Set `JWT_SECRET` to a longer value |
| `Port 8080 already in use` | `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081` |
| 401 on cart/orders | Log in again and send `Authorization: Bearer <token>`; tokens expire after 24 h |
| 403 on `/api/admin/**` or book writes | The account is a USER; log in as `admin` or promote the user |
| `./mvnw: Permission denied` | `chmod +x mvnw` |
