//
//  ProductDetailView.swift
//  ProductReview
//
//  Product detail screen with reviews and AI features
//

import SwiftUI

struct ProductDetailView: View {
    @EnvironmentObject var navigationRouter: NavigationRouter
    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel: ProductDetailViewModel

    @State private var showAddReview = false
    @State private var selectedRatingFilter: Int? = nil

    init(productId: Int) {
        _viewModel = StateObject(wrappedValue: ProductDetailViewModel(productId: productId))
    }

    @ViewBuilder
    private var imageFallbackView: some View {
        ZStack {
            AppColors.card
            VStack(spacing: 8) {
                Image(systemName: "photo.fill")
                    .foregroundStyle(AppColors.foreground.opacity(0.7))
                    .font(.largeTitle)
                Text("No Image")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.foreground.opacity(0.6))
            }
        }
    }

    @ViewBuilder
    private func productImageView(for product: Product) -> some View {
        if let imageURL = product.resolvedImageURL {
            CachedAsyncImage(url: imageURL) { image in
                Color.clear
                    .overlay {
                        image
                            .resizable()
                            .scaledToFill()
                    }
            } placeholder: {
                imageFallbackView
                    .overlay {
                        ProgressView()
                            .tint(AppColors.foreground)
                    }
            } failure: {
                imageFallbackView
            }
        } else {
            imageFallbackView
        }
    }

    private func handleBack() {
        if navigationRouter.path.isEmpty {
            dismiss()
        } else {
            navigationRouter.pop()
        }
    }

    var body: some View {
        ZStack {
            AppColors.background
                .ignoresSafeArea()

            if viewModel.product == nil && !viewModel.isLoading && viewModel.error != nil {
                EmptyStateView.error(message: viewModel.error ?? "Unknown error") {
                    Task { await viewModel.loadProduct() }
                }
            } else {
                ScrollView(showsIndicators: false) {
                    if let product = viewModel.product {
                        detailContent(
                            for: product,
                            viewportHeight: UIScreen.main.bounds.height
                        )
                        .containerRelativeFrame(.horizontal, alignment: .leading)
                    } else {
                        ProductDetailSkeleton()
                            .padding(.horizontal, AppSpacing.lg)
                            .padding(.top, AppSpacing.lg)
                            .containerRelativeFrame(.horizontal, alignment: .leading)
                    }
                }
                .ignoresSafeArea(edges: .top)
            }
        }
        .overlay(alignment: .top) {
            HStack {
                Button {
                    handleBack()
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(Color.primary)
                        .frame(width: 40, height: 40)
                        .glassCard(AppGlass.card, cornerRadius: AppRadius.full)
                        .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 2)
                }
                .buttonStyle(.plain)

                Spacer()

                Button {
                    Task { await viewModel.toggleWishlist() }
                } label: {
                    Image(systemName: viewModel.isInWishlist ? "heart.fill" : "heart")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(viewModel.isInWishlist ? AppColors.primary : Color.primary)
                        .frame(width: 44, height: 44)
                        .glassCard(AppGlass.card, cornerRadius: AppRadius.full)
                        .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 2)
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, AppSpacing.lg)
            .padding(.top, AppSpacing.sm)
            .safeAreaPadding(.top)
            .frame(maxWidth: .infinity)
        }
        .overlay(alignment: .bottomTrailing) {
            if viewModel.product != nil {
                Button {
                    showAddReview = true
                } label: {
                    Image(systemName: "plus")
                        .font(.title2)
                        .fontWeight(.semibold)
                        .foregroundStyle(Color.white)
                        .frame(width: 56, height: 56)
                        .background(AppGradients.brand, in: Circle())
                }
                .buttonStyle(.plain)
                .glow(AppGlow.primary)
                .padding(.trailing, AppSpacing.lg)
                .padding(.bottom, AppSpacing.lg)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar(.hidden, for: .navigationBar)
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

    @ViewBuilder
    private func detailContent(
        for product: Product,
        viewportHeight: CGFloat
    ) -> some View {
        let heroHeight = max(320, min(viewportHeight * 0.50, 520))

        VStack(spacing: 0) {
            ZStack(alignment: .top) {
                productImageView(for: product)
                    .frame(maxWidth: .infinity)
                    .frame(height: heroHeight)
                    .clipped()
                    .overlay(alignment: .bottom) {
                        AppGradients.hero
                    }
                    .overlay(alignment: .bottomLeading) {
                        VStack(alignment: .leading, spacing: AppSpacing.sm) {
                            if !product.categories.isEmpty {
                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack(spacing: AppSpacing.sm) {
                                        ForEach(product.categories, id: \.self) { category in
                                            Text(category)
                                                .font(.system(size: 11, weight: .semibold))
                                                .foregroundStyle(Color.white)
                                                .padding(.horizontal, 10)
                                                .padding(.vertical, 6)
                                                .background(AppGradients.brand, in: Capsule())
                                        }
                                    }
                                }
                            }

                            Text(product.name)
                                .font(.system(size: AppFontSize.x2l, weight: .bold))
                                .foregroundStyle(AppColors.foreground)
                                .lineLimit(2)

                            Text(product.formattedPrice)
                                .font(.system(size: AppFontSize.base, weight: .semibold))
                                .foregroundStyle(AppColors.primary)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(AppColors.primary.opacity(0.15), in: Capsule())
                        }
                        .padding(.horizontal, AppSpacing.lg)
                        .padding(.bottom, AppSpacing.xl)
                    }
                    .ignoresSafeArea(edges: .top)
            }

            VStack(alignment: .leading, spacing: AppSpacing.lg) {
                HStack(spacing: 0) {
                    ProductInfoStatView(
                        icon: "star.fill",
                        iconColor: AppColors.starFilled,
                        value: product.formattedRating,
                        label: "Rating"
                    )

                    Divider()
                        .overlay(Color.white.opacity(0.18))
                        .padding(.vertical, AppSpacing.sm)

                    ProductInfoStatView(
                        icon: "bubble.left.and.bubble.right.fill",
                        iconColor: AppColors.primary,
                        value: "\(product.reviewCount)",
                        label: "Reviews"
                    )

                    Divider()
                        .overlay(Color.white.opacity(0.18))
                        .padding(.vertical, AppSpacing.sm)

                    ProductInfoStatView(
                        icon: "tag.fill",
                        iconColor: AppColors.primary,
                        value: infoBarPrice(product.price),
                        label: "Price"
                    )
                }
                .padding(.vertical, AppSpacing.md)
                .padding(.horizontal, AppSpacing.sm)
                .glassCard(AppGlass.card, cornerRadius: AppRadius.x2l)
                .glow(AppGlow.primarySoft)
                .offset(y: -20)
                .padding(.bottom, -20)

                if !product.description.isEmpty {
                    Text(product.description)
                        .font(.body)
                        .foregroundStyle(AppColors.foreground.opacity(0.75))
                }

                AIAssistantBanner {
                    navigationRouter.navigate(to: .aiAssistant(productId: product.id, productName: product.name))
                }

                if let summary = product.aiSummary, !summary.isEmpty {
                    AISummaryCard(summary: summary)
                }

                RatingBreakdownView(
                    breakdown: product.ratingBreakdown,
                    totalCount: product.reviewCount,
                    selectedRating: $selectedRatingFilter
                )

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
            .padding(.horizontal, AppSpacing.lg)
            .padding(.bottom, 96)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .frame(maxWidth: .infinity, alignment: .topLeading)
    }

    private func infoBarPrice(_ price: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = "USD"
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        return formatter.string(from: NSNumber(value: price)) ?? String(format: "$%.0f", price)
    }
}

private struct ProductInfoStatView: View {
    let icon: String
    let iconColor: Color
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(iconColor)

            Text(value)
                .font(.system(size: AppFontSize.base, weight: .bold, design: .rounded))
                .foregroundStyle(AppColors.foreground)
                .monospacedDigit()
                .lineLimit(1)
                .minimumScaleFactor(0.8)

            Text(label)
                .font(.system(size: AppFontSize.xs, weight: .medium))
                .foregroundStyle(AppColors.foreground.opacity(0.6))
                .lineLimit(1)
        }
        .frame(maxWidth: .infinity)
    }
}

