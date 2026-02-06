//
//  RatingStarsView.swift
//  ProductReview
//
//  Interactive and display-only star rating components
//

import SwiftUI

// MARK: - Display Stars (Read-only)
struct RatingStarsView: View {
    let rating: Double
    let maxRating: Int
    let size: CGFloat
    let color: Color

    init(rating: Double, maxRating: Int = 5, size: CGFloat = 14, color: Color = .yellow) {
        self.rating = rating
        self.maxRating = maxRating
        self.size = size
        self.color = color
    }

    var body: some View {
        HStack(spacing: 2) {
            ForEach(1...maxRating, id: \.self) { index in
                Image(systemName: starType(for: index))
                    .font(.system(size: size))
                    .foregroundColor(color)
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("\(String(format: "%.1f", rating)) out of \(maxRating) stars")
    }

    private func starType(for index: Int) -> String {
        let value = Double(index)
        if rating >= value {
            return "star.fill"
        } else if rating >= value - 0.5 {
            return "star.leadinghalf.filled"
        } else {
            return "star"
        }
    }
}

// MARK: - Interactive Stars (Selectable)
struct InteractiveRatingStars: View {
    @Binding var rating: Int
    let maxRating: Int
    let size: CGFloat
    let color: Color

    @State private var animatingIndex: Int?

    init(rating: Binding<Int>, maxRating: Int = 5, size: CGFloat = 28, color: Color = .yellow) {
        self._rating = rating
        self.maxRating = maxRating
        self.size = size
        self.color = color
    }

    var body: some View {
        HStack(spacing: 8) {
            ForEach(1...maxRating, id: \.self) { index in
                Image(systemName: index <= rating ? "star.fill" : "star")
                    .font(.system(size: size))
                    .foregroundColor(index <= rating ? color : .gray.opacity(0.4))
                    .scaleEffect(animatingIndex == index ? 1.3 : 1.0)
                    .onTapGesture {
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.5)) {
                            animatingIndex = index
                            rating = index
                        }
                        HapticManager.selection()

                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                            withAnimation {
                                animatingIndex = nil
                            }
                        }
                    }
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Rating: \(rating) out of \(maxRating) stars")
        .accessibilityHint("Tap to change rating")
        .accessibilityAdjustableAction { direction in
            switch direction {
            case .increment:
                if rating < maxRating { rating += 1 }
            case .decrement:
                if rating > 1 { rating -= 1 }
            @unknown default:
                break
            }
        }
    }
}

// MARK: - Preview Helper
private struct RatingPreviewWrapper: View {
    @State var rating = 3
    var body: some View {
        InteractiveRatingStars(rating: $rating)
    }
}

#Preview {
    VStack(spacing: 30) {
        RatingStarsView(rating: 3.5)
        RatingStarsView(rating: 4.2, size: 20, color: .orange)
        RatingPreviewWrapper()
    }
    .padding()
}
