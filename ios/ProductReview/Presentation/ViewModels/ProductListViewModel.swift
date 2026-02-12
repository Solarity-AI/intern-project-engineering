//
//  ProductListViewModel.swift
//  ProductReview
//
//  ViewModel for ProductListView
//

import Foundation

enum ProductSortDirection: String, Identifiable {
    case ascending
    case descending

    var id: String { rawValue }

    var queryValue: String {
        switch self {
        case .ascending:
            return "asc"
        case .descending:
            return "desc"
        }
    }

    var label: String {
        switch self {
        case .ascending:
            return "Ascending"
        case .descending:
            return "Descending"
        }
    }

    var icon: String {
        switch self {
        case .ascending:
            return "arrow.up"
        case .descending:
            return "arrow.down"
        }
    }

    func toggled() -> ProductSortDirection {
        self == .ascending ? .descending : .ascending
    }
}

enum ProductSortCriterion: String, CaseIterable, Identifiable {
    case alphabetical
    case price
    case rating
    case reviews

    var id: String { rawValue }

    var label: String {
        switch self {
        case .alphabetical:
            return "Alphabetical"
        case .price:
            return "Price"
        case .rating:
            return "Rating"
        case .reviews:
            return "Reviews"
        }
    }

    var field: String {
        switch self {
        case .alphabetical:
            return "name"
        case .price:
            return "price"
        case .rating:
            return "averageRating"
        case .reviews:
            return "reviewCount"
        }
    }

    var defaultDirection: ProductSortDirection {
        switch self {
        case .alphabetical, .price:
            return .ascending
        case .rating, .reviews:
            return .descending
        }
    }
}

@MainActor
class ProductListViewModel: ObservableObject {
    @Published var products: [Product] = []
    @Published var globalStats: GlobalStats?
    @Published var isLoading = false
    @Published var error: String?
    @Published var showToast = false
    @Published var toastMessage = ""
    @Published var toastType: ToastType = .error
    @Published var selectedSortCriterion: ProductSortCriterion = .alphabetical
    @Published var sortDirection: ProductSortDirection = .ascending
    @Published private(set) var wishlistProductIds: Set<Int> = []

    private let repository: ProductRepositoryProtocol
    private let wishlistRepository: WishlistRepositoryProtocol
    private var currentPage = 0
    private var totalPages = 1
    private var isLast = false
    private var currentCategory: String?
    private var currentSearch: String?
    private var searchTask: Task<Void, Never>?
    private var wishlistObserver: NSObjectProtocol?

    init(
        repository: ProductRepositoryProtocol = ProductRepository(),
        wishlistRepository: WishlistRepositoryProtocol = WishlistRepository()
    ) {
        self.repository = repository
        self.wishlistRepository = wishlistRepository
        setupWishlistObserver()
    }

    deinit {
        if let wishlistObserver {
            NotificationCenter.default.removeObserver(wishlistObserver)
        }
    }

    private var sortQuery: String {
        "\(selectedSortCriterion.field),\(sortDirection.queryValue)"
    }

    func loadProducts() async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }
        error = nil

        do {
            globalStats = try await repository.getGlobalStats(category: currentCategory, search: currentSearch)

            let result = try await repository.getProducts(
                page: 0,
                size: AppConstants.Pagination.defaultPageSize,
                category: currentCategory,
                search: currentSearch,
                sort: sortQuery
            )

            products = result.products
            totalPages = result.totalPages
            isLast = result.isLast
            currentPage = 0
            await refreshWishlistIds()
        } catch is CancellationError {
            // Ignore cancellation errors - they happen when user navigates away or refreshes
            return
        } catch {
            showError(error.localizedDescription)
        }
    }

    private func showError(_ message: String) {
        error = message
        toastMessage = message
        toastType = .error
        showToast = true
    }

    private func showSuccess(_ message: String) {
        error = nil
        toastMessage = message
        toastType = .success
        showToast = true
    }

    private func setupWishlistObserver() {
        wishlistObserver = NotificationCenter.default.addObserver(
            forName: .wishlistChanged,
            object: nil,
            queue: .main
        ) { [weak self] notification in
            guard let self = self else { return }
            guard let productId = notification.userInfo?["productId"] as? Int else { return }
            guard let isInWishlist = notification.userInfo?["isInWishlist"] as? Bool else { return }

            if isInWishlist {
                self.wishlistProductIds.insert(productId)
            } else {
                self.wishlistProductIds.remove(productId)
            }
        }
    }

    private func refreshWishlistIds() async {
        do {
            let ids = try await wishlistRepository.getWishlistIds()
            wishlistProductIds = Set(ids)
        } catch {
            // Keep previous wishlist state if refresh fails.
        }
    }

    func isInWishlist(productId: Int) -> Bool {
        wishlistProductIds.contains(productId)
    }

    func toggleWishlist(productId: Int) async {
        do {
            try await wishlistRepository.toggleWishlist(productId: productId)
            let isInWishlist = await wishlistRepository.isInWishlist(productId: productId)

            if isInWishlist {
                wishlistProductIds.insert(productId)
            } else {
                wishlistProductIds.remove(productId)
            }

            let message = isInWishlist ? "Added to wishlist" : "Removed from wishlist"
            showSuccess(message)
        } catch is CancellationError {
            // Ignore cancellation errors
            return
        } catch {
            showError(error.localizedDescription)
        }
    }

    func loadMore() async {
        guard !isLoading, !isLast else { return }
        isLoading = true
        defer { isLoading = false }

        do {
            let result = try await repository.getProducts(
                page: currentPage + 1,
                size: AppConstants.Pagination.defaultPageSize,
                category: currentCategory,
                search: currentSearch,
                sort: sortQuery
            )

            products.append(contentsOf: result.products)
            totalPages = result.totalPages
            isLast = result.isLast
            currentPage += 1
        } catch is CancellationError {
            // Ignore cancellation errors
            return
        } catch {
            showError(error.localizedDescription)
        }
    }

    func refresh() async {
        currentPage = 0
        isLast = false
        await loadProducts()
    }

    func filterByCategory(_ category: String?) async {
        currentCategory = category
        currentPage = 0
        isLast = false
        await loadProducts()
    }

    func updateSortCriterion(_ criterion: ProductSortCriterion) async {
        guard selectedSortCriterion != criterion else { return }
        selectedSortCriterion = criterion
        sortDirection = criterion.defaultDirection
        currentPage = 0
        isLast = false
        await loadProducts()
    }

    func toggleSortDirection() async {
        sortDirection = sortDirection.toggled()
        currentPage = 0
        isLast = false
        await loadProducts()
    }

    func search(query: String) async {
        // Cancel previous search task
        searchTask?.cancel()

        // Debounce search
        searchTask = Task {
            try? await Task.sleep(nanoseconds: 500_000_000) // 0.5 second debounce

            guard !Task.isCancelled else { return }

            currentSearch = query.isEmpty ? nil : query
            currentPage = 0
            isLast = false
            await loadProducts()
        }
    }
}
