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
        VStack(alignment: .leading, spacing: 8) {
            // Image skeleton
            ShimmerView()
                .frame(height: 120)
                .cornerRadius(8)

            // Title skeleton
            ShimmerView()
                .frame(height: 16)
                .cornerRadius(4)

            ShimmerView()
                .frame(width: 100, height: 16)
                .cornerRadius(4)

            // Rating skeleton
            ShimmerView()
                .frame(width: 80, height: 12)
                .cornerRadius(4)

            // Price skeleton
            ShimmerView()
                .frame(width: 60, height: 16)
                .cornerRadius(4)
        }
        .padding(12)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
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

#Preview {
    VStack {
        ProductCardSkeleton()
            .frame(width: 180)

        Rectangle()
            .fill(Color.gray.opacity(0.3))
            .frame(width: 200, height: 20)
            .shimmer()
    }
    .padding()
}
