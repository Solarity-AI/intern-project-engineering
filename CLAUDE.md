# CLAUDE.md

## Project Overview

**Product Review Application** - A full-stack cross-platform ecosystem for product reviews with AI-powered insights. Users can browse products, manage wishlists, submit reviews, and interact with an AI assistant for review analysis.

## Tech Stack

### Backend (Java/Spring Boot)
- **Framework:** Spring Boot 3.2.1
- **JDK:** Java 17
- **Database:** H2 (in-memory, dev) / PostgreSQL (prod)
- **Migrations:** Flyway
- **ORM:** Spring Data JPA
- **AI:** OpenAI GPT-4o-mini via simple-openai
- **Caching:** Caffeine
- **Rate Limiting:** Bucket4j
- **Build:** Maven

### Frontend (React Native/Expo)
- **Framework:** React Native 0.81.5 + Expo 54
- **Language:** TypeScript 5.9
- **Navigation:** React Navigation 7
- **State:** React Context API
- **Storage:** AsyncStorage

### Deployment
- **Backend:** TBD (deployment platform migration in progress)
- **Web:** TBD (deployment platform migration in progress)
- **Mobile:** EAS Build

## Directory Structure

```
.
├── backend/                    # Spring Boot REST API
│   ├── src/main/java/com/example/productreview/
│   │   ├── controller/         # REST endpoints (/api/v1/)
│   │   ├── service/            # Business logic
│   │   ├── model/              # JPA entities
│   │   ├── repository/         # Data access
│   │   ├── dto/                # Data transfer objects
│   │   ├── config/             # CORS, caching, rate limiting, OpenAPI
│   │   └── exception/          # Custom exceptions & global handler
│   └── src/main/resources/     # Configuration, Flyway migrations
│
├── mobile/                     # React Native app (Expo)
│   ├── src/
│   │   ├── screens/            # Screen components
│   │   ├── components/         # Reusable UI
│   │   ├── context/            # State providers
│   │   ├── services/           # API client
│   │   ├── constants/          # Theme & config
│   │   ├── hooks/              # Custom hooks (useDebounce, ViewModels)
│   │   └── types/              # TypeScript definitions
│   └── App.tsx                 # Entry point
│
├── ios/                        # Native iOS App (Swift/SwiftUI)
│   ├── ProductReview/
│   │   ├── App/                # Entry, navigation, core utilities
│   │   ├── Data/               # Network, repositories, mappers
│   │   ├── Domain/             # Models & repository protocols
│   │   └── Presentation/       # Views, ViewModels, components
│   └── project.yml             # XcodeGen configuration
│
├── swift-issues/               # iOS UI redesign issue tracking
├── .vscode/                    # VS Code configuration
├── .github/workflows/          # CI/CD
└── Procfile                    # (removed)
```

## Development Commands

### Backend Setup & Run
```bash
cd backend
./mvnw clean install           # Build
./mvnw spring-boot:run         # Run (localhost:8080)
./mvnw test                    # Run tests
```

### Frontend Setup & Run (React Native)
```bash
cd mobile
npm install                    # Install dependencies
npx expo start                 # Start dev server
npx expo start --web           # Web version
npx expo start --android       # Android emulator
```

### iOS Native Setup & Run
```bash
cd ios
xcodegen generate              # Generate Xcode project from project.yml
open ProductReview.xcodeproj   # Open in Xcode, Cmd+R to build & run
```

### Build & Deploy
```bash
# Backend
cd backend && ./mvnw clean package

# Frontend Web
cd mobile && npm run build

# Mobile (EAS)
cd mobile && eas build --platform android
```

## API Endpoints

All endpoints are versioned under `/api/v1/`. Swagger UI available at `/swagger-ui.html` (dev only).

### Products
- `GET /api/v1/products` - List products (paginated, filterable by category/search)
- `GET /api/v1/products/{id}` - Product details with AI summary
- `GET /api/v1/products/stats` - Global statistics

### Reviews
- `GET /api/v1/products/{id}/reviews` - Product reviews (paginated)
- `POST /api/v1/products/{id}/reviews` - Submit review
- `PUT /api/v1/products/reviews/{id}/helpful` - Mark helpful

### AI
- `POST /api/v1/products/{id}/chat` - AI chat about product

### User
- `GET /api/v1/user/wishlist` - Get wishlist IDs
- `GET /api/v1/user/wishlist/products` - Get wishlist products (paginated)
- `POST /api/v1/user/wishlist/{productId}` - Toggle wishlist
- `GET /api/v1/user/notifications` - Get notifications

