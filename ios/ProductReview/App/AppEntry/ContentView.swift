//
//  ContentView.swift
//  ProductReview
//
//  Root content view with navigation
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var navigationRouter = NavigationRouter()

    var body: some View {
        NavigationStack(path: $navigationRouter.path) {
            ProductListView()
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
    }
}

#Preview {
    ContentView()
        .environmentObject(AppState())
}
