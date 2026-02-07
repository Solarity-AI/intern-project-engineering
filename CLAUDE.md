# CLAUDE.md

## Project Overview

**Product Review Application** - A full-stack cross-platform ecosystem for product reviews with AI-powered insights. Users can browse products, manage wishlists, submit reviews, and interact with an AI assistant for review analysis.

## Tech Stack

### Backend (Java/Spring Boot)
- **Framework:** Spring Boot 3.2.1
- **JDK:** Java 17
- **Database:** H2 (in-memory)
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
- **Backend:** Render.com (Docker)
- **Web:** Vercel
- **Mobile:** EAS Build

## Directory Structure

```
.
├── backend/                    # Spring Boot REST API
│   ├── src/main/java/com/example/productreview/
│   │   ├── controller/         # REST endpoints
│   │   ├── service/            # Business logic
│   │   ├── model/              # JPA entities
│   │   ├── repository/         # Data access
│   │   ├── dto/                # Data transfer objects
│   │   ├── config/             # CORS, caching, rate limiting
│   │   └── exception/          # Custom exceptions & global handler
│   └── src/main/resources/     # Configuration (+ application-prod.properties)
│
├── mobile/                     # React Native app
│   ├── src/
│   │   ├── screens/            # Screen components
│   │   ├── components/         # Reusable UI
│   │   ├── context/            # State providers
│   │   ├── services/           # API client
│   │   ├── constants/          # Theme & config
│   │   └── types/              # TypeScript definitions
│   └── App.tsx                 # Entry point
│
├── .vscode/                    # VS Code configuration
├── render.yaml                 # Backend deployment config
└── Procfile                    # Heroku-style process file
```

## Development Commands

### Backend Setup & Run
```bash
cd backend
./mvnw clean install           # Build
./mvnw spring-boot:run         # Run (localhost:8080)
./mvnw test                    # Run tests
```

### Frontend Setup & Run
```bash
cd mobile
npm install                    # Install dependencies
npx expo start                 # Start dev server
npx expo start --web           # Web version
npx expo start --android       # Android emulator
```

### Build & Deploy
```bash
# Backend (Render.com handles via render.yaml)
cd backend && ./mvnw clean package

# Frontend Web (Vercel)
cd mobile && npm run build

# Mobile (EAS)
cd mobile && eas build --platform android
```

## API Endpoints

### Products
- `GET /api/products` - List products (paginated, filterable by category/search)
- `GET /api/products/{id}` - Product details with AI summary
- `GET /api/products/stats` - Global statistics

### Reviews
- `GET /api/products/{id}/reviews` - Product reviews (paginated)
- `POST /api/products/{id}/reviews` - Submit review
- `PUT /api/products/reviews/{id}/helpful` - Mark helpful

### AI
- `POST /api/products/{id}/chat` - AI chat about product

### User
- `GET /api/users/wishlist` - Get wishlist
- `POST /api/users/wishlist/{productId}` - Toggle wishlist
- `GET /api/users/notifications` - Get notifications

## Key Files

### Backend
- `backend/src/main/java/.../controller/ProductController.java` - Main API
- `backend/src/main/java/.../service/ProductServiceImpl.java` - Business logic
- `backend/src/main/java/.../service/AISummaryService.java` - OpenAI integration
- `backend/src/main/java/.../config/CorsConfig.java` - Centralized CORS (environment-based origins)
- `backend/src/main/java/.../config/RateLimitingFilter.java` - Bucket4j rate limiting (per client)
- `backend/src/main/java/.../exception/GlobalExceptionHandler.java` - Error handling with proper HTTP status codes
- `backend/src/main/java/.../dto/ErrorResponse.java` - Structured error responses
- `backend/src/main/resources/application.properties` - Configuration
- `backend/src/main/resources/application-prod.properties` - Production overrides

### Frontend
- `mobile/src/services/api.ts` - API client
- `mobile/src/context/` - State management (Theme, Wishlist, Notifications)
- `mobile/src/screens/ProductListScreen.tsx` - Main product list
- `mobile/src/constants/theme.ts` - Color palette & design tokens

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
- `spring.profiles.active=prod` - Activate production profile (disables H2 console, restricts actuator)

### Frontend
- API base URL configured in `mobile/src/services/api.ts`
- Currently: `https://product-review-app-ybmf.onrender.com`

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

- Backend uses in-memory H2 database (data resets on restart)
- AI summaries are cached for 1 hour (Caffeine)
- User persistence via device ID (X-User-ID header)
- Frontend supports dark/light mode with system preference detection
- H2 console is disabled in production profile (`application-prod.properties`)
- Rate limiting: 60 requests/minute per client (keyed by X-User-ID or IP)
- CORS: configured via properties, not controller annotations
