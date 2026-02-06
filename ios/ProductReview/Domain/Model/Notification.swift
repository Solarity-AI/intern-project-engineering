//
//  Notification.swift
//  ProductReview
//
//  Domain model for Notification
//

import Foundation

struct AppNotification: Identifiable, Hashable {
    let id: Int
    let title: String
    let message: String
    var isRead: Bool
    let createdAt: Date?
    let productId: Int?

    // Formatted date
    var formattedDate: String {
        guard let date = createdAt else { return "" }
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        return formatter.localizedString(for: date, relativeTo: Date())
    }
}

// MARK: - Mock Data
extension AppNotification {
    static let mock = AppNotification(
        id: 1,
        title: "New Review",
        message: "Someone commented on iPhone 15 Pro",
        isRead: false,
        createdAt: Date().addingTimeInterval(-3600),
        productId: 1
    )

    static let mockList: [AppNotification] = [
        AppNotification(id: 1, title: "New Review", message: "Someone commented on iPhone 15 Pro", isRead: false, createdAt: Date().addingTimeInterval(-3600), productId: 1),
        AppNotification(id: 2, title: "Price Drop", message: "Samsung Galaxy S24 is now on sale!", isRead: true, createdAt: Date().addingTimeInterval(-86400), productId: 2),
        AppNotification(id: 3, title: "Order Update", message: "Your order has been shipped", isRead: false, createdAt: Date().addingTimeInterval(-172800), productId: nil)
    ]
}
