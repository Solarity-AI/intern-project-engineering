# 📱 Product Review Full-Stack Application

Welcome to the **Product Review Application**! This project is a comprehensive full-stack ecosystem designed to demonstrate modern software architecture, clean code principles, and seamless cross-platform integration.

The system allows users to explore products, manage wishlists, interact with an AI Assistant for review analysis, and submit real-time feedback.

---

## 🚀 Quick Start (Onboarding Guide)

Follow these steps to get the entire environment running on your local machine from scratch.

### 1. Prerequisites
Ensure you have the following installed:
*   **Java JDK 17+**
*   **Node.js 20+** & **npm**
*   **Git**
*   **Android Studio / Xcode 15+** (for mobile emulation) or **Expo Go** on a physical device.
*   **XcodeGen** (for iOS native app): `brew install xcodegen`

### 2. Backend Setup
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```
*   **API Base URL:** `http://localhost:8080`
*   **H2 Console:** `http://localhost:8080/h2-console` (User: `sa`, Password: [empty])

### 3. Frontend Setup (Mobile)

#### Android
- Kotlin
- Jetpack Compose
- MVVM + StateFlow
- Material 3

See: `README-Android-Kotlin-FINAL.md`

#### iOS (Native Swift App)
- Swift 5.9+ / SwiftUI
- MVVM + Clean Architecture
- Swift Concurrency (async/await, MainActor)
- Minimum iOS 17.0
- XcodeGen for project generation

See: [`ios/README.md`](ios/README.md) for full setup and architecture details.

```bash
cd mobile
npm install
npx expo start
```
*   Press **'w'** for Web version.
*   Press **'a'** for Android Emulator.
*   Scan the QR code with **Expo Go** for physical device testing.

### 4. iOS Native App Setup
```bash
cd ios
xcodegen generate              # Generate Xcode project from project.yml
open ProductReview.xcodeproj   # Open in Xcode
# Select target device (iPhone 16 Pro recommended) → Cmd+R to build & run
```
*   The app defaults to the deployed backend in all builds. Set `useLocalServer = true` in `Constants.swift` for local development.
*   See [`ios/README.md`](ios/README.md) for detailed architecture and feature documentation.

---

## 🗄️ Database Configuration

### Flyway Migrations and Spring Profiles

The backend uses **Flyway** for schema management in production and **H2 in-memory** auto-DDL in development. The active Spring profile controls which strategy applies:

- **Development (default profile):** `spring.flyway.enabled=false` and `spring.jpa.hibernate.ddl-auto=create-drop` in `backend/src/main/resources/application.properties`. Hibernate recreates the schema from JPA entity definitions on every startup — no migration files are executed. The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, username: `sa`, password empty).

- **Production profile (`prod`):** `spring.flyway.enabled=true` and `spring.jpa.hibernate.ddl-auto=none` in `backend/src/main/resources/application-prod.properties`. Hibernate does **not** touch the schema; Flyway exclusively manages it by applying versioned SQL files in order from `backend/src/main/resources/db/migration/` (`V1__initial_schema.sql` → `V2__seed_data.sql` → `V3__add_user_mappings.sql`). Any future schema change must be expressed as a new `V4__...sql` file — editing existing migration files causes a Flyway checksum error and prevents startup.

The `application.properties` keys controlling the datasource and Flyway behaviour:

| Property | Development default | Production (`prod` profile) |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:mem:testdb` | `${JDBC_DATABASE_URL}` (env var) |
| `spring.datasource.driverClassName` | *(H2, auto-detected)* | `org.postgresql.Driver` |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | `none` |
| `spring.flyway.enabled` | `false` | `true` |
| `spring.flyway.baseline-on-migrate` | *(not set)* | `true` |

### Switching to a Different Database Engine

To run the application against a **local PostgreSQL instance** (or another JDBC-compatible database), follow these steps:

1. **Ensure the JDBC driver is on the classpath.** PostgreSQL (`org.postgresql:postgresql`) is already included in `backend/pom.xml`. For MySQL, add `com.mysql:mysql-connector-j`; for other vendors, add the corresponding driver dependency.

2. **Create a new Spring profile properties file** in `backend/src/main/resources/`, for example `application-local-pg.properties`, and set the datasource and Flyway keys:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/productreview
   spring.datasource.username=youruser
   spring.datasource.password=yourpassword
   spring.datasource.driverClassName=org.postgresql.Driver
   spring.jpa.hibernate.ddl-auto=none
   spring.flyway.enabled=true
   spring.flyway.baseline-on-migrate=true
   ```

