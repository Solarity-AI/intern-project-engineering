//
//  ProductRepositoryProtocol.swift
//  ProductReview
//
//  Protocol defining product data operations
//

import Foundation

protocol ProductRepositoryProtocol {
    // MARK: - Products
    func getProducts(
        page: Int,
        size: Int,
        category: String?,
        search: String?,
        sort: String?
    ) async throws -> (products: [Product], totalPages: Int, isLast: Bool)

    func getProduct(id: Int) async throws -> Product
    func getGlobalStats(category: String?, search: String?) async throws -> GlobalStats

    // MARK: - Reviews
    func getReviews(
        productId: Int,
        page: Int,
        size: Int,
        rating: Int?,
        sort: String?
    ) async throws -> (reviews: [Review], totalPages: Int, isLast: Bool)

    func addReview(productId: Int, reviewerName: String, rating: Int, comment: String) async throws -> Review
    func markReviewAsHelpful(reviewId: Int) async throws -> Review
    func getUserVotedReviewIds() async throws -> [Int]

    // MARK: - AI
    func chatWithAI(productId: Int, question: String) async throws -> String
}

// MARK: - Global Stats
struct GlobalStats {
    let totalProducts: Int
    let totalReviews: Int
    let averageRating: Double
}
