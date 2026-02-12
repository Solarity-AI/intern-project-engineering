# OVERVIEW.md

## 1. Project Summary

The **Product Review Application** is a comprehensive full-stack ecosystem demonstrating modern software architecture, clean code principles, and cross-platform integration. The system enables users to:

- Browse and search products with advanced filtering and pagination
- View detailed product information with AI-generated review summaries
- Submit reviews with star ratings and receive helpful vote tracking
- Manage personal wishlists with multi-select batch operations
- Interact with an AI assistant for product review analysis
- Receive and manage notifications

The application serves as an internship training platform where the backend API and React Native reference implementation are provided, and interns implement native iOS (Swift) or Android (Kotlin) frontends.

**Domain Responsibilities:**
- Product catalog management with category-based organization
- User-generated review collection and sentiment analysis
- AI-powered review summarization and Q&A
- User preference persistence (wishlist, notifications) via device-based identification

---

## 2. Technology Stack

### Backend (Java/Spring Boot)

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17 | Runtime language |
| **Spring Boot** | 3.2.1 | Application framework |
| **Spring Data JPA** | - | ORM and data access |
| **Spring Boot Validation** | - | Input validation (Jakarta Bean Validation) |
| **Spring Boot Actuator** | - | Health checks and monitoring |
| **H2 Database** | - | In-memory relational database |
| **Hibernate** | - | JPA implementation |
| **Lombok** | 1.18.30 | Boilerplate code generation |
| **simple-openai** | 3.8.1 | OpenAI GPT-4o-mini integration |
| **Caffeine** | - | High-performance caching |
| **Maven** | 3.9 | Build tool |

### Frontend (React Native/Expo)

| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 19.1.0 | UI library |
| **React Native** | 0.81.5 | Cross-platform mobile framework |
| **Expo** | 54.0.31 | Development and build tooling |
| **TypeScript** | 5.9.2 | Type-safe JavaScript |
| **React Navigation** | 7.x | Screen navigation |
| **AsyncStorage** | 2.2.0 | Local persistence |
| **Expo Linear Gradient** | 15.0.8 | UI gradients |
| **Vercel Analytics** | 1.6.1 | Web analytics |

### iOS Native (Swift/SwiftUI)

| Technology | Version | Purpose |
|------------|---------|---------|
| **Swift** | 5.9+ | Language |
| **SwiftUI** | - | Declarative UI framework |
| **URLSession** | - | Networking (async/await) |
| **NWPathMonitor** | - | Real-time connectivity monitoring |
| **NSCache** | - | In-memory image caching (50MB limit) |
| **UserDefaults** | - | Theme and device ID persistence |
| **XcodeGen** | - | Xcode project generation from YAML |

**Minimum Requirements:** iOS 17.0, Xcode 15.0+, macOS Sonoma (14.0+)

### Deployment Infrastructure

| Platform | Purpose |
|----------|---------|
| **Render.com** | Backend hosting (Docker-based) |
| **Vercel** | Web frontend hosting (CDN) |
| **EAS Build** | Mobile app builds (APK/IPA) |
| **GitHub Actions** | CI/CD automation |

---

## 3. Repository Structure

