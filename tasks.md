# Product Review App - Development Tasks

## Team Assignment

| Role | Focus Area | Tech Stack |
|------|------------|------------|
| **Backend Developer** | Backend Core + React Native Frontend | Spring Boot, Java, React Native, TypeScript |
| **iOS Developer** | iOS Native App | Swift, SwiftUI, Xcode |

**Work Schedule:** 20 hours/week per developer

---

## Backend Developer Tasks (Backend + React Native)

### Category 1: Security & Stability (U1-U10) - Critical (~8.5h)

**Current Issues:**
- `@CrossOrigin(origins = "*")` allows all origins (`ProductController.java:21`, `UserController.java:18`)
- H2 console enabled in production (`application.properties:19`)
- No rate limiting
- `GlobalExceptionHandler.java:20` returns HTTP 404 for all `RuntimeException`
- No custom exception classes

**Tasks:** U1-U10 (details in Quick Reference)

**Files to modify:**
- `backend/src/main/java/com/example/productreview/config/CorsConfig.java` (create)
- `backend/src/main/java/com/example/productreview/controller/*.java`
- `backend/src/main/java/com/example/productreview/exception/` (create multiple)
- `backend/src/main/java/com/example/productreview/dto/ErrorResponse.java` (create)
- `backend/src/main/resources/application.properties`
- `backend/src/main/resources/application-prod.properties` (create)

---

### Category 2: OpenAI Integration (U11-U17) - High (~9h)

**Current Issues:**
- `AISummaryService.java:73-75` uses mock data
- Real OpenAI API calls not implemented
- No streaming support

**Tasks:** U11-U17 (details in Quick Reference)

**Files to modify:**
- `backend/src/main/java/com/example/productreview/service/AISummaryService.java`
- `backend/src/main/java/com/example/productreview/config/OpenAIConfig.java` (create)
- `backend/src/main/resources/application.properties`

---

### Category 3: Frontend (React Native) Improvements (U18-U23) - High (~8h)

**Current Issues:**
- `api.ts:82-86` has basic error handling, swallows detailed error info
- No retry logic
- Generic error messages

**Tasks:** U18-U23 (details in Quick Reference)

**Files to modify:**
- `mobile/src/services/api.ts`
- `mobile/src/screens/*.tsx`
- `mobile/src/components/OfflineBanner.tsx` (create)
- `mobile/src/hooks/useNetworkStatus.ts` (create)

---

### Category 4: Backend Tests (U24-U28) - Medium-High (~9h)

**Current Issues:**
- `ProductServiceTest.java` has only 3 test cases
- No tests for `UserService.java`, `AISummaryService.java`
- No integration tests for notification endpoints

**Tasks:** U24-U28 (details in Quick Reference)

**Files to modify:**
- `backend/src/test/java/com/example/productreview/service/*.java`
- `backend/src/test/java/com/example/productreview/controller/*.java` (create)
- `backend/pom.xml` (add JaCoCo)

---

### Category 5: Database Optimization (U29-U34) - Medium-High (~8h)

**Current Issues:**
- `ProductServiceImpl.java:96` has N+1 query issue
- H2 in-memory database loses data on restart
- No query result caching (except AI summaries)

**Tasks:** U29-U34 (details in Quick Reference)

**Files to modify:**
- `backend/src/main/java/com/example/productreview/repository/*.java`
- `backend/src/main/java/com/example/productreview/service/ProductServiceImpl.java`
- `backend/src/main/resources/application.properties`
- `backend/src/main/resources/db/migration/*.sql` (create)

---

### Category 6: Documentation (U35-U39) - Medium (~5h)

**Tasks:** U35-U39 (details in Quick Reference)

**Files to modify:**
- `backend/pom.xml`
- `backend/src/main/java/com/example/productreview/controller/*.java`
- `backend/src/main/java/com/example/productreview/config/OpenApiConfig.java` (create)

---

### Category 7: Frontend UI/UX Improvements (U40-U48) - Medium (~10h)

**Current Issues:**
- No skeleton loaders during data fetch
- Basic loading spinner only
- No micro-interactions or feedback animations
- Image loading shows empty space

**Tasks:** U40-U48 (details in Quick Reference)

