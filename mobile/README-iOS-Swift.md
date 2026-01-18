# 🍎 iOS Frontend (Swift) – Implementation Guide

> This document describes the **iOS (Swift) frontend implementation** of the Product Review App.  
> It is written **from a native iOS perspective** and should be treated as the **source of truth** for iOS expectations, architecture, and feature scope.

---

## 🎯 Objective

Build a **native, scalable, and maintainable iOS application** that:

- Implements all **Products**, **Reviews**, **Wishlist**, **Notifications**, and **AI** features
- Communicates reliably with the backend API
- Follows **Apple Human Interface Guidelines (HIG)**
- Handles lifecycle, state, and scene changes correctly
- Is future-proof for new feature additions

> ✅ This guide defines **what must exist on iOS**, independent of other platforms.

---

## 🛠️ Tech Stack & Requirements

### Core iOS Stack
- `Swift`
- `iOS SDK`
- `SwiftUI`
- `Xcode`

### Architecture & State
- `MVVM`
- `ObservableObject`
- `@Published`
- `Unidirectional Data Flow (UDF)`

### Networking & Data
- `URLSession`
- `Codable`
- Repository abstraction layer

### Concurrency & Lifecycle
- `async / await`
- `Task`
- `MainActor`
- Scene-aware state handling

### Tooling & Testing
- `Xcode`
- `XCTest`
- `XCUITest`

---

## 📱 Features to Implement

### 🛍️ Product List
- Paginated product listing
- Search functionality
- Category-based filtering
- Sorting options
- Adaptive grid layout
- Pull-to-refresh
- Global product statistics header
- Offline state indication

### ⭐ Reviews System (Must-Have)
- Review list per product
- Add review flow (rating + text)
- Client-side validation
- “Helpful” vote interaction
- “Voted” state hydration
- Loading / empty / error states
- Optimistic UI where safe

### 📦 Product Details
- Product metadata display
- **AI-generated summary card**
- Rating breakdown visualization
- Reviews section entry + inline list
- AI chat entry point
- Wishlist toggle

### ❤️ Wishlist
- Wishlist product listing
- Grid layout support
- Multi-select actions (iOS-friendly)
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

Use a clean, layered architecture aligned with SwiftUI best practices:

```
App/
 ├─ AppEntry/            # App & Scene setup
 ├─ Navigation/          # NavigationStack & routes
 └─ Core/                # Shared utilities, error mapping

Data/
 ├─ Network/             # API clients, DTOs
 ├─ Mapper/              # DTO <-> Domain mapping
 └─ Repository/          # Repository implementations

Domain/
 ├─ Model/               # Domain models
 ├─ UseCase/             # Business logic units
 └─ Repository/          # Repository protocols

Presentation/
 ├─ Views/               # SwiftUI views
 ├─ ViewModels/          # ObservableObjects
 ├─ State/               # ViewState / ViewAction definitions
 └─ Components/          # Reusable UI components
```

### Key Guidelines
- Views are **declarative and lightweight**
- No business logic inside Views
- ViewModels expose observable state only
- UseCases encapsulate business rules
- Repository is the single source of truth
- Side effects are explicit and isolated
- State updates happen on the `MainActor`

---

## 🚀 Step-by-Step Implementation Plan

### Phase 1 – Project Foundation 🧱
1. Initialize SwiftUI app entry
2. Configure global appearance (Light/Dark, Dynamic Type)
3. Setup base navigation using `NavigationStack`
4. Define core folders (`Data`, `Domain`, `Presentation`)

---

### Phase 2 – Networking & Data Layer 🌐
1. Implement API client with `URLSession`
2. Define DTOs and `Codable` models
3. Create Repository protocols and implementations
4. Centralize error mapping and result handling

---

### Phase 3 – Product List 🛍️
1. Build `ProductCardView` and list/grid layout
2. Implement pagination & pull-to-refresh
3. Add search, filter, and sort controls
4. Handle loading, empty, and error states

---

### Phase 4 – Product Details & Reviews ⭐
1. Build product detail view
2. Implement Reviews system:
   - Fetch reviews
   - Add review flow
   - Helpful voting
   - Voted-state hydration
3. Display rating breakdown
4. Ensure smooth state updates on actions

---

### Phase 5 – Wishlist ❤️
1. Implement wishlist repository
2. Grid-based wishlist UI
3. Multi-select & bulk delete
4. Optimistic updates + background sync

---

### Phase 6 – Notifications 🔔
1. List & detail views
2. Unread count badge
3. Mark read / read all
4. Delete single or all notifications

---

### Phase 7 – AI Features 🤖
1. Render AI Summary Card
2. Integrate AI Chat flow
3. Enforce single active request
4. Handle loading, error, and retry states

---

### Phase 8 – Offline, Errors & Quality ✅
1. Offline-aware UI
2. Graceful error handling (`Alert`, inline states)
3. Unit tests for ViewModels & UseCases
4. UI tests for critical navigation flows

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

- This document represents the **iOS-native contract**
- Other platforms must align with the feature scope defined here
- UI visuals may vary, **behavior must not**
