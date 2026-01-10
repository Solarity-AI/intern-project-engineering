# Backend (Spring Boot) – Architecture, API Contract, Caching & Production Guide (Intern Onboarding)

> Scope: `backend/`  
> Audience: New interns / junior backend engineers.  
> Goal: Explain **why each file exists**, **how layers connect**, and how to implement **cache + pagination + AI summary** safely.

---

## 0) High-level Intent

This backend is a **Spring Boot REST API** designed to support:
- Products (browse, detail)
- Reviews (create, list)
- AI summary generation for product reviews (expensive computation)
- Production concerns: caching, consistent error handling, test coverage

The code is structured intentionally with clear layers:
- Controllers (HTTP boundary)
- Services (business logic)
- Repositories (persistence)
- Models (JPA entities)
- DTOs (API contract)
- Global exception handling (reliable client behavior)
- Cache config (performance + scalability)

---

## 1) Folder & File Map (What each part is for)

### 1.1 Entry point

- `ProductReviewApplication.java`
  - Boots Spring context
  - Root of dependency injection graph
  - Enables auto-config and component scan

### 1.2 Configuration

- `config/CacheConfig.java`
  - Defines caching behavior and cache manager setup
  - Enables cache annotations usage in services

### 1.3 Controller (HTTP boundary)

- `controller/ProductController.java`
  - Defines REST endpoints (products, reviews, ai summary if included)
  - **No business logic**; validates input, maps DTOs, calls service layer
  - Returns stable error shapes by relying on `GlobalExceptionHandler`

### 1.4 DTOs (API contract boundary)

- `dto/ProductDTO.java`
- `dto/ReviewDTO.java`
  - Define JSON payload shapes for the client
  - Prevent direct entity exposure
  - Allow evolution of database schema without breaking API

### 1.5 Exceptions

- `exception/GlobalExceptionHandler.java`
  - Central error mapping layer
  - Converts exceptions into consistent HTTP responses
  - Critical for predictable frontend error UX (retry, messages, status codes)

### 1.6 Domain Models (Persistence representation)

- `model/Product.java`
- `model/Review.java`
  - JPA entities (table mapping + relationships)
  - Internal only—should not be serialized directly to client

### 1.7 Persistence layer

- `repository/ProductRepository.java`
- `repository/ReviewRepository.java`
  - Spring Data JPA repositories
  - Own database access
  - Must not contain business rules

### 1.8 Service layer (Core business logic)

- `service/ProductService.java`
  - Central orchestration:
    - read/write products & reviews
    - apply business rules
    - pagination/filter/sort policy
    - DTO mapping
  - Ideal place for transaction boundaries (`@Transactional`)

- `service/AISummaryService.java`
  - Isolates expensive AI summary computation
  - Should be cacheable (summaries change only when reviews change)
  - Keeps ProductService focused and readable

- `service/DataInitializer.java`
  - Seeds demo/dev data at startup
  - Improves dev experience and onboarding

### 1.9 Runtime configuration

- `src/main/resources/application.properties`
  - DB config, port, JPA settings, cache settings
  - Environment-specific override point

### 1.10 Tests

- `src/test/.../ProductControllerIntegrationTest.java`
  - Integration tests: HTTP → service → repository → DB
  - Prevents API regressions

- `src/test/.../ProductServiceTest.java`
  - Service-level unit tests for business rules and edge cases

### 1.11 Build & tooling

- `pom.xml`
  - dependencies and build plugins
- `mvnw`, `mvnw.cmd`
  - Maven Wrapper for consistent builds across machines

---

## 2) Core architectural improvements absorbed into the backend

### 2.1 Lock the API contract (pagination + consistency)

List endpoints should be stable and consistent.

**Products list example**
`GET /products?page=1&pageSize=20&sort=rating_desc&category=...&q=...`

**Response**
```json
{
  "items": [],
  "page": 1,
  "pageSize": 20,
  "total": 180,
  "totalPages": 9,
  "hasNext": true
}
```

**Why this matters**
- Frontend caching keys rely on stable contract
- Prevents subtle UI bugs (wrong counts, stale lists)

### 2.2 AI summary as a first-class backend feature

Endpoint suggestion (choose one consistent style):
- `GET /products/{id}/ai-summary`
or
- `GET /reviews/summary?productId={id}`

**Caching policy**
- AI summaries are expensive → cache them aggressively
- Summaries only change when reviews change

Recommended approach:
- `@Cacheable` on AI summary getter
- `@CacheEvict` when a review is created/updated/deleted

### 2.3 Caching + invalidation (production realism)

Caching without invalidation is worse than no caching.

**Invalidation triggers**
- On review creation:
  - Evict aiSummary(productId)
  - Evict productDetails(productId) if cached
  - Evict productsList(queryParams) if cached
- On product update:
  - Evict productDetails(productId)
  - Evict productsList(*)

CacheConfig exists to make this systematic instead of scattered.

### 2.4 Dependency Inversion Principle (mentor feedback)

> Controllers should depend on abstractions (interfaces), not concrete implementations.

If currently controller injects `ProductService` directly, preferred practice:
- Define `ProductService` as an interface
- Implement `ProductServiceImpl`
- Inject interface into controller

This enables:
- easier testing/mocking
- clearer boundaries
- better maintainability

---

## 3) Run, Build, Test

### Run locally
```bash
cd backend
./mvnw spring-boot:run
```

### Build
```bash
./mvnw clean package
```

### Test
```bash
./mvnw test
```

---

## 4) How frontend consumes this backend

Frontend should treat backend responses as server state and cache them. Backend should:
- provide stable pagination metadata
- return predictable error shapes
- maintain consistency after writes (review creation)

End-to-end flow:
```
Mobile → Controller → Service → Repository → DB
                         └→ AISummaryService (cached)
```

---

## 5) Intern “How to learn this codebase” path

1. Read `ProductController` to see API surface
2. Follow calls into `ProductService`
3. Learn entity relationships in `Product` + `Review`
4. Study DTO mapping in services
5. Study caching annotations + invalidation strategy
6. Run integration tests to observe real request flows

---

## 6) Related docs
- Mobile client architecture → `README_FRONTEND.md`
- System overview + release checklist → root `README.md`
