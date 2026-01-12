# 🤖 Android Frontend (Kotlin) - Implementation Guide

This document serves as the comprehensive guide for interns choosing the **Android (Kotlin)** stack. Your goal is to build a native Android application that consumes the Spring Boot backend API, matching the features of the reference React Native implementation.

---

## 🎯 Objective
Develop a production-ready Android application using **Kotlin** and **Jetpack Compose**. The app must demonstrate modern Android development practices (MAD), clean architecture, and Material Design 3.

---

## 🛠️ Tech Stack & Requirements

| Category | Requirement | Recommended Libraries |
|----------|-------------|----------------------|
| **Language** | Kotlin | - |
| **UI Toolkit** | Jetpack Compose | Material3 |
| **Architecture** | MVVM + Clean Architecture | - |
| **Networking** | Retrofit + OkHttp | Moshi or Gson |
| **Async** | Kotlin Coroutines + Flow | - |
| **DI** | Hilt (Dagger) | Koin (Alternative) |
| **Image Loading** | Coil | Glide |

---

## 📱 Features to Implement

### 1. Product List (Home)
- **UI:** `LazyVerticalGrid` displaying products.
- **Features:**
  - **Pagination:** Implement `Paging 3` library or manual pagination with `LazyColumn`.
  - **Filtering:** BottomSheet or Chips for category filtering.
  - **Search:** Top app bar search functionality.

### 2. Product Details
- **UI:** Scaffold with large product image, collapsing toolbar (optional), and details.
- **Features:**
  - **AI Summary:** Render the `aiSummary` text in a styled Card with an icon.
  - **Rating Breakdown:** Custom Canvas drawing or ProgressBars for star distribution.

### 3. Reviews System
- **UI:** List of reviews.
- **Features:**
  - **Add Review:** A Dialog or BottomSheet to input rating and comment.
  - **State Management:** Optimistic UI updates when a review is added.

---

## 🏗️ Recommended Architecture

The project should follow **Clean Architecture** principles (Data, Domain, UI layers).

```
com.example.productreview/
├── data/                            # Data Layer
│   ├── api/
│   │   ├── ApiService.kt            # Retrofit Interface
│   │   ├── ApiModule.kt             # Hilt Module
│   ├── model/                       # DTOs (Data Transfer Objects)
│   │   ├── ProductDto.kt
│   ├── repository/
│   │   ├── ProductRepositoryImpl.kt
├── domain/                          # Domain Layer (Optional but recommended)
│   ├── model/                       # Business Models
│   ├── repository/                  # Repository Interfaces
│   ├── usecase/                     # Use Cases (e.g., GetProductsUseCase)
├── ui/                              # UI Layer
│   ├── components/                  # Reusable Composables
│   │   ├── ProductCard.kt
│   │   ├── StarRating.kt
│   ├── screens/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HomeViewModel.kt
│   │   ├── detail/
│   │   │   ├── DetailScreen.kt
│   │   │   ├── DetailViewModel.kt
│   ├── theme/                       # Theme & Color Definitions
│   ├── MainActivity.kt
```

---

## 🚀 Step-by-Step Implementation Plan

### Phase 1: Setup & Infrastructure
1.  Create a new Android Studio project (Empty Compose Activity).
2.  Add dependencies: Retrofit, Hilt, Coil, Navigation Compose.
3.  Set up the `NetworkModule` (Base URL, OkHttp Client).

### Phase 2: Data Layer
1.  Define DTOs (`data class`) matching the Backend JSON.
2.  Create `ProductRepository` to handle data fetching.
3.  Implement `Result<T>` wrapper for error handling.

### Phase 3: UI & Navigation
1.  Set up `NavHost` for screen navigation.
2.  Build `ProductCard` and `HomeScreen`.
3.  Connect `HomeViewModel` to Repository and expose `StateFlow<UiState>`.

### Phase 4: Advanced Features
1.  Implement **AI Summary Card** in Detail Screen.
2.  Add "Add Review" functionality using a POST request.
3.  Handle loading states (CircularProgressIndicator) and errors (Snackbar).

---

## 🧪 Testing Requirements
- **Unit Tests:** Test ViewModels and UseCases using `JUnit4` and `MockK`.
- **UI Tests:** Basic Compose tests (`composeTestRule`) to verify elements exist.

## 💡 Best Practices
- Use **Unidirectional Data Flow (UDF)**.
- Handle configuration changes (rotation) automatically via ViewModels.
- Support **Dynamic Colors** (Material You) if possible.

---

## 🔗 API Reference
Base URL: `https://product-review-app-solarityai-a391ad53d79a.herokuapp.com`

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products/{id}/reviews`
