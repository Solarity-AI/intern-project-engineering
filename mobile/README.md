# Mobile (Expo React Native) – Frontend Architecture & Engineering Guide (Intern Onboarding)

> Scope: `mobile/`  
> Audience: New interns / junior engineers who will maintain or extend the mobile client.  
> Goal: Explain **why each file exists**, **what it does**, and **how the pieces connect**—with a production mindset.

---

## 0) High-level Intent

This frontend is a **feature-rich client** (Product List, Product Details, Reviews, Wishlist, Notifications, AI Summary) that aims to behave like a **production app**:

- **Server state** is treated as a first-class system (pagination, caching, retries, deduping, invalidation).
- **UI state** is centralized and predictable (theme, toasts, selection, notifications UI).
- **UX is resilient** under slow networks and errors (consistent loading / empty / error patterns).
- **Release track exists** (dev / preview / prod environments, stable base URL, web builds).

---

## 1) Architecture Overview

### 1.1 Separation of responsibilities

**Rule of thumb**:
- **Server state (remote data)** → TanStack Query (React Query) style caching + invalidation
- **UI/App state (local)** → Context API (Theme, Toast, Wishlist selection, Notifications UI state)
- **Pure UI** → components (render-only, no data fetching)
- **Navigation** → screens (composition layer; coordinates hooks/context/components)

### 1.2 End-to-end flow (typical request)

Example: user opens Product Details → fetch details + reviews + AI summary

```
ProductDetailsScreen
  ├─ uses api client (services/api.ts) to call backend
  ├─ renders components (RatingBreakdown, ReviewCard, AISummaryCard, AddReviewModal)
  ├─ reads global UI state (ThemeContext, ToastContext, WishlistContext)
  └─ triggers refresh/invalidation after "Add Review"
```

---

## 2) Folder & File Map (What each part is for)

> Note: filenames reflect the uploaded project snapshot. If you add new files (e.g., query hooks), keep the same separation rules.

### 2.1 `screens/` – Navigation-level containers

Screens are **composition roots**: they orchestrate UI + state + navigation.

- `ProductListScreen.tsx`
  - Browse products
  - Search + filter + sort + pagination
  - Multi-select entry (long-press) and bulk actions
- `ProductDetailsScreen.tsx`
  - Single product view
  - Reviews + rating breakdown + review creation
  - AI summary surface (calls backend summary endpoint)
- `WishlistScreen.tsx`
  - Favorite products list
  - Reuses selection behavior (SelectableWishlistCard)
- `NotificationsScreen.tsx` / `NotificationDetailScreen.tsx`
  - Notification feed and detail
- `AIAssistantScreen.tsx`
  - Dedicated AI page (UX shell) that should consume **backend-provided** summaries/results

### 2.2 `components/` – Pure UI and reusable widgets

Components should be **data-agnostic**. If a component needs data, the screen passes it as props.

- `ProductCard.tsx`
  - Product tile/card presentation for list/grid
- `SelectableProductCard.tsx` / `SelectableWishlistCard.tsx`
  - Wrap a card with selection UI, selection toggles
- `ReviewCard.tsx`
  - Review row/card for reviews list
- `RatingBreakdown.tsx`
  - Rating histogram; click-to-filter behavior (e.g., tap 5★ to filter)
- `AddReviewModal.tsx`
  - Review creation UI; should call a mutation and notify success/failure via ToastContext
- `AISummaryCard.tsx`
  - Renders AI summary state (loading/error/success) with safe fallbacks
- `AIChatModal.tsx`
  - Optional conversational UI; should not embed AI logic—only display results
- `SearchBar.tsx`
  - Search input UI; should be paired with debouncing at screen/hook level
- `CategoryFilter.tsx`
  - Quick filter chips
- `SortFilter.tsx`
  - Sort/filter controls (value selection UI)
- `StarRating.tsx`
  - Display and/or capture star rating
- `ScreenWrapper.tsx`
  - Consistent layout, padding, background; centralizes cross-platform spacing
- `Button.tsx`
  - Shared button primitive, theme-aware

### 2.3 `context/` – App-wide UI state

- `ThemeContext.tsx`
  - Owns theme mode (dark/light)
  - Provides tokens or theme object usage
- `ToastContext.tsx`
  - Global feedback messages (success/error/info)
  - Used after mutations (add review, add to wishlist, etc.)
- `WishlistContext.tsx`
  - Wishlist storage + selection mode state
  - Multi-select and bulk operations should live here or in a small selection reducer
