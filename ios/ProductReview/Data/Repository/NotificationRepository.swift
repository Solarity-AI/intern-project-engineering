//
//  NotificationRepository.swift
//  ProductReview
//
//  Implementation of NotificationRepositoryProtocol
//

import Foundation

final class NotificationRepository: NotificationRepositoryProtocol {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getNotifications() async throws -> [AppNotification] {
        let dtos: [NotificationDTO] = try await apiClient.request(endpoint: "/api/user/notifications")
        return NotificationMapper.map(dtos)
    }

    func getUnreadCount() async throws -> Int {
        let response: UnreadCountResponse = try await apiClient.request(
            endpoint: "/api/user/notifications/unread-count"
        )
        return response.count
    }

    func markAsRead(notificationId: Int) async throws {
        try await apiClient.requestVoid(
            endpoint: "/api/user/notifications/\(notificationId)/read",
            method: .put
        )
    }

    func markAllAsRead() async throws {
        try await apiClient.requestVoid(
            endpoint: "/api/user/notifications/read-all",
            method: .put
        )
    }

    func createNotification(title: String, message: String, productId: Int?) async throws {
        let request = NotificationCreateRequest(title: title, message: message, productId: productId)
        try await apiClient.requestVoid(
            endpoint: "/api/user/notifications",
            method: .post,
            body: request
        )
    }

    func deleteNotification(notificationId: Int) async throws {
        try await apiClient.requestVoid(
            endpoint: "/api/user/notifications/\(notificationId)",
            method: .delete
        )
    }

    func deleteAllNotifications() async throws {
        try await apiClient.requestVoid(
            endpoint: "/api/user/notifications",
            method: .delete
        )
    }
}
