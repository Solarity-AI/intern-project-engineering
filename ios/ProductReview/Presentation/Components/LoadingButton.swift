//
//  LoadingButton.swift
//  ProductReview
//
//  Button with loading state indicator
//

import SwiftUI

struct LoadingButton: View {
    let title: String
    let isLoading: Bool
    let style: ButtonStyle
    let action: () -> Void

    enum ButtonStyle {
        case primary
        case secondary
        case destructive

        var backgroundColor: Color {
            switch self {
            case .primary: return .blue
            case .secondary: return .gray.opacity(0.2)
            case .destructive: return .red
            }
        }

        var foregroundColor: Color {
            switch self {
            case .primary: return .white
            case .secondary: return .primary
            case .destructive: return .white
            }
        }
    }

    init(_ title: String, isLoading: Bool = false, style: ButtonStyle = .primary, action: @escaping () -> Void) {
        self.title = title
        self.isLoading = isLoading
        self.style = style
        self.action = action
    }

    var body: some View {
        Button {
            guard !isLoading else { return }
            HapticManager.impact(.light)
            action()
        } label: {
            HStack(spacing: 8) {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: style.foregroundColor))
                        .scaleEffect(0.8)
                }
                Text(isLoading ? "Loading..." : title)
                    .fontWeight(.semibold)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(style.backgroundColor.opacity(isLoading ? 0.7 : 1))
            .foregroundColor(style.foregroundColor)
            .cornerRadius(12)
        }
        .disabled(isLoading)
        .accessibilityLabel(title)
        .accessibilityHint(isLoading ? "Loading, please wait" : "Double tap to \(title.lowercased())")
    }
}

#Preview {
    VStack(spacing: 16) {
        LoadingButton("Submit Review", isLoading: false) {}
        LoadingButton("Submit Review", isLoading: true) {}
        LoadingButton("Cancel", style: .secondary) {}
        LoadingButton("Delete", style: .destructive) {}
    }
    .padding()
}
