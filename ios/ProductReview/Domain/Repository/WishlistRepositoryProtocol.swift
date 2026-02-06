//
//  WishlistRepositoryProtocol.swift
//  ProductReview
//
//  Protocol defining wishlist data operations
//

import Foundation

protocol WishlistRepositoryProtocol {
    func getWishlistIds() async throws -> [Int]
    func getWishlistProducts(page: Int, size: Int, sort: String?) async throws -> (products: [Product], totalPages: Int, isLast: Bool)
    func toggleWishlist(productId: Int) async throws
    func isInWishlist(productId: Int) async -> Bool
}