```
.
├── backend/                          # Spring Boot REST API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/productreview/
│   │   │   │   ├── ProductReviewApplication.java
│   │   │   │   ├── controller/       # REST endpoints
│   │   │   │   │   ├── ProductController.java
│   │   │   │   │   └── UserController.java
│   │   │   │   ├── service/          # Business logic
│   │   │   │   │   ├── ProductService.java
│   │   │   │   │   ├── ProductServiceImpl.java
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   ├── AISummaryService.java
│   │   │   │   │   └── DataInitializer.java
│   │   │   │   ├── model/            # JPA entities
│   │   │   │   │   ├── Product.java
│   │   │   │   │   ├── Review.java
│   │   │   │   │   ├── ReviewVote.java
│   │   │   │   │   ├── WishlistItem.java
│   │   │   │   │   └── AppNotification.java
│   │   │   │   ├── repository/       # Data access
│   │   │   │   │   ├── ProductRepository.java
│   │   │   │   │   ├── ReviewRepository.java
│   │   │   │   │   ├── ReviewVoteRepository.java
│   │   │   │   │   ├── WishlistRepository.java
│   │   │   │   │   └── NotificationRepository.java
│   │   │   │   ├── dto/              # Data transfer objects
│   │   │   │   │   ├── ProductDTO.java
│   │   │   │   │   ├── ReviewDTO.java
│   │   │   │   │   └── ErrorResponse.java
│   │   │   │   ├── config/           # Configuration
│   │   │   │   │   ├── CacheConfig.java
│   │   │   │   │   ├── CorsConfig.java
│   │   │   │   │   └── RateLimitingFilter.java
│   │   │   │   └── exception/        # Error handling
│   │   │   │       ├── GlobalExceptionHandler.java
│   │   │   │       ├── ResourceNotFoundException.java
│   │   │   │       ├── ValidationException.java
│   │   │   │       └── UnauthorizedException.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── application-prod.properties
│   │   └── test/                     # Unit & integration tests
│   ├── pom.xml                       # Maven configuration
│   └── Dockerfile                    # Container build
│
├── mobile/                           # React Native (Expo) Frontend
│   ├── src/
│   │   ├── screens/                  # Screen components
│   │   │   ├── ProductListScreen.tsx
│   │   │   ├── ProductDetailsScreen.tsx
│   │   │   ├── WishlistScreen.tsx
│   │   │   ├── NotificationsScreen.tsx
│   │   │   ├── NotificationDetailScreen.tsx
│   │   │   └── AIAssistantScreen.tsx
│   │   ├── components/               # Reusable UI components
│   │   │   ├── ProductCard.tsx
│   │   │   ├── ReviewCard.tsx
│   │   │   ├── SearchBar.tsx
│   │   │   ├── CategoryFilter.tsx
│   │   │   ├── StarRating.tsx
│   │   │   ├── RatingBreakdown.tsx
│   │   │   ├── AISummaryCard.tsx
│   │   │   ├── AddReviewModal.tsx
│   │   │   └── ...
│   │   ├── context/                  # State management
│   │   │   ├── ThemeContext.tsx
│   │   │   ├── WishlistContext.tsx
│   │   │   ├── NotificationContext.tsx
│   │   │   ├── SearchContext.tsx
│   │   │   ├── ToastContext.tsx
│   │   │   └── NetworkContext.tsx
│   │   ├── services/                 # API client
│   │   │   └── api.ts
│   │   ├── constants/                # Theme & configuration
│   │   │   └── theme.ts
│   │   ├── types/                    # TypeScript definitions
│   │   │   └── index.ts
│   │   └── hooks/                    # Custom hooks
│   │       └── useColorScheme.ts
│   ├── App.tsx                       # Entry point
│   ├── package.json                  # Node dependencies
│   ├── app.json                      # Expo configuration
│   ├── eas.json                      # EAS Build configuration
│   ├── vercel.json                   # Web deployment config
│   └── tsconfig.json                 # TypeScript configuration
│
├── ios/                              # Native iOS App (Swift/SwiftUI)
│   ├── ProductReview/
│   │   ├── App/
│   │   │   ├── AppEntry/             # App & Scene setup
│   │   │   │   ├── ProductReviewApp.swift
│   │   │   │   └── ContentView.swift
│   │   │   ├── Navigation/           # NavigationStack & routes
│   │   │   │   └── NavigationRouter.swift
│   │   │   └── Core/                 # Shared utilities
│   │   │       ├── Constants.swift
│   │   │       ├── HapticManager.swift
│   │   │       ├── NetworkMonitor.swift
│   │   │       └── ThemeManager.swift
│   │   ├── Data/
│   │   │   ├── Network/              # API clients, DTOs
│   │   │   │   ├── APIClient.swift
│   │   │   │   └── DTOs.swift
│   │   │   ├── Local/                # Local storage & caching
│   │   │   │   ├── ImageCache.swift
│   │   │   │   └── SearchHistoryManager.swift
│   │   │   ├── Mapper/               # DTO ↔ Domain mapping
│   │   │   │   ├── ProductMapper.swift
│   │   │   │   ├── ReviewMapper.swift
│   │   │   │   └── NotificationMapper.swift
│   │   │   └── Repository/           # Repository implementations
│   │   │       ├── ProductRepository.swift
│   │   │       ├── WishlistRepository.swift
│   │   │       └── NotificationRepository.swift
│   │   ├── Domain/
│   │   │   ├── Model/                # Domain models
│   │   │   │   ├── Product.swift
│   │   │   │   ├── Review.swift
│   │   │   │   └── Notification.swift
│   │   │   └── Repository/           # Repository protocols
│   │   │       ├── ProductRepositoryProtocol.swift
│   │   │       ├── WishlistRepositoryProtocol.swift
│   │   │       └── NotificationRepositoryProtocol.swift
│   │   ├── Presentation/
│   │   │   ├── Views/
│   │   │   │   ├── Product/          # ProductListView, ProductDetailView
│   │   │   │   ├── Wishlist/         # WishlistView
│   │   │   │   ├── Notification/     # NotificationsView
│   │   │   │   └── AI/               # AIAssistantView
│   │   │   ├── ViewModels/           # ObservableObjects
│   │   │   │   ├── ProductListViewModel.swift
│   │   │   │   └── ProductDetailViewModel.swift
│   │   │   └── Components/           # Reusable UI components
│   │   │       ├── AnimatedHeartButton.swift
│   │   │       ├── ConfirmationDialog.swift
│   │   │       ├── EmptyStateView.swift
│   │   │       ├── LoadingButton.swift
│   │   │       ├── RatingStarsView.swift
│   │   │       ├── ShimmerView.swift
│   │   │       └── ToastView.swift
│   │   └── Resources/
│   │       └── Assets.xcassets
│   ├── ProductReviewTests/           # Unit tests
│   ├── ProductReviewUITests/         # UI tests
│   ├── project.yml                   # XcodeGen configuration
│   └── README.md                     # iOS-specific documentation
│
├── .github/workflows/                # CI/CD
│   └── deploy-vercel.yml             # Vercel deployment workflow
├── .vscode/                          # IDE configuration
│   ├── settings.json
│   └── launch.json
├── render.yaml                       # Render.com deployment
├── Procfile                          # Process definition
├── pom.xml                           # Parent Maven project
├── system.properties                 # Java version
├── CLAUDE.md                         # Project documentation
├── tasks.md                          # Task tracking
└── README.md                         # Main documentation
```