private struct AIAssistantBanner: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: AppSpacing.sm) {
                Image(systemName: "sparkles")
                    .font(.system(size: AppFontSize.lg, weight: .semibold))
                    .foregroundStyle(Color.white)

                Text("Ask AI about this product")
                    .font(.system(size: AppFontSize.base, weight: .semibold))
                    .foregroundStyle(Color.white)

                Spacer()

                Image(systemName: "arrow.right")
                    .font(.system(size: AppFontSize.base, weight: .semibold))
                    .foregroundStyle(Color.white)
            }
            .padding(.horizontal, AppSpacing.lg)
            .padding(.vertical, AppSpacing.md)
            .background(AppGradients.ai, in: RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous))
        }
        .buttonStyle(.plain)
        .glow(AppGlow.ai)
    }
}

// MARK: - AI Summary Card
struct AISummaryCard: View {
    let summary: String

    var body: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            HStack(spacing: 8) {
                Image(systemName: "sparkles")
                    .foregroundStyle(AppColors.aiPurple)

                Text("AI Summary")
                    .font(.headline)
                    .foregroundStyle(AppColors.aiPurple)
            }

            Text(summary)
                .font(.body)
                .foregroundStyle(AppColors.foreground.opacity(0.8))
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(AppSpacing.lg)
        .background {
            RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous)
                .fill(AppGradients.ai)
                .opacity(0.15)
        }
        .glassCard(AppGlass.subtle, cornerRadius: AppRadius.xl)
    }
}

