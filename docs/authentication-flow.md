# Authentication Flow

Date: 2026-03-06
Owner: @cenkerenozbek

## Purpose

Protected backend endpoints derive user identity only from validated Clerk bearer
tokens. Client-provided identity headers such as `X-User-ID`,
`X-Authenticated-User-Id`, `X-Clerk-User-Id`, or `X-Forwarded-User` are not used
for identity resolution.

## Request Contract

- Public endpoints can be called without authentication.
- Protected `/api/v1/**` endpoints require `Authorization: Bearer <Clerk JWT>`.
- Clients must not send user identity as a header. If they do, the backend ignores
  it for identity resolution.

## Backend Resolution Path

1. The client obtains a Clerk session token from Clerk SDK state.
2. The client sends that token in the `Authorization` header.
3. `ClerkAuthenticationFilter` verifies the JWT signature and claims.
4. When verification succeeds, the filter stores the Clerk subject on the request
   as `authenticatedClerkUserId`.
5. `AuthenticatedUserIdArgumentResolver` reads that server-side request attribute.
6. `UserMappingService` resolves the Clerk subject to a stable internal user id.
7. Controllers receive the resolved internal id through `@AuthenticatedUserId`.
8. Services and repositories operate only on the resolved internal id.

## Why Header Spoofing Fails

- No `Authorization` header: request is rejected with `401`.
- Expired or tampered token: request is rejected with `401`.
- Valid token plus spoofed identity headers: request succeeds only for the user in
  the validated token, not the spoofed header value.
- Controllers do not read `X-User-ID` or similar headers for identity.

## Validated Components

- `backend/src/main/java/com/example/productreview/config/ClerkAuthenticationFilter.java`
- `backend/src/main/java/com/example/productreview/config/AuthenticatedUserIdArgumentResolver.java`
- `backend/src/main/java/com/example/productreview/controller/UserController.java`
- `backend/src/main/java/com/example/productreview/controller/ProductController.java`

## Validation Evidence

- `backend/src/test/java/com/example/productreview/config/ClerkAuthenticationFilterTest.java`
  verifies header-only spoofing attempts still return `401`.
- `backend/src/test/java/com/example/productreview/config/ClerkAuthenticationIntegrationTest.java`
  verifies spoofed identity headers are ignored for wishlist, notifications, and
  review-vote flows.
- `docs/auth-validation-results.md` records the executed test commands and manual
  reviewer playbook.
