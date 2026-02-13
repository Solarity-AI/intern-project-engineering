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

private enum CachedAsyncImageNetworking {
    static let session: URLSession = {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 10
        configuration.timeoutIntervalForResource = 15
        return URLSession(configuration: configuration)
    }()
}

// MARK: - Cached Async Image
struct CachedAsyncImage<Content: View, Placeholder: View, Failure: View>: View {
    let url: URL?
    @ViewBuilder let content: (Image) -> Content
    @ViewBuilder let placeholder: () -> Placeholder
    @ViewBuilder let failure: () -> Failure

    @State private var image: UIImage?
    @State private var isLoading = false
    @State private var hasFailed = false

    init(
        url: URL?,
        @ViewBuilder content: @escaping (Image) -> Content,
        @ViewBuilder placeholder: @escaping () -> Placeholder,
        @ViewBuilder failure: @escaping () -> Failure
    ) {
        self.url = url
        self.content = content
        self.placeholder = placeholder
        self.failure = failure
    }

    init(
        url: URL?,
        @ViewBuilder content: @escaping (Image) -> Content,
        @ViewBuilder placeholder: @escaping () -> Placeholder
    ) where Failure == Placeholder {
        self.init(url: url, content: content, placeholder: placeholder, failure: placeholder)
    }

    var body: some View {
        Group {
            if let image = image {
                content(Image(uiImage: image))
            } else if hasFailed {
                failure()
            } else {
                placeholder()
                    .task(id: url?.absoluteString) {
                        await loadImage()
                    }
            }
        }
        .onChange(of: url?.absoluteString) { _, _ in
            image = nil
            hasFailed = false
            isLoading = false
        }
    }

    private func loadImage() async {
        guard let url else {
            hasFailed = true
            return
        }
        let urlString = url.absoluteString

        if let cached = await ImageCache.shared.image(for: urlString) {
            image = cached
            hasFailed = false
            return
        }

        guard !isLoading else { return }
        isLoading = true
        defer { isLoading = false }

        do {
            var request = URLRequest(url: url)
            request.timeoutInterval = 10

            let (data, response) = try await CachedAsyncImageNetworking.session.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse,
                  (200...299).contains(httpResponse.statusCode),
                  let uiImage = UIImage(data: data) else {
                hasFailed = true
                return
            }

            await ImageCache.shared.setImage(uiImage, for: urlString)
            image = uiImage
            hasFailed = false
        } catch is CancellationError {
            return
        } catch {
            hasFailed = true
            print("Failed to load image: \(error)")
        }
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