3. **Start the application** with the new profile active:
   ```bash
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local-pg
   ```
   Flyway will apply all migrations from `V1` through the latest version against the target database on first startup. Verify by inspecting the `flyway_schema_history` table afterwards.

4. **For MySQL or other non-PostgreSQL vendors:** The migration SQL files use PostgreSQL-specific syntax (`BIGSERIAL`, `NUMERIC`, `NOW()`). Replace these with vendor equivalents — for example, `BIGINT AUTO_INCREMENT` instead of `BIGSERIAL` and `CURRENT_TIMESTAMP` instead of `NOW()` — or configure `spring.flyway.locations` to point at a vendor-specific migration subdirectory.

> **Note:** The H2 development profile intentionally bypasses Flyway. Schema correctness is only validated against PostgreSQL-compatible SQL. Always test new migrations against a real PostgreSQL instance (local Docker or a staging database) before merging to `main`.

---

## 🏗️ Technical Architecture

The project follows a **Layered Clean Architecture** to ensure maintainability and testability.

### Backend (Spring Boot)
*   **Controller Layer:** Versioned REST API (`/api/v1/`) with full OpenAPI/Swagger documentation and DTO mapping.
*   **Service Layer:** Business logic encapsulation with Dependency Inversion.
*   **Data Layer:** Spring Data JPA with optimized SQL queries for filtering and aggregation.
*   **Database:** H2 in-memory (dev) with PostgreSQL readiness via Flyway migrations (prod). HikariCP connection pooling.
*   **API Documentation:** Swagger UI at `/swagger-ui.html` (development only, disabled in production).
*   **Security:** Centralized CORS configuration, Bucket4j rate limiting (60 req/min per client), production profile with H2 console and Swagger UI disabled.
*   **Error Handling:** Custom exception classes with structured `ErrorResponse` (timestamp, code, message, details).

### Frontend — React Native (Mobile/Web)
*   **State Management:** React Context API for Wishlist, Search, and Notifications.
*   **Responsive Design:** Adaptive layouts for Mobile (Android/iOS) and Web (Heroku).
*   **Networking:** Centralized API service with retry logic (exponential backoff), request deduplication, memory caching, and structured error handling.
*   **UI Design:** Glassmorphism theme with Glass cards, Gradients, and Glow effects.

### Frontend — iOS Native (Swift/SwiftUI)
*   **Architecture:** MVVM with Clean Architecture (Domain, Data, Presentation layers).
*   **Networking:** URLSession with async/await, exponential backoff retry, and connectivity monitoring.
*   **UI Components:** Reusable components including shimmer loading, toast notifications, animated heart buttons, and rating stars.
*   **Utilities:** Haptic feedback, theme management, image caching (NSCache, 50MB), and search history persistence.

---

## 🧩 Key Features

### 🛒 Advanced Product Management
*   **Server-Side Pagination:** Efficiently handles large datasets for both main product list and user wishlist.
*   **Dynamic Multi-Filter:** Search by name and filter by category simultaneously at the database level.
*   **Global Statistics:** Real-time dashboard showing total reviews and average ratings across the platform.

### 🤖 AI-Powered Insights
*   **AI Assistant:** Interactive chat interface to ask specific questions about product reviews.
*   **AI Summary:** Automated sentiment analysis and summary of user feedback.

### 👤 User Experience (UX)
*   **Search History:** Persistent search overlay for quick access to previous queries.
*   **Multi-Select Wishlist:** Batch actions for managing favorite products.
*   **Auto-Refresh:** Real-time UI updates immediately after submitting a review without manual reload.
*   **Dark Mode:** System-wide theme support with persistent user preference.

---

## 📂 Project Structure

```text
.
├── backend/                # Java Spring Boot Source Code
│   ├── src/main/java/      # Business logic & API Controllers
│   ├── src/main/resources/
│   │   ├── db/migration/   # Flyway SQL migrations (V1 schema, V2 seed data, V3 user mappings)
│   │   └── *.properties    # Dev & prod configuration
│   └── README.md           # Detailed Backend Documentation
├── mobile/                 # React Native (Expo) Source Code
│   ├── src/components/     # Reusable UI Components
│   ├── src/screens/        # Screen-level Components
│   ├── src/context/        # Global State Management
│   └── Procfile             # Heroku process definition
├── ios/                    # Native iOS App (Swift/SwiftUI)
│   ├── ProductReview/      # Main app source
│   │   ├── App/            # App entry, navigation, core utilities
│   │   ├── Data/           # Network, local storage, mappers, repositories
│   │   ├── Domain/         # Models & repository protocols
│   │   └── Presentation/   # Views, ViewModels, reusable components
│   ├── ProductReviewTests/ # Unit tests
│   ├── ProductReviewUITests/ # UI tests
│   ├── project.yml         # XcodeGen configuration
│   └── README.md           # iOS-specific documentation
└── README.md               # Main Project Entry Point
```

