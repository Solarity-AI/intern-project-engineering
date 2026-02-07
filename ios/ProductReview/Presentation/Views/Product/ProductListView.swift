//
//  ProductListView.swift
//  ProductReview
//
//  Main product list screen with search, filter, and pagination
//

import SwiftUI

struct ProductListView: View {
    @EnvironmentObject var navigationRouter: NavigationRouter
    @EnvironmentObject var appState: AppState
    @StateObject private var viewModel = ProductListViewModel()
    @ObservedObject private var searchHistoryManager = SearchHistoryManager.shared

    @State private var searchText = ""
    @State private var selectedCategory: String? = nil
    @State private var showCategoryPicker = false
    @FocusState private var isSearchFieldFocused: Bool

    private let categories = ["All", "Electronics", "Smartphones", "Laptops", "Tablets", "Gaming", "Wearables", "Audio", "Accessories"]
    private let cardWidth: CGFloat = 140
    private let cardHeight: CGFloat = 260
    private let gridHorizontalSpacing: CGFloat = 40
    private let gridVerticalSpacing: CGFloat = 12
    private let gridHorizontalPadding: CGFloat = 0
    private var columns: [GridItem] {
        [
            GridItem(.fixed(cardWidth), spacing: gridHorizontalSpacing),
            GridItem(.fixed(cardWidth), spacing: gridHorizontalSpacing)
        ]
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
            .background(Color(.secondarySystemBackground))
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
                .background(Color(.systemBackground))
                .overlay {
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color(.separator).opacity(0.2))
                }
                .clipShape(RoundedRectangle(cornerRadius: 10))
            }
        }
        .padding(.horizontal)
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                searchSection

                if viewModel.products.isEmpty && !viewModel.isLoading && viewModel.error != nil {
                    EmptyStateView.error(message: viewModel.error ?? "Unknown error") {
                        Task { await viewModel.loadProducts() }
                    }
                    .padding(.horizontal)
                } else {
                    // Global Stats Header
                    if let stats = viewModel.globalStats {
                        StatsHeaderView(stats: stats)
                    }

                    // Products Grid or Skeleton Loading
                    LazyVGrid(columns: columns, spacing: gridVerticalSpacing) {
                        if viewModel.products.isEmpty && viewModel.isLoading {
                            // Show shimmer skeletons during initial load
                            ForEach(0..<6, id: \.self) { _ in
                                ProductCardSkeleton()
                                    .frame(width: cardWidth, height: cardHeight)
                            }
                        } else {
                            // Show actual products
                            ForEach(viewModel.products) { product in
                                ProductCardView(product: product)
                                    .frame(width: cardWidth, height: cardHeight)
                                    .onTapGesture {
                                        saveCurrentSearchToHistory()
                                        navigationRouter.navigate(to: .productDetail(productId: product.id))
                                    }
                                    .onAppear {
                                        // Load more when reaching end
                                        if product.id == viewModel.products.last?.id {
                                            Task { await viewModel.loadMore() }
                                        }
                                    }
                            }
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, gridHorizontalPadding)

                    // Loading indicator for pagination (load more)
                    if viewModel.isLoading && !viewModel.products.isEmpty {
                        ProgressView()
                            .padding()
                    }
                }
            }
        }
        .navigationTitle("Products")
        .onChange(of: searchText) { _, newValue in
            Task {
                await viewModel.search(query: newValue)
            }
        }
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Menu {
                    ForEach(categories, id: \.self) { category in
                        Button(category) {
                            selectedCategory = category == "All" ? nil : category
                            Task {
                                await viewModel.filterByCategory(selectedCategory)
                            }
                        }
                    }
                } label: {
                    Image(systemName: "line.3.horizontal.decrease.circle")
                }
                .accessibilityLabel("Filter by category")
                .accessibilityHint("Select a category to filter products")
            }

            ToolbarItem(placement: .topBarTrailing) {
                HStack(spacing: 16) {
                    Button {
                        navigationRouter.navigate(to: .wishlist)
                    } label: {
                        Image(systemName: "heart")
                    }
                    .accessibilityLabel("Wishlist")
                    .accessibilityHint("View your saved products")

                    Button {
                        navigationRouter.navigate(to: .notifications)
                    } label: {
                        ZStack(alignment: .topTrailing) {
                            Image(systemName: "bell")

                            if appState.notificationBadgeCount > 0 {
                                Text("\(appState.notificationBadgeCount)")
                                    .font(.caption2)
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 5)
                                    .padding(.vertical, 2)
                                    .background(Color.red)
                                    .clipShape(Capsule())
                                    .offset(x: 10, y: -8)
                            }
                        }
                    }
                    .accessibilityLabel("Notifications")
                    .accessibilityHint("View your notifications. \(appState.notificationBadgeCount) unread")
                }
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

    var body: some View {
        HStack(spacing: 20) {
            StatItemView(value: "\(stats.totalProducts)", label: "Products")
            StatItemView(value: "\(stats.totalReviews)", label: "Reviews")
            StatItemView(value: String(format: "%.1f", stats.averageRating), label: "Avg Rating")
        }
        .padding()
        .background(Color.blue.opacity(0.1))
        .cornerRadius(12)
        .padding(.horizontal)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(stats.totalProducts) products, \(stats.totalReviews) reviews, average rating \(String(format: "%.1f", stats.averageRating))")
    }
}

struct StatItemView: View {
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.title2)
                .fontWeight(.bold)
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

// MARK: - Product Card View
struct ProductCardView: View {
    let product: Product

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            AsyncImage(url: URL(string: product.imageUrl ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Rectangle()
                    .fill(Color.gray.opacity(0.2))
                    .overlay {
                        Image(systemName: "photo")
                            .foregroundColor(.gray)
                    }
            }
            .frame(width: 140, height: 140)
            .clipped()
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
        .padding(10)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(Color(.systemBackground))
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
