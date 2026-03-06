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
        static let baseURL = "https://solarity-review-api.onrender.com"
        static let timeoutInterval: TimeInterval = 8.0
        static let aiTimeoutInterval: TimeInterval = 20.0 // Longer timeout for AI calls
        static let maxRetries: Int = 3
    }

    // MARK: - Storage Keys
    enum StorageKeys {
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
        static let appDisplayName = "Product Review"
        static let animationDuration: Double = 0.3
        static let cornerRadius: CGFloat = 12
        static let shadowRadius: CGFloat = 4
    }
}