### Architectural Patterns

**Backend**: Layered Clean Architecture
```
Controller → Service → Repository → Entity
     ↓           ↓           ↓
    DTO    Business Logic   JPA/Hibernate
```

**Frontend (React Native)**: Context-based State Management
```
App → Providers → Navigator → Screens → Components
         ↓
    Context (Theme, Wishlist, Notifications, Search, Toast, Network)
```

**Frontend (iOS Native)**: MVVM + Clean Architecture
```
Presentation          Domain              Data
  Views ←→ ViewModels → Repository Protocols ← Repository Implementations
  Components              Models              ├→ APIClient (Network)
                                              ├→ DTOs + Mappers
                                              └→ Local Storage (Cache, UserDefaults)
```

---

## 4. Core Architecture Components

### 4.1 Backend API Layer

**ProductController** (`/api/products`)
- Product listing with pagination, filtering, and sorting
- Single product retrieval with AI summary
- Review management (list, create, helpful voting)
- AI chat endpoint for product questions
- Global statistics aggregation

**UserController** (`/api/user`)
- Wishlist management (get, toggle, paginated products)
- Notification CRUD operations
- User identification via `X-User-ID` header

### 4.2 Service Layer

**ProductServiceImpl**
- Business logic for product and review operations
- Rating statistics calculation
- Cache-aware AI summary generation
- Transactional review submission with stats update

**AISummaryService**
- OpenAI GPT-4o-mini integration
- Mock summary generation for development
- Caffeine-cached summaries (1-hour TTL)
- Intent-based chat responses

**UserService**
- Wishlist toggle operations
- Notification management
- User preference persistence

### 4.3 Data Layer

**Entities:**
- `Product`: Core product with categories, pricing, denormalized stats
- `Review`: User reviews with ratings and helpful counts
- `ReviewVote`: Tracks user votes on reviews
- `WishlistItem`: User-product wishlist association
- `AppNotification`: User notifications with read status

