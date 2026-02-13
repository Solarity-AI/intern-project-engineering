# Frontend Audit Report

**Date:** 2026-02-13
**Scope:** `frontend/app/src/main/java/com/productreview/app/`
**Constraint:** Read-only audit. No code changes applied.

---

## Audit Checklist

| # | Issue | Status | Severity | Applies To |
|---|-------|--------|----------|------------|
| 1 | CORS security gap | N/A | - | Backend only |
| 2 | Backend error handling | N/A | - | Backend only |
| 3 | OpenAI mock data | N/A (frontend consumes) | - | Backend only |
| 4 | Frontend retry logic | **EXISTS but UNUSED** | High | Frontend |
| 5 | Backend unit tests | N/A | - | Backend only |
| 6 | Notification deep linking | **PARTIAL** | Medium | Frontend |
| 7 | N+1 query problem | N/A | - | Backend only |
| 8 | API timeout/retry config | **EXISTS** | Low | Frontend |

---

## Detailed Evidence

### Issue 4: Frontend Retry Logic

**Claim:** No retry on network errors.

**Infrastructure EXISTS:**
- `core/RetryPolicy.kt` (lines 9-70) — full implementation:
  - Exponential backoff with jitter (`backoffMultiplier = 2.0`, `useJitter = true`)
  - Configurable max attempts (default: 3)
  - Base delay 1000ms, max delay 30000ms
  - Error-type classification: network/timeout/server → retriable; client/decoding → not retriable
  - Pre-built profiles: `Default`, `NoRetry`, `Aggressive`, `Quick`
  - `suspend fun <T> executeWithRetry(block)` coroutine-based executor
- `di/NetworkModule.kt` (line 44) — provided as Hilt singleton:
  ```kotlin
  provideRetryPolicy(): RetryPolicy = RetryPolicy.Default
  ```

**But NEVER CALLED:**
- `data/repository/ProductRepository.kt` — `safeApiCall()` catches exceptions, wraps in `FWResult.Failure`, does **not** call `executeWithRetry()`
- `data/repository/NotificationRepository.kt` — same pattern, no retry
- Zero references to `RetryPolicy` or `executeWithRetry` in any `data/` or `ui/` file

**Verdict:** Infrastructure is built and injected but never wired into actual API calls. Network errors fail immediately without retry.

**How to verify:**
```bash
# Should return 0 matches outside core/ and di/
grep -r "executeWithRetry\|retryPolicy" frontend/app/src/main/java/com/productreview/app/data/
grep -r "executeWithRetry\|retryPolicy" frontend/app/src/main/java/com/productreview/app/ui/
```

---

### Issue 6: Notification Deep Linking

**Claim:** Notification deep linking missing.

**What EXISTS:**
- `AndroidManifest.xml` (lines 33-41) — deep link intent-filter declared:
  ```xml
  <intent-filter android:autoVerify="true">
      <data android:scheme="productreview" android:host="app" />
  </intent-filter>
  ```
- `navigation/Routes.kt` (lines 37-50) — routes defined for `notification_detail/{notificationId}`
- `ui/screens/notifications/NotificationsScreen.kt` (lines 202-213) — in-app click navigates to product detail or notification detail

**What is MISSING:**
- No `FirebaseMessaging` / FCM integration
- No `PendingIntent` for system notification tray
- No `NotificationManager` / `NotificationCompat.Builder`
- No `createNotificationChannel()` for Android O+
- No background notification receiver / service

**Verdict:** In-app notification navigation works. System-level push notifications with deep linking are not implemented. Notifications are API-polled, not push-driven.

**How to verify:**
```bash
# Should return 0 matches
grep -ri "FirebaseMessaging\|PendingIntent\|NotificationManager\|NotificationCompat" frontend/
```

---

### Issue 8: API Timeout/Retry Config

**Claim:** Mobile has no timeout config.

**Evidence — CONFIGURED:**
- `di/NetworkModule.kt` — Auth OkHttpClient (lines 73-75):
  ```kotlin
  .connectTimeout(30, TimeUnit.SECONDS)
  .readTimeout(30, TimeUnit.SECONDS)
  .writeTimeout(30, TimeUnit.SECONDS)
  ```
- `di/NetworkModule.kt` — Main OkHttpClient (lines 119-121):
  ```kotlin
  .connectTimeout(30, TimeUnit.SECONDS)
  .readTimeout(30, TimeUnit.SECONDS)
  .writeTimeout(30, TimeUnit.SECONDS)
  ```

**Timeout error classification EXISTS:**
- `core/FWError.kt` (lines 40-48) — `timeout()` error type, `isRetriable = true`
- `core/FWError.kt` (line 159) — `SocketTimeoutException` mapped to `FWError.timeout()`

**Verdict:** Timeouts are configured at 30s for all operations on both OkHttp clients. The claim is **incorrect** — timeout config exists.

**How to verify:**
```bash
grep -n "Timeout" frontend/app/src/main/java/com/productreview/app/di/NetworkModule.kt
```

---

## Frontend Error Handling (Supplementary)

The frontend has comprehensive, differentiated error handling:

| Layer | File | Pattern |
|-------|------|---------|
| Sealed result | `core/FWResult.kt` | `FWResult.Success<T>` / `FWResult.Failure(FWError)` |
| Error types | `core/FWError.kt` | 12 typed errors: network, timeout, noInternet, serverError, clientError, notFound, rateLimited, unauthorized, forbidden, decoding, invalidArgument, unknown |
| HTTP mapping | `core/FWError.kt:142-152` | 401→unauthorized, 403→forbidden, 404→notFound, 429→rateLimited, 4xx→clientError, 5xx→serverError |
| Exception mapping | `core/FWError.kt:155-165` | UnknownHostException→noInternet, SocketTimeoutException→timeout, ConnectException→network, IOException→network |
| Retriability | `core/FWError.kt` | network/timeout/noInternet/serverError/rateLimited → `isRetriable = true` |

---

## Summary

| Issue | Expected | Actual | Gap |
|-------|----------|--------|-----|
| Retry logic | Missing | Infrastructure exists, never called | **Wire RetryPolicy into safeApiCall** |
| Deep linking | Missing | In-app only, no push/system notifications | **Add FCM + PendingIntent** |
| Timeout config | Missing | Configured at 30s on both clients | **None (issue is invalid)** |