---

## 🌐 Deployment

### Production Environment
*   **Backend:** Heroku (Java buildpack, PostgreSQL addon)
*   **Frontend Web:** Heroku (static SPA via `serve`)
*   **Mobile App:** Distributed via **EAS Build (APK)** with **OTA Updates** support.

### Why Heroku?
- ✅ Single platform for both backend and frontend
- ✅ Automatic HTTPS
- ✅ GitHub Actions integration for auto-deploy on push to `main`
- ✅ Managed PostgreSQL addon
- ✅ Health check support
- ✅ Easy environment variable management

### Deployment Workflow
The project includes automated CI/CD via GitHub Actions:
- Push to `main` branch triggers parallel deployment of backend and frontend to Heroku
- Manual deployment available via `workflow_dispatch`
- See `.github/workflows/deploy-heroku.yml` for workflow configuration

### Quick Deploy Commands
```bash
# Heroku apps are deployed automatically via GitHub Actions
# For manual Heroku CLI usage:
heroku logs --tail --app <backend-app-name>    # View backend logs
heroku logs --tail --app <frontend-app-name>   # View frontend logs
```

### Color Palette (Updated 2026-02)
The application features a premium dark SaaS design with glassmorphism effects:

**Dark Mode (Default):**
- Background: `#0B1120` (Deep navy)
- Primary: `#10B981` (Emerald green)
- Accent: `#FBBF24` (Golden yellow)
- Glass cards with `backdrop-filter: blur()` for web

**Light Mode:**
- Background: `#F8FAFC` (Soft slate)
- Primary: `#059669` (Emerald)
- Accent: `#D97706` (Amber)

Theme exports: `Colors`, `Gradients`, `Glass`, `Glow`, `Shadow`, `Spacing`, `FontSize`, `BorderRadius`.

---

## 🎓 Internship Assignment

Future interns are expected to:
1.  Understand the **Backend API** provided in this repository.
2.  Implement a **Native Frontend** (iOS/Swift or Android/Kotlin) that matches the features of the React Native reference implementation.
3.  Refer to `mobile/README-iOS-Swift.md` or `mobile/README-Android-Kotlin.md` for specific requirements.

---

## 🛠️ Troubleshooting

*   **Port 8080 Conflict:** If the backend fails to start, check if another process is using port 8080.
*   **Network Issues:** Ensure the `BASE_URL` in `mobile/src/services/api.ts` matches your backend IP (use local IP for physical devices).
*   **Heroku 404 on Refresh:** SPA routing is handled by `serve -s` in `mobile/Procfile`, which rewrites all 404s to `/index.html`.
*   **iOS — XcodeGen:** If the `.xcodeproj` is missing, install XcodeGen (`brew install xcodegen`) and run `xcodegen generate` inside the `ios/` directory.
*   **iOS — Simulator:** Requires Xcode 15.0+ and macOS Sonoma (14.0+). Target device must be iOS 17.0+.

---

## 📦 Deliverables

The final submission must include the following items:

1. **System Architecture:** An [Excalidraw link] explaining the overall system design.
2. **Frontend Code Walkthrough:** A 3–5 minute demo video [Google Drive Link] explaining the frontend codebase.
3. **Backend Code Walkthrough:** A 3–5 minute demo video [Google Drive Link] explaining the backend architecture.
4. **Application Demo:** A 3–5 minute video [Google Drive Link] showcasing all features on an emulator or real device.
5. **Build Artifacts:** A [Google Drive Link] to download the generated APK (Android) or IPA (iOS).
6. **Web Access:** A public web application link (e.g., Heroku) for testing in a browser.
7. **Future Improvements:** A section describing potential enhancements (see Roadmap below).
8. **Final Presentation:** A slide deck summarizing the project and learnings.

**Maintained by:** @MehmetBegun & Engineering Team
**Last Updated:** February 2026
