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

    @State private var isTwoColumnGrid = true
    @State private var isSelectionMode = false
    @State private var selectedProductIDs: Set<Int> = []

    private let categories = ["All", "Electronics", "Smartphones", "Laptops", "Tablets", "Gaming", "Wearables", "Audio", "Accessories"]
    private let contentHorizontalPadding: CGFloat = AppSpacing.lg
    private let gridHorizontalSpacing: CGFloat = 20
    private let gridVerticalSpacing: CGFloat = AppSpacing.lg
    private let twoColumnCardHeight: CGFloat = 290
    private let singleColumnVerticalSpacing: CGFloat = AppSpacing.md

    private var itemCount: Int {
        viewModel.products.count
    }

    private var averageRating: Double {
        guard !viewModel.products.isEmpty else { return 0 }
        let sum = viewModel.products.reduce(0) { partialResult, product in
            partialResult + product.averageRating
        }
        return sum / Double(viewModel.products.count)
    }

    private var totalValue: Double {
        viewModel.products.reduce(0) { partialResult, product in
            partialResult + product.price
        }
    }

    private var totalValueText: String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = "USD"
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        return formatter.string(from: NSNumber(value: totalValue)) ?? String(format: "$%.0f", totalValue)
    }

    private func cardWidth(for totalWidth: CGFloat) -> CGFloat {
        if isTwoColumnGrid {
            let availableWidth = totalWidth - (contentHorizontalPadding * 2) - gridHorizontalSpacing
            return max(140, floor(availableWidth / 2))
        } else {
            return max(160, floor(totalWidth - (contentHorizontalPadding * 2)))
        }
    }

    private var currentGridVerticalSpacing: CGFloat {
        isTwoColumnGrid ? gridVerticalSpacing : singleColumnVerticalSpacing
    }

    private func columns(for cardWidth: CGFloat) -> [GridItem] {
        isTwoColumnGrid
            ? [GridItem(.fixed(cardWidth), spacing: gridHorizontalSpacing), GridItem(.fixed(cardWidth), spacing: gridHorizontalSpacing)]
            : [GridItem(.flexible())]
    }

    private func startSelection(with productID: Int) {
        isSelectionMode = true
        selectedProductIDs.insert(productID)
    }

    private func toggleSelection(for productID: Int) {
        if selectedProductIDs.contains(productID) {
            selectedProductIDs.remove(productID)
        } else {
            selectedProductIDs.insert(productID)
        }

        if selectedProductIDs.isEmpty {
            isSelectionMode = false
        }
    }

    private func cancelSelectionMode() {
        isSelectionMode = false
        selectedProductIDs.removeAll()
    }

    private func removeSelectedProducts() {
        let ids = Array(selectedProductIDs)
        guard !ids.isEmpty else { return }
        Task {
            await viewModel.removeMultiple(productIds: ids)
        }
        cancelSelectionMode()
    }

    private func clearAllProducts() {
        let ids = viewModel.products.map(\.id)
        guard !ids.isEmpty else { return }
        Task {
            await viewModel.removeMultiple(productIds: ids)
        }
        cancelSelectionMode()
    }

    private var selectedItemCountText: String {
        selectedProductIDs.count == 1 ? "item" : "items"
    }

    @ViewBuilder
    private var pageHeader: some View {
        HStack(alignment: .center, spacing: AppSpacing.sm) {
            Text("Wishlist")
                .font(.system(size: 28, weight: .bold))
                .foregroundStyle(AppColors.foreground)

            Spacer(minLength: 0)

            HStack(spacing: AppSpacing.sm) {
                Button {
                    isTwoColumnGrid.toggle()
                } label: {
                    Image(systemName: isTwoColumnGrid ? "square.grid.2x2" : "rectangle.grid.1x2")
                        .font(.system(size: AppFontSize.base, weight: .semibold))
                        .foregroundStyle(AppColors.primary)
                        .frame(width: 38, height: 38)
                        .glassCard(AppGlass.subtle, cornerRadius: AppRadius.full)
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Toggle grid columns")

                if !viewModel.products.isEmpty {
                    Button("Clear All") {
                        clearAllProducts()
                    }
                    .font(.system(size: AppFontSize.sm, weight: .semibold))
                    .foregroundStyle(AppColors.destructive)
                }
            }
        }
        .padding(.horizontal, contentHorizontalPadding)
        .padding(.top, AppSpacing.sm)
        .padding(.bottom, AppSpacing.md)
    }

    @ViewBuilder
    private var bentoStatsSection: some View {
        VStack(spacing: AppSpacing.md) {
            HStack(spacing: AppSpacing.md) {
                WishlistStatTile(
                    icon: "heart.fill",
                    iconColor: AppColors.primary,
                    value: "\(itemCount)",
                    label: "Items"
                )
                .frame(maxWidth: .infinity)

                WishlistStatTile(
                    icon: "star.fill",
                    iconColor: AppColors.starFilled,
                    value: String(format: "%.1f", averageRating),
                    label: "Avg Rating"
                )
                .frame(maxWidth: .infinity)
            }

            WishlistStatTile(
                icon: "banknote",
                iconColor: AppColors.primary,
                value: totalValueText,
                label: "Total Value"
            )
            .frame(maxWidth: .infinity)
        }
        .padding(.horizontal, contentHorizontalPadding)
    }

    @ViewBuilder
    private func categoryChip(_ category: String) -> some View {
        let categoryValue = category == "All" ? nil : category
        let isActive = viewModel.selectedCategory == categoryValue

        Button {
            Task {
                await viewModel.filterByCategory(categoryValue)
            }
        } label: {
            Text(category)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(isActive ? Color.white : AppColors.foreground.opacity(0.75))
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background {
                    if isActive {
                        Capsule().fill(AppGradients.brand)
                    }
                }
        }
        .buttonStyle(.plain)
        .modifier(WishlistChipStyleModifier(isActive: isActive))
        .accessibilityLabel("Filter by \(category)")
    }

    @ViewBuilder
    private var filterSortSection: some View {
        VStack(spacing: AppSpacing.sm) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(categories, id: \.self) { category in
                        categoryChip(category)
                    }
                }
                .padding(.horizontal, contentHorizontalPadding)
            }

            HStack(spacing: 8) {
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
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(AppColors.foreground)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .glassCard(AppGlass.subtle, cornerRadius: AppRadius.full)
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
                        .foregroundStyle(AppColors.foreground)
                        .animation(.easeInOut(duration: 0.2), value: viewModel.sortDirection)
                        .frame(width: 36, height: 36)
                        .glassCard(AppGlass.subtle, cornerRadius: AppRadius.full)
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Sort direction: \(viewModel.sortDirection.label)")
                .accessibilityHint("Double tap to toggle sort direction")

                Spacer(minLength: 0)
            }
            .padding(.horizontal, contentHorizontalPadding)
        }
    }

    @ViewBuilder
    private var filteredEmptyState: some View {
        VStack(spacing: AppSpacing.md) {
            Image(systemName: "line.3.horizontal.decrease.circle")
                .font(.system(size: 42))
                .foregroundStyle(AppColors.foreground.opacity(0.7))
            Text("No wishlist products match this category")
                .font(.headline)
                .multilineTextAlignment(.center)
                .foregroundStyle(AppColors.foreground)
            Button("Clear Category Filter") {
                Task { await viewModel.filterByCategory(nil) }
            }
            .font(.system(size: AppFontSize.sm, weight: .semibold))
            .foregroundStyle(Color.white)
            .padding(.horizontal, AppSpacing.md)
            .padding(.vertical, AppSpacing.sm)
            .background(AppGradients.brand, in: Capsule())
            .buttonStyle(.plain)
        }
        .padding(.horizontal, contentHorizontalPadding)
        .padding(.top, AppSpacing.lg)
    }

    @ViewBuilder
    private var wishlistEmptyState: some View {
        VStack(spacing: AppSpacing.lg) {
            Image(systemName: "heart.slash")
                .font(.system(size: 58))
                .foregroundStyle(AppColors.primary)

            VStack(spacing: AppSpacing.sm) {
                Text("Your wishlist is empty")
                    .font(.title3.weight(.bold))
                    .foregroundStyle(AppColors.foreground)

                Text("Add products you love to see them here")
                    .font(.body)
                    .foregroundStyle(AppColors.foreground.opacity(0.7))
                    .multilineTextAlignment(.center)
            }

            Button {
                appState.selectedTab = .products
                navigationRouter.popToRoot()
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass")
                    Text("Browse Products")
                        .fontWeight(.semibold)
                }
                .foregroundStyle(Color.white)
                .padding(.horizontal, AppSpacing.xl)
                .padding(.vertical, AppSpacing.md)
                .background(AppGradients.brand, in: Capsule())
                .glow(AppGlow.primarySoft)
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, AppSpacing.xl)
        .padding(.top, AppSpacing.x3l)
    }

    @ViewBuilder
    private var selectionActionBar: some View {
        if isSelectionMode && !selectedProductIDs.isEmpty {
            HStack(spacing: AppSpacing.sm) {
                Button("Cancel") {
                    cancelSelectionMode()
                }
                .font(.system(size: AppFontSize.sm, weight: .semibold))
                .foregroundStyle(Color.white.opacity(0.9))
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(Color.white.opacity(0.16), in: Capsule())

                Spacer(minLength: 0)

                Button {
                    removeSelectedProducts()
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "trash.fill")
                            .font(.system(size: AppFontSize.sm, weight: .semibold))
                        Text("Remove \(selectedProductIDs.count) \(selectedItemCountText)")
                            .font(.system(size: AppFontSize.sm, weight: .semibold))
                    }
                    .foregroundStyle(Color.white)
                    .padding(.horizontal, AppSpacing.md)
                    .padding(.vertical, AppSpacing.sm)
                    .background(Color.white.opacity(0.14), in: Capsule())
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, AppSpacing.md)
            .padding(.vertical, AppSpacing.md)
            .background(
                RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous)
                    .fill(AppColors.destructive.opacity(0.92))
            )
            .padding(.horizontal, contentHorizontalPadding)
            .padding(.bottom, AppSpacing.lg)
        }
    }

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                AppColors.background
                    .ignoresSafeArea()

                Group {
                    if viewModel.products.isEmpty && !viewModel.isLoading && viewModel.error != nil {
                        EmptyStateView.error(message: viewModel.error ?? "Unknown error") {
                            Task { await viewModel.loadWishlist() }
                        }
                    } else {
                        VStack(spacing: AppSpacing.md) {
                            pageHeader

                            if viewModel.products.isEmpty && !viewModel.isLoading && viewModel.selectedCategory == nil {
                                wishlistEmptyState
                            } else {
                                ScrollView(showsIndicators: false) {
                                    VStack(spacing: AppSpacing.md) {
                                        if !viewModel.products.isEmpty {
                                            bentoStatsSection
                                        }

                                        filterSortSection

                                        if viewModel.products.isEmpty && !viewModel.isLoading {
                                            filteredEmptyState
                                        } else {
                                            let currentCardWidth = cardWidth(for: geometry.size.width)
                                            LazyVGrid(columns: columns(for: currentCardWidth), spacing: currentGridVerticalSpacing) {
                                                ForEach(viewModel.products) { product in
                                                    WishlistProductCard(
                                                        product: product,
                                                        cardWidth: currentCardWidth,
                                                        isTwoColumnGrid: isTwoColumnGrid,
                                                        isSelectionMode: isSelectionMode,
                                                        isSelected: selectedProductIDs.contains(product.id),
                                                        onTap: {
                                                            if isSelectionMode {
                                                                toggleSelection(for: product.id)
                                                            } else {
                                                                navigationRouter.navigate(to: .productDetail(productId: product.id))
                                                            }
                                                        },
                                                        onLongPress: {
                                                            startSelection(with: product.id)
                                                        },
                                                        onRemove: {
                                                            Task { await viewModel.removeFromWishlist(productId: product.id) }
                                                        }
                                                    )
                                                    .frame(
                                                        width: isTwoColumnGrid ? currentCardWidth : nil,
                                                        height: isTwoColumnGrid ? twoColumnCardHeight : nil
                                                    )
                                                    .onAppear {
                                                        if product.id == viewModel.products.last?.id {
                                                            Task { await viewModel.loadMore() }
                                                        }
                                                    }
                                                }
                                            }
                                            .padding(.top, AppSpacing.lg)
                                            .padding(.horizontal, contentHorizontalPadding)
                                        }

                                        if viewModel.isLoading {
                                            ProgressView()
                                                .tint(AppColors.primary)
                                                .padding()
                                        }
                                    }
                                    .padding(.bottom, isSelectionMode && !selectedProductIDs.isEmpty ? 100 : AppSpacing.lg)
                                }
                            }
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
                    }
                }
            }
            .overlay(alignment: .bottom) {
                selectionActionBar
            }
        }
        .refreshable {
            await viewModel.refresh()
        }
        .task {
            await viewModel.loadWishlist()
        }
        .onChange(of: viewModel.products.map(\.id)) { _, ids in
            let validIDs = Set(ids)
            selectedProductIDs = selectedProductIDs.intersection(validIDs)
            if selectedProductIDs.isEmpty {
                isSelectionMode = false
            }
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

private struct WishlistStatTile: View {
    let icon: String
    let iconColor: Color
    let value: String
    let label: String

    var body: some View {
        HStack(spacing: AppSpacing.md) {
            Image(systemName: icon)
                .font(.system(size: AppFontSize.base, weight: .semibold))
                .foregroundStyle(iconColor)
                .frame(width: 30, height: 30)

            VStack(alignment: .leading, spacing: 2) {
                Text(value)
                    .font(.system(size: AppFontSize.xl, weight: .bold, design: .rounded))
                    .foregroundStyle(AppColors.foreground)
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)

                Text(label)
                    .font(.system(size: AppFontSize.xs, weight: .medium))
                    .foregroundStyle(AppColors.foreground.opacity(0.65))
            }

            Spacer(minLength: 0)
        }
        .padding(AppSpacing.lg)
        .glassCard(AppGlass.card, cornerRadius: AppRadius.xl)
    }
}

