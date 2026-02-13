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
*   The app auto-connects to the deployed backend in release builds and `localhost:8080` in debug builds.
*   See [`ios/README.md`](ios/README.md) for detailed architecture and feature documentation.

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
*   **Responsive Design:** Adaptive layouts for Mobile (Android/iOS) and Web (Vercel).
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
│   │   ├── db/migration/   # Flyway SQL migrations (V1 schema, V2 seed data)
│   │   └── *.properties    # Dev & prod configuration
│   └── README.md           # Detailed Backend Documentation
├── mobile/                 # React Native (Expo) Source Code
│   ├── src/components/     # Reusable UI Components
│   ├── src/screens/        # Screen-level Components
│   ├── src/context/        # Global State Management
│   └── vercel.json         # Web Deployment Configuration
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
*   **Mobile App:** Distributed via **EAS Build (APK)** with **OTA Updates** support.

### Why Render.com + Vercel?
**Render.com** (Backend):
- ✅ Completely free (750 hours/month)
- ✅ Automatic HTTPS
- ✅ GitHub integration
- ✅ Health check support
- ⚠️ Cold start (~30-60s for first request)

**Vercel** (Frontend):
- ✅ Completely free
- ✅ Global CDN
- ✅ Automatic deployments
- ✅ Edge functions
- ✅ Zero configuration

### Deployment Workflow
The project includes automated CI/CD integration:
- Push to `main` branch triggers automatic Vercel production deployment
- Backend updates deploy automatically via Render.com GitHub integration
- Pull requests create preview deployments for testing
- See deployment guides for detailed instructions

### Quick Deploy Commands
**Backend (Render.com):**
```bash
# Already configured with render.yaml
# Just connect your GitHub repo on render.com dashboard
```

**Frontend (Vercel):**
```bash
cd mobile
npm install -g vercel  # Install Vercel CLI
vercel login           # Login to Vercel
vercel --prod          # Deploy to production
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
*   **Vercel 404 on Refresh:** Fixed via `vercel.json` rewrites. If issues persist, ensure the file is in the `mobile/` root.
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
6. **Web Access:** A public web application link (e.g., Vercel) for testing in a browser.
7. **Future Improvements:** A section describing potential enhancements (see Roadmap below).
8. **Final Presentation:** A slide deck summarizing the project and learnings.

**Maintained by:** @MehmetBegun & Engineering Team
**Last Updated:** February 2026
