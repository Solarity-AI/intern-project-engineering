# Backend Audit Report

**Date:** 2026-02-13
**Scope:** `backend/src/main/java/com/solarityai/productreview/`
**Constraint:** Read-only audit. No code changes applied.

---

## Audit Checklist

| # | Issue | Status | Severity | Applies To |
|---|-------|--------|----------|------------|
| 1 | CORS security gap | **EXISTS** | Medium | Backend |
| 2 | Backend error handling | **PARTIALLY EXISTS** | Low | Backend |
| 3 | OpenAI mock data | **EXISTS** | Low | Backend |
| 4 | Frontend retry logic | N/A | - | Frontend only |
| 5 | Backend unit tests | **EXISTS (zero tests)** | High | Backend |
| 6 | Notification deep linking | N/A | - | Frontend only |
| 7 | N+1 query problem | **EXISTS** | Medium | Backend |
| 8 | API timeout/retry config | N/A | - | Frontend only |

---

## Detailed Evidence

### Issue 1: CORS Security Gap

**Claim:** `@CrossOrigin("*")` allows all origins.

**Evidence — CONFIRMED:**

| File | Line | Code |
|------|------|------|
| `config/WebConfig.java` | 12-16 | `registry.addMapping("/api/**").allowedOrigins("*").allowedMethods("GET","POST","PUT","DELETE","OPTIONS").allowedHeaders("*")` |
| `controller/ProductController.java` | 21 | `@CrossOrigin(origins = "*")` |
| `controller/UserController.java` | 23 | `@CrossOrigin(origins = "*")` |

**Detail:**
- WebConfig implements `WebMvcConfigurer.addCorsMappings()` with wildcard origins for all `/api/**` routes
- Both REST controllers also declare `@CrossOrigin(origins = "*")` at class level (redundant with WebConfig)
- `allowedHeaders("*")` and all HTTP methods are permitted
- `exposedHeaders("X-User-ID")` is the only restriction

**Risk:** Any domain can make cross-origin API requests. Acceptable for local dev; unacceptable in production.

**How to verify:**
```bash
grep -rn "allowedOrigins\|@CrossOrigin" backend/src/main/java/
```

---

### Issue 2: Backend Error Handling

**Claim:** All errors incorrectly return 404.

**Evidence — CLAIM IS INCORRECT:**

**App-level handler** — `config/GlobalExceptionHandler.java` (lines 15-37):
- Handles only `MethodArgumentNotValidException` → HTTP 400
- Returns `Map<String, Object>` (non-standard format)

**Framework-level handler** — `backend-fw/.../exception/handler/GlobalExceptionHandler.java`:
- `NotFoundException` → 404 (`RES_NOT_FOUND`)
- `ValidationException` → 422 (`VAL_FAILED`)
- `IllegalStateException` / `IllegalArgumentException` → 400 (`REQ_BAD_REQUEST`)
- `AccessDeniedException` → 403 (`PERM_ACCESS_DENIED`)
- `ConflictException` → 409 (`STATE_CONFLICT`)
- `AuthenticationException` → 401 (`AUTH_FAILED`)
- `NoHandlerFoundException` → 404 (`RES_ENDPOINT_NOT_FOUND`)
- Generic `Exception` → 500 (`SYS_INTERNAL_ERROR`)
- Plus 15+ more specialized handlers

**Actual issue found:**
The app-level `GlobalExceptionHandler` duplicates the framework's `MethodArgumentNotValidException` handler with an incompatible response format (`Map<String, Object>` vs framework's `ApiErrorResponse`). This may shadow the framework handler depending on Spring bean ordering, causing **response format inconsistency** — not incorrect status codes.

**How to verify:**
```bash
grep -rn "@ExceptionHandler" backend/src/main/java/
# Compare with:
grep -rn "@ExceptionHandler" backend-fw/src/main/java/
```

---

### Issue 3: OpenAI Integration — Mock Data

**Claim:** Currently uses mock data instead of real integration.

**Evidence — CONFIRMED:**

**File:** `service/impl/AIService.java`

| Line | Code | Finding |
|------|------|---------|
| 17 | `@Value("${openai.api.key:test-key}")` | Defaults to test-key |
| 20-22 | `isTestMode()` checks null / "test-key" / "invalid" | Test mode detection |
| 25-33 | `generateReviewSummary()` | **Both** branches call `generateMockSummary()` |
| 35-43 | `chatAboutProduct()` | **Both** branches call `generateMockChatResponse()` |

