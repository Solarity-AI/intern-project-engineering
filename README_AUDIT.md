# ProductReviewApp — Codebase Audit Report

**Date:** 2026-02-13
**Auditor:** Automated (Claude Code)
**Scope:** Full-stack — Android frontend + Spring Boot backend
**Constraint:** Read-only audit. No code changes applied. `backend-fw/` and `android-fw/` not modified.

---

## Master Checklist

| # | Issue | Backend | Frontend | Overall Status |
|---|-------|---------|----------|----------------|
| 1 | CORS security gap (`allowedOrigins("*")`) | **EXISTS** | N/A | Confirmed |
| 2 | Backend error handling (all errors → 404) | **DISPROVEN** | N/A | Not an issue (format conflict only) |
| 3 | OpenAI integration (mock data only) | **EXISTS** | Consumes mock | Confirmed |
| 4 | Frontend retry logic (no retry on errors) | N/A | **EXISTS but UNUSED** | Confirmed (dead code) |
| 5 | Backend unit tests (~3 tests) | **0 tests** | N/A | Confirmed (worse than claimed) |
| 6 | Notification deep linking (missing) | N/A | **PARTIAL** | In-app only, no push/system |
| 7 | Database N+1 query problem | **EXISTS** | N/A | Confirmed |
| 8 | API timeout/retry (no timeout config) | N/A | **EXISTS** | Disproven (30s configured) |

---

## Severity Matrix

| Severity | Issues |
|----------|--------|
| **High** | #5 Zero backend tests, #4 Retry infrastructure unused |
| **Medium** | #1 CORS wildcard, #7 N+1 on categories, #6 No push notifications |
| **Low** | #3 OpenAI mock (intentional MVP), #2 Handler format conflict, #8 Already configured |

---

## Key Findings Summary

### Confirmed Issues

**#1 — CORS `allowedOrigins("*")`**
Three redundant locations: `WebConfig.java`, `ProductController.java`, `UserController.java`. All permit any origin.

**#3 — OpenAI Mock**
`AIService.java` has test-mode detection but both branches (test and production) return `generateMockSummary()` / `generateMockChatResponse()`. No HTTP client or OpenAI DTOs exist.

**#4 — Retry Logic Unused**
`core/RetryPolicy.kt` implements exponential backoff with jitter, is injected via Hilt, but `executeWithRetry()` is called by **zero** repository or ViewModel methods. All `safeApiCall()` flows fail immediately.

**#5 — Zero Backend Tests**
`backend/src/test/java/` directory tree exists but contains no `.java` files. The claim of "~3 tests" overstates coverage.

**#7 — N+1 Queries**
`ProductEntity.categories` uses `@ElementCollection(fetch = FetchType.EAGER)`. Loading 10 products = 11 SQL queries. No `@BatchSize`, `@EntityGraph`, or `JOIN FETCH` mitigations exist.

### Disproven Claims

**#2 — "All errors return 404"**
The framework's `GlobalExceptionHandler` (in `backend-fw`) maps 20+ exception types to proper HTTP codes (400/401/403/404/409/422/500). The actual issue is a minor format conflict: the app-level handler returns `Map` for validation errors while the framework returns `ApiErrorResponse`.

**#8 — "No timeout config"**
Both OkHttp clients in `NetworkModule.kt` configure `connectTimeout(30s)`, `readTimeout(30s)`, `writeTimeout(30s)`. `FWError.kt` classifies `SocketTimeoutException` as retriable.

### Partial

**#6 — Notification Deep Linking**
In-app navigation works (`NotificationsScreen` → product detail / notification detail). But: no FCM, no `PendingIntent`, no `NotificationManager`, no `createNotificationChannel()`. Notifications are API-polled only.

---

## Detailed Reports

| Report | Path | Covers |
|--------|------|--------|
| Frontend Audit | [`frontend/README_AUDIT.md`](frontend/README_AUDIT.md) | Issues #4, #6, #8 + error handling patterns |
| Backend Audit | [`backend/README_AUDIT.md`](backend/README_AUDIT.md) | Issues #1, #2, #3, #5, #7 + evidence + verification commands |

---

## Recommended Priority Order

| Priority | Issue | Action | Est. Effort |
|----------|-------|--------|-------------|
| P0 | #5 Backend tests | Add `ProductServiceImplTest`, `ProductControllerTest`, `ReviewMapperTest` | 8-10h |
| P1 | #4 Wire retry logic | Integrate `RetryPolicy.executeWithRetry()` into repository `safeApiCall()` | 2-3h |
| P1 | #1 CORS lockdown | Replace `"*"` with specific origins in `WebConfig`; remove `@CrossOrigin` from controllers | 1h |
| P2 | #7 N+1 fix | Add `@BatchSize(size=20)` on `ProductEntity.categories` or `JOIN FETCH` in repository | 2h |
| P2 | #2 Handler conflict | Remove app-level `GlobalExceptionHandler` (framework handler covers it) | 30min |
| P3 | #3 OpenAI real integration | Implement HTTP client in `AIService` non-test branch | 8-10h |
| P3 | #6 Push notifications | Add FCM + `PendingIntent` + `NotificationChannel` | 10-12h |
| -- | #8 Timeout config | No action needed (already configured) | 0h |
