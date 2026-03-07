# Authentication Validation Results

Date: 2026-03-06
Owner: @cenkerenozbek

## Summary

Authentication-related fixes were applied in the React Native app to make the required scenarios testable:

- Sign-in now handles additional Clerk verification steps instead of failing with a generic fallback message.
- The app now exposes an explicit logout action.
- User-scoped client state now resets and reloads when the Clerk session changes.
- Wishlist bootstrap now waits for the Clerk token bridge, refreshes from backend on sign-in, and persists local cache per Clerk user instead of one global key.

Automated backend and mobile verification passed in this environment. Native runtime validation on Android Expo and iOS simulator could not be fully executed from this Codex session because Android tooling is not installed and CoreSimulator runtime access is unavailable here.

## Final Identity Spoofing Validation

The final security validation focused on proving that client-supplied identity
headers cannot override the authenticated backend user after the Clerk refactor.

- Requests with spoofed identity headers and no bearer token are rejected with `401`.
- Requests with a valid Clerk token plus spoofed identity headers are executed for
  the token subject only.
- Wishlist, notifications, and review-vote endpoints resolve identity through
  `@AuthenticatedUserId`, which is backed by the verified Clerk subject.
- The active Expo request layer no longer sends the legacy `X-User-ID` header.

## Fixes Applied

### Mobile auth flow fixes

- `mobile/src/screens/SignInScreen.tsx`
  - Added support for Clerk verification states beyond the happy path.
  - Added email code, phone code, MFA email code, MFA phone code, TOTP, and backup code handling.
  - Added resend and reset/start-over actions for interrupted sign-in attempts.

- `mobile/src/screens/ProductListScreen.tsx`
  - Added a logout button using Clerk `signOut()`.
  - Clears API cache and resets screen state on logout.

- `mobile/App.tsx`
  - Clears the in-memory API cache whenever auth becomes unavailable.
  - Holds the signed-in navigation tree until the Clerk token bridge is ready for API requests.

- `mobile/src/context/WishlistContext.tsx`
  - Reloads wishlist data when auth becomes signed-in.
  - Clears in-memory and persisted wishlist state on sign-out.
  - Stores local wishlist cache under `wishlist_products:<clerkUserId>` so local bootstrap aligns with the authenticated backend user.

- `mobile/src/context/NotificationContext.tsx`
  - Reloads notifications when auth becomes signed-in.
  - Clears notification state on sign-out.

- `mobile/src/services/api.ts`
  - Retries transient `getToken()` null/late resolution before sending authenticated requests.
  - Exposes `clearWishlistCache()` so user-specific wishlist fetches bypass stale in-memory GET cache.
  - Stops sending the legacy `X-User-ID` header; protected identity now rides only on the Clerk bearer token.

- `mobile/jest.setup.env.js`
  - Added a default Clerk mock so UI tests can render auth-aware screens without a real Clerk provider.

- `docs/authenticated-wishlist-sync.md`
  - Documents the authenticated wishlist data flow, cache invalidation rules, optimistic rollback behavior, and reviewer manual validation steps.

- `docs/authentication-flow.md`
  - Documents the server-side identity resolution path from Clerk bearer token to internal user mapping.
  - States explicitly that client-provided identity headers are ignored for protected endpoints.

## Executed Validation

### Mobile code-level verification

- `cd mobile && npx tsc --noEmit`
  - Result: PASS

- `cd mobile && npm test -- --runInBand`
  - Result: PASS
  - Test suites: 13 passed
  - Tests: 186 passed

- `cd mobile && npm test -- --runInBand src/services/__tests__/api.test.ts`
  - Result: PASS
  - Confirms Clerk session token attachment, transient token retry behavior, and missing-token behavior in API request construction.

### Backend token enforcement verification

- `cd backend && ./mvnw -Dtest=ClerkJwtVerifierTest,ClerkAuthenticationFilterTest,ClerkAuthenticationIntegrationTest test`
  - Result: PASS
  - Tests: 24 passed

