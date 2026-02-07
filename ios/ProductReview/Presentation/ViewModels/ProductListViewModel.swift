//
//  ProductListViewModel.swift
//  ProductReview
//
//  ViewModel for ProductListView
//

import Foundation

@MainActor
class ProductListViewModel: ObservableObject {
    @Published var products: [Product] = []
    @Published var globalStats: GlobalStats?
    @Published var isLoading = false
    @Published var error: String?
    @Published var showToast = false
    @Published var toastMessage = ""
    @Published var toastType: ToastType = .error

    private let repository: ProductRepositoryProtocol
    private var currentPage = 0
    private var totalPages = 1
    private var isLast = false
    private var currentCategory: String?
    private var currentSearch: String?
    private var searchTask: Task<Void, Never>?

    init(repository: ProductRepositoryProtocol = ProductRepository()) {
        self.repository = repository
    }

    func loadProducts() async {
        guard !isLoading else { return }
        isLoading = true
        error = nil

        do {
            // Load stats
            globalStats = try await repository.getGlobalStats(category: currentCategory, search: currentSearch)

            // Load products
            let result = try await repository.getProducts(
                page: 0,
                size: AppConstants.Pagination.defaultPageSize,
                category: currentCategory,
                search: currentSearch,
                sort: "name,asc"
            )

            products = result.products
            totalPages = result.totalPages
            isLast = result.isLast
            currentPage = 0
        } catch is CancellationError {
            // Ignore cancellation errors - they happen when user navigates away or refreshes
            return
        } catch {
            showError(error.localizedDescription)
        }

        isLoading = false
    }

    private func showError(_ message: String) {
        toastMessage = message
        toastType = .error
        showToast = true
    }

    func loadMore() async {
        guard !isLoading, !isLast else { return }
        isLoading = true

        do {
            let result = try await repository.getProducts(
                page: currentPage + 1,
                size: AppConstants.Pagination.defaultPageSize,
                category: currentCategory,
                search: currentSearch,
                sort: "name,asc"
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

        isLoading = false
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