**Files to modify/create:**
- `mobile/src/components/SkeletonLoader.tsx` (create)
- `mobile/src/components/ProductCardSkeleton.tsx` (create)
- `mobile/src/components/AnimatedButton.tsx` (create)
- `mobile/src/screens/ProductListScreen.tsx`
- `mobile/src/screens/ProductDetailScreen.tsx`
- `mobile/src/components/ProductCard.tsx`

---

### Category 8: Frontend Performance (U49-U55) - Medium (~8h)

**Current Issues:**
- No image caching
- All products loaded at once (no virtualization optimization)
- No request deduplication
- Images not optimized

**Tasks:** U49-U55 (details in Quick Reference)

**Files to modify/create:**
- `mobile/src/services/imageCache.ts` (create)
- `mobile/src/hooks/useDebounce.ts` (create)
- `mobile/src/hooks/useCachedData.ts` (create)
- `mobile/src/components/OptimizedImage.tsx` (create)
- `mobile/src/screens/ProductListScreen.tsx`

---

### Category 9: Frontend Accessibility (U56-U61) - Medium (~6h)

**Current Issues:**
- No accessibility labels
- Touch targets may be too small
- No screen reader support
- Color contrast not verified

**Tasks:** U56-U61 (details in Quick Reference)

**Files to modify:**
- `mobile/src/components/*.tsx`
- `mobile/src/screens/*.tsx`
- `mobile/App.tsx`

---

## iOS Developer Tasks (iOS Swift App)

### Context & Background

Native iOS app built with **SwiftUI** and **MVVM architecture**. Connects to Spring Boot backend API.

**Key Files to Understand First:**
- `ios/ProductReview/App/Core/Constants.swift` - API configuration
- `ios/ProductReview/Data/Network/APIClient.swift` - Network layer
- `ios/ProductReview/Data/Network/DTOs.swift` - Data transfer objects
- `ios/README.md` - Project documentation

**Current Status:**
- App builds and runs in simulator
- Basic CRUD operations work
- Connected to local backend (localhost:8080 in DEBUG mode)

---

### Category 1: Network & Error Handling (C1-C4) - Critical (~5h)

**Current Issues:**
- `APIClient.swift:56` has 30-second timeout (too long for mobile)
- No retry logic
- Errors not displayed to user properly

**Tasks:** C1-C4 (details in Quick Reference)

**Example Code:**
```swift
enum NetworkError: LocalizedError {
    case timeout
    case noConnection
    case serverError(Int)
    case decodingError

    var errorDescription: String? {
        switch self {
        case .timeout: return "Request timed out. Please try again."
        case .noConnection: return "No internet connection."
        case .serverError(let code): return "Server error (\(code)). Please try later."
        case .decodingError: return "Unable to process response."
        }
    }
}
```

**Files to modify:**
- `ios/ProductReview/Data/Network/APIClient.swift`
- `ios/ProductReview/Presentation/Views/Product/ProductListView.swift`
- `ios/ProductReview/Presentation/Views/Product/ProductDetailView.swift`

---

### Category 2: Loading & Empty States (C5-C9) - High (~4h)

**Current Issues:**
- Loading spinner exists but no skeleton/shimmer effect
- Empty states are plain text
- Pull-to-refresh feedback insufficient

**Tasks:** C5-C9 (details in Quick Reference)

**Files to create:**
- `ios/ProductReview/Presentation/Components/ShimmerView.swift`
- `ios/ProductReview/Presentation/Components/EmptyStateView.swift`

**Files to modify:**
- `ios/ProductReview/Presentation/Views/Product/ProductListView.swift`
- `ios/ProductReview/Presentation/Views/Wishlist/WishlistView.swift`

---

### Category 3: Notification Deep Linking (C10-C13) - High (~6h)

**Current Issues:**
- Tapping notification doesn't navigate to product
- No badge count on tab bar
- Local notification scheduling missing

**Tasks:** C10-C13 (details in Quick Reference)

**Example Code:**
```swift
// In ProductReviewApp.swift
@UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse) async {
        // Handle notification tap - navigate to product
    }
}
```

