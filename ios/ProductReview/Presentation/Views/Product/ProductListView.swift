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

    @State private var searchText = ""
    @State private var selectedCategory: String? = nil
    @State private var showCategoryPicker = false

    private let categories = ["All", "Electronics", "Smartphones", "Laptops", "Tablets", "Gaming", "Wearables", "Audio", "Accessories"]
    private let columns = [
        GridItem(.flexible()),
        GridItem(.flexible())
    ]

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Global Stats Header
                if let stats = viewModel.globalStats {
                    StatsHeaderView(stats: stats)
                }

                // Products Grid
                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(viewModel.products) { product in
                        ProductCardView(product: product)
                            .onTapGesture {
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
                .padding(.horizontal)

                // Loading indicator
                if viewModel.isLoading {
                    ProgressView()
                        .padding()
                }
            }
        }
        .navigationTitle("Products")
        .searchable(text: $searchText, prompt: "Search products...")
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
                        Image(systemName: "bell")
                    }
                    .accessibilityLabel("Notifications")
                    .accessibilityHint("View your notifications")
                }
            }
        }
        .refreshable {
            await viewModel.refresh()
        }
        .task {
            await viewModel.loadProducts()
        }
        .alert("Error", isPresented: .constant(viewModel.error != nil)) {
            Button("OK") { viewModel.error = nil }
        } message: {
            Text(viewModel.error ?? "")
        }
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
        VStack(alignment: .leading, spacing: 8) {
            // Image placeholder
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
            .frame(maxWidth: .infinity)
            .frame(height: 120)
            .clipped()
            .contentShape(Rectangle())
            .cornerRadius(8)
            .accessibilityHidden(true)

            // Product info
            Text(product.name)
                .font(.subheadline)
                .fontWeight(.medium)
                .lineLimit(2)

            HStack {
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
        .padding(12)
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
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