// MARK: - Rating Breakdown View
struct RatingBreakdownView: View {
    let breakdown: [Int: Int]
    let totalCount: Int
    @Binding var selectedRating: Int?

    var body: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Rating Breakdown")
                .font(.headline)
                .foregroundStyle(AppColors.foreground)

            ForEach((1...5).reversed(), id: \.self) { star in
                let count = breakdown[star] ?? 0
                let percentage = totalCount > 0 ? Double(count) / Double(totalCount) : 0

                HStack(spacing: AppSpacing.sm) {
                    Button {
                        selectedRating = selectedRating == star ? nil : star
                    } label: {
                        RatingFilterChip(star: star, isSelected: selectedRating == star)
                    }
                    .buttonStyle(.plain)

                    GeometryReader { geometry in
                        ZStack(alignment: .leading) {
                            Capsule()
                                .fill(Color.white.opacity(0.16))
                                .frame(height: 8)

                            Capsule()
                                .fill(AppColors.primary)
                                .frame(width: geometry.size.width * percentage, height: 8)
                        }
                    }
                    .frame(height: 8)

                    Text("\(count)")
                        .font(.caption)
                        .foregroundStyle(AppColors.foreground.opacity(0.72))
                        .frame(width: 30, alignment: .trailing)
                }
            }
        }
        .padding(AppSpacing.md)
        .glassCard(AppGlass.card, cornerRadius: AppRadius.xl)
    }
}

private struct RatingFilterChip: View {
    let star: Int
    let isSelected: Bool

    var body: some View {
        HStack(spacing: 4) {
            Text("\(star)")
                .font(.caption)
                .fontWeight(.semibold)
            Image(systemName: "star.fill")
                .font(.caption)
                .foregroundStyle(AppColors.starFilled)
        }
        .foregroundStyle(AppColors.foreground)
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .frame(width: 58)
        .background {
            if isSelected {
                Capsule()
                    .fill(AppGradients.brand)
            }
        }
        .modifier(UnselectedChipGlassModifier(isSelected: isSelected))
    }
}

private struct UnselectedChipGlassModifier: ViewModifier {
    let isSelected: Bool

    @ViewBuilder
    func body(content: Content) -> some View {
        if isSelected {
            content
        } else {
            content
                .glassCard(AppGlass.subtle, cornerRadius: AppRadius.full)
        }
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
                .foregroundStyle(AppColors.foreground)

            if reviews.isEmpty && !isLoading {
                Text("No reviews yet")
                    .foregroundStyle(AppColors.foreground.opacity(0.6))
                    .padding()
            }

            if reviews.isEmpty && isLoading {
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

                if isLoading {
                    ProgressView()
                        .tint(AppColors.primary)
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
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            HStack {
                Text(review.reviewerName)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundStyle(AppColors.foreground)

                Spacer()

                Text(review.formattedDate)
                    .font(.caption)
                    .foregroundStyle(AppColors.foreground.opacity(0.5))
            }

            HStack(spacing: 2) {
                ForEach(1...5, id: \.self) { star in
                    Image(systemName: star <= review.rating ? "star.fill" : "star")
                        .foregroundStyle(AppColors.starFilled)
                        .font(.caption)
                }
            }

            Text(review.comment)
                .font(.body)
                .foregroundStyle(AppColors.foreground.opacity(0.85))

            Button {
                onHelpfulTap()
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: isVoted ? "hand.thumbsup.fill" : "hand.thumbsup")
                    Text("Helpful (\(review.helpfulCount))")
                        .font(.caption.weight(.semibold))
                }
                .foregroundStyle(isVoted ? Color.white : AppColors.primary)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background {
                    if isVoted {
                        Capsule()
                            .fill(AppGradients.brand)
                    } else {
                        Capsule()
                            .stroke(AppColors.primary.opacity(0.75), lineWidth: 1)
                    }
                }
            }
            .buttonStyle(.plain)
        }
        .padding(AppSpacing.lg)
        .glassCard(AppGlass.card, cornerRadius: AppRadius.xl)
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
