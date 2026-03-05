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

```
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
| `CLERK_PUBLISHABLE_KEY` | Clerk publishable key (optional, for reference) |

### Development (local)

For local development you can provide fallback values in `application.properties` or set the environment variables in your shell:

```bash
export CLERK_SECRET_KEY=sk_test_your-secret-key-here
```

### Production (Heroku)

```bash
heroku config:set CLERK_SECRET_KEY=sk_live_your-secret-key
heroku config:set CLERK_PUBLISHABLE_KEY=pk_live_your-publishable-key
```

The `application-prod.properties` file reads these values via `${CLERK_SECRET_KEY}` and `${CLERK_PUBLISHABLE_KEY}`.

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
| `CLERK_PUBLISHABLE_KEY` | Backend env / Heroku config | Optional |

## Notes

- Authentication UI flows (sign-in, sign-up screens) are not included in this setup phase.
- Backend JWT verification middleware is not implemented at this stage.
- Use development keys (`pk_test_` / `sk_test_`) for all non-production environments.
