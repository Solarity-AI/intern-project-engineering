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
│   │   │   │   │   └── ReviewDTO.java
│   │   │   │   ├── config/           # Configuration
│   │   │   │   │   └── CacheConfig.java
│   │   │   │   └── exception/        # Error handling
│   │   │   │       └── GlobalExceptionHandler.java
│   │   │   └── resources/
│   │   │       └── application.properties
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
└── README.md                         # Main documentation
```

### Architectural Patterns

**Backend**: Layered Clean Architecture
```
Controller → Service → Repository → Entity
     ↓           ↓           ↓
    DTO    Business Logic   JPA/Hibernate
```

**Frontend**: Context-based State Management
```
App → Providers → Navigator → Screens → Components
         ↓
    Context (Theme, Wishlist, Notifications, Search, Toast, Network)
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

### 4.5 Frontend Navigation

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
| `VERCEL_TOKEN` | CI | Vercel deployment auth |
| `VERCEL_ORG_ID` | CI | Vercel organization |
| `VERCEL_PROJECT_ID` | CI | Vercel project |

### Frontend Configuration

**API Base URL:**
```typescript
const BASE_URL = 'https://product-review-app-ybmf.onrender.com';
```

**User Identification:**
- UUID generated and stored in AsyncStorage
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

### Frontend Testing

Manual testing via:
- Expo Go on physical devices
- Android/iOS emulators
- Web browser (localhost:19006)

---

## 9. UI Components

### Screen Components

| Screen | Features |
|--------|----------|
| ProductListScreen | Pagination, search, filters, multi-select, global stats |
| ProductDetailsScreen | Product info, reviews, AI summary, rating breakdown |
| WishlistScreen | Paginated wishlist, batch operations, stats |
| AIAssistantScreen | Chat interface, pre-defined questions |
| NotificationsScreen | Filter chips, read/unread, management |

### Reusable Components

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

### Frontend Optimizations

| Strategy | Implementation |
|----------|----------------|
| AbortController | Race condition protection in ProductList |
| Debounced Search | 1-second delay before API calls |
| Optimistic Updates | Immediate UI feedback, backend sync |
| Memoization | useMemo/useCallback for expensive operations |
| Batched Rendering | Android 10-item max, 50ms batch period |

### Scalability Considerations

- Database: H2 in-memory suitable only for demos; production needs PostgreSQL
- Caching: Caffeine is local; distributed cache (Redis) for multi-instance
- AI Calls: Rate-limited by caching; consider queue for high volume

---

## 11. Security Considerations

### Authentication & Authorization
- **Current**: Device-based UUID identification (no auth)
- **Planned**: JWT-based authentication and RBAC

### Input Validation
- Jakarta Bean Validation on DTOs
- @NotBlank, @Size, @Min, @Max annotations
- Global exception handler returns structured errors

### API Security
- CORS: `origins = "*"` (development; restrict in production)
- No sensitive data exposed in DTOs
- Health endpoint restricted to authorized users

### Database Security
- H2 console enabled (development only)
- Parameterized queries via JPA (SQL injection safe)

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
Frontend (React Native)
    │
    │ HTTP/REST (JSON)
    │ X-User-ID Header
    ▼
Backend (Spring Boot)
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

# Frontend
cd mobile
npm install               # Install dependencies
npx expo start            # Dev server
npx expo start --web      # Web version
npm run build             # Production build
```

### Key File Locations

| Purpose | Path |
|---------|------|
| Backend Entry | `backend/src/main/java/.../ProductReviewApplication.java` |
| API Controllers | `backend/src/main/java/.../controller/` |
| Backend Config | `backend/src/main/resources/application.properties` |
| Frontend Entry | `mobile/App.tsx` |
| API Client | `mobile/src/services/api.ts` |
| Theme | `mobile/src/constants/theme.ts` |
| State Contexts | `mobile/src/context/` |

### Database Access (Development)

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

---

**Maintained by:** @MehmetBegun & Engineering Team
**Last Updated:** February 2026
