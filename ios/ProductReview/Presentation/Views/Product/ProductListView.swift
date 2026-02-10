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

    private func saveCurrentSearchToHistory() {
        searchHistoryManager.addSearch(searchText)
    }

    private var selectedCategoryTitle: String {
        selectedCategory ?? "All Categories"
    }

    @ViewBuilder
    private var filterSortSection: some View {
        HStack {
            Menu {
                ForEach(categories, id: \.self) { category in
                    let categoryValue = category == "All" ? nil : category
                    Button {
                        selectedCategory = categoryValue
                        Task {
                            await viewModel.filterByCategory(categoryValue)
                        }
                    } label: {
                        if selectedCategory == categoryValue {
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
            .accessibilityHint("Select a category to filter products")

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
                .accessibilityLabel("Sort products")
                .accessibilityHint("Select sorting criterion for product list")

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
    private var searchSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.secondary)

                TextField("Search products...", text: $searchText)
                    .textInputAutocapitalization(.never)
                    .disableAutocorrection(true)
                    .submitLabel(.search)
                    .focused($isSearchFieldFocused)
                    .onSubmit {
                        saveCurrentSearchToHistory()
                    }

                if !searchText.isEmpty {
                    Button {
                        searchText = ""
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                    .accessibilityLabel("Clear search")
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(Color("CardBackground"))
            .clipShape(RoundedRectangle(cornerRadius: 10))

            if isSearchFieldFocused && !searchHistoryManager.recentSearches.isEmpty {
                VStack(alignment: .leading, spacing: 0) {
                    HStack {
                        Text("Recent Searches")
                            .font(.caption)
                            .foregroundColor(.secondary)

                        Spacer()

                        Button("Clear") {
                            searchHistoryManager.clearHistory()
                        }
                        .font(.caption)
                        .accessibilityLabel("Clear search history")
                    }
                    .padding(.horizontal, 12)
                    .padding(.top, 8)
                    .padding(.bottom, 4)

                    if recentSearchSuggestions.isEmpty {
                        Text("No matching recent searches")
                            .font(.caption)
                            .foregroundColor(.secondary)
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
                                        .foregroundColor(.secondary)
                                    Text(query)
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
                .background(Color("CardBackground"))
                .overlay {
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color(.separator).opacity(0.2))
                }
                .clipShape(RoundedRectangle(cornerRadius: 10))
            }
        }
        .padding(.horizontal, contentHorizontalPadding)
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                searchSection

                if viewModel.products.isEmpty && !viewModel.isLoading && viewModel.error != nil {
                    // Error state
                    EmptyStateView.error(message: viewModel.error ?? "Unknown error") {
                        Task { await viewModel.loadProducts() }
                    }
                    .padding(.horizontal)
                } else if viewModel.products.isEmpty && !viewModel.isLoading {
                    // Empty state (no results)
                    VStack(spacing: 16) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 60))
                            .foregroundColor(.secondary)
                        Text("No products found")
                            .font(.title2)
                            .fontWeight(.medium)
                        if searchText.isEmpty && selectedCategory == nil {
                            Text("Try refreshing the page")
                                .font(.body)
                                .foregroundColor(.secondary)
                        } else {
                            Text("Try adjusting your search or filter")
                                .font(.body)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding()
                } else {
                    // Global Stats Header
                    if let stats = viewModel.globalStats {
                        StatsHeaderView(stats: stats)
                    }

                    filterSortSection

                    // Products Grid or Skeleton Loading
                    LazyVGrid(columns: columns, spacing: gridVerticalSpacing) {
                        if shouldShowSkeleton {
                            // Show shimmer skeletons during initial load
                            ForEach(0..<6, id: \.self) { _ in
                                ProductCardSkeleton()
                                    .frame(width: cardWidth, height: cardHeight)
                            }
                        } else {
                            // Show actual products
                            ForEach(viewModel.products) { product in
                                ZStack(alignment: .topTrailing) {
                                    ProductCardView(product: product, cardWidth: cardWidth)
                                        .frame(width: cardWidth, height: cardHeight)
                                        .onTapGesture {
                                            saveCurrentSearchToHistory()
                                            navigationRouter.navigate(to: .productDetail(productId: product.id))
                                        }

                                    Button {
                                        Task {
                                            await viewModel.toggleWishlist(productId: product.id)
                                        }
                                    } label: {
                                        Image(systemName: viewModel.isInWishlist(productId: product.id) ? "heart.fill" : "heart")
                                            .font(.system(size: 14, weight: .semibold))
                                            .foregroundColor(viewModel.isInWishlist(productId: product.id) ? .red : .primary)
                                            .padding(8)
                                            .background(.ultraThinMaterial, in: Circle())
                                    }
                                    .buttonStyle(.plain)
                                    .padding(.top, 8)
                                    .padding(.trailing, 2)
                                    .offset(x: -16, y: 6)
                                    .opacity(0.75)
                                    .accessibilityLabel(viewModel.isInWishlist(productId: product.id) ? "Remove from wishlist" : "Add to wishlist")
                                    .accessibilityHint("Double tap to \(viewModel.isInWishlist(productId: product.id) ? "remove from" : "add to") wishlist")
                                }
                                .frame(width: cardWidth, height: cardHeight)
                                    .onAppear {
                                        // Load more when reaching end
                                        if product.id == viewModel.products.last?.id {
                                            Task { await viewModel.loadMore() }
                                        }
                                    }
                            }
                        }
                    }
                    .id(shouldShowSkeleton ? "product-list-skeleton" : "product-list-cards-\(viewModel.products.count)")
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, contentHorizontalPadding)

                    // Loading indicator for pagination (load more)
                    if viewModel.isLoading && !viewModel.products.isEmpty {
                        ProgressView()
                            .padding()
                    }
                }
            }
        }
        .background(Color("AppBackground"))
        .navigationTitle("Products")
        .navigationBarTitleDisplayMode(.inline)
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

// MARK: - Stats Header View
struct StatsHeaderView: View {
    let stats: GlobalStats

    private var statItems: [StatItemModel] {
        [
            StatItemModel(
                id: "products",
                value: stats.totalProducts.formatted(.number.notation(.compactName)),
                label: "Products",
                icon: "cube.fill",
                iconColors: [Color("AccentColor"), Color("AccentColor").opacity(0.65)],
                badgeText: "Catalog",
                accessibilityLabel: "\(stats.totalProducts) products"
            ),
            StatItemModel(
                id: "reviews",
                value: stats.totalReviews.formatted(.number.notation(.compactName)),
                label: "Reviews",
                icon: "text.bubble.fill",
                iconColors: [Color.indigo, Color.blue],
                badgeText: "Voices",
                accessibilityLabel: "\(stats.totalReviews) reviews"
            ),
            StatItemModel(
                id: "avg-rating",
                value: String(format: "%.1f", stats.averageRating),
                label: "Avg Rating",
                icon: "star.fill",
                iconColors: [Color.orange, Color.yellow],
                badgeText: "Out of 5",
                accessibilityLabel: "Average rating \(String(format: "%.1f", stats.averageRating))"
            )
        ]
    }

    var body: some View {
        ViewThatFits(in: .horizontal) {
            HStack(spacing: 8) {
                ForEach(statItems) { item in
                    StatItemView(item: item)
                }
            }

            VStack(spacing: 8) {
                HStack(spacing: 8) {
                    StatItemView(item: statItems[0])
                    StatItemView(item: statItems[1])
                }
                StatItemView(item: statItems[2])
            }
        }
        .padding(10)
        .background(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .fill(
                    LinearGradient(
                        colors: [Color("CardBackground"), Color("SurfaceMuted").opacity(0.7)],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
        )
        .overlay {
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(
                    LinearGradient(
                        colors: [Color("Border"), Color("AccentColor").opacity(0.35)],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 1
                )
        }
        .shadow(color: .black.opacity(0.06), radius: 8, x: 0, y: 3)
        .padding(.horizontal)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(stats.totalProducts) products, \(stats.totalReviews) reviews, average rating \(String(format: "%.1f", stats.averageRating))")
    }
}

struct StatItemModel: Identifiable {
    let id: String
    let value: String
    let label: String
    let icon: String
    let iconColors: [Color]
    let badgeText: String
    let accessibilityLabel: String
}

struct StatItemView: View {
    let item: StatItemModel

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(alignment: .top) {
                ZStack {
                    Circle()
                        .fill(
                            LinearGradient(
                                colors: item.iconColors,
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                    Image(systemName: item.icon)
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundStyle(.white)
                }
                .frame(width: 22, height: 22)

                Spacer(minLength: 0)

                Text(item.badgeText)
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundStyle(Color("SecondaryText"))
                    .padding(.horizontal, 6)
                    .padding(.vertical, 3)
                    .background(Color("SurfaceMuted"), in: Capsule())
            }

            HStack(alignment: .firstTextBaseline, spacing: 0) {
                Text(item.value)
                    .font(.system(size: 20, weight: .bold, design: .rounded))
                    .monospacedDigit()
                    .foregroundStyle(Color("PrimaryText"))

                Spacer(minLength: 6)

                Text(item.label)
                    .font(.caption2)
                    .foregroundStyle(Color("SecondaryText"))
                    .lineLimit(1)
                    .multilineTextAlignment(.trailing)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.leading, 6)
        .padding(.trailing, 8)
        .padding(.vertical, 7)
        .background(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .fill(Color("CardBackground").opacity(0.85))
        )
        .overlay {
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .stroke(Color("Border").opacity(0.9), lineWidth: 1)
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(item.accessibilityLabel)
    }
}

// MARK: - Product Card View
struct ProductCardView: View {
    let product: Product
    let cardWidth: CGFloat

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
