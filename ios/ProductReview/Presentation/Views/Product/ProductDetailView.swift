//
//  ProductDetailView.swift
//  ProductReview
//
//  Product detail screen with reviews and AI features
//

import SwiftUI

struct ProductDetailView: View {
    @EnvironmentObject var navigationRouter: NavigationRouter
    @StateObject private var viewModel: ProductDetailViewModel

    @State private var showAddReview = false
    @State private var selectedRatingFilter: Int? = nil

    init(productId: Int) {
        _viewModel = StateObject(wrappedValue: ProductDetailViewModel(productId: productId))
    }

    var body: some View {
        Group {
            if viewModel.product == nil && !viewModel.isLoading && viewModel.error != nil {
                // Error state
                EmptyStateView.error(message: viewModel.error ?? "Unknown error") {
                    Task { await viewModel.loadProduct() }
                }
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        if let product = viewModel.product {
                            // Product Image
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
                                            .font(.largeTitle)
                                    }
                            }
                            .frame(height: 250)
                            .clipped()

                            VStack(alignment: .leading, spacing: 12) {
                                // Categories
                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack {
                                        ForEach(product.categories, id: \.self) { category in
                                            Text(category)
                                                .font(.caption)
                                                .padding(.horizontal, 12)
                                                .padding(.vertical, 6)
                                                .background(Color.blue.opacity(0.1))
                                                .cornerRadius(16)
                                        }
                                    }
                                }

                                // Title and Price
                                Text(product.name)
                                    .font(.title2)
                                    .fontWeight(.bold)

                                Text(product.formattedPrice)
                                    .font(.title3)
                                    .fontWeight(.semibold)
                                    .foregroundColor(.blue)

                                // Rating
                                HStack {
                                    Image(systemName: "star.fill")
                                        .foregroundColor(.yellow)
                                    Text(product.formattedRating)
                                        .fontWeight(.medium)
                                    Text("(\(product.reviewCount) reviews)")
                                        .foregroundColor(.secondary)
                                }

                                // Description
                                Text(product.description)
                                    .font(.body)
                                    .foregroundColor(.secondary)

                                // AI Summary Card
                                if let summary = product.aiSummary {
                                    AISummaryCard(summary: summary)
                                }

                                // Rating Breakdown
                                RatingBreakdownView(
                                    breakdown: product.ratingBreakdown,
                                    totalCount: product.reviewCount,
                                    selectedRating: $selectedRatingFilter
                                )

                                // Reviews Section
                                ReviewsSection(
                                    reviews: viewModel.reviews,
                                    votedReviewIds: viewModel.votedReviewIds,
                                    isLoading: viewModel.isLoadingReviews,
                                    onHelpfulTap: { reviewId in
                                        Task { await viewModel.markHelpful(reviewId: reviewId) }
                                    },
                                    onLoadMore: {
                                        Task { await viewModel.loadMoreReviews() }
                                    }
                                )
                            }
                            .padding()
                        } else {
                            // Show skeleton for initial load and transient states.
                            ProductDetailSkeleton()
                        }
                    }
                }
            }
        }
        .navigationTitle("Product Details")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                HStack {
                    Button {
                        Task { await viewModel.toggleWishlist() }
                    } label: {
                        Image(systemName: viewModel.isInWishlist ? "heart.fill" : "heart")
                            .foregroundColor(viewModel.isInWishlist ? .red : .primary)
                    }

                    if let product = viewModel.product {
                        Button {
                            navigationRouter.navigate(to: .aiAssistant(productId: product.id, productName: product.name))
                        } label: {
                            Image(systemName: "bubble.left.and.bubble.right")
                        }
                    }
                }
            }
        }
        .sheet(isPresented: $showAddReview) {
            AddReviewSheet(
                productName: viewModel.product?.name ?? "",
                onSubmit: { name, rating, comment in
                    Task {
                        await viewModel.addReview(reviewerName: name, rating: rating, comment: comment)
                    }
                }
            )
        }
        .overlay(alignment: .bottomTrailing) {
            if viewModel.product != nil {
                Button {
                    showAddReview = true
                } label: {
                    Image(systemName: "plus")
                        .font(.title2)
                        .fontWeight(.semibold)
                        .foregroundColor(.white)
                        .frame(width: 56, height: 56)
                        .background(Color.blue)
                        .clipShape(Circle())
                        .shadow(radius: 4)
                }
                .padding()
            }
        }
        .task {
            await viewModel.loadProduct()
            await viewModel.loadReviews()
        }
        .onChange(of: selectedRatingFilter) { _, newValue in
            Task {
                await viewModel.filterReviewsByRating(newValue)
            }
        }
        .toast(
            isPresented: $viewModel.showToast,
            message: viewModel.toastMessage,
            type: viewModel.toastType,
            duration: 3.0
        )
    }
}

