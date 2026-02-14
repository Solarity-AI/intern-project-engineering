# ProductReviewApp — Android Frontend

Kotlin/Jetpack Compose Android application with Hilt DI, Retrofit networking, and framework-backed error/result types.

## Build & Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- The `android-fw` directory must exist at `../../android-fw` relative to `frontend/` (Gradle composite build resolves it automatically)

### CLI build
```bash
cd frontend
./gradlew :app:assembleDebug
```

### Android Studio
1. Open the `frontend/` directory as a project.
2. Gradle will resolve `android-fw` modules via the composite build in `settings.gradle.kts`.
3. Run on emulator or device.

### Backend base URL
The app reads `BuildConfig.BASE_URL` (set in `build.gradle.kts`).

| Target | URL |
|--------|-----|
| Emulator | `http://10.0.2.2:8080` (default) |
| Physical device on same LAN | `http://<your-machine-ip>:8080` |

## Framework Integration

### Composite build setup
`settings.gradle.kts` includes:
```kotlin
includeBuild("../../android-fw") {
    dependencySubstitution {
        substitute(module("com.solary:fw-core")).using(project(":fw-core"))
        substitute(module("com.solary:fw-logging")).using(project(":fw-logging"))
        substitute(module("com.solary:fw-networking")).using(project(":fw-networking"))
        substitute(module("com.solary:fw-dto")).using(project(":fw-dto"))
        substitute(module("com.solary:fw-pagination")).using(project(":fw-pagination"))
        substitute(module("com.solary:fw-validation")).using(project(":fw-validation"))
    }
}
```

### Usage rules
1. **Import framework types directly**: use `import fw.core.FWError`, `import fw.core.FWResult`, `import fw.core.DispatcherProvider`.
2. **Do not copy or redefine framework types** (e.g., do not create a local `data class FWError`).
3. **Extension functions** are acceptable when the framework type lacks a method the app needs. Place them in `com.productreview.app.core/` and import the framework type at the top of the file.
4. **Avoid `import fw.core.*` wildcard** in files that also use local types from `com.productreview.app.core.*` — the wildcard causes ambiguity with `RetryPolicy` and `RefreshController` which exist in both packages. Use specific imports instead.

### Current framework types in use

| Framework type | Import | Files using it |
|---|---|---|
| `fw.core.FWError` | `import fw.core.FWError` | 13 files (all repositories, ViewModels, DI, core internals) |
| `fw.core.FWResult` | `import fw.core.FWResult` | 12 files |
| `fw.core.DispatcherProvider` | `import fw.core.DispatcherProvider` | 3 files (`NetworkModule`, `ProductListViewModel`, `DispatcherProvider.kt`) |
| `fw.core.DefaultDispatcherProvider` | `import fw.core.DefaultDispatcherProvider` | 1 file (`NetworkModule`) |

### App-specific extensions (bridge layer)

| File | What it provides |
|---|---|
| `core/FWError.kt` | Companion extensions: `timeout`, `noInternet`, `serverError`, `clientError`, `notFound`, `rateLimited`, `forbidden`, `fromHttpStatus`, `fromThrowable` |
| `core/FWResult.kt` | Instance extensions: `onSuccess {}`, `onFailure {}` |
| `core/DispatcherProvider.kt` | `TestDispatcherProvider` class for unit tests |

### Known gaps (local types that bypass framework equivalents)

| Local file | Local types | Why not framework | Framework equivalent |
|---|---|---|---|
| `core/RetryPolicy.kt` | `RetryPolicy` | Local adds `executeWithRetry()`, backoff, jitter | `fw.core.RetryPolicy` (simpler API) |
| `core/RefreshController.kt` | `RefreshController` | Local adds `executeRefreshWithResult()` | `fw.core.RefreshController` |
| `core/logging/Logger.kt` | `LogLevel`, `LogCategory`, `LogKey`, `LogEvent`, `Logger`, `DefaultLogger`, `NoOpLogger` | Local `LogEvent` has Builder pattern + `message`/`error`/`timestamp`; different category/key enums | `fw.logging.*` |
| `core/pagination/Paginator.kt` | `Paginator<T>` | Local exposes `items: StateFlow<List<T>>`; richer `PagingState` | `fw.pagination.Paginator` |
| `core/pagination/PaginationModels.kt` | `PageCursor`, `Page<T>`, `PagingState`, `PagingResult<T>` | Local adds `totalPages`, `totalElements`, `error` fields | `fw.pagination.*` |

### Not yet adopted

| Module | Status |
|--------|--------|
| `fw.logging` | Not used — API mismatch (Builder pattern, different enums) |
| `fw.networking` | Not used — app uses Retrofit + OkHttp directly |
| `fw.pagination` | Not used — API mismatch (missing `items` StateFlow, fewer `PagingState` fields) |
| `fw.dto` | Not used — app uses `kotlinx.serialization` |
| `fw.validation` | Not used — inline validation in repositories |
| `fw.core.UiState` / `ScreenStateManager` | Not used — ViewModels use custom `MutableStateFlow` patterns |

## Project Structure

```
app/src/main/java/com/productreview/app/
├── core/                  # Framework bridge + local utilities
│   ├── FWError.kt         # Companion extensions on fw.core.FWError
│   ├── FWResult.kt        # Extensions on fw.core.FWResult
│   ├── DispatcherProvider.kt  # TestDispatcherProvider
│   ├── RetryPolicy.kt    # Local (API mismatch with fw.core.RetryPolicy)
│   ├── RefreshController.kt   # Local (API mismatch)
│   ├── logging/Logger.kt # Local logging system
│   └── pagination/        # Local pagination (Paginator + models)
├── data/
│   ├── model/ApiModels.kt    # DTOs (kotlinx.serialization)
│   ├── remote/               # Retrofit API interfaces + interceptors
│   ├── repository/           # ProductRepo, NotificationRepo, WishlistRepo
│   └── local/                # DataStore preferences
├── di/NetworkModule.kt       # Hilt DI (provides fw.core.DispatcherProvider)
├── domain/model/             # Domain models
├── navigation/               # Compose Navigation
├── notifications/            # Android notification helpers
└── ui/
    ├── components/           # Reusable Compose components
    ├── screens/              # Screen + ViewModel pairs
    ├── theme/                # Material3 theming
    └── viewmodel/            # Shared ViewModels
```
