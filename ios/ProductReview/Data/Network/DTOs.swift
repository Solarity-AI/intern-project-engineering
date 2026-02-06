//
//  DTOs.swift
//  ProductReview
//
//  Data Transfer Objects matching the backend API
//

import Foundation

// MARK: - Page Response
struct PageResponse<T: Decodable>: Decodable {
    let content: [T]
    let totalElements: Int
    let totalPages: Int
    let number: Int
    let size: Int
    let last: Bool
}

// MARK: - Product DTO
struct ProductDTO: Decodable, Identifiable {
    let id: Int
    let name: String
    let description: String
    let categories: [String]
    let price: Double
    let imageUrl: String?
    let averageRating: Double?
    let reviewCount: Int?
    let ratingBreakdown: [String: Int]?
    let aiSummary: String?
}

// MARK: - Review DTO
struct ReviewDTO: Codable, Identifiable {
    let id: Int?
    let reviewerName: String?
    let rating: Int
    let comment: String
    let helpfulCount: Int?
    let createdAt: String?
    let productId: Int?
}

// MARK: - Review Request
struct ReviewRequest: Encodable {
    let reviewerName: String
    let rating: Int
    let comment: String
}

// MARK: - Global Stats DTO
struct GlobalStatsDTO: Decodable {
    let totalProducts: Int
    let totalReviews: Int
    let averageRating: Double
}

// MARK: - Notification DTO
struct NotificationDTO: Decodable, Identifiable {
    let id: Int
    let title: String
    let message: String
    let isRead: Bool
    let createdAt: String
    let productId: Int?
}

// MARK: - Notification Create Request
struct NotificationCreateRequest: Encodable {
    let title: String
    let message: String
    let productId: Int?
}

// MARK: - Unread Count Response
struct UnreadCountResponse: Decodable {
    let count: Int
}

// MARK: - Chat Request
struct ChatRequest: Encodable {
    let question: String
}

// MARK: - Chat Response
struct ChatResponse: Decodable {
    let answer: String
}