- `cd backend && ./mvnw -Dtest=ClerkAuthenticationFilterTest,ClerkAuthenticationIntegrationTest,UserControllerIntegrationTest,UserMappingServiceTest test`
  - Result: PASS
  - Tests: 51 passed

Validated backend scenarios:

- Missing authentication token -> rejected with `401`
- Expired authentication token -> rejected with `401`
- Invalid/tampered authentication token -> rejected by verifier
- Valid Clerk token -> request proceeds
- Spoofed `X-User-ID`, `X-Authenticated-User-Id`, `X-Clerk-User-Id`, and `X-Forwarded-User` headers -> ignored in favor of the verified Clerk subject
- Spoofed identity headers without `Authorization` -> rejected with `401`

### Manual reviewer playbook

To manually confirm the same behavior against a local or deployed backend, obtain
a valid Clerk session token for a known account and run:

```bash
curl -i \
  -H "Authorization: Bearer <valid-clerk-token-for-real-user>" \
  -H "X-User-ID: spoofed-user" \
  -H "X-Authenticated-User-Id: spoofed-user" \
  -H "X-Clerk-User-Id: spoofed-user" \
  https://<backend-host>/api/v1/user/wishlist
```

Expected result: `200` for the real token owner only. The spoofed header values do
not change which wishlist is returned.

```bash
curl -i \
  -H "X-User-ID: spoofed-user" \
  https://<backend-host>/api/v1/user/wishlist
```

Expected result: `401 Unauthorized` because protected identity is not accepted from
client headers.

### iOS build verification

- `xcodebuild -scheme ProductReview -project ios/ProductReview.xcodeproj -destination 'generic/platform=iOS Simulator' build`
  - Result: PASS
  - Confirms the native iOS project compiles successfully for simulator targets.

## Runtime Validation Matrix

| Scenario | Android Expo Build | iOS Mobile Build | Backend | Notes |
|---|---|---|---|---|
| Login flow | BLOCKED in this environment | BLOCKED in this environment | PASS | Mobile code path fixed; runtime execution requires device/simulator access |
| Token issuance after login | BLOCKED in this environment | BLOCKED in this environment | PASS | Mobile request layer injects Clerk bearer tokens; backend accepts valid Clerk tokens |
| Token refresh / continued token availability | BLOCKED in this environment | BLOCKED in this environment | PASS | `getToken()` wiring present; native runtime revalidation still needs device/simulator |
| Logout | FIXED in app code, runtime not executed | FIXED in app code, runtime not executed | PASS | Logout button added; auth-scoped state now clears on sign-out |
| Session persistence across restart | BLOCKED in this environment | BLOCKED in this environment | PASS | Native Clerk token cache remains wired in `App.tsx` for Android/iOS |
| Reject missing token | N/A | N/A | PASS | Verified by backend auth filter/integration tests |
| Reject invalid/expired token | N/A | N/A | PASS | Verified by JWT verifier/filter tests |

## Environment Constraints

These prevented full Android/iOS runtime execution from this session:

- Android:
  - `adb` not installed
  - `emulator` not installed

- iOS:
  - `xcrun simctl` could not access a usable CoreSimulator runtime from this environment
  - Generic simulator build succeeds, but interactive simulator execution was not available

## Recommended Manual Follow-up

To close the remaining runtime acceptance criteria outside this Codex session:

1. Android Expo build
   - Run `cd mobile && npx expo start`
   - Launch Android emulator or Expo Go device
   - Validate: login, token issuance, persistence after restart, logout, post-logout 401 behavior

2. iOS mobile build
   - Run the Expo iOS app or native iOS target on a simulator/device
   - Validate the same five scenarios

3. For both platforms, use a protected backend endpoint such as:
   - `GET /api/v1/user/wishlist`
   - `GET /api/v1/user/notifications`

Expected outcomes:

- No token -> `401`
- Invalid/expired token -> `401`
- Valid Clerk token -> `200`
- Logout -> no further protected requests succeed until login is restored

---

