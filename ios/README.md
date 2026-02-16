# iOS Product Review App

Native iOS implementation of the Product Review Application using Swift and SwiftUI.

## Tech Stack

- **Language**: Swift 5.9+
- **UI Framework**: SwiftUI
- **Architecture**: MVVM with Clean Architecture
- **Networking**: URLSession with async/await
- **Concurrency**: Swift Concurrency (async/await, MainActor)
- **Minimum iOS**: 17.0

## Project Structure

```
ios/
├── ProductReview/
│   ├── App/
│   │   ├── AppEntry/           # App & Scene setup
│   │   │   ├── ProductReviewApp.swift
│   │   │   └── ContentView.swift
│   │   ├── Navigation/         # NavigationStack & routes
│   │   │   └── NavigationRouter.swift
│   │   └── Core/               # Shared utilities
│   │       ├── Constants.swift
│   │       ├── HapticManager.swift
│   │       ├── NetworkMonitor.swift
│   │       └── ThemeManager.swift
│   │
│   ├── Data/
│   │   ├── Network/            # API clients, DTOs
│   │   │   ├── APIClient.swift
│   │   │   └── DTOs.swift
│   │   ├── Local/              # Local storage & caching
│   │   │   ├── ImageCache.swift
│   │   │   └── SearchHistoryManager.swift
│   │   ├── Mapper/             # DTO <-> Domain mapping
│   │   │   ├── ProductMapper.swift
│   │   │   ├── ReviewMapper.swift
│   │   │   └── NotificationMapper.swift
│   │   └── Repository/         # Repository implementations
│   │       ├── ProductRepository.swift
│   │       ├── WishlistRepository.swift
│   │       └── NotificationRepository.swift
│   │
│   ├── Domain/
│   │   ├── Model/              # Domain models
│   │   │   ├── Product.swift
│   │   │   ├── Review.swift
│   │   │   └── Notification.swift
│   │   └── Repository/         # Repository protocols
│   │       ├── ProductRepositoryProtocol.swift
│   │       ├── WishlistRepositoryProtocol.swift
│   │       └── NotificationRepositoryProtocol.swift
│   │
│   ├── Presentation/
│   │   ├── Views/
│   │   │   ├── Product/        # Product list & detail
│   │   │   │   ├── ProductListView.swift
│   │   │   │   └── ProductDetailView.swift
│   │   │   ├── Wishlist/       # Wishlist screen
│   │   │   │   └── WishlistView.swift
│   │   │   ├── Notification/   # Notifications
│   │   │   │   └── NotificationsView.swift
│   │   │   └── AI/             # AI Assistant
│   │   │       └── AIAssistantView.swift
│   │   ├── ViewModels/         # ObservableObjects
│   │   │   ├── ProductListViewModel.swift
│   │   │   └── ProductDetailViewModel.swift
│   │   └── Components/         # Reusable UI components
│   │       ├── AnimatedHeartButton.swift
│   │       ├── ConfirmationDialog.swift
│   │       ├── EmptyStateView.swift
│   │       ├── LoadingButton.swift
│   │       ├── RatingStarsView.swift
│   │       ├── ShimmerView.swift
│   │       └── ToastView.swift
│   │
│   └── Resources/
│       └── Assets.xcassets
│
├── ProductReviewTests/         # Unit tests
└── ProductReviewUITests/       # UI tests
```

## Setup Instructions

### Prerequisites
- Xcode 15.0+
- macOS Sonoma (14.0+)
- iOS Simulator or physical device (iOS 17.0+)

### Getting Started

1. **Install XcodeGen** (if not installed)
   ```bash
   brew install xcodegen
   ```

2. **Generate Xcode Project**
   ```bash
   cd ios
   xcodegen generate
   ```

3. **Open in Xcode**
   ```bash
   open ProductReview.xcodeproj
   ```

4. **Build & Run**
   - Select target device/simulator (iPhone 16 Pro recommended)
   - Press `Cmd + R` to build and run

5. **Start Backend** (for local development)
   ```bash
   cd ../backend
   ./mvnw spring-boot:run
   ```

## Features

### Implemented Screens

