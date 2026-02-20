//
//  ProductListView.swift
//  ProductReview
//
//  Main product list screen with search, filter, and pagination
//

import SwiftUI

struct ProductListView: View {
    @EnvironmentObject var navigationRouter: NavigationRouter
    @StateObject private var viewModel = ProductListViewModel()
    @ObservedObject private var searchHistoryManager = SearchHistoryManager.shared

    @State private var searchText = ""
    @State private var selectedCategory: String? = nil
    @FocusState private var isSearchFieldFocused: Bool

    @State private var isSelectionMode = false
    @State private var selectedProductIDs: Set<Int> = []
    @State private var containerWidth: CGFloat = UIScreen.main.bounds.width

    private let categories = ["All", "Electronics", "Smartphones", "Laptops", "Tablets", "Gaming", "Wearables", "Audio", "Accessories"]

    private let contentHorizontalPadding: CGFloat = AppSpacing.lg
    private let gridHorizontalSpacing: CGFloat = 20
    private let gridVerticalSpacing: CGFloat = AppSpacing.lg
    private let heroHeight: CGFloat = 260

    private var cardWidth: CGFloat {
        let availableWidth = containerWidth - (contentHorizontalPadding * 2) - gridHorizontalSpacing
        return max(140, floor(availableWidth / 2))
    }

    private let cardHeight: CGFloat = 290

    private var columns: [GridItem] {
        [
            GridItem(.fixed(cardWidth), spacing: gridHorizontalSpacing),
            GridItem(.fixed(cardWidth), spacing: gridHorizontalSpacing)
        ]
    }

    private var shouldShowSkeleton: Bool {
        viewModel.products.isEmpty && viewModel.isLoading
    }

    private var recentSearchSuggestions: [String] {
        let trimmed = searchText.trimmingCharacters(in: .whitespacesAndNewlines)

        if trimmed.isEmpty {
            return searchHistoryManager.recentSearches
        }

        return searchHistoryManager.recentSearches.filter {
            $0.localizedCaseInsensitiveContains(trimmed)
        }
    }

    private var newToWishlistCount: Int {
        selectedProductIDs.reduce(into: 0) { result, id in
            if !viewModel.isInWishlist(productId: id) {
                result += 1
            }
        }
    }

