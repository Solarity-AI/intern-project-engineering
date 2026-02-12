//
//  WishlistView.swift
//  ProductReview
//
//  Wishlist screen with product management
//

import SwiftUI

struct WishlistView: View {
    @EnvironmentObject var navigationRouter: NavigationRouter
    @EnvironmentObject var appState: AppState
    @StateObject private var viewModel = WishlistViewModel()

    private let categories = ["All", "Electronics", "Smartphones", "Laptops", "Tablets", "Gaming", "Wearables", "Audio", "Accessories"]
    private let contentHorizontalPadding: CGFloat = 16
    private let gridHorizontalSpacing: CGFloat = 20
    private let gridVerticalSpacing: CGFloat = 16
    private var cardWidth: CGFloat {
        let availableWidth = UIScreen.main.bounds.width - (contentHorizontalPadding * 2) - gridHorizontalSpacing
        return max(140, floor(availableWidth / 2))
    }
    private let cardHeight: CGFloat = 260
    private var columns: [GridItem] {
        [
            GridItem(.fixed(cardWidth), spacing: gridHorizontalSpacing),
            GridItem(.fixed(cardWidth), spacing: gridHorizontalSpacing)
        ]
    }

    private var selectedCategoryTitle: String {
        viewModel.selectedCategory ?? "All Categories"
    }

    @ViewBuilder
    private var filterSortSection: some View {
        HStack {
            Menu {
                ForEach(categories, id: \.self) { category in
                    let categoryValue = category == "All" ? nil : category
                    Button {
                        Task {
                            await viewModel.filterByCategory(categoryValue)
                        }
                    } label: {
                        if viewModel.selectedCategory == categoryValue {
                            Label(category, systemImage: "checkmark")
                        } else {
                            Text(category)
                        }
                    }
                }
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "line.3.horizontal.decrease.circle")
                    Text(selectedCategoryTitle)
                        .lineLimit(1)
                }
                .font(.subheadline.weight(.medium))
                .foregroundStyle(Color("PrimaryText"))
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(Color("CardBackground"), in: Capsule())
                .overlay {
                    Capsule()
                        .stroke(Color("Border"), lineWidth: 1)
                }
            }
            .accessibilityLabel("Filter by category")
            .accessibilityHint("Select a category to filter wishlist products")

            Spacer(minLength: 24)

