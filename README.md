# ProductReviewApp

A full-stack product review application with an Android (Kotlin/Jetpack Compose) frontend and a Spring Boot (Java) backend. Both platforms consume their respective Solarity AI frameworks.

## Architecture

```
ProductReviewApp-Kotlin-FW/
├── frontend/          # Android app (Kotlin, Jetpack Compose, Hilt)
├── backend/           # Spring Boot REST API (Java 21, H2, MapStruct)
├── android-fw/        # Android framework (composite build) — DO NOT EDIT
└── backend-fw/        # Backend framework (Maven dep)       — DO NOT EDIT
```

## Framework Source of Truth

The frameworks are **external dependencies** that live alongside this project:

| Framework | Path | Integration | Package root |
|-----------|------|-------------|--------------|
| Android FW | `../android-fw` | Gradle composite build (`includeBuild`) | `fw.core`, `fw.logging`, `fw.networking`, `fw.pagination`, `fw.dto`, `fw.validation` |
| Backend FW | `../backend-fw` | Maven dependency (`com.solarityai:backend-fw:1.0.0.0`) | `com.solarityai.backendfw.*` |

**Rules:**
1. Never copy or duplicate framework types into application code.
2. Import framework types directly (e.g., `import fw.core.FWError`, `import com.solarityai.backendfw.foundation.entity.BaseEntity`).
3. If the framework lacks a method you need, add an **extension function** in the app that imports the framework type — do not redefine the type locally.
4. The `android-fw/` and `backend-fw/` directories inside this repo are read-only references. Do not modify them.

## Framework Usage Summary

| Platform | Coverage | Key modules used |
|----------|----------|------------------|
| Android | **48%** | `fw.core` (FWError, FWResult, DispatcherProvider) |
| Backend | **60%** | `foundation` (BaseEntity, BaseDto, BaseRepository, BaseController), `query` (PageRequestDto, PageResponse), `exception` (NotFoundException, ApiErrorResponse) |
| Overall | **54%** | |

See `frontend/README.md` and `backend/README.md` for platform-specific details.

## Quick Start

### Backend
```bash
cd backend
mvn spring-boot:run
```
Server starts at `http://localhost:8080`.

### Android
Open `frontend/` in Android Studio, sync Gradle, then Run.
The app expects the backend at `http://10.0.2.2:8080` (emulator) by default.

## Known Gaps (framework modules not yet adopted)

- **Android**: `fw.logging`, `fw.networking`, `fw.pagination`, `fw.dto`, `fw.validation`, `fw.core.UiState`/`ScreenStateManager`
- **Backend**: `validation`, `cache`, `security`, `logger`, `domain` (events), `metrics`, `retry`, `governance`
