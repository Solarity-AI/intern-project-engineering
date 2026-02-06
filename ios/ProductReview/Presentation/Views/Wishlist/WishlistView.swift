//
//  WishlistView.swift
//  ProductReview
//
//  Wishlist screen with product management
//

import SwiftUI

struct WishlistView: View {
    @EnvironmentObject var navigationRouter: NavigationRouter
    @StateObject private var viewModel = WishlistViewModel()
    @State private var selectedIds: Set<Int> = []
    @State private var isSelectionMode = false

    private let columns = [
        GridItem(.flexible()),
        GridItem(.flexible())
    ]

    var body: some View {
        Group {
            if viewModel.products.isEmpty && !viewModel.isLoading {
                EmptyStateView.wishlist {
                    navigationRouter.popToRoot()
                }
            } else {
                ScrollView {
                    LazyVGrid(columns: columns, spacing: 16) {
                        ForEach(viewModel.products) { product in
                            WishlistProductCard(
                                product: product,
                                isSelected: selectedIds.contains(product.id),
                                isSelectionMode: isSelectionMode,
                                onTap: {
                                    if isSelectionMode {
                                        toggleSelection(product.id)
                                    } else {
                                        navigationRouter.navigate(to: .productDetail(productId: product.id))
                                    }
                                },
                                onRemove: {
                                    Task { await viewModel.removeFromWishlist(productId: product.id) }
                                }
                            )
                            .onAppear {
                                if product.id == viewModel.products.last?.id {
                                    Task { await viewModel.loadMore() }
                                }
                            }
                        }
                    }
                    .padding()

                    if viewModel.isLoading {
                        ProgressView()
                            .padding()
                    }
                }
            }
        }
        .navigationTitle("Wishlist")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                if !viewModel.products.isEmpty {
                    Button(isSelectionMode ? "Done" : "Select") {
                        isSelectionMode.toggle()
                        if !isSelectionMode {
                            selectedIds.removeAll()
                        }
                    }
                }
            }
        }
        .overlay(alignment: .bottom) {
            if isSelectionMode && !selectedIds.isEmpty {
                Button(role: .destructive) {
                    Task {
                        await viewModel.removeMultiple(productIds: Array(selectedIds))
                        selectedIds.removeAll()
                        isSelectionMode = false
                    }
                } label: {
                    Label("Remove \(selectedIds.count) items", systemImage: "trash")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .tint(.red)
                .padding()
                .background(.ultraThinMaterial)
            }
        }
        .refreshable {
            await viewModel.refresh()
        }
        .task {
            await viewModel.loadWishlist()
        }
        .alert("Error", isPresented: Binding(
            get: { viewModel.error != nil },
            set: { if !$0 { viewModel.error = nil } }
        )) {
            Button("OK") { viewModel.error = nil }
        } message: {
            Text(viewModel.error ?? "")
        }
    }

    private func toggleSelection(_ id: Int) {
        if selectedIds.contains(id) {
            selectedIds.remove(id)
        } else {
            selectedIds.insert(id)
        }
    }
}

// MARK: - Wishlist Product Card
struct WishlistProductCard: View {
    let product: Product
    let isSelected: Bool
    let isSelectionMode: Bool
    let onTap: () -> Void
    let onRemove: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            ZStack(alignment: .topTrailing) {
                // Image
                AsyncImage(url: URL(string: product.imageUrl ?? "")) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    Rectangle()
                        .fill(Color.gray.opacity(0.2))
                }
                .frame(maxWidth: .infinity)
                .frame(height: 120)
                .clipped()
                .contentShape(Rectangle())
                .cornerRadius(8)

                // Selection indicator
                if isSelectionMode {
                    Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                        .foregroundColor(isSelected ? .blue : .white)
                        .background(Circle().fill(isSelected ? .white : .black.opacity(0.3)))
                        .padding(8)
                } else {
                    // Remove button
                    Button {
                        onRemove()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.white)
                            .background(Circle().fill(.black.opacity(0.5)))
                    }
                    .padding(8)
                }
            }

            Text(product.name)
                .font(.subheadline)
                .fontWeight(.medium)
                .lineLimit(2)

            Text(product.formattedPrice)
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(.blue)
        }
        .padding(12)
        .background(isSelected ? Color.blue.opacity(0.1) : Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
        .onTapGesture { onTap() }
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(product.name), \(product.formattedPrice)\(isSelected ? ", selected" : "")")
        .accessibilityHint(isSelectionMode ? "Double tap to toggle selection" : "Double tap to view details")
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

// MARK: - ViewModel
@MainActor
class WishlistViewModel: ObservableObject {
    @Published var products: [Product] = []
    @Published var isLoading = false
    @Published var error: String?

    private let repository: WishlistRepositoryProtocol
    private var currentPage = 0
    private var isLastPage = false

    init(repository: WishlistRepositoryProtocol = WishlistRepository()) {
        self.repository = repository
    }

    func loadWishlist() async {
        isLoading = true
        error = nil

        do {
            let result = try await repository.getWishlistProducts(page: 0, size: 20, sort: nil)
            products = result.products
            isLastPage = result.isLast
            currentPage = 0
        } catch {
            self.error = error.localizedDescription
        }

        isLoading = false
    }

    func loadMore() async {
        guard !isLoading, !isLastPage else { return }
        isLoading = true

        do {
            let result = try await repository.getWishlistProducts(page: currentPage + 1, size: 20, sort: nil)
            products.append(contentsOf: result.products)
            isLastPage = result.isLast
            currentPage += 1
        } catch {
            self.error = error.localizedDescription
        }

        isLoading = false
    }

    func refresh() async {
        currentPage = 0
        isLastPage = false
        await loadWishlist()
    }

    func removeFromWishlist(productId: Int) async {
        // Optimistic update
        products.removeAll { $0.id == productId }

        do {
            try await repository.toggleWishlist(productId: productId)
        } catch {
            // Reload on error
            await loadWishlist()
            self.error = error.localizedDescription
        }
    }

    func removeMultiple(productIds: [Int]) async {
        // Optimistic update
        products.removeAll { productIds.contains($0.id) }

        var hasFailed = false
        for id in productIds {
            do {
                try await repository.toggleWishlist(productId: id)
            } catch {
                hasFailed = true
                self.error = error.localizedDescription
            }
        }

        if hasFailed {
            await loadWishlist()
        }
    }
}

#Preview {
    NavigationStack {
        WishlistView()
            .environmentObject(NavigationRouter())
    }
}
