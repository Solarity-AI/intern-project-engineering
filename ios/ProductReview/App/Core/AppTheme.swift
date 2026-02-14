//
//  AppTheme.swift
//  ProductReview
//
//  Centralized design tokens for colors, gradients, glass, glow, spacing, typography, and radius.
//

import SwiftUI

struct AppColors {
    // Semantic assets keep colors in sync with Light/Dark mode.
    static let background = Color("AppBackground")
    static let foreground = Color("PrimaryText")
    static let card = Color("CardBackground")

    static let primary = Color(hex: "10B981")
    static let starFilled = Color(hex: "FBBF24")
    static let destructive = Color(hex: "F87171")

    static let aiPurple = Color(hex: "8B5CF6")
    static let orderBlue = Color(hex: "3B82F6")
    static let premiumGold = Color(hex: "F59E0B")
    static let meshTeal = Color(hex: "14B8A6")

    static let glassBase = Color("SurfaceMuted")
}

struct AppShadow {
    struct Style {
        let color: Color
        let opacity: Double
        let radius: CGFloat
        let x: CGFloat
        let y: CGFloat
    }

    static let soft = Style(color: .black, opacity: 0.10, radius: 12, x: 0, y: 4)
    static let medium = Style(color: .black, opacity: 0.16, radius: 24, x: 0, y: 8)
    static let hover = Style(color: .black, opacity: 0.20, radius: 32, x: 0, y: 12)
}

struct AppGradients {
    static let brand = LinearGradient(
        colors: [Color(hex: "10B981"), Color(hex: "059669")],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    static let ai = LinearGradient(
        colors: [Color(hex: "8B5CF6"), Color(hex: "6366F1")],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    static let premium = LinearGradient(
        colors: [Color(hex: "F59E0B"), Color(hex: "FBBF24")],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    static let hero = LinearGradient(
        stops: [
            .init(color: .clear, location: 0.0),
            .init(color: AppColors.background.opacity(0.45), location: 0.5),
            .init(color: AppColors.background, location: 1.0)
        ],
        startPoint: .top,
        endPoint: .bottom
    )

    static func meshOrb(color: Color, opacity: Double) -> RadialGradient {
        RadialGradient(
            colors: [color.opacity(opacity), color.opacity(opacity * 0.2), .clear],
            center: .center,
            startRadius: 0,
            endRadius: 200
        )
    }
}

struct AppGlass {
    struct Style {
        let background: Color
        let opacity: Double
        let blur: Material
    }

    static let card = Style(background: AppColors.glassBase, opacity: 0.55, blur: .ultraThinMaterial)
    static let strong = Style(background: AppColors.glassBase, opacity: 0.70, blur: .thinMaterial)
    static let subtle = Style(background: AppColors.glassBase, opacity: 0.35, blur: .ultraThinMaterial)
    static let elevated = Style(background: AppColors.glassBase, opacity: 0.75, blur: .regularMaterial)
}

struct AppGlow {
    struct Style {
        let color: Color
        let opacity: Double
        let radius: CGFloat
    }

    static let primary = Style(color: AppColors.primary, opacity: 0.45, radius: 20)
    static let primarySoft = Style(color: AppColors.primary, opacity: 0.25, radius: 14)
    static let accent = Style(color: AppColors.starFilled, opacity: 0.40, radius: 18)
    static let ai = Style(color: AppColors.aiPurple, opacity: 0.45, radius: 20)
}

struct AppSpacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 12
    static let lg: CGFloat = 16
    static let xl: CGFloat = 24
    static let x2l: CGFloat = 32
    static let x3l: CGFloat = 40
    static let x4l: CGFloat = 48
    static let x5l: CGFloat = 64
}

struct AppFontSize {
    static let xs: CGFloat = 12
    static let sm: CGFloat = 14
    static let base: CGFloat = 16
    static let lg: CGFloat = 18
    static let xl: CGFloat = 20
    static let x2l: CGFloat = 24
    static let x3l: CGFloat = 30
    static let x4l: CGFloat = 36
    static let x5l: CGFloat = 42
    static let x6l: CGFloat = 52
}

struct AppRadius {
    static let sm: CGFloat = 6
    static let md: CGFloat = 8
    static let lg: CGFloat = 12
    static let xl: CGFloat = 16
    static let x2l: CGFloat = 24
    static let x3l: CGFloat = 32
    static let full: CGFloat = 9999
}

private extension Color {
    init(hex: String) {
        let normalized = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: normalized).scanHexInt64(&int)

        let red = Double((int >> 16) & 0xFF) / 255.0
        let green = Double((int >> 8) & 0xFF) / 255.0
        let blue = Double(int & 0xFF) / 255.0

        self.init(.sRGB, red: red, green: green, blue: blue, opacity: 1)
    }

    init(rgb: (Int, Int, Int)) {
        self.init(
            .sRGB,
            red: Double(rgb.0) / 255.0,
            green: Double(rgb.1) / 255.0,
            blue: Double(rgb.2) / 255.0,
            opacity: 1
        )
    }

    init(rgba: (Int, Int, Int, Double)) {
        self.init(
            .sRGB,
            red: Double(rgba.0) / 255.0,
            green: Double(rgba.1) / 255.0,
            blue: Double(rgba.2) / 255.0,
            opacity: rgba.3
        )
    }
}

private struct ThemeTokenPreviewSection<Content: View>: View {
    let title: String
    let content: Content