            HStack(spacing: 6) {
                Menu {
                    ForEach(ProductSortCriterion.allCases) { criterion in
                        Button {
                            Task {
                                await viewModel.updateSortCriterion(criterion)
                            }
                        } label: {
                            if viewModel.selectedSortCriterion == criterion {
                                Label(criterion.label, systemImage: "checkmark")
                            } else {
                                Text(criterion.label)
                            }
                        }
                    }
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "arrow.up.arrow.down.circle")
                        Text(viewModel.selectedSortCriterion.label)
                            .lineLimit(1)
                    }
                    .font(.subheadline.weight(.medium))
                    .foregroundStyle(Color("PrimaryText"))
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(Color("CardBackground"), in: Capsule())
                    .overlay {
                        Capsule()
                            .stroke(Color("Border"), lineWidth: 1)
                    }
                }
                .accessibilityLabel("Sort wishlist")
                .accessibilityHint("Select sorting criterion for wishlist products")

                Button {
                    Task {
                        await viewModel.toggleSortDirection()
                    }
                } label: {
                    Image(systemName: viewModel.sortDirection.icon)
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(Color.black)
                        .animation(.easeInOut(duration: 0.2), value: viewModel.sortDirection)
                        .frame(width: 34, height: 34)
                        .background(Color("CardBackground"), in: Circle())
                        .overlay {
                            Circle()
                                .stroke(Color("Border"), lineWidth: 1)
                        }
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Sort direction: \(viewModel.sortDirection.label)")
                .accessibilityHint("Double tap to toggle sort direction")
            }
        }
        .padding(.horizontal, contentHorizontalPadding)
    }

    @ViewBuilder
    private var filteredEmptyState: some View {
        VStack(spacing: 12) {
            Image(systemName: "line.3.horizontal.decrease.circle")
                .font(.system(size: 42))
                .foregroundStyle(.secondary)
            Text("No wishlist products match this category")
                .font(.headline)
                .multilineTextAlignment(.center)
            Button("Clear Category Filter") {
                Task { await viewModel.filterByCategory(nil) }
            }
            .buttonStyle(.bordered)
        }
        .padding(.horizontal, contentHorizontalPadding)
        .padding(.top, 24)
    }

    var body: some View {
        ZStack {
            Color("AppBackground")
                .ignoresSafeArea()

            Group {
                if viewModel.products.isEmpty && !viewModel.isLoading && viewModel.error != nil {
                    EmptyStateView.error(message: viewModel.error ?? "Unknown error") {
                        Task { await viewModel.loadWishlist() }
                    }
                } else if viewModel.products.isEmpty && !viewModel.isLoading && viewModel.selectedCategory == nil {
                    EmptyStateView.wishlist {
                        appState.selectedTab = .products
                        navigationRouter.popToRoot()
                    }
                } else {
                    VStack(spacing: 12) {
                        filterSortSection

                        ScrollView {
                            VStack(spacing: 0) {
                                if viewModel.products.isEmpty && !viewModel.isLoading {
                                    filteredEmptyState
                                } else {
                                    LazyVGrid(columns: columns, spacing: gridVerticalSpacing) {
                                        ForEach(viewModel.products) { product in
                                            ZStack(alignment: .topTrailing) {
                                                WishlistProductCard(
                                                    product: product,
                                                    cardWidth: cardWidth,
                                                    onTap: {
                                                        navigationRouter.navigate(to: .productDetail(productId: product.id))
                                                    }
                                                )
                                                .frame(width: cardWidth, height: cardHeight)

                                                Button {
                                                    Task { await viewModel.removeFromWishlist(productId: product.id) }
                                                } label: {
                                                    Image(systemName: "heart.fill")
                                                        .font(.system(size: 14, weight: .semibold))
                                                        .foregroundColor(.red)
                                                        .padding(8)
                                                        .background(.ultraThinMaterial, in: Circle())
                                                }
                                                .buttonStyle(.plain)
                                                .padding(.top, 8)
                                                .padding(.trailing, 2)
                                                .offset(x: -16, y: 6)
                                                .opacity(0.75)
                                                .accessibilityLabel("Remove from wishlist")
                                                .accessibilityHint("Double tap to remove from wishlist")
                                            }
                                            .frame(width: cardWidth, height: cardHeight)
                                            .onAppear {
                                                if product.id == viewModel.products.last?.id {
                                                    Task { await viewModel.loadMore() }
                                                }
                                            }
                                        }
                                    }
                                }

                                if viewModel.isLoading {
                                    ProgressView()
                                        .padding()
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.horizontal, contentHorizontalPadding)
                        }
                        .background(Color("AppBackground"))
                    }
                }
            }
        }
        .navigationTitle("Wishlist")
        .navigationBarTitleDisplayMode(.inline)
        .refreshable {
            await viewModel.refresh()
        }
        .task {
            await viewModel.loadWishlist()
        }
        .toast(
            isPresented: $viewModel.showToast,
            message: viewModel.toastMessage,
            type: viewModel.toastType,
            duration: 3.0,
            actionTitle: viewModel.toastActionTitle,
            onAction: {
                viewModel.handleToastAction()
            }
        )
        .alert("Error", isPresented: Binding(
            get: { viewModel.error != nil },
            set: { if !$0 { viewModel.error = nil } }
        )) {
            Button("OK") { viewModel.error = nil }
        } message: {
            Text(viewModel.error ?? "")
        }
    }

}

// MARK: - Wishlist Product Card
struct WishlistProductCard: View {
    let product: Product
    let cardWidth: CGFloat
    let onTap: () -> Void

    private var cardInnerPadding: CGFloat {
        max(10, floor(cardWidth * 0.07))
    }

    private var imageSize: CGFloat {
        max(140, cardWidth - (cardInnerPadding * 2))
    }