| Screen | Features |
|--------|----------|
| **ProductListView** | Pagination, search, category filter, global stats, pull-to-refresh |
| **ProductDetailView** | Product info, AI summary, rating breakdown, reviews, add review |
| **WishlistView** | Grid view, multi-select, batch delete, optimistic updates |
| **NotificationsView** | List, mark as read, delete, swipe actions |
| **AIAssistantView** | Chat interface, predefined questions, loading states |

### Architecture Highlights

- **MVVM Pattern**: ViewModels expose observable state via `@Published`
- **Repository Pattern**: Abstracts data access behind protocols
- **Dependency Injection**: Repositories injected into ViewModels
- **Async/Await**: Modern Swift concurrency throughout
- **MainActor**: All UI updates on main thread
- **Optimistic Updates**: Immediate UI feedback with backend sync

## API Configuration

The app connects to the backend API at:
```
Production: https://solarity-review-api.herokuapp.com
Development: http://localhost:8080 (DEBUG builds)
```

Configuration is in `App/Core/Constants.swift`:
```swift
enum AppConstants {
    enum API {
        #if DEBUG
        static let useLocalServer = true
        #else
        static let useLocalServer = false
        #endif

        static var baseURL: String {
            useLocalServer ? "http://localhost:8080" : "https://solarity-review-api.herokuapp.com"
        }
        static let timeoutInterval: TimeInterval = 10.0
        static let aiTimeoutInterval: TimeInterval = 20.0
    }
}
```

### Network Features
- **Exponential Backoff Retry**: Automatic retry with 1s → 2s → 4s delays
- **User-Friendly Errors**: Clear messages for timeout, no connection, server errors
- **Connectivity Monitoring**: Real-time network status with offline banner

## User Identification

The app generates a unique device UUID on first launch, stored in UserDefaults:
- Key: `device_user_id`
- Sent as `X-User-ID` header on all API requests
- Enables per-device wishlist and notifications

## Reusable Components

| Component | Description |
|-----------|-------------|
| `EmptyStateView` | Empty state with icon, title, subtitle, and action button |
| `ShimmerView` | Loading skeleton with shimmer animation |
| `ToastView` | Toast notifications (success, error, warning, info) |
| `LoadingButton` | Button with loading spinner state |
| `AnimatedHeartButton` | Wishlist heart with bounce animation |
| `RatingStarsView` | Display and interactive star ratings |
| `ConfirmationDialog` | Reusable confirmation dialogs |

## Core Utilities

| Utility | Description |
|---------|-------------|
| `HapticManager` | Centralized haptic feedback (impact, notification, selection) |
| `ThemeManager` | Theme persistence (system, light, dark) with UserDefaults |
| `NetworkMonitor` | Real-time connectivity monitoring with offline banner |
| `ImageCache` | In-memory image caching (NSCache, 50MB limit) |
| `SearchHistoryManager` | Search history persistence (max 10 items) |

## Implemented Features

- ✅ Xcode project with XcodeGen
- ✅ Offline detection and banner
- ✅ Search history persistence
- ✅ Theme toggle (system/light/dark)
- ✅ Error retry with exponential backoff
- ✅ Loading skeletons (shimmer)
- ✅ Haptic feedback
- ✅ Accessibility labels and hints
- ✅ Toast notifications
- ✅ Image caching
- ✅ Animated interactions

## Next Steps

1. **Testing**
   - Unit tests for ViewModels
   - Integration tests for repositories
   - UI tests for critical flows

2. **Polish**
   - Localization (i18n)
   - App icons and launch screen
   - Push notifications integration

## Development in VS Code

While Xcode is recommended for iOS development, you can edit Swift files in VS Code with these extensions:
- **Swift** (sswg.swift-lang) - Language support
- **Apple Swift Format** (vknabel.vscode-apple-swift-format) - Code formatting

Build and run from terminal:
```bash
# Build (requires Xcode command line tools)
xcodebuild -scheme ProductReview -destination 'platform=iOS Simulator,name=iPhone 15'

# Run tests
xcodebuild test -scheme ProductReview -destination 'platform=iOS Simulator,name=iPhone 15'
```

## Resources

- [SwiftUI Documentation](https://developer.apple.com/documentation/swiftui)
- [Swift Concurrency](https://docs.swift.org/swift-book/documentation/the-swift-programming-language/concurrency/)
- [Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)
