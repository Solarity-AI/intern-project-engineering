# 🤖 Android Frontend (Kotlin) – Implementation Guide

> This document describes the **Android (Kotlin) frontend implementation** of the Product Review App.  
> It is written **from a native Android perspective** and should be treated as the **source of truth** for Android expectations, architecture, and feature scope.

---

## 🎯 Objective

Build a **native, scalable, and maintainable Android application** that:

- Implements all **Products**, **Reviews**, **Wishlist**, **Notifications**, and **AI** features
- Communicates reliably with the backend API
- Follows Android best practices (architecture, lifecycle, state)
- Provides smooth UX across configuration changes and varying screen sizes
- Is future-proof for new feature additions

> ✅ This guide defines **what must exist on Android**, independent of how other platforms implement it.

---

## 🛠️ Tech Stack & Requirements

### Core Android Stack
- `Kotlin`
- `Android SDK`
- `Jetpack Compose`
- `Material 3`
- `AndroidX` (Lifecycle, Navigation, etc.)

### Architecture & State
- `MVVM`
- `ViewModel`
- `StateFlow`
- `Unidirectional Data Flow (UDF)`

### Networking & Data
- `Retrofit`
- `OkHttp`
- `Gson` or `Kotlinx Serialization`

### Async & Lifecycle
- `Coroutines`
- `Flow`
- Lifecycle-aware components

### Tooling & Testing
- `Gradle`
- `JUnit`
- `MockK`
- (Optional) Compose UI tests (`composeTestRule`)

---

## 📱 Features to Implement

### 🛍️ Product List
- Paginated product listing
- Search functionality
- Category-based filtering
- Sorting options
- Adaptive grid layout (dynamic columns)
- **Long-press multi-select** (Android standard)
- Bulk add-to-wishlist action
- Global product statistics header
- Offline state indication

### ⭐ Reviews System (Must-Have)
- Review list per product
- Add review flow (rating + text)
- Client-side validation (required fields, min/max constraints)
- “Helpful” vote interaction per review
- “Voted” state hydration (avoid double-voting; reflect voted items on UI)
- Loading / empty / error states for reviews
- Optimistic UI where safe (e.g., helpful count)

### 📦 Product Details
- Product metadata display
- **AI-generated summary card**
- Rating breakdown visualization (distribution)
- Reviews section entry + inline list
- AI chat entry point
- Wishlist toggle

### ❤️ Wishlist
- Wishlist product listing (backend-driven)
- Grid layout support
- Multi-select mode
- Bulk delete action
- Optimistic UI updates

### 🔔 Notifications
- Notification list screen
- Notification detail screen
- Unread count badge
- Mark as read / mark all as read
- Delete single or all notifications

### 🤖 AI Features
- AI Summary Card
- AI Chat interface
  - Single active request
  - Clear loading & error states

---

## 🏗️ Recommended Architecture

Use a clean, layered structure that prevents UI from owning business logic:

```
app/
 ├─ di/                 # Dependency injection modules (e.g., Hilt modules)
 ├─ navigation/         # Nav graph, routes, destinations
 └─ core/               # Shared utilities (result wrappers, error mapping, etc.)

data/
 ├─ remote/             # Retrofit APIs, DTOs, interceptors
 ├─ local/              # (Optional) Room/DataStore caching
 ├─ mapper/             # DTO <-> Domain mappers
 └─ repository/         # Repo implementations

domain/
 ├─ model/              # Domain models
 ├─ usecase/            # Business logic (one responsibility each)
 └─ repository/         # Repo interfaces

ui/
 ├─ theme/              # Material 3 theme, typography, colors
 ├─ components/         # Reusable composables (cards, banners, etc.)
 ├─ screens/            # Feature screens (List, Details, Wishlist, Notifications)
 ├─ state/              # UiState + UiEvent + UiEffect definitions
 └─ viewmodel/          # ViewModels per feature
```

### Key Guidelines
- UI is **declarative** and observes state only.
- No business logic inside Composables.
- ViewModels expose `StateFlow<UiState>` and accept explicit user intents/events.
- Repositories are the **single source of truth** for network/cache decisions.
- Standardize network results (e.g., `Result<T>` / `ApiResult<T>`) and error mapping.
- Keep state immutable and predictable (`copy()` patterns for UiState).

---

