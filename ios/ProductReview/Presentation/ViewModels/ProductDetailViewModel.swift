//
//  ProductDetailViewModel.swift
//  ProductReview
//
//  ViewModel for ProductDetailView
//

import Foundation

@MainActor
class ProductDetailViewModel: ObservableObject {
    @Published var product: Product?
    @Published var reviews: [Review] = []
    @Published var votedReviewIds: Set<Int> = []
    @Published var isLoading = false
    @Published var isLoadingReviews = false
    @Published var isInWishlist = false
    @Published var error: String?

    private let productId: Int
    private let productRepository: ProductRepositoryProtocol
    private let wishlistRepository: WishlistRepositoryProtocol

    private var currentPage = 0
    private var isLastPage = false
    private var currentRatingFilter: Int?

    init(
        productId: Int,
        productRepository: ProductRepositoryProtocol = ProductRepository(),
        wishlistRepository: WishlistRepositoryProtocol = WishlistRepository()
    ) {
        self.productId = productId
        self.productRepository = productRepository
        self.wishlistRepository = wishlistRepository
    }

    func loadProduct() async {
        isLoading = true
        error = nil

        do {
            product = try await productRepository.getProduct(id: productId)
            isInWishlist = await wishlistRepository.isInWishlist(productId: productId)
        } catch {
            self.error = error.localizedDescription
        }

        isLoading = false
    }

    func loadReviews() async {
        isLoadingReviews = true

        do {
            // Load voted review IDs first
            votedReviewIds = Set(try await productRepository.getUserVotedReviewIds())

            // Load reviews
            let result = try await productRepository.getReviews(
                productId: productId,
                page: 0,
                size: 10,
                rating: currentRatingFilter,
                sort: "createdAt,desc"
            )

            reviews = result.reviews
            isLastPage = result.isLast
            currentPage = 0
        } catch {
            self.error = error.localizedDescription
        }

        isLoadingReviews = false
    }

    func loadMoreReviews() async {
        guard !isLoadingReviews, !isLastPage else { return }
        isLoadingReviews = true

        do {
            let result = try await productRepository.getReviews(
                productId: productId,
                page: currentPage + 1,
                size: 10,
                rating: currentRatingFilter,
                sort: "createdAt,desc"
            )

            reviews.append(contentsOf: result.reviews)
            isLastPage = result.isLast
            currentPage += 1
        } catch {
            self.error = error.localizedDescription
        }

        isLoadingReviews = false
    }

    func filterReviewsByRating(_ rating: Int?) async {
        currentRatingFilter = rating
        currentPage = 0
        isLastPage = false
        await loadReviews()
    }

    func addReview(reviewerName: String, rating: Int, comment: String) async {
        do {
            let newReview = try await productRepository.addReview(
                productId: productId,
                reviewerName: reviewerName,
                rating: rating,
                comment: comment
            )

            // Insert at beginning
            reviews.insert(newReview, at: 0)

            // Reload product to get updated stats
            await loadProduct()
        } catch {
            self.error = error.localizedDescription
        }
    }

    func markHelpful(reviewId: Int) async {
        do {
            let updatedReview = try await productRepository.markReviewAsHelpful(reviewId: reviewId)

            // Toggle voted state
            if votedReviewIds.contains(reviewId) {
                votedReviewIds.remove(reviewId)
            } else {
                votedReviewIds.insert(reviewId)
            }

            // Update review in list
            if let index = reviews.firstIndex(where: { $0.id == reviewId }) {
                reviews[index] = updatedReview
            }
        } catch {
            self.error = error.localizedDescription
        }
    }

    func toggleWishlist() async {
        do {
            try await wishlistRepository.toggleWishlist(productId: productId)
            isInWishlist.toggle()
        } catch {
            self.error = error.localizedDescription
        }
    }
}
