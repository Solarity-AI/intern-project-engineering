//
//  GlowModifier.swift
//  ProductReview
//
//  Reusable glow effect using AppGlow tokens.
//

import SwiftUI

struct GlowModifier: ViewModifier {
    let style: AppGlow.Style
    let x: CGFloat
    let y: CGFloat

    func body(content: Content) -> some View {
        content.shadow(color: style.color.opacity(style.opacity), radius: style.radius, x: x, y: y)
    }
}

extension View {
    func glow(_ style: AppGlow.Style, x: CGFloat = 0, y: CGFloat = 0) -> some View {
        modifier(GlowModifier(style: style, x: x, y: y))
    }
}
