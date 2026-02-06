//
//  ThemeManager.swift
//  ProductReview
//
//  Theme management with persistence
//

import SwiftUI

enum AppTheme: String, CaseIterable {
    case system
    case light
    case dark

    var displayName: String {
        switch self {
        case .system: return "System"
        case .light: return "Light"
        case .dark: return "Dark"
        }
    }

    var colorScheme: ColorScheme? {
        switch self {
        case .system: return nil
        case .light: return .light
        case .dark: return .dark
        }
    }

    var icon: String {
        switch self {
        case .system: return "circle.lefthalf.filled"
        case .light: return "sun.max.fill"
        case .dark: return "moon.fill"
        }
    }
}

@MainActor
class ThemeManager: ObservableObject {
    static let shared = ThemeManager()

    @Published var currentTheme: AppTheme {
        didSet {
            saveTheme()
        }
    }

    private init() {
        let savedTheme = UserDefaults.standard.string(forKey: AppConstants.StorageKeys.theme)
        self.currentTheme = AppTheme(rawValue: savedTheme ?? "") ?? .system
    }

    private func saveTheme() {
        UserDefaults.standard.set(currentTheme.rawValue, forKey: AppConstants.StorageKeys.theme)
    }

    func cycleTheme() {
        let allThemes = AppTheme.allCases
        guard let currentIndex = allThemes.firstIndex(of: currentTheme) else { return }
        let nextIndex = (currentIndex + 1) % allThemes.count
        currentTheme = allThemes[nextIndex]
    }
}

// MARK: - Theme Toggle View
struct ThemeToggleView: View {
    @ObservedObject var themeManager = ThemeManager.shared

    var body: some View {
        Menu {
            ForEach(AppTheme.allCases, id: \.self) { theme in
                Button {
                    themeManager.currentTheme = theme
                } label: {
                    Label(theme.displayName, systemImage: theme.icon)
                }
            }
        } label: {
            Image(systemName: themeManager.currentTheme.icon)
        }
        .accessibilityLabel("Theme")
        .accessibilityHint("Current theme: \(themeManager.currentTheme.displayName)")
    }
}

#Preview {
    ThemeToggleView()
}