## 🚀 Step-by-Step Implementation Plan

### Phase 1 – Project Foundation 🧱
1. Initialize project with:
   - `Jetpack Compose` + `Material 3`
   - Base `Navigation` setup
2. Establish architecture scaffolding:
   - `data/`, `domain/`, `ui/` modules/packages
   - Result/error handling (`ApiResult`, error mapper)
3. Add dependencies:
   - `Retrofit`, `OkHttp`, JSON serializer
   - `Coroutines`, `Flow`
   - (Optional) `Hilt` for DI

---

### Phase 2 – Networking & API Layer 🌐
1. Create Retrofit API interfaces for:
   - Products, Reviews, Wishlist, Notifications, AI
2. Add `OkHttp` interceptors:
   - Logging (debug)
   - Common headers (if required by backend)
3. Define DTOs + mappers:
   - `ProductDto -> Product`
   - `ReviewDto -> Review`
   - etc.

---

### Phase 3 – Product List (Pagination + Filters) 🛍️
1. Build UI:
   - `ProductCard`
   - List/Grid layout with adaptive columns
2. Implement pagination:
   - Backend-driven page parameters
   - Loading, empty, error states
3. Add controls:
   - Search input
   - Category filter
   - Sort selector
4. Add Android multi-select:
   - Enter selection mode with `long-press`
   - Selected-state UI
   - Bulk action bar (e.g., add to wishlist)

---

### Phase 4 – Product Details + Reviews ⭐
1. Product detail screen:
   - Product header + metadata
   - Rating breakdown UI
2. Reviews system:
   - Fetch: `GET /api/products/{id}/reviews`
   - Add: `POST /api/products/{id}/reviews`
   - Helpful: `PUT /api/products/reviews/{id}/helpful`
   - Voted hydration: `GET /api/products/reviews/voted`
3. Review UI states:
   - Loading skeleton/progress
   - Empty state (“No reviews yet”)
   - Error + retry
4. Add review flow:
   - Modal/dialog or separate screen
   - Validation + submit
   - Refresh list after success

---

### Phase 5 – Wishlist ❤️
1. Implement wishlist state:
   - Fetch wishlist IDs and/or paged wishlist products
2. UI:
   - Grid support
   - Multi-select + bulk remove
3. Sync strategy:
   - Optimistic update for toggles
   - Background refresh to confirm backend state

---

### Phase 6 – Notifications 🔔
1. List + detail screens
2. Unread count badge:
   - `GET /api/user/notifications/unread-count`
3. Actions:
   - Mark read: `PUT /api/user/notifications/{id}/read`
   - Read all: `PUT /api/user/notifications/read-all`
   - Delete one/all

---

### Phase 7 – AI Features 🤖
1. AI Summary Card:
   - Render backend-provided summary content in details screen
2. AI Chat:
   - `POST /api/products/{id}/chat`
   - Enforce single active request
   - Clear loading/error states

---

### Phase 8 – Offline, Errors & Quality ✅
1. Offline UX:
   - Network awareness
   - Offline banner + graceful fallbacks
2. Testing:
   - Unit tests for ViewModels/UseCases
   - Basic Compose UI tests for critical flows

---

## 🔗 API Reference

### Base URL
```
https://product-review-app-solarityai-a391ad53d79a.herokuapp.com
```

### Products
- `GET /api/products`
- `GET /api/products/stats`
- `GET /api/products/{id}`

### Reviews
- `GET /api/products/{id}/reviews`
- `POST /api/products/{id}/reviews`
- `PUT /api/products/reviews/{id}/helpful`
- `GET /api/products/reviews/voted`

### AI
- `POST /api/products/{id}/chat`

### Wishlist
- `GET /api/user/wishlist`
- `POST /api/user/wishlist/{productId}`
- `GET /api/user/wishlist/products`

### Notifications
- `GET /api/user/notifications`
- `GET /api/user/notifications/unread-count`
- `PUT /api/user/notifications/{id}/read`
- `PUT /api/user/notifications/read-all`
- `POST /api/user/notifications`
- `DELETE /api/user/notifications/{id}`
- `DELETE /api/user/notifications`

---

## ✅ Final Notes

- This document represents the **Android-native contract**
- Other platforms must align with the feature scope defined here
- UI visuals may vary, **behavior must not**
