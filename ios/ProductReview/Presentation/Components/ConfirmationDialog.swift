//
//  ConfirmationDialog.swift
//  ProductReview
//
//  Reusable confirmation dialog modifier
//

import SwiftUI

struct ConfirmationDialogConfig {
    let title: String
    let message: String
    let confirmTitle: String
    let confirmRole: ButtonRole?
    let onConfirm: () -> Void

    init(
        title: String,
        message: String,
        confirmTitle: String = "Confirm",
        confirmRole: ButtonRole? = nil,
        onConfirm: @escaping () -> Void
    ) {
        self.title = title
        self.message = message
        self.confirmTitle = confirmTitle
        self.confirmRole = confirmRole
        self.onConfirm = onConfirm
    }

    // Preset configurations
    static func delete(itemName: String, onConfirm: @escaping () -> Void) -> ConfirmationDialogConfig {
        ConfirmationDialogConfig(
            title: "Delete \(itemName)?",
            message: "This action cannot be undone.",
            confirmTitle: "Delete",
            confirmRole: .destructive,
            onConfirm: onConfirm
        )
    }

    static func removeFromWishlist(onConfirm: @escaping () -> Void) -> ConfirmationDialogConfig {
        ConfirmationDialogConfig(
            title: "Remove from Wishlist?",
            message: "This item will be removed from your wishlist.",
            confirmTitle: "Remove",
            confirmRole: .destructive,
            onConfirm: onConfirm
        )
    }

    static func clearAll(itemType: String, onConfirm: @escaping () -> Void) -> ConfirmationDialogConfig {
        ConfirmationDialogConfig(
            title: "Clear All \(itemType)?",
            message: "This will remove all \(itemType.lowercased()). This action cannot be undone.",
            confirmTitle: "Clear All",
            confirmRole: .destructive,
            onConfirm: onConfirm
        )
    }
}

struct ConfirmationDialogModifier: ViewModifier {
    @Binding var isPresented: Bool
    let config: ConfirmationDialogConfig

    func body(content: Content) -> some View {
        content
            .confirmationDialog(config.title, isPresented: $isPresented, titleVisibility: .visible) {
                Button(config.confirmTitle, role: config.confirmRole) {
                    config.onConfirm()
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text(config.message)
            }
    }
}

extension View {
    func confirmationDialog(isPresented: Binding<Bool>, config: ConfirmationDialogConfig) -> some View {
        modifier(ConfirmationDialogModifier(isPresented: isPresented, config: config))
    }
}