**Repositories:** Spring Data JPA with custom JPQL queries for:
- Category-based filtering
- Combined search + category queries
- Rating distribution aggregation
- Statistics calculations

### 4.4 Frontend State Management

**Context Providers:**
| Context | Purpose |
|---------|---------|
| ThemeContext | Dark/light mode with persistence |
| WishlistContext | Dual-source sync (local + backend) |
| NotificationContext | Notification state with optimistic updates |
| SearchContext | Search history persistence |
| ToastContext | Animated toast notifications |
| NetworkContext | Connectivity monitoring |

### 4.5 iOS Native Architecture

**Data Layer:**
- `APIClient`: URLSession-based networking with async/await, exponential backoff retry (1s → 2s → 4s), user-friendly error messages, and connectivity monitoring via NWPathMonitor
- `DTOs`: Codable data transfer objects matching the backend JSON contract
- `Mappers`: ProductMapper, ReviewMapper, NotificationMapper for DTO → Domain model conversion
- `Repositories`: Concrete implementations of domain repository protocols (Product, Wishlist, Notification)
- `ImageCache`: NSCache-backed in-memory image cache (50MB limit)
- `SearchHistoryManager`: UserDefaults-backed search history (max 10 items)

**Domain Layer:**
- `Models`: Product, Review, Notification domain entities
- `Repository Protocols`: Abstract interfaces for data access (ProductRepositoryProtocol, WishlistRepositoryProtocol, NotificationRepositoryProtocol)

**Presentation Layer:**
- `ViewModels`: @Published-based ObservableObjects (ProductListViewModel, ProductDetailViewModel) with @MainActor for thread safety
- `Views`: SwiftUI views for all screens (ProductList, ProductDetail, Wishlist, Notifications, AIAssistant)
- `Components`: 7 reusable components (EmptyStateView, ShimmerView, ToastView, LoadingButton, AnimatedHeartButton, RatingStarsView, ConfirmationDialog)

**Core Utilities:**
- `ThemeManager`: System/light/dark theme persistence via UserDefaults
- `HapticManager`: Centralized haptic feedback (impact, notification, selection)
- `NetworkMonitor`: Real-time connectivity monitoring with offline banner display
- `Constants`: API configuration with debug/release URL switching

**User Identification:**
- UUID generated on first launch, stored in UserDefaults (`device_user_id`)
- Sent as `X-User-ID` header on all API requests (matches React Native behavior)

### 4.6 Frontend Navigation (React Native)

Stack-based navigation using React Navigation:
```
ProductList (root)
├── ProductDetails
│   └── AIAssistant
├── Wishlist
├── Notifications
│   └── NotificationDetail
```

Deep linking support for web via Expo Linking.

### 4.7 iOS Navigation

NavigationStack-based routing via `NavigationRouter`:
```
ProductListView (root)
├── ProductDetailView
│   └── AIAssistantView
├── WishlistView
└── NotificationsView
```

---

## 5. Key Features

### 5.1 Server-Side Pagination
- Configurable page size and sorting
- Efficient database queries using Spring Data Pageable
- Load-more pattern in frontend with race condition protection

### 5.2 Dynamic Multi-Filter
- Combined category + search filtering at database level
- Case-insensitive search on product names
- Custom JPQL queries for optimized filtering

### 5.3 AI-Powered Insights
- **Review Summaries**: GPT-4o-mini analysis with sentiment detection
- **Chat Interface**: Question-based product review analysis
- **Caching**: 1-hour Caffeine cache to reduce API calls
- **Mock Mode**: Development-friendly fallback when no API key

### 5.4 Real-Time Statistics
- Global dashboard: total products, reviews, average rating
- Per-product rating breakdown (1-5 star distribution)
- Dynamic updates after review submission

### 5.5 User Experience Features
- Dark/light mode with system preference detection
- Offline detection with retry capability
- Multi-select mode with haptic feedback
- Search history persistence
- Optimistic UI updates

### 5.6 Review Voting System
- Toggle-based helpful voting
- Per-user vote tracking (prevents duplicates)
- Anonymous voting support

---

## 6. Configuration & Environment Management

### Backend Configuration

