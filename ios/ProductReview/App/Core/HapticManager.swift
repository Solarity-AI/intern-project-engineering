//
//  HapticManager.swift
//  ProductReview
//
//  Centralized haptic feedback manager
//

import UIKit

enum HapticManager {
    static func impact(_ style: UIImpactFeedbackGenerator.FeedbackStyle = .medium) {
        let generator = UIImpactFeedbackGenerator(style: style)
        generator.impactOccurred()
    }

    static func notification(_ type: UINotificationFeedbackGenerator.FeedbackType) {
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(type)
    }

    static func selection() {
        let generator = UISelectionFeedbackGenerator()
        generator.selectionChanged()
    }

    // Convenience methods
    static func success() { notification(.success) }
    static func error() { notification(.error) }
    static func warning() { notification(.warning) }
    static func light() { impact(.light) }
    static func heavy() { impact(.heavy) }
}