// MARK: - AI Summary Card
struct AISummaryCard: View {
    let summary: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: "sparkles")
                    .foregroundColor(.purple)
                Text("AI Summary")
                    .font(.headline)
            }

            Text(summary)
                .font(.body)
                .foregroundColor(.secondary)
        }
        .padding()
        .background(
            LinearGradient(
                colors: [Color.purple.opacity(0.1), Color.blue.opacity(0.1)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .cornerRadius(12)
    }
}

// MARK: - Rating Breakdown View
struct RatingBreakdownView: View {
    let breakdown: [Int: Int]
    let totalCount: Int
    @Binding var selectedRating: Int?

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Rating Breakdown")
                .font(.headline)

            ForEach((1...5).reversed(), id: \.self) { star in
                let count = breakdown[star] ?? 0
                let percentage = totalCount > 0 ? Double(count) / Double(totalCount) : 0

                Button {
                    selectedRating = selectedRating == star ? nil : star
                } label: {
                    HStack {
                        Text("\(star)")
                            .font(.caption)
                            .frame(width: 16)

                        Image(systemName: "star.fill")
                            .font(.caption)
                            .foregroundColor(.yellow)

                        GeometryReader { geometry in
                            ZStack(alignment: .leading) {
                                Rectangle()
                                    .fill(Color.gray.opacity(0.2))
                                    .frame(height: 8)
                                    .cornerRadius(4)

                                Rectangle()
                                    .fill(selectedRating == star ? Color.blue : Color.yellow)
                                    .frame(width: geometry.size.width * percentage, height: 8)
                                    .cornerRadius(4)
                            }
                        }
                        .frame(height: 8)

                        Text("\(count)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .frame(width: 30, alignment: .trailing)
                    }
                }
                .buttonStyle(.plain)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

// MARK: - Reviews Section
struct ReviewsSection: View {
    let reviews: [Review]
    let votedReviewIds: Set<Int>
    let isLoading: Bool
    let onHelpfulTap: (Int) -> Void
    let onLoadMore: () -> Void

    var body: some View {
        LazyVStack(alignment: .leading, spacing: 12) {
            Text("Reviews")
                .font(.headline)

            if reviews.isEmpty && !isLoading {
                Text("No reviews yet")
                    .foregroundColor(.secondary)
                    .padding()
            }

            if reviews.isEmpty && isLoading {
                // Show skeleton loading for initial reviews load
                ForEach(0..<3, id: \.self) { _ in
                    ReviewCardSkeleton()
                }
            } else {
                ForEach(reviews) { review in
                    ReviewCardView(
                        review: review,
                        isVoted: votedReviewIds.contains(review.id),
                        onHelpfulTap: { onHelpfulTap(review.id) }
                    )
                    .onAppear {
                        if review.id == reviews.last?.id {
                            onLoadMore()
                        }
                    }
                }

                // Show progress indicator for pagination (load more)
                if isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                        .padding()
                }
            }
        }
    }
}

// MARK: - Review Card View
struct ReviewCardView: View {
    let review: Review
    let isVoted: Bool
    let onHelpfulTap: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(review.reviewerName)
                    .font(.subheadline)
                    .fontWeight(.medium)
                Spacer()
                Text(review.formattedDate)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            // Stars
            HStack(spacing: 2) {
                ForEach(1...5, id: \.self) { star in
                    Image(systemName: star <= review.rating ? "star.fill" : "star")
                        .foregroundColor(.yellow)
                        .font(.caption)
                }
            }

            Text(review.comment)
                .font(.body)

            // Helpful button
            Button {
                onHelpfulTap()
            } label: {
                HStack(spacing: 4) {
                    Image(systemName: isVoted ? "hand.thumbsup.fill" : "hand.thumbsup")
                    Text("Helpful (\(review.helpfulCount))")
                        .font(.caption)
                }
                .foregroundColor(isVoted ? .blue : .secondary)
            }
            .buttonStyle(.plain)
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

// MARK: - Add Review Sheet
struct AddReviewSheet: View {
    let productName: String
    let onSubmit: (String, Int, String) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var reviewerName = ""
    @State private var rating = 5
    @State private var comment = ""

    private var isValid: Bool {
        reviewerName.count >= 2 && comment.count >= 10
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Your Name") {
                    TextField("Enter your name", text: $reviewerName)
                }

                Section("Rating") {
                    Picker("Rating", selection: $rating) {
                        ForEach(1...5, id: \.self) { star in
                            HStack {
                                ForEach(1...star, id: \.self) { _ in
                                    Image(systemName: "star.fill")
                                        .foregroundColor(.yellow)
                                }
                            }
                            .tag(star)
                        }
                    }
                    .pickerStyle(.wheel)
                }

                Section("Comment") {
                    TextEditor(text: $comment)
                        .frame(minHeight: 100)
                }
            }
            .navigationTitle("Review \(productName)")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Submit") {
                        onSubmit(reviewerName, rating, comment)
                        dismiss()
                    }
                    .disabled(!isValid)
                }
            }
        }
    }
}

#Preview {
    NavigationStack {
        ProductDetailView(productId: 1)
            .environmentObject(NavigationRouter())
    }
}
