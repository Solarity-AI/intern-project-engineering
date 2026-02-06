//
//  APIClient.swift
//  ProductReview
//
//  Centralized API client using URLSession and async/await
//

import Foundation

// MARK: - API Error
enum APIError: Error, LocalizedError {
    case invalidURL
    case invalidResponse
    case httpError(statusCode: Int, message: String?)
    case decodingError(Error)
    case networkError(Error)
    case noConnection
    case timeout
    case nonIdempotentMethod
    case unknown

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid URL"
        case .invalidResponse:
            return "Invalid server response"
        case .httpError(let statusCode, _):
            return Self.userFriendlyMessage(for: statusCode)
        case .decodingError:
            return "Unable to process server response. Please try again."
        case .networkError:
            return "Connection failed. Please check your internet."
        case .noConnection:
            return "No internet connection. Please check your network settings."
        case .timeout:
            return "Request timed out. Please try again."
        case .nonIdempotentMethod:
            return "Retry is not supported for non-idempotent requests."
        case .unknown:
            return "Something went wrong. Please try again."
        }
    }

    /// User-friendly messages for HTTP status codes
    private static func userFriendlyMessage(for statusCode: Int) -> String {
        switch statusCode {
        case 400:
            return "Invalid request. Please try again."
        case 401:
            return "Session expired. Please restart the app."
        case 403:
            return "Access denied."
        case 404:
            return "Content not found."
        case 429:
            return "Too many requests. Please wait a moment."
        case 500...599:
            return "Server is temporarily unavailable. Please try later."
        default:
            return "Something went wrong. Please try again."
        }
    }

    /// Whether the error is retryable
    var isRetryable: Bool {
        switch self {
        case .timeout, .networkError, .noConnection:
            return true
        case .httpError(let statusCode, _):
            return statusCode >= 500 || statusCode == 429
        case .nonIdempotentMethod:
            return false
        default:
            return false
        }
    }
}

// MARK: - HTTP Method
enum HTTPMethod: String {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case delete = "DELETE"
}

// MARK: - API Client
actor APIClient {
    static let shared = APIClient()

    private let baseURL: String
    private let session: URLSession
    private var userId: String

    private init() {
        self.baseURL = AppConstants.API.baseURL
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = AppConstants.API.timeoutInterval
        self.session = URLSession(configuration: config)

        // Load or generate user ID
        if let savedUserId = UserDefaults.standard.string(forKey: AppConstants.StorageKeys.userId) {
            self.userId = savedUserId
        } else {
            let newUserId = UUID().uuidString
            UserDefaults.standard.set(newUserId, forKey: AppConstants.StorageKeys.userId)
            self.userId = newUserId
        }
    }

    // MARK: - Generic Request Method
    func request<T: Decodable>(
        endpoint: String,
        method: HTTPMethod = .get,
        queryParams: [String: String]? = nil,
        body: Encodable? = nil
    ) async throws -> T {
        // Build URL with query parameters
        var urlComponents = URLComponents(string: baseURL + endpoint)

        if let queryParams = queryParams {
            urlComponents?.queryItems = queryParams.map { URLQueryItem(name: $0.key, value: $0.value) }
        }

        guard let url = urlComponents?.url else {
            throw APIError.invalidURL
        }

        // Build request
        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(userId, forHTTPHeaderField: "X-User-ID")

        // Add body if present
        if let body = body {
            let encoder = JSONEncoder()
            request.httpBody = try encoder.encode(body)
        }

        // Execute request
        do {
            let (data, response) = try await session.data(for: request)

            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIError.invalidResponse
            }

            // Check status code
            guard (200...299).contains(httpResponse.statusCode) else {
                let message = String(data: data, encoding: .utf8)
                throw APIError.httpError(statusCode: httpResponse.statusCode, message: message)
            }

            // Handle empty response for void returns
            if T.self == EmptyResponse.self, data.isEmpty {
                return EmptyResponse() as! T
            }

            // Decode response
            let decoder = JSONDecoder()
            decoder.dateDecodingStrategy = .iso8601
            return try decoder.decode(T.self, from: data)

        } catch let error as APIError {
            throw error
        } catch let error as DecodingError {
            throw APIError.decodingError(error)
        } catch let error as URLError {
            switch error.code {
            case .timedOut:
                throw APIError.timeout
            case .notConnectedToInternet, .networkConnectionLost:
                throw APIError.noConnection
            default:
                throw APIError.networkError(error)
            }
        } catch {
            throw APIError.networkError(error)
        }
    }

    // MARK: - Request with Retry
    func requestWithRetry<T: Decodable>(
        endpoint: String,
        method: HTTPMethod = .get,
        queryParams: [String: String]? = nil,
        body: Encodable? = nil,
        maxRetries: Int = 3
    ) async throws -> T {
        // Reject non-idempotent methods to avoid duplicate mutations on retry
        switch method {
        case .post, .put, .delete:
            throw APIError.nonIdempotentMethod
        case .get:
            break
        }

        var lastError: Error = APIError.unknown
        var delay: UInt64 = 1_000_000_000 // 1 second

        for attempt in 0..<maxRetries {
            do {
                return try await request(
                    endpoint: endpoint,
                    method: method,
                    queryParams: queryParams,
                    body: body
                )
            } catch let error as APIError where error.isRetryable {
                lastError = error
                if attempt < maxRetries - 1 {
                    try await Task.sleep(nanoseconds: delay)
                    delay *= 2 // Exponential backoff
                }
            } catch {
                throw error // Non-retryable error, throw immediately
            }
        }
        throw lastError
    }

    // MARK: - Request without response body
    func requestVoid(
        endpoint: String,
        method: HTTPMethod = .get,
        queryParams: [String: String]? = nil,
        body: Encodable? = nil
    ) async throws {
        let _: EmptyResponse = try await request(
            endpoint: endpoint,
            method: method,
            queryParams: queryParams,
            body: body
        )
    }
}

// MARK: - Empty Response
struct EmptyResponse: Decodable {}