**Files to modify:**
- `ios/ProductReview/Presentation/Views/Notification/NotificationsView.swift`
- `ios/ProductReview/App/AppEntry/ContentView.swift`
- `ios/ProductReview/App/AppEntry/ProductReviewApp.swift`

---

### Category 4: Wishlist Improvements (C14-C17) - Medium (~5h)

**Current Issues:**
- Multi-select exists but batch delete UI needs polish
- No confirmation dialog before delete
- Optimistic updates can fail silently

**Tasks:** C14-C17 (details in Quick Reference)

**Files to modify:**
- `ios/ProductReview/Presentation/Views/Wishlist/WishlistView.swift`
- `ios/ProductReview/Presentation/Components/ToastView.swift` (create)

---

### Category 5: Search History (C18-C20) - Medium (~3h)

**Current Issues:**
- Search works but history not saved between sessions
- No recent searches shown

**Tasks:** C18-C20 (details in Quick Reference)

**Files to create:**
- `ios/ProductReview/Data/Local/SearchHistoryManager.swift`

**Files to modify:**
- `ios/ProductReview/Presentation/Views/Product/ProductListView.swift`

---

### Category 6: Theme Toggle (C21-C24) - Medium (~5h)

**Current Issues:**
- App follows system theme but no manual toggle
- Theme preference not persisted

**Tasks:** C21-C24 (details in Quick Reference)

**Files to modify:**
- `ios/ProductReview/App/AppEntry/ProductReviewApp.swift`
- `ios/ProductReview/App/AppEntry/ContentView.swift`
- `ios/ProductReview/Resources/Assets.xcassets/` (add color sets)

---

### Category 7: Animations (C25-C28) - Low (~4h)

**Tasks:** C25-C28 (details in Quick Reference)

**Files to modify:**
- `ios/ProductReview/Presentation/Views/Product/ProductListView.swift`
- `ios/ProductReview/Presentation/Views/Product/ProductDetailView.swift`

---

### Category 8: Unit Tests (C29-C32) - Medium (~5h)

**Current Issues:**
- Only 2 mapper tests exist
- No ViewModel tests
- No mock API client

**Tasks:** C29-C32 (details in Quick Reference)

**Files to create:**
- `ios/ProductReviewTests/Mocks/MockAPIClient.swift`
- `ios/ProductReviewTests/ViewModels/ProductListViewModelTests.swift`
- `ios/ProductReviewTests/ViewModels/ProductDetailViewModelTests.swift`

---

### Category 9: UI Tests (C33-C37) - Low (~5h)

**Tasks:** C33-C37 (details in Quick Reference)

**Files to modify:**
- `ios/ProductReviewUITests/ProductReviewUITests.swift`

---

## Summary Table

### Backend Developer (61 tasks → ~69h)

| Category | Tasks | Priority | Hours |
|----------|-------|----------|-------|
| Security & Stability | U1-U10 | Critical | ~8.5h |
| OpenAI Integration | U11-U17 | High | ~9h |
| Frontend Error Handling | U18-U23 | High | ~8h |
| Backend Tests | U24-U28 | Medium-High | ~9h |
| Database Optimization | U29-U34 | Medium-High | ~8h |
| Documentation | U35-U39 | Medium | ~5h |
| Frontend UI/UX | U40-U48 | Medium | ~10h |
| Frontend Performance | U49-U55 | Medium | ~8h |
| Frontend Accessibility | U56-U61 | Medium | ~6h |

**Total: 61 tasks → ~69 hours (~3.5 weeks at 20h/week)**

---

### iOS Developer (37 tasks → ~42h)

| Category | Tasks | Priority | Hours |
|----------|-------|----------|-------|
| Network & Error Handling | C1-C4 | Critical | ~5h |
| Loading & Empty States | C5-C9 | High | ~4h |
| Notification Deep Linking | C10-C13 | High | ~6h |
| Wishlist Improvements | C14-C17 | Medium | ~5h |
| Search History | C18-C20 | Medium | ~3h |
| Theme Toggle | C21-C24 | Medium | ~5h |
| Animations | C25-C28 | Low | ~4h |
| Unit Tests | C29-C32 | Medium | ~5h |
| UI Tests | C33-C37 | Low | ~5h |

