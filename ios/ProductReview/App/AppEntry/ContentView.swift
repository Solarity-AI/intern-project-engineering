//
//  ContentView.swift
//  ProductReview
//
//  Root content view with navigation
//

import SwiftUI

enum AppTab: Hashable {
    case products
    case wishlist
    case notifications
    case settings
}

struct ContentView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var navigationRouter = NavigationRouter()

    var body: some View {
        ZStack {
            Color("AppBackground")
                .ignoresSafeArea()

            NavigationStack(path: $navigationRouter.path) {
                TabView(selection: $appState.selectedTab) {
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

                    SettingsView()
                        .tabItem {
                            Label("Settings", systemImage: "gearshape")
                        }
                        .tag(AppTab.settings)
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
        }
        .environmentObject(navigationRouter)
        .task(id: appState.pendingProductId) {
            guard let productId = appState.pendingProductId else { return }

            // Always route deep links from a clean navigation stack.
            appState.selectedTab = .products
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

private struct SettingsView: View {
    @EnvironmentObject private var themeManager: ThemeManager

    var body: some View {
        Form {
            Section("Appearance") {
                Picker("Theme", selection: $themeManager.currentTheme) {
                    ForEach(AppTheme.allCases, id: \.self) { theme in
                        Label(theme.displayName, systemImage: theme.icon)
                            .tag(theme)
                    }
                }
                .pickerStyle(.menu)
                .listRowBackground(Color("CardBackground"))
            }
        }
        .scrollContentBackground(.hidden)
        .background(Color("AppBackground"))
        .navigationTitle("Settings")
    }
}

#Preview {
    ContentView()
        .environmentObject(AppState())
        .environmentObject(ThemeManager.shared)
}