## Key Files

### Backend
- `backend/src/main/java/.../controller/ProductController.java` - Main API
- `backend/src/main/java/.../service/ProductServiceImpl.java` - Business logic
- `backend/src/main/java/.../service/AISummaryService.java` - OpenAI integration
- `backend/src/main/java/.../config/CorsConfig.java` - Centralized CORS (environment-based origins)
- `backend/src/main/java/.../config/OpenApiConfig.java` - Swagger/OpenAPI configuration
- `backend/src/main/java/.../config/RateLimitingFilter.java` - Bucket4j rate limiting (per client)
- `backend/src/main/java/.../exception/GlobalExceptionHandler.java` - Error handling with proper HTTP status codes
- `backend/src/main/java/.../dto/ErrorResponse.java` - Structured error responses
- `backend/src/main/resources/application.properties` - Configuration
- `backend/src/main/resources/application-prod.properties` - Production overrides
- `backend/src/main/resources/db/migration/` - Flyway SQL migrations

### Frontend (React Native)
- `mobile/src/services/api.ts` - API client (retry, cache, dedup, structured errors, mutation cache invalidation)
- `mobile/src/context/` - State management (Theme, Wishlist, Notifications, Network)
- `mobile/src/screens/ProductListScreen.tsx` - Main product list
- `mobile/src/constants/theme.ts` - Theme tokens (Colors, Gradients, Glass, Glow, Shadow)
- `mobile/src/screens/useProductListViewModel.ts` - Product list business logic
- `mobile/src/screens/useProductDetailViewModel.ts` - Product detail business logic

### iOS Native
- `ios/ProductReview/App/Core/Constants.swift` - API config (debug defaults to remote server)
- `ios/ProductReview/App/Core/AppTheme.swift` - Design system (colors, glass, glow)
- `ios/ProductReview/Data/Network/APIClient.swift` - Networking with async/await
- `ios/ProductReview/Presentation/ViewModels/` - ObservableObject ViewModels
- `ios/ProductReview/Presentation/Views/` - SwiftUI screens

## Coding Conventions

### Java (Backend)
- Use Lombok for boilerplate reduction (@Data, @AllArgsConstructor)
- DTOs for API responses, entities for persistence
- Service interfaces with Impl classes
- Custom JPA queries in repositories
- Custom exception classes (ResourceNotFoundException, ValidationException, UnauthorizedException)
- Global exception handler with structured ErrorResponse (timestamp, code, message, details)
- Centralized CORS config via `CorsConfig.java` (no `@CrossOrigin` on controllers)

### TypeScript (Frontend)
- Functional components with hooks
- Context API for global state
- Centralized API service with typed responses
- Theme constants for consistent styling
- 2-space indentation

## Environment Variables

### Backend
- `PORT` - Server port (default: 8080)
- `OPENAI_API_KEY` - OpenAI API key for AI features
- `cors.allowed-origins` - Comma-separated allowed CORS origins (default: localhost dev ports)
- `rate-limit.requests-per-minute` - Rate limit per client (default: 60)
- `spring.profiles.active=prod` - Activate production profile (PostgreSQL, Flyway, restricted actuator)
- `JDBC_DATABASE_URL` - PostgreSQL JDBC URL (prod only, provided by hosting platform)
- `CORS_ALLOWED_ORIGINS` - Comma-separated allowed CORS origins (prod)

### Frontend
- `EXPO_PUBLIC_API_URL` - Backend API URL (set via hosting platform or GitHub Actions secret)
- API base URL exported from `mobile/src/services/api.ts` (`export const BASE_URL`)
- Shared by `NetworkContext.tsx` for health checks

## Testing

### Backend
```bash
cd backend && ./mvnw test
```

### H2 Console (Development)
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- Username: sa
- Password: password

## Notes

- Dev profile uses in-memory H2 (`ddl-auto=create-drop`, Flyway disabled, data resets on restart)
- Prod profile uses PostgreSQL (`ddl-auto=validate`, Flyway enabled, persistent data)
- AI summaries are cached for 1 hour (Caffeine)
- User persistence via device ID (X-User-ID header)
- Frontend defaults to dark mode with glassmorphism UI (Glass cards, Gradients, Glow effects)
- GET cache is automatically invalidated after successful mutations (postReview, markReviewAsHelpful, toggleWishlist)
- H2 console is disabled in production profile (`application-prod.properties`)
- Rate limiting: 60 requests/minute per client (keyed by X-User-ID or IP)
- CORS: configured via properties, not controller annotations
