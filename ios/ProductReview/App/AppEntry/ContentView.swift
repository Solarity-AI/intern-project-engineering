//
//  ContentView.swift
//  ProductReview
//
//  Root content view with navigation
//

import SwiftUI

struct ContentView: View {
    private enum AppTab: Hashable {
        case products
        case wishlist
        case notifications
    }

    @EnvironmentObject var appState: AppState
    @StateObject private var navigationRouter = NavigationRouter()
    @State private var selectedTab: AppTab = .products

    var body: some View {
        NavigationStack(path: $navigationRouter.path) {
            TabView(selection: $selectedTab) {
                ProductListView()
                    .tabItem {
                        Label("Products", systemImage: "bag")
                    }
                    .tag(AppTab.products)

                WishlistView()
                    .tabItem {
                        Label("Wishlist", systemImage: "heart")
                    }
                    .tag(AppTab.wishlist)

                NotificationsView()
                    .tabItem {
                        Label("Notifications", systemImage: "bell")
                    }
                    .badge(appState.notificationBadgeCount)
                    .tag(AppTab.notifications)
            }
            .navigationDestination(for: Route.self) { route in
                switch route {
                case .productDetail(let productId):
                    ProductDetailView(productId: productId)
                case .wishlist:
                    WishlistView()
                case .notifications:
                    NotificationsView()
                case .notificationDetail(let notification):
                    NotificationDetailView(notification: notification)
                case .aiAssistant(let productId, let productName):
                    AIAssistantView(productId: productId, productName: productName)
                }
            }
        }
        .environmentObject(navigationRouter)
        .task(id: appState.pendingProductId) {
            guard let productId = appState.pendingProductId else { return }

            // Always route deep links from a clean navigation stack.
            selectedTab = .products
            navigationRouter.popToRoot()
            navigationRouter.navigate(to: .productDetail(productId: productId))

            // Clear pending product after navigation completes.
            try? await Task.sleep(nanoseconds: 500_000_000)
            if appState.pendingProductId == productId {
                appState.pendingProductId = nil
            }
        }
    }
}

#Preview {
    ContentView()
        .environmentObject(AppState())
}