**Total: 37 tasks → ~42 hours (~2 weeks at 20h/week)**

---

## Weekly Sprint Plan (20 hours/week)

### Week 1

**Backend Developer (20h):**
| Task | Description | Hours |
|------|-------------|-------|
| U1 | Create CorsConfig.java (environment-based origins) | 1h |
| U2 | Remove @CrossOrigin annotations from controllers | 0.5h |
| U3 | Disable H2 console for production | 0.5h |
| U4 | Add rate limiting filter (Bucket4j) | 1.5h |
| U5 | Write CORS tests | 0.5h |
| U6 | Create custom exception classes | 1h |
| U7 | Update GlobalExceptionHandler (proper HTTP codes) | 1h |
| U8 | Create ErrorResponse DTO | 0.5h |
| U9 | Update services to throw proper exceptions | 1.5h |
| U10 | Write exception handling unit tests | 1h |
| U11 | Implement OpenAI client (simple-openai) | 2h |
| U12 | Add request/response logging | 1h |
| U13 | Add OpenAI API rate limiting | 1h |
| U14 | Add exponential backoff retry logic | 1h |
| U15 | Implement response caching (Caffeine) | 1h |
| U16 | Add streaming support for chat endpoint | 2h |
| U17 | Write OpenAI integration tests | 1h |
| | **Week 1 Total** | **18h** |

**iOS Developer (20h):**
| Task | Description | Hours |
|------|-------------|-------|
| C1 | Reduce APIClient timeout (10s regular, 20s AI) | 1h |
| C2 | Add exponential backoff retry (3 attempts) | 2h |
| C3 | Create NetworkError enum (user-friendly messages) | 1h |
| C4 | Add error alert presentation in views | 1h |
| C5 | Create ShimmerView component | 1.5h |
| C6 | Create EmptyStateView component | 1h |
| C7 | Add shimmer to ProductListView | 0.5h |
| C8 | Add empty state to WishlistView | 0.5h |
| C9 | Improve pull-to-refresh feedback | 0.5h |
| C10 | Notification tap → product detail navigation | 2h |
| C11 | Add badge count to notifications tab | 1h |
| C12 | Setup UNUserNotificationCenter for local notifications | 1.5h |
| C13 | Handle background notification tap | 1.5h |
| C14 | Add confirmation alert before batch delete | 1h |
| C15 | Create ToastView component | 1.5h |
| C16 | Add undo capability for deletes | 1.5h |
| C17 | Improve batch operation error handling | 1h |
| | **Week 1 Total** | **20h** |

---

### Week 2

**Backend Developer (20h):**
| Task | Description | Hours |
|------|-------------|-------|
| U18 | Create ApiError class (typed error responses) | 1h |
| U19 | Add exponential backoff retry mechanism | 2h |
| U20 | Add request timeout configuration | 0.5h |
| U21 | Update screens with user-friendly error messages | 2h |
| U22 | Add offline detection and banner component | 1.5h |
| U23 | Write API error handling unit tests | 1h |
| U24 | Write ProductService error case tests | 2h |
| U25 | Write UserService unit tests | 2h |
| U26 | Write AISummaryService tests | 2h |
| U27 | Write User endpoint integration tests | 2h |
| U28 | Configure JaCoCo code coverage | 1h |
| U29 | Fix N+1 query - add explicit join @Query | 2h |
| U30 | Add database indexes | 1h |
| | **Week 2 Total** | **20h** |

**iOS Developer (20h):**
| Task | Description | Hours |
|------|-------------|-------|
| C18 | Create SearchHistoryManager (UserDefaults) | 1h |
| C19 | Show recent searches below search bar | 1.5h |
| C20 | Add clear history button | 0.5h |
| C21 | Add theme toggle in settings | 1h |
| C22 | Persist theme preference in UserDefaults | 1h |
| C23 | Create custom colors for both modes | 1.5h |
| C24 | Test all screens in both themes | 1.5h |
| C25 | Add hero animation (list → detail) | 1.5h |
| C26 | Add tab switch slide animation | 1h |
| C27 | Add wishlist heart bounce effect | 0.5h |
| C28 | Add image load fade-in animation | 1h |
| C29 | Create MockAPIClient | 1h |
| C30 | Write ProductListViewModel tests | 1.5h |
| C31 | Write ProductDetailViewModel tests | 1.5h |
| C32 | Write error state and edge case tests | 1h |
| C33 | Test product list loading and scrolling | 1h |
| C34 | Test product detail navigation | 1h |
| | **Week 2 Total** | **19h** |

