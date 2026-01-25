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

- **Framework**: Spring Boot 3.2.1
- **Backend-FW**: Solarity AI Backend Framework 1.0.0.0
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA
- **Mapping**: MapStruct 1.5.5
- **Caching**: Caffeine
- **Validation**: Jakarta Validation
- **Java**: 17

## Project Structure

```
src/main/java/com/solarityai/productreview/
├── entity/              # JPA entities (extends BaseEntity)
├── dto/                 # Data Transfer Objects (extends BaseDto)
├── mapper/              # MapStruct mappers
├── repository/          # Spring Data repositories
├── service/             # Business logic layer
│   └── impl/           # Service implementations
├── controller/          # REST controllers
├── config/              # Configuration classes
└── ProductReviewApplication.java
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- backend-fw framework installed in local Maven repository

### Installation

1. Navigate to the project directory:
```bash
cd ProductReviewApp-Backend-FW
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

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

The application uses backend-fw's global exception handling:
- All exceptions are caught and returned in a standard format
- Validation errors include field-level details
- 404 errors for not found resources
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

### Running Tests

```bash
mvn test
```

## License

This project is part of the Solarity AI ecosystem.