**application.properties:**
```properties
# Server
server.port=${PORT:8080}

# Database (H2 In-Memory)
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop

# OpenAI
openai.api.key=${OPENAI_API_KEY:your-api-key-here}
openai.model=gpt-4o-mini
openai.max.tokens=500

# Caching
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=1h

# Actuator (Health Checks)
management.endpoints.web.exposure.include=health,info
```

### Environment Variables

| Variable | Required | Purpose |
|----------|----------|---------|
| `PORT` | No | Server port (default: 8080) |
| `OPENAI_API_KEY` | No | AI features (falls back to mock) |
| `cors.allowed-origins` | No | Comma-separated CORS origins (default: localhost dev ports) |
| `rate-limit.requests-per-minute` | No | Rate limit per client (default: 60) |
| `spring.profiles.active` | No | Set to `prod` for production profile |
| `VERCEL_TOKEN` | CI | Vercel deployment auth |
| `VERCEL_ORG_ID` | CI | Vercel organization |
| `VERCEL_PROJECT_ID` | CI | Vercel project |

### Frontend Configuration (React Native)

**API Base URL:**
```typescript
const BASE_URL = 'https://product-review-app-ybmf.onrender.com';
```

**User Identification:**
- UUID generated and stored in AsyncStorage
- Passed via `X-User-ID` header on all requests

### Frontend Configuration (iOS Native)

**API Base URL** (`App/Core/Constants.swift`):
```swift
#if DEBUG
static let useLocalServer = true   // http://localhost:8080
#else
static let useLocalServer = false  // https://product-review-app-ybmf.onrender.com
#endif
```

**User Identification:**
- UUID generated and stored in UserDefaults (`device_user_id`)
- Passed via `X-User-ID` header on all requests

---

## 7. Deployment & Operations

### 7.1 Backend Deployment (Render.com)

**Multi-stage Docker Build:**
```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
EXPOSE 10000
CMD ["java", "-jar", "app.jar"]
```

**Render Configuration (render.yaml):**
- Docker-based deployment
- Health check: `/actuator/health`
- Environment: PORT=10000, JAVA_VERSION=17

### 7.2 Frontend Web Deployment (Vercel)

**GitHub Actions Workflow:**
- Triggers on `main` branch pushes to `mobile/`
- Node.js 20 with npm ci
- Deploys via `amondnet/vercel-action`

**Vercel Configuration:**
- SPA routing: All paths → `/index.html`
- Cache: 1-year immutable headers
- Output: `dist/` directory

### 7.3 Mobile App Builds (EAS)

**Build Channels:**
- `preview`: APK builds for testing
- `production`: AAB for Play Store

### 7.4 Operational Characteristics

| Aspect | Details |
|--------|---------|
| Cold Start | Backend: ~30-60s on Render free tier |
| Database | In-memory H2 (resets on restart) |
| Caching | AI summaries cached 1 hour |
| Health | `/actuator/health` monitored |
| Updates | OTA via Expo Updates |

---

## 8. Testing Strategy

### Backend Testing

**Unit Tests (ProductServiceTest):**
- JUnit 5 + Mockito
- Mocked repositories and services
- Tests: getAllProducts, getProductDTOById, addReview

**Integration Tests (ProductControllerIntegrationTest):**
- Spring Boot Test + MockMvc
- Full request/response cycle testing
- Validation error scenarios

**Test Execution:**
```bash
cd backend && ./mvnw test
```

### Frontend Testing (React Native)

Manual testing via:
- Expo Go on physical devices
- Android/iOS emulators
- Web browser (localhost:19006)

### iOS Native Testing

**Unit Tests (ProductReviewTests):**
- XCTest framework
- Tests for mappers (ReviewMapper field alignment with DTOs and domain models)
- Repository and ViewModel test scaffolding

**UI Tests (ProductReviewUITests):**
- XCUITest framework
- App launch and navigation flow tests

**Test Execution:**
```bash
# Via Xcode
xcodebuild test -scheme ProductReview -destination 'platform=iOS Simulator,name=iPhone 16 Pro'
```

---

## 9. UI Components

### Screen Components (React Native)

