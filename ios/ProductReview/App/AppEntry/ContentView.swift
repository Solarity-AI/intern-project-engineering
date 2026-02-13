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

struct BrandTopBarTitle: View {
    let title: String

    var body: some View {
        HStack(spacing: 8) {
            Image("TopBarLogo")
                .resizable()
                .scaledToFill()
                .frame(width: 24, height: 24)
                .clipShape(RoundedRectangle(cornerRadius: 6))
                .overlay {
                    RoundedRectangle(cornerRadius: 6)
                        .stroke(Color(.separator), lineWidth: 0.5)
                }
                .accessibilityHidden(true)

            Text(title)
                .font(.headline)
                .lineLimit(1)
        }
    }
}

struct ContentView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var navigationRouter = NavigationRouter()

    private var currentTabTitle: String {
        switch appState.selectedTab {
        case .products:
            return "Products"
        case .wishlist:
            return "Wishlist"
        case .notifications:
            return "Notifications"
        case .settings:
            return "Settings"
        }
    }

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
                .navigationTitle(currentTabTitle)
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    if navigationRouter.path.isEmpty {
                        ToolbarItem(placement: .principal) {
                            if appState.selectedTab == .products {
                                BrandTopBarTitle(title: AppConstants.UI.appDisplayName)
                            } else {
                                Text(currentTabTitle)
                                    .font(.headline)
                                    .lineLimit(1)
                            }
                        }
                    }
                }
                .toolbar(.visible, for: .navigationBar)
                .toolbarBackground(.visible, for: .navigationBar)
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

    private var appVersion: String {
        let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "Unknown"
        let build = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "Unknown"
        return "\(version) (\(build))"
    }

    var body: some View {
        Form {
            // MARK: - Appearance
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

            // MARK: - About
            Section("About") {
                // App Version
                HStack {
                    Label("Version", systemImage: "info.circle")
                    Spacer()
                    Text(appVersion)
                        .foregroundStyle(.secondary)
                }
                .listRowBackground(Color("CardBackground"))

                // Feedback
                Link(destination: URL(string: "https://github.com/anthropics/claude-code/issues")!) {
                    HStack {
                        Label("Send Feedback", systemImage: "envelope")
                        Spacer()
                        Image(systemName: "arrow.up.right")
                            .font(.caption)
                            .foregroundStyle(.tertiary)
                    }
                }
                .listRowBackground(Color("CardBackground"))

                // Rate App
                Button {
                    if let url = URL(string: "https://apps.apple.com/app/id123456789?action=write-review") {
                        UIApplication.shared.open(url)
                    }
                } label: {
                    HStack {
                        Label("Rate on App Store", systemImage: "star")
                        Spacer()
                        Image(systemName: "arrow.up.right")
                            .font(.caption)
                            .foregroundStyle(.tertiary)
                    }
                }
                .foregroundStyle(.primary)
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
