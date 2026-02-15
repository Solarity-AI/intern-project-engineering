//
//  GlassCardModifier.swift
//  ProductReview
//
//  Reusable glass card styling.
//

import SwiftUI

struct GlassCardModifier: ViewModifier {
    let style: AppGlass.Style
    let cornerRadius: CGFloat

    func body(content: Content) -> some View {
        content
            .background(
                style.blur,
                in: RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
            )
            .background(
                style.background.opacity(style.opacity),
                in: RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
            )
            .overlay {
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .stroke(Color.white.opacity(0.08), lineWidth: 1)
            }
    }
}

extension View {
    func glassCard(_ style: AppGlass.Style = AppGlass.card, cornerRadius: CGFloat = AppRadius.xl) -> some View {
        modifier(GlassCardModifier(style: style, cornerRadius: cornerRadius))
    }
}