    private func saveCurrentSearchToHistory() {
        searchHistoryManager.addSearch(searchText)
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

    private func addSelectedToWishlist() async {
        let idsToAdd = selectedProductIDs.filter { !viewModel.isInWishlist(productId: $0) }

        for id in idsToAdd {
            await viewModel.toggleWishlist(productId: id)
        }

        cancelSelectionMode()
    }

    @ViewBuilder
    private var heroBackground: some View {
        ZStack {
            LinearGradient(
                stops: [
                    .init(color: AppColors.orderBlue.opacity(0.36), location: 0.0),
                    .init(color: AppColors.meshTeal.opacity(0.24), location: 0.26),
                    .init(color: AppColors.aiPurple.opacity(0.16), location: 0.62),
                    .init(color: .clear, location: 1.0)
                ],
                startPoint: .top,
                endPoint: .bottom
            )

            AppGradients.meshOrb(color: AppColors.primary, opacity: 0.16)
                .frame(width: 300, height: 300)
                .offset(x: -120, y: -130)

            AppGradients.meshOrb(color: AppColors.aiPurple, opacity: 0.10)
                .frame(width: 250, height: 250)
                .offset(x: 130, y: -30)

            AppGradients.meshOrb(color: AppColors.meshTeal, opacity: 0.10)
                .frame(width: 200, height: 200)
                .offset(x: 0, y: 110)
        }
    }

    @ViewBuilder
    private var heroSection: some View {
        ZStack {
            VStack(spacing: AppSpacing.sm) {
                Image("TopBarLogo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 54, height: 54)
                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                    .overlay {
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .stroke(Color("Border"), lineWidth: 1)
                    }

                Text("ProductReview")
                    .font(.system(size: AppFontSize.x3l, weight: .bold, design: .rounded))
                    .foregroundStyle(AppColors.foreground)

            }
            .padding(.top, AppSpacing.xl)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .frame(maxWidth: .infinity)
        .frame(height: heroHeight)
    }

    @ViewBuilder
    private var searchSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(AppColors.primary)

                TextField(
                    "",
                    text: $searchText,
                    prompt: Text("Search products...")
                        .foregroundStyle(AppColors.foreground.opacity(0.4))
                )
                .textInputAutocapitalization(.never)
                .disableAutocorrection(true)
                .submitLabel(.search)
                .focused($isSearchFieldFocused)
                .foregroundStyle(AppColors.foreground)
                .onSubmit {
                    saveCurrentSearchToHistory()
                }

                if !searchText.isEmpty {
                    Button {
                        searchText = ""
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundStyle(AppColors.primary)
                    }
                    .accessibilityLabel("Clear search")
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
            .glassCard(AppGlass.subtle, cornerRadius: AppRadius.full)

            if isSearchFieldFocused && !searchHistoryManager.recentSearches.isEmpty {
                VStack(alignment: .leading, spacing: 0) {
                    HStack {
                        Text("Recent Searches")
                            .font(.caption)
                            .foregroundStyle(AppColors.foreground.opacity(0.65))

                        Spacer()

                        Button("Clear") {
                            searchHistoryManager.clearHistory()
                        }
                        .font(.caption)
                        .foregroundStyle(AppColors.primary)
                        .accessibilityLabel("Clear search history")
                    }
                    .padding(.horizontal, 12)
                    .padding(.top, 8)
                    .padding(.bottom, 6)

                    if recentSearchSuggestions.isEmpty {
                        Text("No matching recent searches")
                            .font(.caption)
                            .foregroundStyle(AppColors.foreground.opacity(0.65))
                            .padding(.horizontal, 12)
                            .padding(.bottom, 8)
                    } else {
                        ForEach(recentSearchSuggestions, id: \.self) { query in
                            Button {
                                searchText = query
                                isSearchFieldFocused = false
                            } label: {
                                HStack(spacing: 8) {
                                    Image(systemName: "clock.arrow.circlepath")
                                        .foregroundStyle(AppColors.foreground.opacity(0.65))
                                    Text(query)
                                        .foregroundStyle(AppColors.foreground)
                                        .lineLimit(1)
                                    Spacer()
                                }
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
                .padding(.bottom, 6)
                .glassCard(AppGlass.subtle, cornerRadius: AppRadius.lg)
            }
        }
        .padding(.horizontal, contentHorizontalPadding)
    }

    @ViewBuilder
    private func categoryChip(_ category: String) -> some View {
        let categoryValue = category == "All" ? nil : category
        let isActive = selectedCategory == categoryValue

        Button {
            selectedCategory = categoryValue
            Task {
                await viewModel.filterByCategory(categoryValue)
            }
        } label: {
            Text(category)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(isActive ? Color.white : AppColors.foreground.opacity(0.7))
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background {
                    if isActive {
                        Capsule().fill(AppGradients.brand)
                    }
                }
        }
        .buttonStyle(.plain)
        .modifier(
            CategoryChipStyleModifier(
                isActive: isActive
            )
        )
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
                .accessibilityLabel("Sort products")
                .accessibilityHint("Select sorting criterion for product list")

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
    private var productGridSection: some View {
        LazyVGrid(columns: columns, spacing: gridVerticalSpacing) {
            if shouldShowSkeleton {
                ForEach(0..<6, id: \.self) { _ in
                    ProductCardSkeleton()
                        .frame(width: cardWidth, height: cardHeight)
                }
            } else {
                ForEach(viewModel.products) { product in
                    ZStack(alignment: .topTrailing) {
                        ProductCardView(
                            product: product,
                            cardWidth: cardWidth,
                            isSelectionMode: isSelectionMode,
                            isSelected: selectedProductIDs.contains(product.id)
                        )
                        .frame(width: cardWidth, height: cardHeight)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            if isSelectionMode {
                                toggleSelection(for: product.id)
                            } else {
                                saveCurrentSearchToHistory()
                                navigationRouter.navigate(to: .productDetail(productId: product.id))
                            }
                        }
                        .onLongPressGesture {
                            startSelection(with: product.id)
                        }

                        AnimatedHeartButton(
                            isLiked: Binding(
                                get: { viewModel.isInWishlist(productId: product.id) },
                                set: { _ in
                                    Task {
                                        await viewModel.toggleWishlist(productId: product.id)
                                    }
                                }
                            ),
                            onToggle: {},
                            activeColor: AppColors.primary,
                            inactiveColor: AppColors.foreground.opacity(0.65),
                            size: 18
                        )
                        .frame(width: 44, height: 44)
                        .background(.ultraThinMaterial, in: Circle())
                        .padding(.top, 8)
                        .padding(.trailing, 2)
                        .offset(x: -16, y: 2)
                        .opacity(0.9)
                    }
                    .onAppear {
                        if product.id == viewModel.products.last?.id {
                            Task { await viewModel.loadMore() }
                        }
                    }
                }
            }
        }
        .id(shouldShowSkeleton ? "product-list-skeleton" : "product-list-cards")
        .frame(maxWidth: .infinity)
        .padding(.horizontal, contentHorizontalPadding)
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
                .background(Color.white.opacity(0.12), in: Capsule())

                Spacer(minLength: 0)

                Text("\(selectedProductIDs.count) selected")
                    .font(.system(size: AppFontSize.sm, weight: .medium))
                    .foregroundStyle(Color.white.opacity(0.95))

                Spacer(minLength: 0)

                Button {
                    Task {
                        await addSelectedToWishlist()
                    }
                } label: {
                    Text("Add \(newToWishlistCount) to Wishlist")
                        .font(.system(size: AppFontSize.sm, weight: .semibold))
                        .foregroundStyle(Color.white)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                        .background(Color.white.opacity(0.15), in: Capsule())
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
            .background(
                AppGradients.brand,
                in: RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous)
            )
            .padding(.horizontal, contentHorizontalPadding)
            .padding(.bottom, 12)
            .shadow(color: AppGlow.primarySoft.color.opacity(AppGlow.primarySoft.opacity), radius: AppGlow.primarySoft.radius, x: 0, y: 6)
            .transition(.move(edge: .bottom).combined(with: .opacity))
        }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: AppSpacing.lg) {
                heroSection

                if let stats = viewModel.globalStats {
                    StatsHeaderView(stats: stats)
                        .padding(.horizontal, contentHorizontalPadding)
                        .offset(y: -20)
                        .padding(.bottom, -20)
                }

                searchSection
                filterSortSection

                if viewModel.products.isEmpty && !viewModel.isLoading && viewModel.error != nil {
                    EmptyStateView.error(message: viewModel.error ?? "Unknown error") {
                        Task { await viewModel.loadProducts() }
                    }
                    .padding(.horizontal, contentHorizontalPadding)
                } else if viewModel.products.isEmpty && !viewModel.isLoading {
                    VStack(spacing: AppSpacing.lg) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 60))
                            .foregroundStyle(AppColors.foreground.opacity(0.6))
                        Text("No products found")
                            .font(.title2)
                            .fontWeight(.medium)
                            .foregroundStyle(AppColors.foreground)

                        if searchText.isEmpty && selectedCategory == nil {
                            Text("Try refreshing the page")
                                .font(.body)
                                .foregroundStyle(AppColors.foreground.opacity(0.65))
                        } else {
                            Text("Try adjusting your search or filter")
                                .font(.body)
                                .foregroundStyle(AppColors.foreground.opacity(0.65))
                        }
                    }
                    .padding(contentHorizontalPadding)
                } else {
                    productGridSection

                    if viewModel.isLoading && !viewModel.products.isEmpty {
                        ProgressView()
                            .tint(AppColors.primary)
                            .padding(.bottom, AppSpacing.sm)
                    }
                }
            }
            .padding(.bottom, isSelectionMode && !selectedProductIDs.isEmpty ? 92 : AppSpacing.md)
        }
        .background {
            GeometryReader { proxy in
                Color.clear
                    .onAppear { containerWidth = proxy.size.width }
                    .onChange(of: proxy.size.width) { _, newWidth in containerWidth = newWidth }
            }
        }
        .background {
            ZStack(alignment: .top) {
                AppColors.background
                    .ignoresSafeArea()

                GeometryReader { proxy in
                    heroBackground
                        .frame(height: heroHeight + proxy.safeAreaInsets.top)
                        .ignoresSafeArea(edges: .top)
                }
            }
        }
        .overlay(alignment: .bottom) {
            selectionActionBar
        }
        .navigationTitle("Products")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(AppColors.background, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbarBackground(AppColors.background, for: .tabBar)
        .toolbarBackground(.visible, for: .tabBar)
        .toolbar(.visible, for: .tabBar)
        .onChange(of: searchText) { _, newValue in
            Task {
                await viewModel.search(query: newValue)
            }
        }
        .refreshable {
            await viewModel.refresh()
        }
        .task {
            await viewModel.loadProducts()
        }
        .toast(
            isPresented: $viewModel.showToast,
            message: viewModel.toastMessage,
            type: viewModel.toastType,
            duration: 3.0
        )
    }
}

private struct CategoryChipStyleModifier: ViewModifier {
    let isActive: Bool

