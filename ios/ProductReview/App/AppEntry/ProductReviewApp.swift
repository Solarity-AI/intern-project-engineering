//
//  ProductReviewApp.swift
//  ProductReview
//
//  Main entry point for the Product Review iOS application
//

import SwiftUI

@main
struct ProductReviewApp: App {
    @StateObject private var appState = AppState()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
                .preferredColorScheme(appState.colorScheme)
        }
    }
}

// MARK: - App State
@MainActor
class AppState: ObservableObject {
    @Published var colorScheme: ColorScheme? = nil
    @Published var isOnline: Bool = true

    init() {
        // Load saved theme preference
        if let savedTheme = UserDefaults.standard.string(forKey: "theme") {
            colorScheme = savedTheme == "dark" ? .dark : .light
        }
    }

    func toggleTheme() {
        if colorScheme == .dark {
            colorScheme = .light
            UserDefaults.standard.set("light", forKey: "theme")
        } else {
            colorScheme = .dark
            UserDefaults.standard.set("dark", forKey: "theme")
        }
    }
}
