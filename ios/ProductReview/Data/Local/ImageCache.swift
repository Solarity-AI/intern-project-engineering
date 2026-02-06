//
//  ImageCache.swift
//  ProductReview
//
//  In-memory image caching for better performance
//

import SwiftUI

actor ImageCache {
    static let shared = ImageCache()

    private var cache = NSCache<NSString, UIImage>()

    private init() {
        cache.countLimit = 100 // Max 100 images
        cache.totalCostLimit = 50 * 1024 * 1024 // 50 MB
    }

    func image(for url: String) -> UIImage? {
        cache.object(forKey: url as NSString)
    }

    func setImage(_ image: UIImage, for url: String) {
        cache.setObject(image, forKey: url as NSString)
    }

    func removeImage(for url: String) {
        cache.removeObject(forKey: url as NSString)
    }

    func clearCache() {
        cache.removeAllObjects()
    }
}

// MARK: - Cached Async Image
struct CachedAsyncImage<Content: View, Placeholder: View>: View {
    let url: URL?
    @ViewBuilder let content: (Image) -> Content
    @ViewBuilder let placeholder: () -> Placeholder

    @State private var image: UIImage?
    @State private var isLoading = false

    var body: some View {
        Group {
            if let image = image {
                content(Image(uiImage: image))
            } else {
                placeholder()
                    .task {
                        await loadImage()
                    }
            }
        }
    }

    private func loadImage() async {
        guard let url = url else { return }
        let urlString = url.absoluteString

        // Check cache first
        if let cached = await ImageCache.shared.image(for: urlString) {
            self.image = cached
            return
        }

        // Download image
        guard !isLoading else { return }
        isLoading = true

        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            if let uiImage = UIImage(data: data) {
                await ImageCache.shared.setImage(uiImage, for: urlString)
                self.image = uiImage
            }
        } catch {
            print("Failed to load image: \(error)")
        }

        isLoading = false
    }
}

#Preview {
    CachedAsyncImage(url: URL(string: "https://picsum.photos/200")) { image in
        image.resizable().aspectRatio(contentMode: .fill)
    } placeholder: {
        Rectangle().fill(Color.gray.opacity(0.3))
    }
    .frame(width: 200, height: 200)
    .cornerRadius(12)
}
