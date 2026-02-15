//
//  AnimatedHeartButton.swift
//  ProductReview
//
//  Animated heart button with bounce effect for wishlist
//

import SwiftUI

struct AnimatedHeartButton: View {
    @Binding var isLiked: Bool
    let onToggle: () -> Void
    let activeColor: Color
    let inactiveColor: Color
    let size: CGFloat

    @State private var scale: CGFloat = 1.0

    init(
        isLiked: Binding<Bool>,
        onToggle: @escaping () -> Void,
        activeColor: Color = AppColors.primary,
        inactiveColor: Color = AppColors.foreground.opacity(0.7),
        size: CGFloat = 20
    ) {
        _isLiked = isLiked
        self.onToggle = onToggle
        self.activeColor = activeColor
        self.inactiveColor = inactiveColor
        self.size = size
    }

    var body: some View {
        Button {
            withAnimation(.spring(response: 0.3, dampingFraction: 0.5)) {
                scale = 1.3
            }

            DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.5)) {
                    scale = 1.0
                }
            }

            HapticManager.impact(.medium)
            isLiked.toggle()
            onToggle()
        } label: {
            Image(systemName: isLiked ? "heart.fill" : "heart")
                .font(.system(size: size, weight: .semibold))
                .foregroundColor(isLiked ? activeColor : inactiveColor)
                .scaleEffect(scale)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(isLiked ? "Remove from wishlist" : "Add to wishlist")
        .accessibilityHint("Double tap to \(isLiked ? "remove from" : "add to") wishlist")
    }
}

// MARK: - Standalone Heart Toggle
struct HeartToggle: View {
    let isLiked: Bool
    let size: CGFloat

    @State private var animate = false

    var body: some View {
        Image(systemName: isLiked ? "heart.fill" : "heart")
            .font(.system(size: size))
            .foregroundColor(isLiked ? AppColors.primary : AppColors.foreground.opacity(0.7))
            .scaleEffect(animate ? 1.2 : 1.0)
            .onChange(of: isLiked) { _, newValue in
                if newValue {
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.5)) {
                        animate = true
                    }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                        withAnimation {
                            animate = false
                        }
                    }
                }
            }
    }
}

#Preview {
    struct PreviewWrapper: View {
        @State var isLiked = false

        var body: some View {
            VStack(spacing: 40) {
                AnimatedHeartButton(isLiked: $isLiked) {
                    print("Toggled!")
                }

                HeartToggle(isLiked: isLiked, size: 30)

                Text(isLiked ? "Liked" : "Not liked")
            }
        }
    }

    return PreviewWrapper()
}
