# Product Review App - Complete Setup and Run Guide

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Prerequisites](#2-prerequisites)
3. [Backend Setup](#3-backend-setup)
   - [Common Syntax Error Causes](#31-common-syntax-error-causes)
   - [Environment Setup](#32-environment-setup)
   - [Dependency Installation](#33-dependency-installation)
   - [Configuration](#34-configuration)
   - [Running the Backend](#35-running-the-backend)
   - [Database Setup](#36-database-setup)
4. [Frontend Setup (Android/Kotlin)](#4-frontend-setup-androidkotlin)
   - [Prerequisites](#41-prerequisites)
   - [Installation](#42-installation)
   - [Configuration](#43-configuration)
   - [Running the Frontend](#44-running-the-frontend)
5. [Running the Full Project](#5-running-the-full-project)
6. [Testing Guide](#6-testing-guide)
7. [Android Studio Configuration](#7-android-studio-configuration)
8. [Troubleshooting](#8-troubleshooting)
9. [API Reference](#9-api-reference)
10. [Verification Checklist](#10-verification-checklist)

---

## 1. Project Overview

This project consists of two main components:

| Component | Technology Stack | Location |
|-----------|-----------------|----------|
| **Backend** | Spring Boot 4.0.1, Java 21, Maven, H2/PostgreSQL | `backend/` |
| **Frontend** | Kotlin, Jetpack Compose, Hilt, Retrofit | `kotlin-app/` |

### Architecture

```
ProductReviewApp-Kotlin-FW/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/         # Java source files
│   ├── src/main/resources/    # Configuration files
│   └── pom.xml                # Maven dependencies
└── kotlin-app/                # Android Kotlin Frontend
    └── app/                   # Android application module
```

---

## 2. Prerequisites

### Required Software

| Software | Minimum Version | Verification Command | Download Link |
|----------|----------------|---------------------|---------------|
| Java JDK | 21 | `java -version` | [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) |
| Maven | 3.9+ | `mvn -version` | [Apache Maven](https://maven.apache.org/download.cgi) |
| Android Studio | Hedgehog (2023.1.1)+ | - | [Android Studio](https://developer.android.com/studio) |
| Android SDK | 35 (compileSdk) | Android Studio SDK Manager | Via Android Studio |
| Kotlin | 1.9+ | Android Studio | Bundled with Android Studio |

### Verify Java Installation

**PowerShell / CMD / VS Code Terminal:**
```powershell
java -version
```

Expected output:
```
openjdk version "21.0.x" 2024-xx-xx
OpenJDK Runtime Environment (build 21.0.x+xx)
OpenJDK 64-Bit Server VM (build 21.0.x+xx, mixed mode, sharing)
```

### Verify Maven Installation

**PowerShell / CMD / VS Code Terminal:**
```powershell
mvn -version
```

Expected output:
```
Apache Maven 3.9.x
Maven home: C:\path\to\maven
Java version: 21.0.x, vendor: ...
```

> **Note:** If Maven is not installed, you can use the Maven Wrapper (`mvnw`) included in the project.

---

## 3. Backend Setup

### 3.1 Common Syntax Error Causes

When encountering syntax errors in Controller and Service layers, the following are common causes:

| Issue | Symptom | Cause | Solution |
|-------|---------|-------|----------|
| **Missing Lombok** | Getter/Setter methods not found | Lombok not configured | Add Lombok dependency and enable annotation processing |
| **Incompatible Java Version** | Unexpected token errors | Project requires Java 21 | Install and configure JDK 21 |
| **Maven Dependencies Not Downloaded** | Import statements fail | Dependencies not resolved | Run `mvn clean install` |
| **Spring Boot Version Mismatch** | jakarta.* imports fail | Spring Boot 3+ uses jakarta.* namespace | Use correct import package |
| **Annotation Processor Not Enabled** | @Data, @Builder not working | IDE annotation processing disabled | Enable annotation processing in IDE |
| **Missing Spring Annotations** | Bean not found | Missing @Service, @Repository | Add appropriate annotations |

#### Technical Explanation

**1. Missing or Incompatible Dependencies**

Spring Boot 4.0.1 requires Jakarta EE 10 namespace. If you see errors like:
```
cannot find symbol: class javax.persistence.Entity
```
This indicates you're using old `javax.*` imports instead of `jakarta.*`.

**2. Incorrect Java Version**

The project requires Java 21. Using an older version will cause:
- Record classes not recognized
- Pattern matching syntax errors
- Sealed classes compilation failures

**3. Lombok Configuration Issues**

If you see errors like:
```
cannot find symbol: method getXxx()
cannot find symbol: method setXxx()
```
This indicates Lombok is not processing annotations.

**4. Missing Annotation Processor Configuration**

Maven compiler plugin must have annotation processors configured:
```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </path>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
    </path>
</annotationProcessorPaths>
```

### 3.2 Environment Setup

#### Step 1: Navigate to Backend Directory

**PowerShell:**
```powershell
cd C:\Users\ERENCAN\Solary\ProductReviewApp-Kotlin-FW\backend
```

**CMD:**
```cmd
cd C:\Users\ERENCAN\Solary\ProductReviewApp-Kotlin-FW\backend
```

**VS Code Terminal:**
```bash
cd backend
```

#### Step 2: Set JAVA_HOME Environment Variable

**PowerShell (Session):**
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

**CMD (Session):**
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
```

**Permanent (System Environment Variables):**
1. Open System Properties > Advanced > Environment Variables
2. Add `JAVA_HOME` = `C:\Program Files\Java\jdk-21`
3. Add `%JAVA_HOME%\bin` to `PATH`

### 3.3 Dependency Installation

#### Using Maven Wrapper (Recommended)

**PowerShell:**
```powershell
.\mvnw.cmd clean install -DskipTests
```

**CMD:**
```cmd
mvnw.cmd clean install -DskipTests
```

**VS Code Terminal (Git Bash):**
```bash
./mvnw clean install -DskipTests
```

#### Using Global Maven

**All Environments:**
```bash
mvn clean install -DskipTests
```

#### Expected Output

```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  XX.XXX s
[INFO] Finished at: 2024-XX-XXTXX:XX:XX+XX:XX
```

### 3.4 Configuration

#### Create Environment File

**PowerShell:**
```powershell
Copy-Item .env.example .env
```

**CMD:**
```cmd
copy .env.example .env
```

**VS Code Terminal:**
```bash
cp .env.example .env
```

#### Configure .env File

Edit `.env` with your settings:

```properties
# Server Configuration
PORT=8080
SPRING_PROFILES_ACTIVE=dev

# OpenAI Configuration (Optional - for AI features)
OPENAI_API_KEY=your-openai-api-key-here

# Production Database (Optional)
# DATABASE_URL=jdbc:postgresql://localhost:5432/productreview
# DATABASE_USERNAME=postgres
# DATABASE_PASSWORD=your_password
```

#### Application Profiles

| Profile | Database | Use Case |
|---------|----------|----------|
| `dev` | H2 (In-Memory) | Local development |
| `prod` | PostgreSQL | Production deployment |
| `test` | H2 (In-Memory) | Automated testing |

### 3.5 Running the Backend

#### Development Mode (H2 Database)

**PowerShell:**
```powershell
.\mvnw.cmd spring-boot:run
```

**CMD:**
```cmd
mvnw.cmd spring-boot:run
```

**VS Code Terminal:**
```bash
./mvnw spring-boot:run
```

**Or with explicit profile:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Production Mode (PostgreSQL)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

#### Using JAR File

**Build:**
```bash
./mvnw clean package -DskipTests
```

**Run:**
```bash
java -jar target/product-review-backend-1.0.0.jar
```

**With Profile:**
```bash
java -jar -Dspring.profiles.active=prod target/product-review-backend-1.0.0.jar
```

#### Verify Backend is Running

Open a browser and navigate to:
- Health Check: http://localhost:8080/actuator/health
- H2 Console (dev): http://localhost:8080/h2-console
- API Documentation: http://localhost:8080/swagger-ui.html

### 3.6 Database Setup

#### Development (H2 - Automatic)

No setup required. The H2 database is automatically created in-memory and seeded with sample data on startup.

**H2 Console Access:**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:productreview`
- Username: `sa`
- Password: `password`

#### Production (PostgreSQL)

**Step 1: Install PostgreSQL**

Download from: https://www.postgresql.org/download/windows/

**Step 2: Create Database**

**PowerShell / CMD:**
```bash
psql -U postgres
```

**SQL:**
```sql
CREATE DATABASE productreview;
CREATE USER productreview_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE productreview TO productreview_user;
\q
```

**Step 3: Configure Connection**

Update `.env`:
```properties
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://localhost:5432/productreview
DATABASE_USERNAME=productreview_user
DATABASE_PASSWORD=your_secure_password
```

**Step 4: Run Migrations**

The application uses Hibernate's `ddl-auto=update` to automatically create/update tables on startup.

---

## 4. Frontend Setup (Android/Kotlin)

### 4.1 Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 35 (API Level 35)
- Kotlin 1.9+
- JDK 17 (for Android builds)

### 4.2 Installation

#### Step 1: Open Project in Android Studio

1. Open Android Studio
2. Select "Open" from the welcome screen
3. Navigate to `ProductReviewApp-Kotlin-FW/kotlin-app`
4. Click "OK"

#### Step 2: Sync Gradle

Android Studio will automatically prompt to sync Gradle. Click "Sync Now".

**Or manually sync:**
- File > Sync Project with Gradle Files
- Or press `Ctrl+Shift+O` (Windows)

#### Step 3: Download Dependencies

Dependencies are automatically downloaded during Gradle sync.

**Manual Gradle sync via terminal:**

**PowerShell / CMD:**
```powershell
cd kotlin-app
.\gradlew.bat dependencies
```

**VS Code / Git Bash:**
```bash
cd kotlin-app
./gradlew dependencies
```

### 4.3 Configuration

#### Configure Backend URL

The backend URL is configured in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
```

| Environment | URL |
|-------------|-----|
| Android Emulator | `http://10.0.2.2:8080` |
| Physical Device (same network) | `http://YOUR_PC_IP:8080` |
| Production | `https://your-production-url.com` |

**To change the URL:**

Edit `kotlin-app/app/build.gradle.kts`:
```kotlin
defaultConfig {
    // For local development with emulator
    buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")

    // For physical device (replace with your PC's IP)
    // buildConfigField("String", "BASE_URL", "\"http://192.168.1.100:8080\"")
}
```

#### Find Your PC's IP Address

**PowerShell:**
```powershell
(Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.InterfaceAlias -notlike "*Loopback*" }).IPAddress
```

**CMD:**
```cmd
ipconfig | findstr IPv4
```

### 4.4 Running the Frontend

#### Using Android Studio

1. Select a device from the device dropdown
2. Click the "Run" button (green play icon)
3. Or press `Shift+F10`

#### Using Command Line

**Build APK:**
```bash
cd kotlin-app
./gradlew assembleDebug
```

**Install on connected device:**
```bash
./gradlew installDebug
```

**Build and install:**
```bash
./gradlew assembleDebug installDebug
```

---

## 5. Running the Full Project

### Recommended Startup Order

```
1. Database (if using PostgreSQL)
       ↓
2. Backend Server
       ↓
3. Frontend (Android App)
```

### Step-by-Step Instructions

#### Step 1: Start the Backend

**Terminal 1:**
```bash
cd ProductReviewApp-Kotlin-FW/backend
./mvnw spring-boot:run
```

Wait for:
```
Started ProductReviewApplication in X.XXX seconds
```

#### Step 2: Verify Backend

Open browser: http://localhost:8080/actuator/health

Expected response:
```json
{"status":"UP"}
```

#### Step 3: Start the Frontend

1. Open Android Studio
2. Open project: `ProductReviewApp-Kotlin-FW/kotlin-app`
3. Select emulator or device
4. Click Run

#### Step 4: Verify Connection

In the Android app:
1. Products should load on the home screen
2. Clicking a product should show details
3. Reviews should display

### Verify Frontend-Backend Communication

**Using cURL (PowerShell):**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/products?page=0&size=5" | ConvertTo-Json
```

**Using cURL (CMD/Git Bash):**
```bash
curl -X GET "http://localhost:8080/api/products?page=0&size=5"
```

**Expected Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "iPhone 15 Pro",
      "description": "The latest iPhone with A17 Pro chip...",
      "categories": ["Electronics", "Smartphones"],
      "price": 999.99,
      ...
    }
  ],
  "totalElements": 24,
  "totalPages": 5,
  ...
}
```

---

## 6. Testing Guide

### Backend Unit Tests

**Run all tests:**
```bash
cd backend
./mvnw test
```

**Run specific test class:**
```bash
./mvnw test -Dtest=ProductServiceImplTest
```

**Run with coverage:**
```bash
./mvnw test jacoco:report
```

Coverage report: `target/site/jacoco/index.html`

### Backend Integration Tests

```bash
./mvnw verify
```

### Frontend Unit Tests

**Run all tests:**
```bash
cd kotlin-app
./gradlew test
```

**Run with coverage:**
```bash
./gradlew testDebugUnitTest jacocoTestReport
```

### Frontend UI Tests (Instrumented)

```bash
./gradlew connectedAndroidTest
```

### End-to-End Testing

1. Start the backend
2. Run the Android app
3. Manually verify:
   - [ ] Product list loads
   - [ ] Product details display
   - [ ] Reviews load and paginate
   - [ ] Add review works
   - [ ] Wishlist toggle works
   - [ ] Notifications display
   - [ ] AI chat responds (if API key configured)

---

## 7. Android Studio Configuration

### Opening the Project

1. Launch Android Studio
2. Click "Open"
3. Navigate to `ProductReviewApp-Kotlin-FW/kotlin-app`
4. Click "OK"
5. Wait for Gradle sync to complete

### SDK Configuration

1. Go to: File > Project Structure > SDK Location
2. Set Android SDK location (typically `C:\Users\YOUR_NAME\AppData\Local\Android\Sdk`)
3. Click "Apply"

### JDK Configuration

1. Go to: File > Project Structure > SDK Location
2. Under "Gradle JDK", select JDK 17
3. Click "Apply"

### Gradle Configuration

1. Go to: File > Settings > Build, Execution, Deployment > Build Tools > Gradle
2. Set "Gradle JDK" to JDK 17
3. Set "Build and run using" to "Gradle"
4. Click "Apply"

### Emulator Setup

1. Go to: Tools > Device Manager
2. Click "Create Device"
3. Select a phone (e.g., Pixel 7)
4. Select system image: API 35 (Android 14)
5. Finish the setup

### Physical Device Setup

1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect device via USB
4. Accept the debugging prompt on the device

### Backend URL for Different Environments

| Environment | BASE_URL Value |
|-------------|----------------|
| Android Emulator | `http://10.0.2.2:8080` |
| Physical Device (USB) | `http://YOUR_PC_IP:8080` |
| Physical Device (WiFi) | `http://YOUR_PC_IP:8080` |

> **Important:** For physical devices, ensure your PC's firewall allows connections on port 8080.

### Debugging

**Logcat:**
1. View > Tool Windows > Logcat
2. Filter by package: `com.productreview.app`

**Network Inspection:**
1. View > Tool Windows > App Inspection
2. Select your running app
3. Go to "Network Inspector"

---

## 8. Troubleshooting

### Backend Issues

| Symptom | Possible Cause | Solution | Command(s) |
|---------|---------------|----------|------------|
| `java: package jakarta.persistence does not exist` | Dependencies not downloaded | Clean and rebuild | `./mvnw clean install` |
| `cannot find symbol: method getXxx()` | Lombok not working | Enable annotation processing | IDE Settings > Build > Compiler > Annotation Processors |
| `Port 8080 already in use` | Another process using port | Kill process or change port | `netstat -ano \| findstr :8080` |
| `Connection refused` | Backend not running | Start backend first | `./mvnw spring-boot:run` |
| `java.lang.UnsupportedClassVersionError` | Wrong Java version | Use JDK 21 | `java -version` |
| `No compiler is provided` | JDK not installed (only JRE) | Install full JDK | Download JDK 21 |
| `Could not resolve dependencies` | Network/repository issues | Check internet, clear cache | `./mvnw dependency:purge-local-repository` |

### Frontend Issues

| Symptom | Possible Cause | Solution | Command(s) |
|---------|---------------|----------|------------|
| Gradle sync failed | Incorrect JDK | Use JDK 17 for Android | File > Project Structure > SDK Location |
| SDK not found | SDK path not configured | Set SDK path | File > Project Structure > SDK Location |
| `Connection timed out` | Wrong backend URL | Use `10.0.2.2` for emulator | Edit `build.gradle.kts` |
| App crashes on start | Missing internet permission | Check AndroidManifest.xml | Should have `INTERNET` permission |
| `Network on main thread` | Blocking call on UI thread | Use coroutines | Already implemented with Retrofit |
| Build failed with KSP | KSP version mismatch | Update versions | Sync Gradle |

### Network Issues

**Check if backend is accessible from emulator:**
```bash
adb shell curl http://10.0.2.2:8080/actuator/health
```

**Check if backend is accessible from physical device:**
```bash
adb shell curl http://YOUR_PC_IP:8080/actuator/health
```

**Windows Firewall Rule:**
```powershell
New-NetFirewallRule -DisplayName "Product Review Backend" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```

---

## 9. API Reference

### Product Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List products (paginated) |
| GET | `/api/products/{id}` | Get product details |
| GET | `/api/products/stats` | Get global statistics |
| GET | `/api/products/{id}/reviews` | Get product reviews |
| POST | `/api/products/{id}/reviews` | Add a review |
| PUT | `/api/products/reviews/{id}/helpful` | Mark review helpful |
| GET | `/api/products/reviews/voted` | Get user's voted reviews |
| POST | `/api/products/{id}/chat` | AI chat about product |

### User Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/user/wishlist` | Get wishlist IDs |
| GET | `/api/user/wishlist/products` | Get wishlist products |
| POST | `/api/user/wishlist/{productId}` | Toggle wishlist |
| GET | `/api/user/notifications` | Get notifications |
| GET | `/api/user/notifications/unread-count` | Get unread count |
| PUT | `/api/user/notifications/{id}/read` | Mark as read |
| PUT | `/api/user/notifications/read-all` | Mark all as read |
| POST | `/api/user/notifications` | Create notification |
| DELETE | `/api/user/notifications/{id}` | Delete notification |
| DELETE | `/api/user/notifications` | Delete all notifications |

### Headers

All user-specific endpoints require:
```
X-User-ID: <user-uuid>
```

---

## 10. Verification Checklist

Use this checklist to verify your setup is complete:

### Environment Setup
- [ ] Java 21 installed and JAVA_HOME configured
- [ ] Maven installed or Maven Wrapper available
- [ ] Android Studio installed
- [ ] Android SDK 35 installed
- [ ] JDK 17 configured in Android Studio

### Backend
- [ ] Dependencies downloaded (`./mvnw clean install`)
- [ ] `.env` file created from `.env.example`
- [ ] Backend starts without errors (`./mvnw spring-boot:run`)
- [ ] Health endpoint responds: http://localhost:8080/actuator/health
- [ ] H2 Console accessible: http://localhost:8080/h2-console
- [ ] Products endpoint returns data: http://localhost:8080/api/products

### Frontend
- [ ] Project opens in Android Studio
- [ ] Gradle sync completes successfully
- [ ] `BASE_URL` configured correctly
- [ ] Emulator or device available
- [ ] App installs and launches
- [ ] App connects to backend (products load)

### Integration
- [ ] Products display in app
- [ ] Product details load
- [ ] Reviews display and paginate
- [ ] Add review works
- [ ] Wishlist toggle works
- [ ] Notifications work

### Optional
- [ ] OpenAI API key configured
- [ ] AI chat feature works
- [ ] PostgreSQL configured (production)

---

## Quick Reference Commands

### Backend

```bash
# Navigate to backend
cd ProductReviewApp-Kotlin-FW/backend

# Install dependencies
./mvnw clean install -DskipTests

# Run in development mode
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build JAR
./mvnw clean package -DskipTests
```

### Frontend

```bash
# Navigate to frontend
cd ProductReviewApp-Kotlin-FW/kotlin-app

# Sync dependencies
./gradlew dependencies

# Build debug APK
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Run tests
./gradlew test
```

---

**Document Version:** 1.0.0
**Last Updated:** January 2026
**Project:** ProductReviewApp-Kotlin-FW
