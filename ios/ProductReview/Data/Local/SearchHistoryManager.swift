//
//  SearchHistoryManager.swift
//  ProductReview
//
//  Manages search history persistence using UserDefaults
//

import Foundation

@MainActor
class SearchHistoryManager: ObservableObject {
    static let shared = SearchHistoryManager()

    @Published private(set) var recentSearches: [String] = []

    private let maxHistoryCount = 10
    private let storageKey = AppConstants.StorageKeys.searchHistory

    private init() {
        loadHistory()
    }

    func addSearch(_ query: String) {
        let trimmed = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }

        // Remove if exists (to move to top)
        recentSearches.removeAll { $0.lowercased() == trimmed.lowercased() }

        // Add to beginning
        recentSearches.insert(trimmed, at: 0)

        // Limit count
        if recentSearches.count > maxHistoryCount {
            recentSearches = Array(recentSearches.prefix(maxHistoryCount))
        }

        saveHistory()
    }

    func removeSearch(_ query: String) {
        recentSearches.removeAll { $0 == query }
        saveHistory()
    }

    func clearHistory() {
        recentSearches.removeAll()
        saveHistory()
    }

    private func loadHistory() {
        recentSearches = UserDefaults.standard.stringArray(forKey: storageKey) ?? []
    }

    private func saveHistory() {
        UserDefaults.standard.set(recentSearches, forKey: storageKey)
    }
}
