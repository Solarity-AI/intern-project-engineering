//
//  NotificationRepositoryProtocol.swift
//  ProductReview
//
//  Protocol defining notification data operations
//

import Foundation

protocol NotificationRepositoryProtocol {
    func getNotifications() async throws -> [AppNotification]
    func getUnreadCount() async throws -> Int
    func markAsRead(notificationId: Int) async throws
    func markAllAsRead() async throws
    func createNotification(title: String, message: String, productId: Int?) async throws
    func deleteNotification(notificationId: Int) async throws
    func deleteAllNotifications() async throws
}