---

### Week 3

**Backend Developer (20h):**
| Task | Description | Hours |
|------|-------------|-------|
| U31 | Add pagination validation (max 100 items) | 1h |
| U32 | Configure HikariCP connection pooling | 1h |
| U33 | Create PostgreSQL migration scripts | 2h |
| U34 | Add database profile switching | 1h |
| U35 | Add springdoc-openapi dependency | 0.5h |
| U36 | Configure Swagger UI endpoint | 0.5h |
| U37 | Add OpenAPI annotations to controllers | 2h |
| U38 | Document request/response schemas | 1h |
| U39 | Add API versioning (/api/v1/) | 1h |
| U40 | Create SkeletonLoader base component | 1h |
| U41 | Create ProductCardSkeleton component | 1h |
| U42 | Add skeleton to ProductListScreen | 1h |
| U43 | Add skeleton to ProductDetailScreen | 1h |
| U44 | Create AnimatedButton with press feedback | 1.5h |
| U45 | Add image placeholder with fade-in effect | 1h |
| U46 | Add pull-to-refresh animation enhancement | 1h |
| U47 | Add rating star animation on selection | 1h |
| | **Week 3 Total** | **19.5h** |

**iOS Developer (3h - Final):**
| Task | Description | Hours |
|------|-------------|-------|
| C35 | Test wishlist add/remove flow | 1h |
| C36 | Test search functionality | 1h |
| C37 | Test error state display | 1h |
| | **Week 3 iOS Total** | **3h** |

---

### Week 4

**Backend Developer (20h):**
| Task | Description | Hours |
|------|-------------|-------|
| U48 | Add wishlist heart animation (bounce) | 1.5h |
| U49 | Create image caching service | 1.5h |
| U50 | Create OptimizedImage component | 1h |
| U51 | Add search debounce hook | 1h |
| U52 | Implement request deduplication | 1.5h |
| U53 | Add FlatList performance optimizations | 1h |
| U54 | Implement lazy loading for images | 1h |
| U55 | Add memory cache for API responses | 1h |
| U56 | Add accessibilityLabel to all touchables | 1h |
| U57 | Add accessibilityHint for complex actions | 1h |
| U58 | Ensure minimum touch target size (44x44) | 1h |
| U59 | Add accessibilityRole to components | 1h |
| U60 | Test with screen reader (VoiceOver/TalkBack) | 1h |
| U61 | Fix color contrast issues (WCAG AA) | 1h |
| | **Week 4 Total** | **15.5h** |

**Note:** Backend Developer completes in ~3.5 weeks. Remaining 4.5h in Week 4 can be used for code review, bug fixes, or additional polish.

---

## Notes

- All tasks should include code review before merging
- iOS tasks require Xcode 15+ and macOS Sonoma
- Backend tasks can be developed on any OS with Java 17+
- Use feature branches: `feat/task-name` or `fix/task-name`
- Write conventional commits (see CLAUDE.md for format)

---

## Quick Reference - Task Checklist

### Backend Developer

#### Security & Stability (Critical)
| # | Task | Hours |
|---|------|-------|
| U1 | Create CorsConfig.java (environment-based origins) | 1h |
| U2 | Remove @CrossOrigin annotations from controllers | 0.5h |
| U3 | Disable H2 console for production profile | 0.5h |
| U4 | Add rate limiting filter (Bucket4j) | 1.5h |
| U5 | Write CORS tests | 0.5h |
| U6 | Create custom exception classes (ResourceNotFound, Validation, Unauthorized) | 1h |
| U7 | Update GlobalExceptionHandler (proper HTTP status codes) | 1h |
| U8 | Create ErrorResponse DTO (timestamp, code, message, details) | 0.5h |
| U9 | Update services to throw appropriate exceptions | 1.5h |
| U10 | Write exception handling unit tests | 1h |

