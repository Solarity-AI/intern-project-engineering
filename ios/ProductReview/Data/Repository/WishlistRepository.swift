//
//  WishlistRepository.swift
//  ProductReview
//
//  Implementation of WishlistRepositoryProtocol
//

import Foundation

final class WishlistRepository: WishlistRepositoryProtocol {
    private enum UserInfoKey {
        static let productId = "productId"
        static let isInWishlist = "isInWishlist"
    }

    private let apiClient: APIClient
    private var cachedIds: Set<Int> = []
    private let cacheKey = AppConstants.StorageKeys.wishlist

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
        loadCachedIds()
    }

    private func loadCachedIds() {
        if let data = UserDefaults.standard.data(forKey: cacheKey),
           let ids = try? JSONDecoder().decode([Int].self, from: data) {
            cachedIds = Set(ids)
        }
    }

    private func saveCachedIds() {
        if let data = try? JSONEncoder().encode(Array(cachedIds)) {
            UserDefaults.standard.set(data, forKey: cacheKey)
        }
    }

    func getWishlistIds() async throws -> [Int] {
        let ids: [Int] = try await apiClient.request(endpoint: "/api/user/wishlist")
        cachedIds = Set(ids)
        saveCachedIds()
        return ids
    }

    func getWishlistProducts(
        page: Int,
        size: Int,
        sort: String?
    ) async throws -> (products: [Product], totalPages: Int, isLast: Bool) {
        var params: [String: String] = [
            "page": String(page),
            "size": String(size)
        ]

        if let sort = sort {
            params["sort"] = sort
        }

        let response: PageResponse<ProductDTO> = try await apiClient.request(
            endpoint: "/api/user/wishlist/products",
            queryParams: params
        )

        let products = ProductMapper.map(response.content)
        cachedIds = Set(products.map(\.id))
        saveCachedIds()
        return (products, response.totalPages, response.last)
    }

    func toggleWishlist(productId: Int) async throws {
        try await apiClient.requestVoid(
            endpoint: "/api/user/wishlist/\(productId)",
            method: .post
        )

        // Re-sync from server to avoid stale cache drift across repository instances.
        do {
            let ids: [Int] = try await apiClient.request(endpoint: "/api/user/wishlist")
            cachedIds = Set(ids)
        } catch {
            // Fall back to local toggle if server refresh fails after a successful toggle call.
            if cachedIds.contains(productId) {
                cachedIds.remove(productId)
            } else {
                cachedIds.insert(productId)
            }
        }

        saveCachedIds()

        NotificationCenter.default.post(
            name: .wishlistChanged,
            object: nil,
            userInfo: [
                UserInfoKey.productId: productId,
                UserInfoKey.isInWishlist: cachedIds.contains(productId)
            ]
        )
    }

    func isInWishlist(productId: Int) async -> Bool {
        // Prefer server truth when reachable; fall back to local cache offline.
        do {
            let ids: [Int] = try await apiClient.request(endpoint: "/api/user/wishlist")
            cachedIds = Set(ids)
            saveCachedIds()
        } catch {
            loadCachedIds()
        }
        return cachedIds.contains(productId)
    }
}
