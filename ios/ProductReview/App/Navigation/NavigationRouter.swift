//
//  NavigationRouter.swift
//  ProductReview
//
//  Centralized navigation management using NavigationStack
//

import SwiftUI

// MARK: - Route Definitions
enum Route: Hashable {
    case productDetail(productId: Int)
    case wishlist
    case notifications
    case notificationDetail(notificationId: Int)
    case aiAssistant(productId: Int, productName: String)
}

// MARK: - Navigation Router
@MainActor
class NavigationRouter: ObservableObject {
    @Published var path = NavigationPath()

    func navigate(to route: Route) {
        path.append(route)
    }

    func pop() {
        if !path.isEmpty {
            path.removeLast()
        }
    }

    func popToRoot() {
        path = NavigationPath()
    }
}
