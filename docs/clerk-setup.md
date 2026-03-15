# Clerk Authentication Setup

This document describes how to configure Clerk authentication for the Product Review application.

## 1. Create a Clerk Project

1. Go to [clerk.com](https://clerk.com) and sign in or create an account.
2. Click **Create application** in the Clerk dashboard.
3. Name your application (e.g., `product-review`), select your desired sign-in methods, and click **Create application**.
4. The free plan is sufficient for development and testing.

## 2. Obtain API Keys

In the Clerk dashboard, navigate to **API Keys** under your application settings.

- **Publishable key** — starts with `pk_test_` (development) or `pk_live_` (production)
- **Secret key** — starts with `sk_test_` (development) or `sk_live_` (production)

> Never commit secret keys to version control.

## 3. Configure the React Native (Expo) App

### Environment file

Copy `.env.example` to `.env.local` in the `mobile/` directory and set your publishable key:

```dotenv
EXPO_PUBLIC_API_URL=https://your-backend-url
EXPO_PUBLIC_CLERK_PUBLISHABLE_KEY=pk_test_your-publishable-key-here
```

The `EXPO_PUBLIC_` prefix is required for Expo to expose the variable to the JavaScript bundle on all platforms (Android, iOS, web).

### How it works

`App.tsx` initialises `ClerkProvider` at the root of the component tree using the publishable key. A `tokenCache` backed by `expo-secure-store` persists session tokens securely in the device keychain on iOS and the Android Keystore.

## 4. Configure the Backend

Set the following environment variables on your backend server (Heroku config vars, `.env`, etc.):

| Variable | Description |
|---|---|
| `CLERK_SECRET_KEY` | Clerk secret key (required) |
| `CLERK_PUBLISHABLE_KEY` | Clerk publishable key (required) |
| `CLERK_JWT_VERIFICATION_KEY` | Clerk JWT public key used by the backend auth filter to verify session tokens |

The backend auth middleware also accepts `CLERK_JWT_KEY` or `CLERK_PEM_PUBLIC_KEY` as fallbacks if your environment already uses one of those names.

### Development (local)

Local development now defaults to `clerk.auth.enabled=false` in `backend/src/main/resources/application.properties`, so the backend can boot without Clerk credentials while you work on non-auth flows.

If you want local backend requests to enforce Clerk authentication, enable it explicitly and provide the verification key:

```bash
export CLERK_AUTH_ENABLED=true
export CLERK_SECRET_KEY=sk_test_your-secret-key-here
export CLERK_PUBLISHABLE_KEY=pk_test_your-publishable-key-here
export CLERK_JWT_VERIFICATION_KEY="-----BEGIN PUBLIC KEY-----..."
```

If `CLERK_AUTH_ENABLED=true` and `CLERK_JWT_VERIFICATION_KEY` is missing, the backend is expected to fail fast during startup.

### Production (Render)

Set these environment variables on the Render web service before the first production deploy:

```bash
CLERK_AUTH_ENABLED=true
CLERK_JWT_VERIFICATION_KEY='-----BEGIN PUBLIC KEY-----...'
CLERK_AUTHORIZED_PARTIES='https://your-frontend-origin.example'
CLERK_SECRET_KEY=sk_live_your-secret-key
CLERK_PUBLISHABLE_KEY=pk_live_your-publishable-key
```

`CLERK_JWT_VERIFICATION_KEY` is the one required for backend startup. The verifier also accepts the same value via `CLERK_JWT_KEY` or `CLERK_PEM_PUBLIC_KEY` if your environment already uses one of those names.

If you are intentionally deploying without Clerk protection for a temporary environment, set `CLERK_AUTH_ENABLED=false`. Do not use that setting for a public production deployment.

## 5. Verify the Connection

1. Start the app with `npx expo start` and open it on an Android emulator, iOS simulator, or physical device.
2. Confirm the app starts without `ClerkProvider` initialization errors in Metro logs on both Android and iOS.
3. In the Clerk dashboard, open **Users** and verify your previously created test account is present.
4. Open the user detail and verify at least one active/recent session exists.

## Environment Variable Summary

| Variable | Location | Required |
|---|---|---|
| `EXPO_PUBLIC_CLERK_PUBLISHABLE_KEY` | `mobile/.env.local` | Yes |
| `CLERK_SECRET_KEY` | Backend env / Heroku config | Yes (prod) |
| `CLERK_PUBLISHABLE_KEY` | Backend env / Heroku config | Yes (prod) |
| `CLERK_JWT_VERIFICATION_KEY` | Backend env / Heroku config | Yes (prod) |

## Notes

- Authentication UI flows (sign-in, sign-up screens) are not included in this setup phase.
- Backend JWT verification is enforced by the Java auth filter when `clerk.auth.enabled=true` and a Clerk public verification key is configured.
- Protected backend identity is derived only from the validated Clerk bearer token; client-provided headers such as `X-User-ID` are ignored for identity resolution.
- In the default local profile, Clerk auth is opt-in; in production it remains enabled by default.
- Use development keys (`pk_test_` / `sk_test_`) for all non-production environments.
- See `docs/authentication-flow.md` for the end-to-end identity resolution flow and spoofing validation notes.
