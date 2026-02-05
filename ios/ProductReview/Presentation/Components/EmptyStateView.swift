//
//  EmptyStateView.swift
//  ProductReview
//
//  Reusable empty state component with icon, title, subtitle, and optional action
//

import SwiftUI

struct EmptyStateView: View {
    let icon: String
    let title: String
    let subtitle: String
    var actionTitle: String?
    var action: (() -> Void)?

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 60))
                .foregroundColor(.secondary)
                .accessibilityHidden(true)

            Text(title)
                .font(.title2)
                .fontWeight(.medium)
                .multilineTextAlignment(.center)

            Text(subtitle)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            if let actionTitle = actionTitle, let action = action {
                Button(actionTitle, action: action)
                    .buttonStyle(.borderedProminent)
                    .padding(.top, 8)
            }
        }
        .padding()
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(title). \(subtitle)")
    }
}

// MARK: - Preset Empty States
extension EmptyStateView {
    static func wishlist(onBrowse: @escaping () -> Void) -> EmptyStateView {
        EmptyStateView(
            icon: "heart.slash",
            title: "Your wishlist is empty",
            subtitle: "Add products you love to see them here",
            actionTitle: "Browse Products",
            action: onBrowse
        )
    }

    static func noResults(searchQuery: String) -> EmptyStateView {
        EmptyStateView(
            icon: "magnifyingglass",
            title: "No results found",
            subtitle: "No products match \"\(searchQuery)\""
        )
    }

    static func noNotifications() -> EmptyStateView {
        EmptyStateView(
            icon: "bell.slash",
            title: "No notifications",
            subtitle: "You're all caught up!"
        )
    }

    static func error(message: String, onRetry: @escaping () -> Void) -> EmptyStateView {
        EmptyStateView(
            icon: "exclamationmark.triangle",
            title: "Something went wrong",
            subtitle: message,
            actionTitle: "Try Again",
            action: onRetry
        )
    }

    static func noConnection(onRetry: @escaping () -> Void) -> EmptyStateView {
        EmptyStateView(
            icon: "wifi.slash",
            title: "No connection",
            subtitle: "Please check your internet connection",
            actionTitle: "Retry",
            action: onRetry
        )
    }
}

#Preview("Wishlist Empty") {
    EmptyStateView.wishlist(onBrowse: {})
}

#Preview("No Results") {
    EmptyStateView.noResults(searchQuery: "iPhone 20")
}

#Preview("Error") {
    EmptyStateView.error(message: "Failed to load products", onRetry: {})
}
