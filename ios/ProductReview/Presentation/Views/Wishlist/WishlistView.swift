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
    @State private var showBulkDeleteAlert = false

    private let columns = [
        GridItem(.flexible()),
        GridItem(.flexible())
    ]
    private var selectedItemWord: String {
        selectedIds.count == 1 ? "item" : "items"
    }

    var body: some View {
        Group {
            if viewModel.products.isEmpty && !viewModel.isLoading && viewModel.error != nil {
                EmptyStateView.error(message: viewModel.error ?? "Unknown error") {
                    Task { await viewModel.loadWishlist() }
                }
            } else if viewModel.products.isEmpty && !viewModel.isLoading {
                EmptyStateView.wishlist {
                    navigationRouter.popToRoot()
                }
            } else {
                VStack(spacing: 0) {
                    if !viewModel.products.isEmpty {
                        HStack {
                            if isSelectionMode {
                                Text("\(selectedIds.count) selected")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }

                            Spacer()

                            Button(isSelectionMode ? "Done" : "Select") {
                                isSelectionMode.toggle()
                                if !isSelectionMode {
                                    selectedIds.removeAll()
                                }
                            }
                            .fontWeight(.semibold)
                        }
                        .padding(.horizontal)
                        .padding(.top, 8)
                    }

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
        }
        .navigationTitle("Wishlist")
        .safeAreaInset(edge: .bottom) {
            if isSelectionMode && !selectedIds.isEmpty {
                Button(role: .destructive) {
                    showBulkDeleteAlert = true
                } label: {
                    Label("Remove \(selectedIds.count) \(selectedItemWord)", systemImage: "trash")
                        .frame(maxWidth: .infinity, minHeight: 32)
                }
                .buttonStyle(.borderedProminent)
                .tint(.red)
                .padding(.horizontal)
                .padding(.vertical, 8)
                .background(.ultraThinMaterial)
                .alert("Remove selected \(selectedItemWord)?", isPresented: $showBulkDeleteAlert) {
                    Button("Cancel", role: .cancel) {}
                    Button("Remove", role: .destructive) {
                        guard !selectedIds.isEmpty else { return }

                        let idsToRemove = Array(selectedIds)
                        Task {
                            await viewModel.removeMultiple(productIds: idsToRemove)
                            selectedIds.removeAll()
                            isSelectionMode = false
                        }
                    }
                } message: {
                    Text("This will remove \(selectedIds.count) selected \(selectedItemWord) from your wishlist.")
                }
            }
        }
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
            actionTitle: viewModel.isUndoAvailable ? "Undo" : nil,
            onAction: {
                viewModel.undoPendingRemoval()
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
    private struct RemovedItem {
        let product: Product
        let index: Int
    }

    private struct PendingRemoval {
        let id: UUID
        let items: [RemovedItem]
        let productIds: [Int]
        let commitTask: Task<Void, Never>
    }

    @Published var products: [Product] = []
    @Published var isLoading = false
    @Published var error: String?
    @Published var showToast = false
    @Published var toastMessage = ""
    @Published var toastType: ToastType = .info
    @Published var isUndoAvailable = false

    private let repository: WishlistRepositoryProtocol
    private var currentPage = 0
    private var isLastPage = false
    private var pendingRemoval: PendingRemoval?
    private let undoWindowNanoseconds: UInt64 = 3_000_000_000

    init(repository: WishlistRepositoryProtocol = WishlistRepository()) {
        self.repository = repository
    }

    private func itemWord(_ count: Int) -> String {
        count == 1 ? "item" : "items"
    }

    private func showSuccessToast(_ message: String) {
        isUndoAvailable = false
        toastMessage = message
        toastType = .success
        showToast = true
    }

    private func showErrorToast(_ message: String) {
        isUndoAvailable = false
        toastMessage = message
        toastType = .error
        showToast = true
    }

    private func showWarningToast(_ message: String) {
        isUndoAvailable = false
        toastMessage = message
        toastType = .warning
        showToast = true
    }

    private func showUndoToast(_ message: String) {
        isUndoAvailable = true
        toastMessage = message
        toastType = .success
        showToast = true
    }

    private func clearPendingRemoval() {
        pendingRemoval?.commitTask.cancel()
        pendingRemoval = nil
        isUndoAvailable = false
    }

    private func commitRemovalToServer(productIds: [Int], showSuccessOnComplete: Bool = true) async {
        var removedCount = 0
        var failedCount = 0

        for id in productIds {
            do {
                try await repository.toggleWishlist(productId: id)
                removedCount += 1
            } catch {
                failedCount += 1
            }
        }

        if failedCount > 0 {
            await loadWishlist()
        }

        if failedCount == 0 {
            guard showSuccessOnComplete else { return }
            showSuccessToast("Removed \(removedCount) \(itemWord(removedCount)) from wishlist")
        } else if removedCount == 0 {
            showErrorToast("Failed to remove selected \(itemWord(failedCount))")
        } else {
            showWarningToast("Removed \(removedCount) \(itemWord(removedCount)); \(failedCount) failed")
        }
    }

    private func commitPendingRemovalIfNeeded() async {
        guard let pendingRemoval else { return }
        self.pendingRemoval = nil
        isUndoAvailable = false
        pendingRemoval.commitTask.cancel()
        await commitRemovalToServer(productIds: pendingRemoval.productIds, showSuccessOnComplete: false)
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
            productIds: idsToRemove,
            commitTask: commitTask
        )
    }

    private func commitPendingRemovalIfMatching(id: UUID) async {
        guard let pendingRemoval, pendingRemoval.id == id else { return }
        self.pendingRemoval = nil
        isUndoAvailable = false
        await commitRemovalToServer(productIds: pendingRemoval.productIds, showSuccessOnComplete: false)
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