| Screen | Features |
|--------|----------|
| ProductListScreen | Pagination, search, filters, multi-select, global stats |
| ProductDetailsScreen | Product info, reviews, AI summary, rating breakdown |
| WishlistScreen | Paginated wishlist, batch operations, stats |
| AIAssistantScreen | Chat interface, pre-defined questions |
| NotificationsScreen | Filter chips, read/unread, management |

### Screen Components (iOS Native)

| Screen | Features |
|--------|----------|
| ProductListView | Pagination, search, category filter, global stats, pull-to-refresh |
| ProductDetailView | Product info, AI summary, rating breakdown, reviews, add review |
| WishlistView | Grid view, multi-select, batch delete, optimistic updates |
| NotificationsView | List, mark as read, delete, swipe actions |
| AIAssistantView | Chat interface, predefined questions, loading states |

### Reusable Components (React Native)

| Component | Purpose |
|-----------|---------|
| ProductCard | Grid-friendly product display |
| ReviewCard | Review with helpful voting |
| StarRating | 5-star rating display |
| RatingBreakdown | Rating distribution chart |
| SearchBar | Search with history dropdown |
| CategoryFilter | Horizontal category chips |
| AISummaryCard | AI summary with gradient |
| OfflineBanner | Network status indicator |
| Button | Multi-variant button component |

### Reusable Components (iOS Native)

| Component | Purpose |
|-----------|---------|
| EmptyStateView | Empty state with icon, title, subtitle, and action button |
| ShimmerView | Loading skeleton with shimmer animation |
| ToastView | Toast notifications (success, error, warning, info) |
| LoadingButton | Button with loading spinner state |
| AnimatedHeartButton | Wishlist heart with bounce animation |
| RatingStarsView | Display and interactive star ratings |
| ConfirmationDialog | Reusable confirmation dialogs |

### Theme System

**WCAG AA Compliant Color Palette:**

| Mode | Background | Primary | Accent |
|------|------------|---------|--------|
| Light | #FAFAFA | #0066FF | #4F46E5 |
| Dark | #0A0A0A | #3B82F6 | #C7D2FE |

**Design Tokens:**
- Spacing: xs(4), sm(8), md(12), lg(16), xl(20)...
- Border Radius: sm(6), md(8), lg(12), xl(16)
- Typography: xs(12)...4xl(36)
- Shadows: soft, hover variants

---

## 10. Performance & Scalability

### Backend Optimizations

| Strategy | Implementation |
|----------|----------------|
| Denormalized Stats | `averageRating`, `reviewCount` cached on Product |
| Caffeine Cache | AI summaries cached 100 items, 1-hour TTL |
| Pagination | Server-side with configurable page size |
| Lazy Loading | Reviews fetched separately from products |
| Query Optimization | Custom JPQL for combined filters |

### Frontend Optimizations (React Native)

| Strategy | Implementation |
|----------|----------------|
| AbortController | Race condition protection in ProductList |
| Debounced Search | 1-second delay before API calls |
| Optimistic Updates | Immediate UI feedback, backend sync |
| Memoization | useMemo/useCallback for expensive operations |
| Batched Rendering | Android 10-item max, 50ms batch period |

### Frontend Optimizations (iOS Native)

| Strategy | Implementation |
|----------|----------------|
| Image Caching | NSCache with 50MB limit, 100 item count limit |
| Exponential Backoff | Automatic retry with 1s → 2s → 4s delays on idempotent requests |
| Optimistic Updates | Immediate UI feedback with backend sync |
| @MainActor | All UI state updates dispatched to main thread |
| Connectivity Monitoring | NWPathMonitor with offline banner and auto-recovery |

### Scalability Considerations

- Database: H2 in-memory suitable only for demos; production needs PostgreSQL
- Caching: Caffeine is local; distributed cache (Redis) for multi-instance
- AI Calls: Rate-limited by caching; consider queue for high volume

---

## 11. Security Considerations

### Authentication & Authorization
- **Current**: Device-based UUID identification (no auth)
- **Planned**: JWT-based authentication and RBAC

### Input Validation & Error Handling
- Jakarta Bean Validation on DTOs (@NotBlank, @Size, @Min, @Max)
- Custom exception classes: `ResourceNotFoundException` (404), `ValidationException` (400), `UnauthorizedException` (401)
- Structured `ErrorResponse` DTO with timestamp, code, message, and details
- `GlobalExceptionHandler` maps each exception type to correct HTTP status

