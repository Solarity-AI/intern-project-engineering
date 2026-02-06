//
//  Product.swift
//  ProductReview
//
//  Domain model for Product
//

import Foundation

struct Product: Identifiable, Hashable {
    let id: Int
    let name: String
    let description: String
    let categories: [String]
    let price: Double
    let imageUrl: String?
    let averageRating: Double
    let reviewCount: Int
    let ratingBreakdown: [Int: Int]
    let aiSummary: String?

    // Formatted price string
    var formattedPrice: String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = "USD"
        return formatter.string(from: NSNumber(value: price)) ?? "$\(price)"
    }

    // Categories as comma-separated string
    var categoriesText: String {
        categories.joined(separator: ", ")
    }

    // Formatted rating
    var formattedRating: String {
        String(format: "%.1f", averageRating)
    }
}

// MARK: - Mock Data
extension Product {
    static let mock = Product(
        id: 1,
        name: "iPhone 15 Pro",
        description: "The most advanced iPhone ever with titanium design and A17 Pro chip.",
        categories: ["Electronics", "Smartphones"],
        price: 999.99,
        imageUrl: "https://images.unsplash.com/photo-1695048133142-1a20484d2569",
        averageRating: 4.7,
        reviewCount: 245,
        ratingBreakdown: [5: 150, 4: 60, 3: 20, 2: 10, 1: 5],
        aiSummary: "Based on customer reviews, this product is highly praised for its premium build quality and performance."
    )
}
