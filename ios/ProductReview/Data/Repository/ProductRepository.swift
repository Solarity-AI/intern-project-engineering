//
//  ProductRepository.swift
//  ProductReview
//
//  Implementation of ProductRepositoryProtocol
//

import Foundation

final class ProductRepository: ProductRepositoryProtocol {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    // MARK: - Products

    func getProducts(
        page: Int,
        size: Int,
        category: String?,
        search: String?,
        sort: String?
    ) async throws -> (products: [Product], totalPages: Int, isLast: Bool) {
        var params: [String: String] = [
            "page": String(page),
            "size": String(size)
        ]

        if let category = category, !category.isEmpty {
            params["category"] = category
        }
        if let search = search, !search.isEmpty {
            params["search"] = search
        }
        if let sort = sort {
            params["sort"] = sort
        }

        let response: PageResponse<ProductDTO> = try await apiClient.request(
            endpoint: "/api/products",
            queryParams: params
        )

        let products = ProductMapper.map(response.content)
        return (products, response.totalPages, response.last)
    }

    func getProduct(id: Int) async throws -> Product {
        let dto: ProductDTO = try await apiClient.request(endpoint: "/api/products/\(id)")
        return ProductMapper.map(dto)
    }

    func getGlobalStats(category: String?, search: String?) async throws -> GlobalStats {
        var params: [String: String] = [:]
        if let category = category, !category.isEmpty {
            params["category"] = category
        }
        if let search = search, !search.isEmpty {
            params["search"] = search
        }

        let dto: GlobalStatsDTO = try await apiClient.request(
            endpoint: "/api/products/stats",
            queryParams: params.isEmpty ? nil : params
        )

        return GlobalStats(
            totalProducts: dto.totalProducts,
            totalReviews: dto.totalReviews,
            averageRating: dto.averageRating
        )
    }

    // MARK: - Reviews

    func getReviews(
        productId: Int,
        page: Int,
        size: Int,
        rating: Int?,
        sort: String?
    ) async throws -> (reviews: [Review], totalPages: Int, isLast: Bool) {
        var params: [String: String] = [
            "page": String(page),
            "size": String(size)
        ]

        if let rating = rating {
            params["rating"] = String(rating)
        }
        if let sort = sort {
            params["sort"] = sort
        }

        let response: PageResponse<ReviewDTO> = try await apiClient.request(
            endpoint: "/api/products/\(productId)/reviews",
            queryParams: params
        )

        let reviews = ReviewMapper.map(response.content)
        return (reviews, response.totalPages, response.last)
    }

    func addReview(productId: Int, reviewerName: String, rating: Int, comment: String) async throws -> Review {
        let request = ReviewRequest(reviewerName: reviewerName, rating: rating, comment: comment)

        let dto: ReviewDTO = try await apiClient.request(
            endpoint: "/api/products/\(productId)/reviews",
            method: .post,
            body: request
        )

        guard let review = ReviewMapper.map(dto) else {
            throw APIError.invalidResponse
        }
        return review
    }

    func markReviewAsHelpful(reviewId: Int) async throws -> Review {
        let dto: ReviewDTO = try await apiClient.request(
            endpoint: "/api/products/reviews/\(reviewId)/helpful",
            method: .put
        )

        guard let review = ReviewMapper.map(dto) else {
            throw APIError.invalidResponse
        }
        return review
    }

    func getUserVotedReviewIds() async throws -> [Int] {
        let ids: [Int] = try await apiClient.request(endpoint: "/api/products/reviews/voted")
        return ids
    }

    // MARK: - AI

    func chatWithAI(productId: Int, question: String) async throws -> String {
        let request = ChatRequest(question: question)

        let response: ChatResponse = try await apiClient.request(
            endpoint: "/api/products/\(productId)/chat",
            method: .post,
            body: request
        )

        return response.answer
    }
}