private struct WishlistChipStyleModifier: ViewModifier {
    let isActive: Bool

    @ViewBuilder
    func body(content: Content) -> some View {
        if isActive {
            content.glow(AppGlow.primarySoft)
        } else {
            content.glassCard(AppGlass.subtle, cornerRadius: AppRadius.full)
        }
    }
}

// MARK: - Wishlist Product Card
struct WishlistProductCard: View {
    let product: Product
    let cardWidth: CGFloat
    let isTwoColumnGrid: Bool
    let isSelectionMode: Bool
    let isSelected: Bool
    let onTap: () -> Void
    let onLongPress: () -> Void
    let onRemove: () -> Void

    private var cardInnerPadding: CGFloat {
        if isTwoColumnGrid {
            return max(10, floor(cardWidth * 0.07))
        } else {
            return max(10, floor(cardWidth * 0.05))
        }
    }

    private var imageSize: CGFloat {
        let maxImageWidth = cardWidth - (cardInnerPadding * 2)
        if isTwoColumnGrid {
            return max(140, min(195, maxImageWidth))
        } else {
            return min(340, maxImageWidth)
        }
    }

    private var resolvedImageURL: URL? {
        product.resolvedImageURL
    }

    @ViewBuilder
    private var imageFallbackView: some View {
        ZStack {
            AppColors.card
            VStack(spacing: 8) {
                Image(systemName: "photo.fill")
                    .font(.title)
                    .foregroundStyle(AppColors.foreground.opacity(0.7))
                Text("No Image")
                    .font(.caption2)
                    .foregroundStyle(AppColors.foreground.opacity(0.65))
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
                    .tint(AppColors.primary)
                    .frame(width: imageSize, height: imageSize)
                    .background(AppColors.card)
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
        ZStack(alignment: .topTrailing) {
            VStack(alignment: .leading, spacing: AppSpacing.sm) {
                if let category = product.categories.first {
                    Text(category)
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundStyle(Color.white)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(AppGradients.brand, in: Capsule())
                }

                productImageView
                    .frame(width: imageSize, height: imageSize)
                    .contentShape(Rectangle())
                    .cornerRadius(AppRadius.md)
                    .accessibilityHidden(true)

                Text(product.name)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundStyle(AppColors.foreground)
                    .lineLimit(2)
                    .fixedSize(horizontal: false, vertical: true)

                Spacer(minLength: 0)

                HStack(spacing: 2) {
                    Image(systemName: "star.fill")
                        .foregroundStyle(AppColors.starFilled)
                        .font(.caption)
                        .accessibilityHidden(true)
                    Text(product.formattedRating)
                        .font(.caption)
                        .foregroundStyle(AppColors.foreground)
                    Text("(\(product.reviewCount))")
                        .font(.caption)
                        .foregroundStyle(AppColors.foreground.opacity(0.65))
                }
                .accessibilityElement(children: .combine)
                .accessibilityLabel("\(product.formattedRating) stars, \(product.reviewCount) reviews")

                Text(product.formattedPrice)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundStyle(AppColors.primary)
            }
            .padding(cardInnerPadding)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
            .glassCard(AppGlass.card, cornerRadius: AppRadius.lg)
            .overlay {
                RoundedRectangle(cornerRadius: AppRadius.lg, style: .continuous)
                    .stroke(Color.white.opacity(0.07), lineWidth: 1)
            }
            .contentShape(Rectangle())
            .onTapGesture { onTap() }
            .onLongPressGesture { onLongPress() }
            .accessibilityElement(children: .combine)
            .accessibilityLabel("\(product.name), \(product.formattedPrice), \(product.formattedRating) stars")
            .accessibilityHint("Double tap to view details")

            Button {
                onRemove()
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundStyle(AppColors.destructive)
                    .frame(width: 24, height: 24)
                    .glassCard(AppGlass.subtle, cornerRadius: AppRadius.full)
            }
            .buttonStyle(.plain)
            .padding(8)
            .accessibilityLabel("Remove from wishlist")
            .accessibilityHint("Double tap to remove from wishlist")

            if isSelectionMode {
                Circle()
                    .fill(isSelected ? AppColors.primary : AppColors.background.opacity(0.6))
                    .frame(width: 22, height: 22)
                    .overlay {
                        Image(systemName: isSelected ? "checkmark" : "circle")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundStyle(Color.white)
                    }
                    .padding(8)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
            }
        }
    }
}

// MARK: - ViewModel
@MainActor
class WishlistViewModel: ObservableObject {
    private struct RemovedItem {
        let product: Product
        let index: Int
        let precedingProductId: Int?
        let followingProductId: Int?
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
        var serverWishlistIds: Set<Int> = []
        var canVerifyServerState = true

        do {
            serverWishlistIds = Set(try await repository.getWishlistIds())
        } catch {
            canVerifyServerState = false
            failedItems = items
        }

        if canVerifyServerState {
            for item in items {
                // If already absent on server, treat it as successfully removed.
                guard serverWishlistIds.contains(item.product.id) else {
                    removedCount += 1
                    continue
                }

                do {
                    try await repository.toggleWishlist(productId: item.product.id)
                    serverWishlistIds.remove(item.product.id)
                    removedCount += 1
                } catch {
                    failedItems.append(item)
                }
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

        let removalIndexSet = Set(indexedMatches.map { $0.offset })
        let removedItems = indexedMatches.map { entry -> RemovedItem in
            let index = entry.offset
            let product = entry.element

            var precedingProductId: Int?
            var previousIndex = index - 1
            while previousIndex >= 0 {
                if !removalIndexSet.contains(previousIndex) {
                    precedingProductId = products[previousIndex].id
                    break
                }
                previousIndex -= 1
            }

            var followingProductId: Int?
            var nextIndex = index + 1
            while nextIndex < products.count {
                if !removalIndexSet.contains(nextIndex) {
                    followingProductId = products[nextIndex].id
                    break
                }
                nextIndex += 1
            }

            return RemovedItem(
                product: product,
                index: index,
                precedingProductId: precedingProductId,
                followingProductId: followingProductId
            )
        }
        let idsToRemove = removedItems.map { $0.product.id }

        products.removeAll { idsToRemove.contains($0.id) }
        showUndoToast("Removed \(idsToRemove.count) \(itemWord(idsToRemove.count))")

        let pendingId = UUID()
        let commitTask = Task {
            do {
                try await Task.sleep(nanoseconds: undoWindowNanoseconds)
            } catch {
                return
            }
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
            guard !products.contains(where: { $0.id == item.product.id }) else { continue }

            let anchoredIndex: Int?
            if let precedingProductId = item.precedingProductId,
               let precedingIndex = products.firstIndex(where: { $0.id == precedingProductId }) {
                anchoredIndex = precedingIndex + 1
            } else if let followingProductId = item.followingProductId,
                      let followingIndex = products.firstIndex(where: { $0.id == followingProductId }) {
                anchoredIndex = followingIndex
            } else {
                anchoredIndex = nil
            }

            let fallbackIndex = min(item.index, products.count)
            let insertIndex = min(max(anchoredIndex ?? fallbackIndex, 0), products.count)
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
            .environmentObject(AppState())
    }
}
