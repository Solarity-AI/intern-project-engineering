//
//  Review.swift
//  ProductReview
//
//  Domain model for Review
//

import Foundation

struct Review: Identifiable, Hashable {
    let id: Int
    let reviewerName: String
    let rating: Int
    let comment: String
    let helpfulCount: Int
    let createdAt: Date?
    let productId: Int?

    // Formatted date
    var formattedDate: String {
        guard let date = createdAt else { return "" }
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        return formatter.localizedString(for: date, relativeTo: Date())
    }

    // Rating as stars
    var starsText: String {
        let safe = min(max(rating, 0), 5)
        return String(repeating: "★", count: safe) + String(repeating: "☆", count: 5 - safe)
    }
}

// MARK: - Mock Data
extension Review {
    static let mock = Review(
        id: 1,
        reviewerName: "John Doe",
        rating: 5,
        comment: "Excellent product! Highly recommended for anyone looking for quality.",
        helpfulCount: 42,
        createdAt: Date().addingTimeInterval(-86400),
        productId: 1
    )

    static let mockList: [Review] = [
        Review(id: 1, reviewerName: "John Doe", rating: 5, comment: "Amazing product!", helpfulCount: 42, createdAt: Date().addingTimeInterval(-86400), productId: 1),
        Review(id: 2, reviewerName: "Jane Smith", rating: 4, comment: "Great quality, fast shipping.", helpfulCount: 28, createdAt: Date().addingTimeInterval(-172800), productId: 1),
        Review(id: 3, reviewerName: "Bob Wilson", rating: 3, comment: "Good but a bit expensive.", helpfulCount: 15, createdAt: Date().addingTimeInterval(-259200), productId: 1)
    ]
}
