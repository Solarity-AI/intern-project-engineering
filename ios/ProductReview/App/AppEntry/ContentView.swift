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
        .onChange(of: appState.pendingProductId) { oldValue, newValue in
            // Handle deep link navigation
            if let productId = newValue {
                selectedTab = .products
                navigationRouter.navigate(to: .productDetail(productId: productId))
                // Clear pending product after navigation
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    appState.pendingProductId = nil
                }
            }
        }
    }
}

#Preview {
    ContentView()
        .environmentObject(AppState())
}
