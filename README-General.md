# 📱 Product Review Application

**Backend:** Spring Boot (Java)  
**Frontend:** React Native (Expo) - *Reference Implementation*  
**Database:** H2 (Dev) / PostgreSQL (Prod)  
**Deployment:** Heroku (Backend) & Vercel (Web)

---

## 📌 Project Overview

The **Product Review Application** is a full-stack system designed to demonstrate modern software architecture patterns. It allows users to browse products, filter by categories, view detailed reviews with AI-generated summaries, and submit their own feedback.

The project emphasizes **clean architecture, REST API design, performance optimization (pagination, server-side filtering), and cross-platform development**.

---

## 🎓 For Future Interns

**Assignment:** You must choose and implement **only one** of the following frontend stacks:

1.  **iOS (Swift):** See [README-iOS-Swift.md](./README-iOS-Swift.md) for requirements.
2.  **Android (Kotlin):** See [README-Android-Kotlin.md](./README-Android-Kotlin.md) for requirements.

> **Note:** The current `mobile/` folder contains a **React Native** implementation. This serves as a **reference** for how the UI should look and how to consume the Backend API. You are expected to build a native version (Swift or Kotlin) matching these features.

---

## 📦 Deliverables

The final submission must include the following items:

1.  **System Architecture:** An [Excalidraw link] explaining the overall system design.
2.  **Frontend Code Walkthrough:** A 3–5 minute demo video [Google Drive Link] explaining the frontend codebase.
3.  **Backend Code Walkthrough:** A 3–5 minute demo video [Google Drive Link] explaining the backend architecture.
4.  **Application Demo:** A 3–5 minute video [Google Drive Link] showcasing all features on an emulator or real device.
5.  **Build Artifacts:** A [Google Drive Link] to download the generated APK (Android) or IPA (iOS).
6.  **Web Access:** A public web application link (e.g., Vercel) for testing in a browser.
7.  **Future Improvements:** A section describing potential enhancements (see Roadmap below).
8.  **Final Presentation:** A slide deck summarizing the project and learnings.

---

## 🏗️ System Architecture

The system follows a layered architecture:
1.  **Presentation Layer:** Mobile/Web App (React Native, Swift, or Kotlin)
2.  **API Layer:** Spring Boot REST Controllers
3.  **Business Layer:** Service Interfaces & Implementations
4.  **Data Layer:** JPA Repositories & Database

---

## 🧩 Key Features Implemented

### 🛒 Product Management
- **Server-Side Pagination:** Efficiently loads data in chunks.
- **Dynamic Filtering:** Filter products by category and reviews by rating.
- **Rating Breakdown:** Server-calculated distribution of star ratings.

### 🤖 AI Integration
- **AI Review Summary:** Automatically generates a summary of user reviews using AI logic.
- **AI Assistant:** Interactive chat interface for product queries.

### 🛠️ Technical Highlights
- **Dependency Inversion:** Controllers depend on Interfaces, not concrete classes.
- **DTO Pattern:** Strict separation between Database Entities and API responses.
- **Optimized SQL:** Custom queries for aggregation and performance.

---

## 🚀 Future Improvements & Technical Roadmap

### 1. Security Enhancements (Planned)
- **Spring Security Integration:** Integrate Spring Security 6.
- **JWT Authentication:** Implement stateless authentication using JSON Web Tokens.
- **Role-Based Access Control (RBAC):** Differentiate between Admin and User roles.
- **Secure Endpoints:** Protect sensitive actions like `POST /reviews`.

### 2. Validation with AOP (Planned)
- **Centralized Validation:** Use Aspect-Oriented Programming (AOP) to handle request validation globally.
- **Consistent Error Handling:** Unified error response structure across all endpoints.
- **Reusable Aspects:** Create custom annotations for business rules.

### 3. Integration Testing (Planned)
- **Testcontainers:** Use Docker containers to spin up real database instances for testing.
- **Reproducible Tests:** Ensure tests run consistently across different environments (CI/CD, Local).
- **End-to-End Testing:** Validate full user flows from API to Database.

---

## 🛠️ How to Run Locally

### Backend
```bash
cd backend
./mvnw spring-boot:run
```
See [backend/README.md](./backend/README.md) for more details.

### Frontend (Reference Implementation - React Native)
```bash
cd mobile
npm install
npx expo start
```
See [mobile/README.md](./mobile/README.md) for more details.
- Press `w` for Web
- Press `a` for Android (Emulator)
- Scan QR code for iOS (Expo Go)

---

## 📂 Project Structure

- **/backend:** Spring Boot application source code.
- **/mobile:** React Native (Expo) application source code (Reference).
- **README-iOS-Swift.md:** Instructions for iOS implementation option.
- **README-Android-Kotlin.md:** Instructions for Android implementation option.