### API Security
- **CORS:** Centralized `CorsConfig.java` with environment-based allowed origins (no `@CrossOrigin` on controllers)
- **Rate Limiting:** Bucket4j filter — 60 requests/minute per client (keyed by X-User-ID or IP)
- No sensitive data exposed in DTOs
- Health endpoint restricted to authorized users

### Database Security
- H2 console enabled in development, **disabled in production** (`application-prod.properties`)
- Parameterized queries via JPA (SQL injection safe)

### Production Profile (`application-prod.properties`)
- H2 console disabled
- Actuator restricted to health endpoint only
- CORS origins restricted to production domains

### Container Security
- Alpine-based images (minimal attack surface)
- Non-root user recommended for production
- Secrets via environment variables (not in code)

---

## 12. Integration Points

### External APIs

| Service | Purpose | Integration |
|---------|---------|-------------|
| OpenAI (GPT-4o-mini) | Review analysis & chat | simple-openai library |
| Vercel Analytics | Web visitor tracking | @vercel/analytics |
| Vercel Speed Insights | Performance monitoring | @vercel/speed-insights |

### Internal Service Communication

```
Frontend (React Native)          Frontend (iOS Native)
    │                                │
    │ HTTP/REST (JSON)               │ HTTP/REST (JSON)
    │ X-User-ID Header              │ X-User-ID Header
    ▼                                ▼
Backend (Spring Boot) ◄──────────────┘
    │
    ├── ProductController ─┬─→ ProductService ─→ ProductRepository ─→ H2
    │                      └─→ AISummaryService ─→ OpenAI API
    │
    └── UserController ────→ UserService ─→ WishlistRepository
                                          └─→ NotificationRepository
```

### API Contract Summary

**Products:**
- `GET /api/products` - List (paginated, filterable)
- `GET /api/products/{id}` - Details with AI summary
- `GET /api/products/stats` - Global statistics
- `GET /api/products/{id}/reviews` - Reviews (paginated)
- `POST /api/products/{id}/reviews` - Submit review
- `PUT /api/products/reviews/{id}/helpful` - Toggle helpful
- `POST /api/products/{id}/chat` - AI chat

**User:**
- `GET /api/user/wishlist` - Wishlist IDs
- `GET /api/user/wishlist/products` - Wishlist products
- `POST /api/user/wishlist/{productId}` - Toggle wishlist
- `GET /api/user/notifications` - List notifications
- CRUD operations for notification management

---

## Appendix: Quick Reference

### Development Commands

```bash
# Backend
cd backend
./mvnw clean install      # Build
./mvnw spring-boot:run    # Run (localhost:8080)
./mvnw test               # Test

# Frontend (React Native)
cd mobile
npm install               # Install dependencies
npx expo start            # Dev server
npx expo start --web      # Web version
npm run build             # Production build

# Frontend (iOS Native)
cd ios
xcodegen generate         # Generate Xcode project from project.yml
open ProductReview.xcodeproj  # Open in Xcode (Cmd+R to build & run)
xcodebuild test -scheme ProductReview -destination 'platform=iOS Simulator,name=iPhone 16 Pro'
```

### Key File Locations

| Purpose | Path |
|---------|------|
| Backend Entry | `backend/src/main/java/.../ProductReviewApplication.java` |
| API Controllers | `backend/src/main/java/.../controller/` |
| Backend Config | `backend/src/main/resources/application.properties` |
| RN Frontend Entry | `mobile/App.tsx` |
| RN API Client | `mobile/src/services/api.ts` |
| RN Theme | `mobile/src/constants/theme.ts` |
| RN State Contexts | `mobile/src/context/` |
| iOS Entry | `ios/ProductReview/App/AppEntry/ProductReviewApp.swift` |
| iOS API Client | `ios/ProductReview/Data/Network/APIClient.swift` |
| iOS Constants | `ios/ProductReview/App/Core/Constants.swift` |
| iOS ViewModels | `ios/ProductReview/Presentation/ViewModels/` |
| iOS Project Config | `ios/project.yml` |

### Database Access (Development)

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

---

**Maintained by:** @MehmetBegun & Engineering Team
**Last Updated:** February 2026
