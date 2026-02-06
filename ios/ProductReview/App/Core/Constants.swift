//
//  Constants.swift
//  ProductReview
//
//  App-wide constants and configuration
//

import Foundation

enum AppConstants {
    // MARK: - API Configuration
    enum API {
        // Toggle for local development vs production
        #if DEBUG
        static let useLocalServer = true
        #else
        static let useLocalServer = false
        #endif

        static var baseURL: String {
            useLocalServer ? "http://localhost:8080" : "https://product-review-app-ybmf.onrender.com"
        }
        static let timeoutInterval: TimeInterval = 10.0
        static let aiTimeoutInterval: TimeInterval = 20.0 // Longer timeout for AI calls
    }

    // MARK: - Storage Keys
    enum StorageKeys {
        static let userId = "device_user_id"
        static let theme = "theme"
        static let wishlist = "wishlist"
        static let searchHistory = "search_history"
    }

    // MARK: - Pagination
    enum Pagination {
        static let defaultPageSize = 20
    }

    // MARK: - UI
    enum UI {
        static let animationDuration: Double = 0.3
        static let cornerRadius: CGFloat = 12
        static let shadowRadius: CGFloat = 4
    }
}
