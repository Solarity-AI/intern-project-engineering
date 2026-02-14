# Product Review App - Backend (with backend-fw)

A Spring Boot backend application for product reviews, built using the backend-fw framework.

## Features

- **Product Management**: Browse products with filtering by category and search
- **Review System**: Add reviews with ratings (1-5 stars)
- **Review Voting**: Users can vote reviews as helpful
- **Wishlist**: Users can save favorite products
- **Notifications**: User notification system
- **AI Features**: AI-generated review summaries and product chat
- **Statistics**: Product statistics with filtering support

## Technology Stack

- **Framework**: Spring Boot 4.0.1
- **Backend-FW**: Solarity AI Backend Framework 1.0.0.0
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA
- **Mapping**: MapStruct 1.5.5
- **Caching**: Caffeine
- **Validation**: Jakarta Validation
- **Java**: 21

## Framework Integration

### Dependency
Declared in `pom.xml`:
```xml
<dependency>
    <groupId>com.solarityai</groupId>
    <artifactId>backend-fw</artifactId>
    <version>1.0.0.0</version>
</dependency>
```

The framework source lives at `../backend-fw`. It must be installed in your local Maven repository before building this project (`cd ../backend-fw && mvn install`).

### Usage rules
1. **Import framework types directly** from `com.solarityai.backendfw.*`. Do not recreate framework patterns locally.
2. **Extend framework base classes** for entities (`BaseEntity`), DTOs (`BaseDto`), repositories (`BaseRepository`), and controllers (`BaseController`).
3. **Use framework query types** (`PageRequestDto`, `PageResponse`, `PageMetadata`) for pagination.
4. **Use framework exceptions** (`NotFoundException`) and error responses (`ApiErrorResponse`) for consistent error handling.

### Framework modules currently used

| Module | Types used | Files |
|--------|-----------|-------|
| `foundation.entity` | `BaseEntity` | 5 entities: `ProductEntity`, `ReviewEntity`, `ReviewVoteEntity`, `WishlistItemEntity`, `AppNotificationEntity` |
| `foundation.model` | `BaseDto`, `ApiErrorResponse` | 3 DTOs (`ProductDto`, `ReviewDto`, `AppNotificationDto`) + `GlobalExceptionHandler` |
| `foundation.repository` | `BaseRepository` | 5 repos: `ProductRepository`, `ReviewRepository`, `ReviewVoteRepository`, `WishlistItemRepository`, `AppNotificationRepository` |
| `foundation.controller` | `BaseController` | 2 controllers: `ProductController`, `UserController` |
| `query.model` | `PageRequestDto` | `ProductService`, `UserService`, `ProductServiceImpl`, `UserServiceImpl`, `ProductController`, `UserController` |
| `query.response` | `PageResponse`, `PageMetadata` | `ProductService`, `ProductServiceImpl`, `ProductController` |
| `exception` | `NotFoundException` | `ProductServiceImpl`, `UserServiceImpl`, `GlobalExceptionHandler` |

**Coverage**: 20 of 36 source files (56%) import from backend-fw.

### Known gaps (not yet adopted)

| Module | What it provides | Current approach |
|--------|-----------------|-----------------|
| `validation` | `ValidationResult`, `SpringValidatorEngine` | Jakarta `@Valid`/`@NotBlank` on DTOs directly |
| `cache` | `QueryCache`, `CachePolicy`, event-driven invalidation | Spring `@Cacheable` + Caffeine directly |
| `security` | JWT auth, `AuthService`, Spring Security integration | Custom `SecurityConfig.java` |
| `logger` | Structured logging | Standard SLF4J |
| `domain` | `DomainEvent`, `DomainEventPublisher` | No domain events |
| `metrics` | Micrometer-based framework metrics | Not used |
| `retry` | Circuit breaker, retry policies | Not used |
| `governance` | Feature flags, kill switches, audit | Not used |

## Project Structure