- `NotificationContext.tsx`
  - Notification list state & read/unread logic (UI-level)

### 2.4 `constants/` – Tokens and seed data

- `theme.ts`
  - Design tokens: spacing, colors, typography
  - Single source of truth for layout stability across screens
- `data.ts`
  - Sample/static data (for dev or fallback) or shared constants

### 2.5 `hooks/` – Cross-cutting utilities

- `useColorScheme.ts`
  - Wraps RN/Expo color scheme detection or custom logic
  - Usually combined with ThemeContext to persist overrides

### 2.6 `services/` – API client boundary

- `api.ts`
  - The API boundary. Centralizes:
    - base URL
    - request/response parsing
    - consistent error mapping
    - timeouts
  - Screens/hooks call **only** through this layer.

---

## 3) The “High Impact” Improvements absorbed into the architecture

### 3.1 API contract + caching + pagination (stabilize server state)

This app is feature-rich; the biggest risk is **data correctness** and **consistency** under real usage:
- pagination + filtering + sorting
- AI summary fetch
- review counts updating
- request duplication and accidental re-fetch storms

**Recommended policy (architecture-level):**
- Treat Products, Reviews, and AI Summary as **server state**
- Use a single caching policy for all screens

#### Pagination contract (shared language with backend)
List endpoints should follow:

`GET /products?page=1&pageSize=20&sort=rating_desc&category=...&q=...`

Response example:
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

#### AI Summary as a first-class query
- Endpoint: `GET /products/{id}/ai-summary`
- Frontend caching policy:
  - `staleTime`: 10–30 minutes (summaries do not need constant recomputation)
  - `retry`: 1
  - strong fallback UI (empty/error)

#### Cache invalidation rules (critical correctness)
After `AddReviewModal` succeeds:
- invalidate ProductDetails query for that product
- invalidate Reviews query for that product
- invalidate ProductsList query for current filters
- invalidate AI summary query for that product (summary changed)

This avoids stale UI without manual refresh logic scattered across screens.

### 3.2 UX Reliability: Unified Loading / Empty / Error

This is a senior-level polish layer. The app should not feel “random” across screens.

**Recommended pattern:**
- Loading → skeletons (not spinners everywhere)
- Empty → actionable message (e.g., clear filters, add first review)
- Error → retry + error message mapping

Where to apply:
- `ProductListScreen`, `WishlistScreen`, `ProductDetailsScreen` (reviews + summary), `NotificationsScreen`

### 3.3 Release Track: env config + observability + web deploy readiness

#### Environments
- dev / preview / prod should have different API base URLs
- use `EXPO_PUBLIC_API_BASE_URL` per environment

#### Observability (recommended)
- Add Sentry (or similar) to capture:
  - unhandled runtime errors
  - network failures
  - navigation breadcrumbs

#### Web deployment readiness
- Expo Web build should be documented and reproducible
- include a "Release checklist" in root README

---

## 4) Multi-select behavior (Product List + Wishlist)

Selection must be predictable:

- Enter selection mode: **long-press**
- In selection mode: tap toggles selection
- Back button should **exit selection mode first**
- Provide a sticky action bar with bulk actions (add/remove/clear)

In code terms:
- `WishlistContext` (and/or a small local reducer) should own:
  - `isSelectionMode`
  - `selectedIds` (prefer `Set<string>`)
- `SelectableProductCard` and `SelectableWishlistCard` render selection UI

---

## 5) Developer Workflow

### Run
```bash
cd mobile
npm install
npx expo start
```

### Recommended scripts & hygiene
- Keep theme tokens in `constants/theme.ts`
- Keep API calls centralized in `services/api.ts`
- Avoid “fetch in components”—screens/hooks only

---

## 6) Intern “How to learn this codebase” path

1. Read `constants/theme.ts` and `ScreenWrapper.tsx` to understand layout system
2. Read contexts (Theme/Toast/Wishlist/Notifications) to understand global UI state
3. Read `services/api.ts` to understand backend integration boundaries
4. Read `ProductListScreen.tsx` (search/filter/sort/pagination)
5. Read `ProductDetailsScreen.tsx` (reviews + AI summary + add review invalidation)
6. Read `WishlistScreen.tsx` and multi-select behavior
7. Study “unified states” pattern (loading/empty/error)

---

## 7) Related docs
- Backend API + caching policies → `README_BACKEND.md`
- System overview + release checklist → root `README.md`