Critical code (lines 25-33):
```java
public String generateReviewSummary(ProductEntity product) {
    if (isTestMode()) {
        return generateMockSummary(product);     // mock
    }
    // In a real implementation, this would call OpenAI API
    return generateMockSummary(product);          // also mock
}
```

- No HTTP client (RestTemplate, WebClient, OkHttp) for OpenAI API
- No OpenAI request/response DTOs
- Mock uses rule-based string templates keyed on rating thresholds and question keywords
- Results cached via `@Cacheable(value = "aiSummaries", key = "#product.id")`
- Config in `application.yml`: `openai.api.key: ${OPENAI_API_KEY:test-key}`

**How to verify:**
```bash
grep -n "RestTemplate\|WebClient\|HttpClient\|openai.com" backend/src/main/java/**/*.java
# Should return 0 matches
```

---

### Issue 5: Backend Unit Tests

**Claim:** Only ~3 tests exist.

**Evidence — WORSE THAN CLAIMED (zero tests):**

**Path:** `backend/src/test/java/com/solarityai/productreview/`

```
backend/src/test/
└── java/
    └── com/
        └── solarityai/
            └── productreview/
                (EMPTY — no files)
```

**Test file count: 0**

No unit tests, integration tests, controller tests, or mapper tests exist.

**How to verify:**
```bash
find backend/src/test -name "*.java" -type f | wc -l
# Returns 0
```

---

### Issue 7: N+1 Query Problem

**Claim:** N+1 query problem exists.

**Evidence — CONFIRMED:**

**Primary N+1 source:**
- `entity/ProductEntity.java` line 23:
  ```java
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "product_categories", joinColumns = @JoinColumn(name = "product_id"))
  private Set<String> categories = new HashSet<>();
  ```
  When loading a page of N products, Hibernate executes 1 query for products + N queries for categories = **N+1 queries**.

**Entity relationship summary:**

| Entity | Field | Annotation | Fetch | Risk |
|--------|-------|-----------|-------|------|
| ProductEntity | categories | `@ElementCollection` | **EAGER** | **N+1 on list queries** |
| ProductEntity | reviews | `@OneToMany` | LAZY (default) | Safe |
| ReviewEntity | product | `@ManyToOne` | LAZY (explicit) | Safe |

**No mitigations found:**
- No `JOIN FETCH` in any repository `@Query`
- No `@EntityGraph` annotations
- No `@BatchSize` on collections
- `ProductRepository` custom queries use `JOIN` for category filtering but not for eager fetching

**Secondary concern:**
- `UserServiceImpl.getWishlistProducts()` (lines 50-61): executes 2 queries (get IDs, then get products by IDs) — acceptable but suboptimal
- `UserServiceImpl.markAllNotificationsAsRead()` (lines 105-107): loads all, mutates, saves all — could use bulk UPDATE

**How to verify:**
```bash
# Enable Hibernate SQL logging in application.yml:
# spring.jpa.show-sql: true
# Then load product list page and count SELECT statements
grep -rn "FetchType.EAGER\|@ElementCollection" backend/src/main/java/
```

---

## Summary

| # | Issue | Verdict | Notes |
|---|-------|---------|-------|
| 1 | CORS `*` | **Confirmed** | 3 locations: WebConfig + 2 controllers |
| 2 | All errors → 404 | **Disproven** | Framework handles 20+ types correctly; app handler has format conflict |
| 3 | OpenAI mock | **Confirmed** | Both code branches return mock; no HTTP client exists |
| 5 | Zero tests | **Confirmed** (worse than claimed) | 0 files, not ~3 |
| 7 | N+1 queries | **Confirmed** | `@ElementCollection(EAGER)` on categories |

---

## Next Steps (minimal, no architecture changes)

| # | Action | Effort |
|---|--------|--------|
| 1 | Restrict CORS origins in `WebConfig`; remove redundant `@CrossOrigin` from controllers | Small |
| 2 | Remove app-level `GlobalExceptionHandler` — framework handler already covers it with correct `ApiErrorResponse` format | Small |
| 3 | Add `@BatchSize(size = 20)` on `ProductEntity.categories` or use `JOIN FETCH` in product listing query | Small |
| 4 | Add test classes: `ProductServiceImplTest`, `ProductControllerTest`, `ReviewMapperTest` | Medium |
| 5 | Implement real OpenAI HTTP client in non-test branch of `AIService` | Medium |