    @ViewBuilder
    func body(content: Content) -> some View {
        if isActive {
            content
                .glow(AppGlow.primarySoft)
        } else {
            content
                .glassCard(AppGlass.subtle, cornerRadius: AppRadius.full)
        }
    }
}

// MARK: - Stats Header View
struct StatsHeaderView: View {
    let stats: GlobalStats

    var body: some View {
        HStack(spacing: AppSpacing.md) {
            StatsColumn(
                label: "AVG. RATING",
                value: String(format: "%.1f", stats.averageRating),
                iconName: "star.fill",
                highlighted: false
            )

            Divider()
                .overlay(AppColors.foreground.opacity(0.18))

            StatsColumn(
                label: "REVIEWS",
                value: stats.totalReviews.formatted(.number.notation(.compactName)),
                iconName: "text.bubble.fill",
                highlighted: false
            )

            Divider()
                .overlay(AppColors.foreground.opacity(0.18))

            StatsColumn(
                label: "PRODUCTS",
                value: stats.totalProducts.formatted(.number.notation(.compactName)),
                iconName: "cube.box.fill",
                highlighted: false
            )
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, AppSpacing.md)
        .padding(.vertical, AppSpacing.md)
        .glassCard(AppGlass.card, cornerRadius: AppRadius.x2l)
        .shadow(
            color: AppGlow.primarySoft.color.opacity(AppGlow.primarySoft.opacity),
            radius: AppGlow.primarySoft.radius,
            x: 0,
            y: 5
        )
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Average rating \(String(format: "%.1f", stats.averageRating)), \(stats.totalReviews) reviews, \(stats.totalProducts) products")
    }
}

private struct StatsColumn: View {
    let label: String
    let value: String
    let iconName: String
    let highlighted: Bool