```
src/main/java/com/solarityai/productreview/
├── entity/              # JPA entities (extends BaseEntity)
├── dto/                 # Data Transfer Objects (extends BaseDto)
├── mapper/              # MapStruct mappers
├── repository/          # Spring Data repositories (extends BaseRepository)
├── service/             # Business logic interfaces
│   └── impl/           # Service implementations
├── controller/          # REST controllers (extends BaseController)
├── config/              # Configuration classes
└── ProductReviewApplication.java
```

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- backend-fw framework installed in local Maven repository

### Build & Run

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The server starts at `http://localhost:8080`.

### Running Tests

```bash
cd backend
mvn test
```

### H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`

- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (leave empty)

## API Endpoints

### Product Endpoints (`/api/products`)

- `GET /api/products/stats` - Get product statistics (with optional category and search filters)
- `GET /api/products` - List all products (paginated, with optional filters)
- `GET /api/products/{id}` - Get product by ID (includes AI summary and rating breakdown)
- `GET /api/products/{id}/reviews` - Get product reviews (paginated, with optional rating filter)
- `POST /api/products/{id}/reviews` - Add a review to a product
- `PUT /api/products/reviews/{reviewId}/helpful` - Toggle helpful vote on a review
- `GET /api/products/reviews/voted` - Get list of review IDs the user has voted
- `POST /api/products/{id}/chat` - Chat about product reviews with AI

### User Endpoints (`/api/user`)

**Wishlist:**
- `GET /api/user/wishlist` - Get user's wishlist product IDs (requires X-User-ID header)
- `GET /api/user/wishlist/products` - Get user's wishlist products (paginated)
- `POST /api/user/wishlist/{productId}` - Toggle product in wishlist

**Notifications:**
- `GET /api/user/notifications` - Get user's notifications
- `GET /api/user/notifications/unread-count` - Get unread notification count
- `PUT /api/user/notifications/{id}/read` - Mark notification as read
- `PUT /api/user/notifications/read-all` - Mark all notifications as read
- `POST /api/user/notifications` - Create a notification
- `DELETE /api/user/notifications/{id}` - Delete a notification
- `DELETE /api/user/notifications` - Delete all user notifications

## Configuration

Edit `src/main/resources/application.yml` to customize:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:testdb
  h2:
    console:
      enabled: true

openai:
  api:
    key: ${OPENAI_API_KEY:test-key}  # Set via environment variable
```

## Sample Data

On startup, the application automatically initializes with:
- 24 sample products across multiple categories
- Random reviews for each product (1-10 reviews)
- 30 additional reviews for iPhone 15 Pro (for pagination testing)

## User Identification

The API uses the `X-User-ID` header for user-specific operations (wishlist, notifications, voting).
The frontend should generate a UUID and send it with each request.

## AI Features

The application includes AI-powered features:
- **Review Summaries**: Automatically generated summaries of product reviews
- **Product Chat**: Ask questions about products and reviews

By default, the app runs in test mode with mock AI responses. To enable real AI:
1. Get an OpenAI API key
2. Set the `OPENAI_API_KEY` environment variable
3. Restart the application

## Error Handling

The application uses backend-fw's exception handling:
- `NotFoundException` mapped to HTTP 404 via `GlobalExceptionHandler`
- `ApiErrorResponse` provides a standard JSON error envelope
- Validation errors include field-level details
- Proper HTTP status codes for all scenarios

## Caching

AI summaries are cached using Caffeine:
- Cache size: 100 entries
- TTL: 1 hour
- Cache is cleared on application restart

## Development

### Adding a New Entity

1. Create entity in `entity/` package (extend `BaseEntity`)
2. Create DTOs in `dto/` package (extend `BaseDto` if needed)
3. Create MapStruct mapper in `mapper/` package
4. Create repository in `repository/` package (extend `BaseRepository`)
5. Create service interface and implementation in `service/`
6. Create controller in `controller/` package (extend `BaseController`)

## License

This project is part of the Solarity AI ecosystem.
