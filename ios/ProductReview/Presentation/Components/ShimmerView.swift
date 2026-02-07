//
//  ShimmerView.swift
//  ProductReview
//
//  Shimmer loading effect for skeleton screens
//

import SwiftUI

struct ShimmerView: View {
    @State private var phase: CGFloat = 0

    var body: some View {
        LinearGradient(
            gradient: Gradient(colors: [
                Color.gray.opacity(0.2),
                Color.gray.opacity(0.4),
                Color.gray.opacity(0.2)
            ]),
            startPoint: .leading,
            endPoint: .trailing
        )
        .mask(Rectangle())
        .offset(x: phase)
        .onAppear {
            withAnimation(.linear(duration: 1.5).repeatForever(autoreverses: false)) {
                phase = 200
            }
        }
    }
}

// MARK: - Product Card Skeleton
struct ProductCardSkeleton: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            ShimmerView()
                .frame(width: 140, height: 140)
                .cornerRadius(8)

            ShimmerView()
                .frame(height: 16)
                .cornerRadius(4)

            ShimmerView()
                .frame(width: 100, height: 16)
                .cornerRadius(4)

            Spacer(minLength: 0)

            ShimmerView()
                .frame(width: 80, height: 12)
                .cornerRadius(4)

            ShimmerView()
                .frame(width: 60, height: 16)
                .cornerRadius(4)
        }
        .padding(10)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay {
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color(.separator), lineWidth: 1)
        }
    }
}

// MARK: - Product Detail Skeleton
struct ProductDetailSkeleton: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Image skeleton
            ShimmerView()
                .frame(height: 250)

            VStack(alignment: .leading, spacing: 12) {
                // Categories
                HStack {
                    ShimmerView()
                        .frame(width: 80, height: 24)
                        .cornerRadius(16)
                    ShimmerView()
                        .frame(width: 60, height: 24)
                        .cornerRadius(16)
                }

                // Title
                ShimmerView()
                    .frame(height: 24)
                    .cornerRadius(4)
                ShimmerView()
                    .frame(width: 200, height: 24)
                    .cornerRadius(4)

                // Price
                ShimmerView()
                    .frame(width: 100, height: 20)
                    .cornerRadius(4)

                // Rating
                ShimmerView()
                    .frame(width: 150, height: 16)
                    .cornerRadius(4)

                // Description
                VStack(alignment: .leading, spacing: 8) {
                    ShimmerView()
                        .frame(height: 14)
                        .cornerRadius(4)
                    ShimmerView()
                        .frame(height: 14)
                        .cornerRadius(4)
                    ShimmerView()
                        .frame(width: 250, height: 14)
                        .cornerRadius(4)
                }
            }
            .padding()
        }
    }
}

// MARK: - Review Card Skeleton
struct ReviewCardSkeleton: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Reviewer name and date
            HStack {
                ShimmerView()
                    .frame(width: 120, height: 16)
                    .cornerRadius(4)
                Spacer()
                ShimmerView()
                    .frame(width: 80, height: 12)
                    .cornerRadius(4)
            }

            // Stars
            ShimmerView()
                .frame(width: 100, height: 12)
                .cornerRadius(4)

            // Comment
            VStack(alignment: .leading, spacing: 4) {
                ShimmerView()
                    .frame(height: 14)
                    .cornerRadius(4)
                ShimmerView()
                    .frame(height: 14)
                    .cornerRadius(4)
                ShimmerView()
                    .frame(width: 200, height: 14)
                    .cornerRadius(4)
            }

            // Helpful button
            ShimmerView()
                .frame(width: 80, height: 12)
                .cornerRadius(4)
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

// MARK: - Shimmer Modifier
struct ShimmerModifier: ViewModifier {
    @State private var phase: CGFloat = 0

    func body(content: Content) -> some View {
        content
            .overlay(
                LinearGradient(
                    gradient: Gradient(colors: [
                        .clear,
                        Color.white.opacity(0.4),
                        .clear
                    ]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .offset(x: phase)
                .mask(content)
            )
            .onAppear {
                withAnimation(.linear(duration: 1.5).repeatForever(autoreverses: false)) {
                    phase = 300
                }
            }
    }
}

extension View {
    func shimmer() -> some View {
        modifier(ShimmerModifier())
    }
}

#Preview("Product Card Skeleton") {
    HStack(spacing: 16) {
        ProductCardSkeleton()
            .frame(width: 180)
        ProductCardSkeleton()
            .frame(width: 180)
    }
    .padding()
}

#Preview("Product Detail Skeleton") {
    ScrollView {
        ProductDetailSkeleton()
    }
}

#Preview("Review Card Skeleton") {
    VStack(spacing: 12) {
        ReviewCardSkeleton()
        ReviewCardSkeleton()
        ReviewCardSkeleton()
    }
    .padding()
}

#Preview("Shimmer Modifier") {
    Rectangle()
        .fill(Color.gray.opacity(0.3))
        .frame(width: 200, height: 20)
        .shimmer()
        .padding()
}