#### OpenAI Integration (High)
| # | Task | Hours |
|---|------|-------|
| U11 | Implement OpenAI client (simple-openai dependency) | 2h |
| U12 | Add request/response logging | 1h |
| U13 | Add OpenAI API rate limiting | 1h |
| U14 | Add exponential backoff retry logic | 1h |
| U15 | Implement response caching (Caffeine) | 1h |
| U16 | Add streaming support for chat endpoint | 2h |
| U17 | Write OpenAI integration tests (mock responses) | 1h |

#### Frontend (React Native) Improvements (High)
| # | Task | Hours |
|---|------|-------|
| U18 | Create ApiError class (typed error responses) | 1h |
| U19 | Add exponential backoff retry mechanism | 2h |
| U20 | Add request timeout configuration | 0.5h |
| U21 | Update screens with user-friendly error messages | 2h |
| U22 | Add offline detection and banner component | 1.5h |
| U23 | Write API error handling unit tests | 1h |

#### Backend Tests (Medium-High)
| # | Task | Hours |
|---|------|-------|
| U24 | Write ProductService error case tests (invalid ID, null review) | 2h |
| U25 | Write UserService unit tests (wishlist, notifications) | 2h |
| U26 | Write AISummaryService tests (mock, caching) | 2h |
| U27 | Write User endpoint integration tests | 2h |
| U28 | Configure JaCoCo code coverage | 1h |

#### Database Optimization (Medium-High)
| # | Task | Hours |
|---|------|-------|
| U29 | Fix N+1 query - add explicit join @Query | 2h |
| U30 | Add database indexes (category, search columns) | 1h |
| U31 | Add pagination validation (max 100 items) | 1h |
| U32 | Configure HikariCP connection pooling | 1h |
| U33 | Create PostgreSQL migration scripts | 2h |
| U34 | Add database profile switching (H2 dev, PostgreSQL prod) | 1h |

#### Documentation (Medium)
| # | Task | Hours |
|---|------|-------|
| U35 | Add springdoc-openapi dependency | 0.5h |
| U36 | Configure Swagger UI endpoint | 0.5h |
| U37 | Add OpenAPI annotations to controllers | 2h |
| U38 | Document request/response schemas | 1h |
| U39 | Add API versioning (/api/v1/) | 1h |

#### Frontend UI/UX Improvements (Medium)
| # | Task | Hours |
|---|------|-------|
| U40 | Create SkeletonLoader base component | 1h |
| U41 | Create ProductCardSkeleton component | 1h |
| U42 | Add skeleton to ProductListScreen | 1h |
| U43 | Add skeleton to ProductDetailScreen | 1h |
| U44 | Create AnimatedButton with press feedback | 1.5h |
| U45 | Add image placeholder with fade-in effect | 1h |
| U46 | Add pull-to-refresh animation enhancement | 1h |
| U47 | Add rating star animation on selection | 1h |
| U48 | Add wishlist heart animation (bounce) | 1.5h |

#### Frontend Performance (Medium)
| # | Task | Hours |
|---|------|-------|
| U49 | Create image caching service | 1.5h |
| U50 | Create OptimizedImage component | 1h |
| U51 | Add search debounce hook | 1h |
| U52 | Implement request deduplication | 1.5h |
| U53 | Add FlatList performance optimizations | 1h |
| U54 | Implement lazy loading for images | 1h |
| U55 | Add memory cache for API responses | 1h |

#### Frontend Accessibility (Medium)
| # | Task | Hours |
|---|------|-------|
| U56 | Add accessibilityLabel to all touchables | 1h |
| U57 | Add accessibilityHint for complex actions | 1h |
| U58 | Ensure minimum touch target size (44x44) | 1h |
| U59 | Add accessibilityRole to components | 1h |
| U60 | Test with screen reader (VoiceOver/TalkBack) | 1h |
| U61 | Fix color contrast issues (WCAG AA) | 1h |

| | **BACKEND TOTAL** | **~69h** |

---

### iOS Developer

