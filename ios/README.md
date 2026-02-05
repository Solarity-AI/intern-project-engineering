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
│   │       └── Constants.swift
│   │
│   ├── Data/
│   │   ├── Network/            # API clients, DTOs
│   │   │   ├── APIClient.swift
│   │   │   └── DTOs.swift
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

1. **Open in Xcode**
   ```bash
   cd ios
   # Create Xcode project manually or use swift package init
   open .
   ```

2. **Create Xcode Project**
   - Open Xcode
   - Create new iOS App project
   - Select SwiftUI for interface
   - Set minimum deployment target to iOS 17.0
   - Copy source files into the project structure

3. **Build & Run**
   - Select target device/simulator
   - Press Cmd+R to build and run

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
https://product-review-app-ybmf.onrender.com
```

Configuration is in `App/Core/Constants.swift`:
```swift
enum AppConstants {
    enum API {
        static let baseURL = "https://product-review-app-ybmf.onrender.com"
        static let timeoutInterval: TimeInterval = 30.0
    }
}
```

## User Identification

The app generates a unique device UUID on first launch, stored in UserDefaults:
- Key: `device_user_id`
- Sent as `X-User-ID` header on all API requests
- Enables per-device wishlist and notifications

## Next Steps

To complete the iOS implementation:

1. **Create Xcode Project**
   - Initialize new SwiftUI project in Xcode
   - Configure signing & capabilities
   - Set up asset catalog (app icons, colors)

2. **Add Missing Features**
   - Offline detection and banner
   - Search history persistence
   - Theme toggle (light/dark mode)
   - Error retry mechanisms

3. **Testing**
   - Unit tests for ViewModels
   - Integration tests for repositories
   - UI tests for critical flows

4. **Polish**
   - Loading skeletons
   - Animations and transitions
   - Accessibility support
   - Localization

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