    init(title: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text(title)
                .font(.system(size: AppFontSize.lg, weight: .semibold))
                .foregroundStyle(AppColors.foreground)

            content
        }
        .padding(AppSpacing.md)
        .glassCard(AppGlass.subtle, cornerRadius: AppRadius.lg)
    }
}

private struct ThemeTokenSwatch: View {
    let name: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: AppSpacing.xs) {
            RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                .fill(color)
                .frame(height: 44)
                .overlay {
                    RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                        .stroke(Color.white.opacity(0.12), lineWidth: 1)
                }

            Text(name)
                .font(.system(size: AppFontSize.xs, weight: .medium))
                .foregroundStyle(AppColors.foreground.opacity(0.9))
        }
    }
}

#Preview("AppTheme Tokens") {
    ScrollView {
        VStack(spacing: AppSpacing.md) {
            ThemeTokenPreviewSection(title: "Colors") {
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: AppSpacing.sm) {
                    ThemeTokenSwatch(name: "background", color: AppColors.background)
                    ThemeTokenSwatch(name: "foreground", color: AppColors.foreground)
                    ThemeTokenSwatch(name: "card", color: AppColors.card)
                    ThemeTokenSwatch(name: "primary", color: AppColors.primary)
                    ThemeTokenSwatch(name: "starFilled", color: AppColors.starFilled)
                    ThemeTokenSwatch(name: "destructive", color: AppColors.destructive)
                    ThemeTokenSwatch(name: "aiPurple", color: AppColors.aiPurple)
                    ThemeTokenSwatch(name: "premiumGold", color: AppColors.premiumGold)
                }
            }

            ThemeTokenPreviewSection(title: "Gradients") {
                VStack(spacing: AppSpacing.sm) {
                    RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                        .fill(AppGradients.brand)
                        .frame(height: 48)
                    RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                        .fill(AppGradients.ai)
                        .frame(height: 48)
                    RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                        .fill(AppGradients.premium)
                        .frame(height: 48)
                    RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                        .fill(AppGradients.hero)
                        .frame(height: 48)

                    Circle()
                        .fill(AppGradients.meshOrb(color: .purple, opacity: 0.15))
                        .frame(width: 110, height: 110)
                        .frame(maxWidth: .infinity)
                }
            }

            ThemeTokenPreviewSection(title: "Glass + Glow") {
                VStack(spacing: AppSpacing.sm) {
                    Text("Card")
                        .font(.system(size: AppFontSize.base, weight: .semibold))
                        .foregroundStyle(AppColors.foreground)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, AppSpacing.md)
                        .glassCard(AppGlass.card)

                    Text("Primary Glow")
                        .font(.system(size: AppFontSize.base, weight: .semibold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, AppSpacing.md)
                        .background(AppGradients.brand, in: RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous))
                        .glow(AppGlow.primary)

                    Text("AI Glow")
                        .font(.system(size: AppFontSize.base, weight: .semibold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, AppSpacing.md)
                        .background(AppGradients.ai, in: RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous))
                        .glow(AppGlow.ai)
                }
            }
        }
        .padding(AppSpacing.lg)
    }
    .background(AppColors.background.ignoresSafeArea())
    .preferredColorScheme(.dark)
}
