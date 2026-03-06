# Authenticated Wishlist Sync

Date: 2026-03-06
Owner: @cenkerenozbek

## Scope

This document describes how wishlist state is resolved for the same Clerk-authenticated user across Expo iOS and web after the authentication hardening changes on `feat/clerk-auth-hardening`.

## Root Cause

Cross-client wishlist drift came from three client-side behaviors that were no longer valid after backend identity moved to validated Clerk tokens:

1. Signed-in screens could mount before the API token bridge was ready, so the first wishlist fetch after sign-in could be sent without `Authorization`.
2. Wishlist GET responses were cached in-memory for up to 60 seconds, so a refresh inside the same client process could reuse stale wishlist reads.
3. Local wishlist persistence used one global AsyncStorage key (`wishlist_products`) instead of a Clerk-user-scoped key, so device-local cache was not aligned with the authenticated backend user.

The backend identity mapping itself is now stable:

- `ClerkAuthenticationFilter` validates the bearer token and exposes the Clerk subject on the request.
- `AuthenticatedUserIdArgumentResolver` resolves that Clerk subject to a stable internal user ID via `UserMappingService`.
- Wishlist reads and writes in `UserController` use `@AuthenticatedUserId`, not the caller-supplied `X-User-ID` header.

## Current Data Flow

1. User signs in with Clerk.
2. `AuthBootstrap` installs `setAuthTokenProvider(() => getToken())`.
3. The signed-in navigation tree stays blocked until the token bridge is ready.
4. `WishlistProvider` clears wishlist API cache and optionally hydrates only the current Clerk user's local wishlist cache.
5. `WishlistProvider` fetches `/api/v1/user/wishlist` with the Clerk bearer token.
6. Backend resolves the Clerk subject to the same internal user ID on every client.
7. Returned wishlist IDs replace stale local state, while in-flight optimistic mutations are preserved until they settle.

## Cache And Persistence Rules

- Backend is the source of truth for wishlist membership.
- Local persistence is only a bootstrap optimization and is keyed as `wishlist_products:<clerkUserId>`.
- Signing out clears the last signed-in user's persisted wishlist cache and removes the legacy unscoped `wishlist_products` entry.
- `clearWishlistCache()` invalidates both `/api/v1/user/wishlist` and `/api/v1/user/wishlist/products` in-memory cache entries.
- Wishlist refresh paths that must bypass cache:
  - initial signed-in bootstrap
  - app foreground on mobile
  - browser focus / tab visibility regain on web
  - periodic sync loop in `WishlistProvider`
  - pull-to-refresh and focus refresh in `WishlistScreen`
  - any successful wishlist toggle

## Optimistic Update Rules

- Add/remove operations update local UI immediately.
- If backend persistence fails, the optimistic mutation is rolled back:
  - failed add => product is removed from local wishlist
  - failed remove => removed product is restored locally
  - batched add/remove failures are rolled back per item
- `clearWishlist()` issues compensating removes for items whose add request was still in flight.

## Validation Notes

Automated validation completed in this environment:

- `mobile/src/services/__tests__/api.test.ts` verifies bearer-token attachment and retry behavior for transient token availability.
- Backend auth integration tests verify that wishlist reads and writes resolve from the validated Clerk token and ignore spoofed `X-User-ID` headers.

Manual runtime validation still needs to be executed by the reviewer on a real web + Expo iOS environment:

1. Sign in to the same Clerk account in browser A and browser B.
2. Favorite a product in browser A.
3. Refresh browser B and confirm the same product appears.
4. Restart Expo iOS, refresh the product list, and confirm the same product appears.
5. Clear browser site data, sign back into the same Clerk account, and confirm the wishlist repopulates from backend data.
6. Repeat add/remove while forcing one backend failure and confirm the optimistic UI rolls back.