    private var resolvedImageURL: URL? {
        product.resolvedImageURL
    }

    @ViewBuilder
    private var imageFallbackView: some View {
        ZStack {
            Color("CardBackground")
            VStack(spacing: 8) {
                Image(systemName: "photo.fill")
                    .font(.title)
                    .foregroundColor(.secondary)
                Text("No Image")
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }
        }
    }

    @ViewBuilder
    private var productImageView: some View {
        if let resolvedImageURL {
            CachedAsyncImage(url: resolvedImageURL) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: imageSize, height: imageSize)
                    .clipped()
            } placeholder: {
                ProgressView()
                    .frame(width: imageSize, height: imageSize)
                    .background(Color("CardBackground"))
            } failure: {
                imageFallbackView
                    .frame(width: imageSize, height: imageSize)
            }
        } else {
            imageFallbackView
                .frame(width: imageSize, height: imageSize)
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            productImageView
            .frame(width: imageSize, height: imageSize)
            .contentShape(Rectangle())
            .cornerRadius(8)
            .accessibilityHidden(true)

            Text(product.name)
                .font(.subheadline)
                .fontWeight(.medium)
                .lineLimit(2)
                .fixedSize(horizontal: false, vertical: true)

            Spacer(minLength: 0)

            HStack(spacing: 2) {
                Image(systemName: "star.fill")
                    .foregroundColor(.yellow)
                    .font(.caption)
                    .accessibilityHidden(true)
                Text(product.formattedRating)
                    .font(.caption)
                Text("(\(product.reviewCount))")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .accessibilityElement(children: .combine)
            .accessibilityLabel("\(product.formattedRating) stars, \(product.reviewCount) reviews")

            Text(product.formattedPrice)
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(.blue)
        }
        .padding(cardInnerPadding)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(Color("CardBackground"))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay {
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color(.separator), lineWidth: 1)
        }
        .onTapGesture { onTap() }
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(product.name), \(product.formattedPrice), \(product.formattedRating) stars")
        .accessibilityHint("Double tap to view details")
    }
}

// MARK: - ViewModel
@MainActor
class WishlistViewModel: ObservableObject {
    private struct RemovedItem {
        let product: Product
        let index: Int
    }

    private struct PendingRemoval {
        let id: UUID
        let items: [RemovedItem]
        let commitTask: Task<Void, Never>
    }

    enum ToastAction {
        case undoPendingRemoval
        case retryFailedRemovals
    }

    @Published var products: [Product] = []
    @Published var isLoading = false
    @Published var error: String?
    @Published var showToast = false
    @Published var toastMessage = ""
    @Published var toastType: ToastType = .info
    @Published var toastAction: ToastAction?
    @Published var selectedCategory: String? = nil
    @Published var selectedSortCriterion: ProductSortCriterion = .alphabetical
    @Published var sortDirection: ProductSortDirection = .ascending

    private let repository: WishlistRepositoryProtocol
    private var currentPage = 0
    private var isLastPage = false
    private let maxClientCategoryPages = 5
    private var pendingRemoval: PendingRemoval?
    private var failedRemovalItems: [RemovedItem] = []
    private let undoWindowNanoseconds: UInt64 = 3_000_000_000

    init(repository: WishlistRepositoryProtocol = WishlistRepository()) {
        self.repository = repository
    }

    private var sortQuery: String {
        "\(selectedSortCriterion.field),\(sortDirection.queryValue)"
    }

    private func filterBySelectedCategory(_ incomingProducts: [Product]) -> [Product] {
        guard let selectedCategory else { return incomingProducts }
        return incomingProducts.filter { product in
            product.categories.contains {
                $0.caseInsensitiveCompare(selectedCategory) == .orderedSame
            }
        }
    }

    private func itemWord(_ count: Int) -> String {
        count == 1 ? "item" : "items"
    }

    var toastActionTitle: String? {
        switch toastAction {
        case .undoPendingRemoval:
            return "Undo"
        case .retryFailedRemovals:
            return "Retry"
        case .none:
            return nil
        }
    }