    var body: some View {
        VStack(spacing: 2) {
            HStack(spacing: 5) {
                Image(systemName: iconName)
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundStyle(AppColors.foreground.opacity(0.75))
                    .accessibilityHidden(true)

                Text(value)
                    .font(.system(size: AppFontSize.xl, weight: .bold, design: .rounded))
                    .foregroundStyle(AppColors.foreground)
                    .monospacedDigit()
                    .multilineTextAlignment(.center)
            }

            Text(label)
                .font(.system(size: 10, weight: .semibold))
                .foregroundStyle(AppColors.foreground.opacity(0.6))
                .lineLimit(1)
                .multilineTextAlignment(.center)

            Rectangle()
                .fill(AppColors.primary)
                .frame(height: 2)
                .opacity(highlighted ? 1 : 0)
                .padding(.top, 1)
        }
        .frame(maxWidth: .infinity, alignment: .center)
    }
}

// MARK: - Product Card View
struct ProductCardView: View {
    let product: Product
    let cardWidth: CGFloat
    let isSelectionMode: Bool
    let isSelected: Bool

    private var cardInnerPadding: CGFloat {
        max(10, floor(cardWidth * 0.07))
    }

    private var imageSize: CGFloat {
        max(130, cardWidth - (cardInnerPadding * 2))
    }

    private var resolvedImageURL: URL? {
        product.resolvedImageURL
    }

    private var primaryCategory: String {
        product.categories.first ?? "General"
    }

    @ViewBuilder
    private var categoryBadge: some View {
        Text(primaryCategory.uppercased())
            .font(.system(size: 10, weight: .semibold))
            .foregroundStyle(Color.white)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(AppGradients.brand, in: Capsule())
            .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private var imageFallbackView: some View {
        ZStack {
            AppColors.background.opacity(0.65)

            VStack(spacing: 8) {
                Image(systemName: "photo.fill")
                    .font(.title)
                    .foregroundStyle(AppColors.foreground.opacity(0.65))
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
                    .background(AppColors.background.opacity(0.45))
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
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            categoryBadge

            productImageView
                .frame(width: imageSize, height: imageSize)
                .contentShape(Rectangle())
                .cornerRadius(AppRadius.md)
                .offset(y: 8)
                .accessibilityHidden(true)

            Text(product.name)
                .font(.subheadline)
                .fontWeight(.medium)
                .foregroundStyle(AppColors.foreground)
                .lineLimit(2)
                .fixedSize(horizontal: false, vertical: true)
                .offset(y: 8)

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
                .stroke(isSelected ? AppColors.primary : Color.white.opacity(0.07), lineWidth: isSelected ? 1.5 : 1)
        }
        .overlay(alignment: .topTrailing) {
            if isSelectionMode {
                Circle()
                    .fill(isSelected ? AppColors.primary : AppColors.background.opacity(0.55))
                    .frame(width: 20, height: 20)
                    .overlay {
                        Image(systemName: isSelected ? "checkmark" : "circle")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundStyle(Color.white)
                    }
                    .padding(8)
            }
        }
        .shadow(
            color: AppShadow.soft.color.opacity(AppShadow.soft.opacity),
            radius: AppShadow.soft.radius,
            x: AppShadow.soft.x,
            y: AppShadow.soft.y
        )
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(product.name), \(product.formattedPrice), \(product.formattedRating) stars")
        .accessibilityHint("Double tap to view details")
    }
}

// MARK: - Preview
#Preview {
    NavigationStack {
        ProductListView()
            .environmentObject(NavigationRouter())
    }
}
