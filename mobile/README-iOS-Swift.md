# 🍎 iOS Frontend (Swift) - Implementation Guide

This document serves as the comprehensive guide for interns choosing the **iOS (Swift)** stack. Your goal is to build a native iOS application that consumes the Spring Boot backend API, matching the features of the reference React Native implementation.

---

## 🎯 Objective
Develop a production-ready iOS application using **Swift** and **SwiftUI** (preferred) or UIKit. The app must demonstrate clean architecture, modern concurrency, and responsive UI.

---

## 🛠️ Tech Stack & Requirements

| Category | Requirement | Recommended Libraries |
|----------|-------------|----------------------|
| **Language** | Swift 5.9+ | - |
| **UI Framework** | SwiftUI (Preferred) | - |
| **Architecture** | MVVM (Model-View-ViewModel) | - |
| **Networking** | Async/Await (Concurrency) | Alamofire (Optional) or URLSession |
| **Image Loading** | AsyncImage | Kingfisher or SDWebImage |
| **Dependency Manager** | Swift Package Manager (SPM) | - |

---

## 📱 Features to Implement

### 1. Product List (Home)
- **UI:** Grid or List view of products with images, names, prices, and ratings.
- **Features:**
  - **Pagination:** Implement "Infinite Scroll" to load more products as the user scrolls.
  - **Filtering:** Filter by category (e.g., Electronics, Fashion).
  - **Search:** Search bar to query products by name.

### 2. Product Details
- **UI:** Hero image, detailed description, price, and "Add to Cart/Wishlist" buttons.
- **Features:**
  - **AI Summary:** Display the `aiSummary` field from the API in a dedicated card.
  - **Rating Breakdown:** Visualize star ratings (5★ to 1★) using a bar chart.

### 3. Reviews System
- **UI:** List of user reviews with timestamps and helpful counts.
- **Features:**
  - **Add Review:** A modal/sheet to submit a rating (1-5) and comment.
  - **Helpful Vote:** Ability to mark a review as helpful.

---

## 🏗️ Recommended Architecture (MVVM)

The project should follow a strict **MVVM** pattern to separate logic from UI.

```
ProductReviewApp/
├── App/
│   ├── ProductReviewApp.swift       # Entry Point
│   ├── AppState.swift               # Global State (if needed)
├── Models/                          # Data Structures (Codable)
│   ├── Product.swift
│   ├── Review.swift
│   ├── Page.swift                   # Pagination Wrapper
├── Views/                           # SwiftUI Views
│   ├── Components/                  # Reusable UI (StarRating, ProductCard)
│   ├── Screens/
│   │   ├── ProductList/
│   │   │   ├── ProductListView.swift
│   │   │   ├── ProductListViewModel.swift
│   │   ├── ProductDetail/
│   │   │   ├── ProductDetailView.swift
│   │   │   ├── ProductDetailViewModel.swift
├── Services/                        # Networking Layer
│   ├── APIService.swift             # Generic API Client
│   ├── Endpoints.swift              # URL Definitions
├── Utils/                           # Extensions & Helpers
│   ├── Constants.swift
│   ├── Extensions.swift
├── Resources/
│   ├── Assets.xcassets
│   ├── Localizable.strings
```

---

## 🚀 Step-by-Step Implementation Plan

### Phase 1: Setup & Networking
1.  Create a new Xcode project (SwiftUI).
2.  Set up `APIService` using `URLSession` and Swift Concurrency (`async/await`).
3.  Define `Codable` structs for `Product` and `Review` matching the Backend JSON.

### Phase 2: Product Listing
1.  Create `ProductListViewModel` to fetch data.
2.  Implement `ProductListView` with a `LazyVGrid`.
3.  Add pagination logic (detect end of list and fetch next page).

### Phase 3: Details & Reviews
1.  Pass `productId` to `ProductDetailView`.
2.  Fetch full details and reviews in parallel (`async let`).
3.  Implement the **AI Summary Card** UI.

### Phase 4: User Interaction
1.  Create `AddReviewView` as a `.sheet`.
2.  Implement `POST` request to submit reviews.
3.  Add validation (e.g., comment length > 10 chars).

---

## 🧪 Testing Requirements
- **Unit Tests:** Write tests for `ViewModels` and `APIService` (mocking URLSession).
- **UI Tests:** Simple UI tests for navigation flows.

## 💡 Best Practices
- Use **Dependency Injection** for services.
- Handle **Loading** and **Error** states gracefully (shimmer effects, alerts).
- Ensure **Dark Mode** support.

---

## 🔗 API Reference
Base URL: `https://product-review-app-solarityai-a391ad53d79a.herokuapp.com`

- `GET /api/products?page=0&size=10`
- `GET /api/products/{id}`
- `GET /api/products/{id}/reviews`
- `POST /api/products/{id}/reviews`