    private func resetToastActionState() {
        toastAction = nil
        failedRemovalItems.removeAll()
    }

    private func failedItemsDescription(_ items: [RemovedItem]) -> String {
        let names = items.map { $0.product.name }
        if names.count <= 2 {
            return names.joined(separator: ", ")
        }
        let shown = names.prefix(2).joined(separator: ", ")
        return "\(shown) +\(names.count - 2) more"
    }

    private func showSuccessToast(_ message: String) {
        resetToastActionState()
        toastMessage = message
        toastType = .success
        showToast = true
    }

    private func showErrorToast(_ message: String) {
        resetToastActionState()
        toastMessage = message
        toastType = .error
        showToast = true
    }

    private func showWarningToast(_ message: String) {
        resetToastActionState()
        toastMessage = message
        toastType = .warning
        showToast = true
    }

    private func showUndoToast(_ message: String) {
        toastAction = .undoPendingRemoval
        toastMessage = message
        toastType = .success
        showToast = true
    }

    private func showRetryToast(_ message: String, type: ToastType, failedItems: [RemovedItem]) {
        toastAction = .retryFailedRemovals
        failedRemovalItems = failedItems
        toastMessage = message
        toastType = type
        showToast = true
    }

    func handleToastAction() {
        switch toastAction {
        case .undoPendingRemoval:
            undoPendingRemoval()
        case .retryFailedRemovals:
            Task { await retryFailedRemovals() }
        case .none:
            break
        }
    }

    private func clearPendingRemoval() {
        pendingRemoval?.commitTask.cancel()
        pendingRemoval = nil
        if toastAction == .undoPendingRemoval {
            toastAction = nil
        }
    }

    private func commitRemovalToServer(items: [RemovedItem], showSuccessOnComplete: Bool = true) async {
        var removedCount = 0
        var failedItems: [RemovedItem] = []

        for item in items {
            do {
                try await repository.toggleWishlist(productId: item.product.id)
                removedCount += 1
            } catch {
                failedItems.append(item)
            }
        }

        if !failedItems.isEmpty {
            await loadWishlist()
        }

        if failedItems.isEmpty {
            guard showSuccessOnComplete else { return }
            showSuccessToast("Removed \(removedCount) \(itemWord(removedCount)) from wishlist")
        } else {
            let failedNames = failedItemsDescription(failedItems)
            if removedCount == 0 {
                showRetryToast(
                    "Failed to remove \(failedItems.count) \(itemWord(failedItems.count)): \(failedNames)",
                    type: .error,
                    failedItems: failedItems
                )
            } else {
                showRetryToast(
                    "Removed \(removedCount) \(itemWord(removedCount)); failed: \(failedNames)",
                    type: .warning,
                    failedItems: failedItems
                )
            }
        }
    }

    private func commitPendingRemovalIfNeeded() async {
        guard let pendingRemoval else { return }
        self.pendingRemoval = nil
        pendingRemoval.commitTask.cancel()
        toastAction = nil
        await commitRemovalToServer(items: pendingRemoval.items, showSuccessOnComplete: false)
    }

    private func scheduleRemoval(productIds: [Int]) async {
        await commitPendingRemovalIfNeeded()

        let indexedMatches = products.enumerated().filter { entry in
            productIds.contains(entry.element.id)
        }
        guard !indexedMatches.isEmpty else { return }

        let removedItems = indexedMatches.map { RemovedItem(product: $0.element, index: $0.offset) }
        let idsToRemove = removedItems.map { $0.product.id }

        products.removeAll { idsToRemove.contains($0.id) }
        showUndoToast("Removed \(idsToRemove.count) \(itemWord(idsToRemove.count))")

        let pendingId = UUID()
        let commitTask = Task {
            try? await Task.sleep(nanoseconds: undoWindowNanoseconds)
            await commitPendingRemovalIfMatching(id: pendingId)
        }

        pendingRemoval = PendingRemoval(
            id: pendingId,
            items: removedItems,
            commitTask: commitTask
        )
    }