## Cross-Client Wishlist Sync (Week 10 fix — 2026-03-06)

### Root Cause

Two separate bugs caused authenticated wishlist state to diverge across clients.

**Bug 1 — React effect ordering race (primary)**

`WishlistProvider` is a child of `AuthBootstrap`.  React fires `useEffect` hooks
bottom-up (child-first, then parent).  When the user signs in from the signed-out
state (`isSignedIn` transitions `false → true`):

1. `WishlistProvider.useEffect` fires first → calls `loadWishlist()` →
   `getWishlistApi()` → `buildApiHeaders()` → `authTokenProvider` is still `null`
   (set to `null` by `AuthBootstrap` during sign-out; not yet replaced because
   `AuthBootstrap.useEffect` hasn't run yet) → no `Authorization` header →
   backend returns 401 → error caught silently → wishlist stays empty.
2. `AuthBootstrap.useEffect` fires after → `setAuthTokenProvider(tokenProvider)`
   — too late for step 1.

This is why wishlist was empty after clearing browser site data (forces sign-out →
sign-in) or on the first sign-in in a fresh Expo iOS session.

The bug did not affect the initial app load path (user already signed in on cold
start) because in that path `isAuthBridgeReady` starts as `false`, keeping children
unmounted until `AuthBootstrap` completes token verification.

**Bug 2 — Stale in-memory API cache**

`requestWithRetry` caches GET responses in `memoryCache` for 60 seconds.  Each
client process has its own independent cache.  When client A toggles a wishlist item:

- `invalidateCache('/api/v1/user/wishlist')` clears **client A's** cache only.
- Client B's cache is unaffected.  Pull-to-refresh on client B calls
  `getWishlistProducts()` → cache hit → stale data returned.

### Fixes Applied

**`mobile/src/context/AuthTokenReadyContext.ts` (new) + `mobile/App.tsx` — break the race cleanly**

A `setIsAuthBridgeReady(false)` call inside the effect would NOT prevent the race:
child effects still fire before the parent effect, so the 401 would happen before
the state update was processed.  The correct fix is a positive readiness signal.

`AuthTokenReadyContext` is a new boolean context (default `false`) provided by
`AuthBootstrap`.  `AuthBootstrap.useEffect`:
- Sets `isTokenProviderReady=false` when Clerk is unloaded or user is signed out.
- On `isSignedIn=true`: calls `setAuthTokenProvider(tokenProvider)` **synchronously**
  (module-level variable assignment), then queues `setIsTokenProviderReady(true)`.

`setAuthTokenProvider` is synchronous, so by the time React processes the
`setIsTokenProviderReady(true)` state update and re-renders, `authTokenProvider`
already points at the new provider.

Sequence after the fix when user signs in:
1. `isSignedIn` goes false→true.  `isTokenProviderReady` is still `false`.
2. `WishlistProvider.useEffect([isAuthLoaded, isSignedIn, isTokenProviderReady])`
   fires: `isSignedIn=true, isTokenProviderReady=false` → guard returns early.
   No `loadWishlist()`, no 401.
3. `AuthBootstrap.useEffect` fires: `setAuthTokenProvider(tokenProvider)` (sync),
   `setIsTokenProviderReady(true)` (queued state update).
4. React re-renders: `isTokenProviderReady=true` propagates via context.
5. `WishlistProvider.useEffect` fires again (dep changed): `isSignedIn=true,
   isTokenProviderReady=true` → `clearWishlistCache()` → `loadWishlist()` →
   `buildApiHeaders()` → token available → 200 → wishlist synced. ✓

**`mobile/src/services/api.ts` — `clearWishlistCache()`**

Exported a new `clearWishlistCache()` helper that calls
`invalidateCache('/api/v1/user/wishlist')`, invalidating both the ID list and paged
products cache entries.

**`mobile/src/context/WishlistContext.tsx` — force-fresh on bootstrap**

`loadWishlist()` now calls `clearWishlistCache()` at the top, ensuring that every
sign-in (and any future explicit `refreshWishlist()` call) bypasses any residual
cached entry and hits the authenticated backend endpoint.

**`mobile/src/screens/WishlistScreen.tsx` — force-fresh on pull-to-refresh**

`handleRefresh` now calls `clearWishlistCache()` before `fetchWishlist()` so that
cross-client wishlist changes are visible immediately when the user pulls to refresh,
without waiting for the 60-second cache TTL to expire.

### Authenticated Wishlist Data Flow (post-fix)

```
Client (Expo iOS or web)
  │
  ├── Sign-in
  │     AuthBootstrap: setAuthTokenProvider(getToken)
  │     setIsAuthBridgeReady(false) → children unmount
  │     verifySessionToken() → setIsAuthBridgeReady(true) → children remount
  │     WishlistContext.loadWishlist():
  │       clearWishlistCache()
  │       GET /api/v1/user/wishlist  Bearer <clerk-token>
  │           ↓ ClerkAuthenticationFilter: verifies JWT, sets clerkUserId attr
  │           ↓ AuthenticatedUserIdArgumentResolver:
  │               UserMappingService.getOrCreateByClerkUserId(clerkUserId)
  │               → same internalUserId for the same Clerk account on all clients
  │           ↓ UserService.getWishlist(internalUserId) → product IDs
  │       Backend responds with wishlist IDs
  │     WishlistContext merges backend IDs with local cache
  │
  ├── Toggle wishlist item
  │     Optimistic local update + POST /api/v1/user/wishlist/{id}
  │     On success: invalidateCache('/api/v1/user/wishlist')
  │     On failure: optimistic update rolled back
  │
  └── Pull-to-refresh (WishlistScreen)
        clearWishlistCache()
        GET /api/v1/user/wishlist/products  Bearer <clerk-token>
        → authenticated backend always returns current state for this Clerk user
```

### Cache Invalidation Rules

| Event | Cache cleared |
|---|---|
| Sign-out | Entire `memoryCache` cleared via `clearApiCache()` |
| Sign-in | Wishlist cache cleared via `clearWishlistCache()` in `loadWishlist()` |
| Wishlist toggle (add or remove) | Wishlist cache cleared via `invalidateCache('/api/v1/user/wishlist')` |
| Pull-to-refresh on WishlistScreen | Wishlist cache cleared via `clearWishlistCache()` |

### Validation Matrix

| Scenario | Expected | Verified |
|---|---|---|
| Same Clerk account, two web browsers — favorite on A, refresh B | B sees item after refresh | Code path confirmed (cache cleared on refresh) |
| Same Clerk account, web and Expo iOS — favorite on web, restart iOS | iOS sees item after restart | Code path confirmed (fresh fetch on sign-in) |
| Clear browser site data, sign back in with same account | Wishlist restored from backend | Code path confirmed (race fixed; backend is source of truth) |
| Expo iOS app restart, same account | Wishlist restored from backend | Code path confirmed |
| Optimistic add fails | Local state rolled back | Existing behavior; unchanged |
| Sign-out | Wishlist cleared locally | Existing behavior; unchanged |

### Automated Test Results

```
cd mobile && npx tsc --noEmit
Result: PASS

cd mobile && npm test -- --runInBand
Result: PASS
Test suites: 13 passed
Tests: 186 passed
```

---

## Acceptance Criteria Status

- Android Expo build successfully completes login authentication flow: BLOCKED in this environment
- iOS build successfully completes login authentication flow: BLOCKED in this environment
- Authentication tokens are issued after successful login: CODE PATH READY, runtime device validation pending
- Session persists correctly across application restarts: CODE PATH READY, runtime device validation pending
- Logout invalidates session and prevents further authenticated requests: FIXED in app code, runtime device validation pending
- Backend endpoints reject requests without authentication tokens: PASS
- Backend endpoints reject requests with invalid or expired tokens: PASS
- Validation results are documented and shared: PASS
- Pull request with any required fixes is merged into the main branch if changes were necessary: NOT DONE HERE