#### Network & Error Handling (Critical)
| # | Task | Hours |
|---|------|-------|
| C1 | Reduce APIClient timeout (10s regular, 20s AI) | 1h |
| C2 | Add exponential backoff retry (3 attempts: 1s, 2s, 4s) | 2h |
| C3 | Create NetworkError enum (user-friendly messages) | 1h |
| C4 | Add error alert presentation in views | 1h |

#### Loading & Empty States (High)
| # | Task | Hours |
|---|------|-------|
| C5 | Create ShimmerView component | 1.5h |
| C6 | Create EmptyStateView component (icon + message) | 1h |
| C7 | Add shimmer to ProductListView | 0.5h |
| C8 | Add empty state to WishlistView | 0.5h |
| C9 | Improve pull-to-refresh visual feedback | 0.5h |

#### Notification Deep Linking (High)
| # | Task | Hours |
|---|------|-------|
| C10 | Notification tap → product detail navigation | 2h |
| C11 | Add badge count to notifications tab | 1h |
| C12 | Setup UNUserNotificationCenter for local notifications | 1.5h |
| C13 | Handle background notification tap | 1.5h |

#### Wishlist Improvements (Medium)
| # | Task | Hours |
|---|------|-------|
| C14 | Add confirmation alert before batch delete | 1h |
| C15 | Create ToastView component (success feedback) | 1.5h |
| C16 | Add undo capability for accidental deletes | 1.5h |
| C17 | Improve batch operation error handling | 1h |

#### Search History (Medium)
| # | Task | Hours |
|---|------|-------|
| C18 | Create SearchHistoryManager (UserDefaults) | 1h |
| C19 | Show recent searches below search bar | 1.5h |
| C20 | Add clear history button | 0.5h |

#### Theme Toggle (Medium)
| # | Task | Hours |
|---|------|-------|
| C21 | Add theme toggle in settings/profile | 1h |
| C22 | Persist theme preference in UserDefaults | 1h |
| C23 | Create custom colors for both modes | 1.5h |
| C24 | Test all screens in both themes | 1.5h |

#### Animations (Low)
| # | Task | Hours |
|---|------|-------|
| C25 | Add hero animation (list → detail) | 1.5h |
| C26 | Add tab switch slide animation | 1h |
| C27 | Add wishlist heart bounce effect | 0.5h |
| C28 | Add image load fade-in animation | 1h |

#### Unit Tests (Medium)
| # | Task | Hours |
|---|------|-------|
| C29 | Create MockAPIClient | 1h |
| C30 | Write ProductListViewModel tests | 1.5h |
| C31 | Write ProductDetailViewModel tests | 1.5h |
| C32 | Write error state and edge case tests | 1h |

#### UI Tests (Low)
| # | Task | Hours |
|---|------|-------|
| C33 | Test product list loading and scrolling | 1h |
| C34 | Test product detail navigation | 1h |
| C35 | Test wishlist add/remove flow | 1h |
| C36 | Test search functionality | 1h |
| C37 | Test error state display | 1h |

| | **iOS TOTAL** | **~42h** |

---

## Simple Summary

### Backend Developer (61 tasks)
| Category | Tasks | Hours |
|----------|-------|-------|
| Security & Stability | 10 | ~8.5h |
| OpenAI Integration | 7 | ~9h |
| Frontend Error Handling | 6 | ~8h |
| Backend Tests | 5 | ~9h |
| Database Optimization | 6 | ~8h |
| Documentation | 5 | ~5h |
| Frontend UI/UX | 9 | ~10h |
| Frontend Performance | 7 | ~8h |
| Frontend Accessibility | 6 | ~6h |
| **TOTAL** | **61** | **~69h** |

### iOS Developer (37 tasks)
| Category | Tasks | Hours |
|----------|-------|-------|
| Network & Error Handling | 4 | ~5h |
| Loading & Empty States | 5 | ~4h |
| Notification Deep Linking | 4 | ~6h |
| Wishlist Improvements | 4 | ~5h |
| Search History | 3 | ~3h |
| Theme Toggle | 4 | ~5h |
| Animations | 4 | ~4h |
| Unit Tests | 4 | ~5h |
| UI Tests | 5 | ~5h |
| **TOTAL** | **37** | **~42h** |