    private func commitPendingRemovalIfMatching(id: UUID) async {
        guard let pendingRemoval, pendingRemoval.id == id else { return }
        self.pendingRemoval = nil
        toastAction = nil
        await commitRemovalToServer(items: pendingRemoval.items, showSuccessOnComplete: false)
    }

    private func retryFailedRemovals() async {
        guard !failedRemovalItems.isEmpty else { return }
        let itemsToRetry = failedRemovalItems
        resetToastActionState()
        await commitRemovalToServer(items: itemsToRetry, showSuccessOnComplete: true)
    }

    func undoPendingRemoval() {
        guard let pendingRemoval else { return }
        clearPendingRemoval()

        for item in pendingRemoval.items.sorted(by: { $0.index < $1.index }) {
            let insertIndex = min(item.index, products.count)
            products.insert(item.product, at: insertIndex)
        }

        showSuccessToast("Restored \(pendingRemoval.items.count) \(itemWord(pendingRemoval.items.count))")
    }

    func loadWishlist() async {
        isLoading = true
        defer { isLoading = false }
        error = nil

        do {
            var page = 0
            var fetchedProducts: [Product] = []
            var isLastFetchedPage = false

            while true {
                let result = try await repository.getWishlistProducts(
                    page: page,
                    size: 20,
                    sort: sortQuery
                )
                fetchedProducts.append(contentsOf: filterBySelectedCategory(result.products))
                isLastFetchedPage = result.isLast

                if selectedCategory == nil
                    || !fetchedProducts.isEmpty
                    || isLastFetchedPage
                    || page >= maxClientCategoryPages - 1 {
                    break
                }

                page += 1
            }

            let reachedClientPageLimit = selectedCategory != nil
                && fetchedProducts.isEmpty
                && !isLastFetchedPage
                && page >= maxClientCategoryPages - 1
            products = fetchedProducts
            currentPage = page
            isLastPage = isLastFetchedPage || reachedClientPageLimit
        } catch is CancellationError {
            return
        } catch {
            self.error = error.localizedDescription
        }
    }

    func loadMore() async {
        guard !isLoading, !isLastPage else { return }
        isLoading = true
        defer { isLoading = false }

        do {
            let firstPage = currentPage + 1
            var page = firstPage
            var fetchedProducts: [Product] = []
            var isLastFetchedPage = false

            while true {
                let result = try await repository.getWishlistProducts(
                    page: page,
                    size: 20,
                    sort: sortQuery
                )
                fetchedProducts.append(contentsOf: filterBySelectedCategory(result.products))
                isLastFetchedPage = result.isLast

                if selectedCategory == nil
                    || !fetchedProducts.isEmpty
                    || isLastFetchedPage
                    || page >= firstPage + maxClientCategoryPages - 1 {
                    break
                }

                page += 1
            }

            products.append(contentsOf: fetchedProducts)
            currentPage = page
            let reachedClientPageLimit = selectedCategory != nil
                && fetchedProducts.isEmpty
                && !isLastFetchedPage
                && page >= firstPage + maxClientCategoryPages - 1
            isLastPage = isLastFetchedPage || reachedClientPageLimit
        } catch is CancellationError {
            return
        } catch {
            self.error = error.localizedDescription
        }
    }

    func refresh() async {
        currentPage = 0
        isLastPage = false
        await loadWishlist()
    }

    func filterByCategory(_ category: String?) async {
        selectedCategory = category
        await refresh()
    }

    func updateSortCriterion(_ criterion: ProductSortCriterion) async {
        guard selectedSortCriterion != criterion else { return }
        selectedSortCriterion = criterion
        sortDirection = criterion.defaultDirection
        await refresh()
    }

    func toggleSortDirection() async {
        sortDirection = sortDirection.toggled()
        await refresh()
    }

    func removeFromWishlist(productId: Int) async {
        await scheduleRemoval(productIds: [productId])
    }

    func removeMultiple(productIds: [Int]) async {
        await scheduleRemoval(productIds: productIds)
    }
}

#Preview {
    NavigationStack {
        WishlistView()
            .environmentObject(NavigationRouter())
    }
}
