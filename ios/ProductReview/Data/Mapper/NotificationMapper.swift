//
//  NotificationMapper.swift
//  ProductReview
//
//  Maps NotificationDTO to AppNotification domain model
//

import Foundation

enum NotificationMapper {
    static func map(_ dto: NotificationDTO) -> AppNotification {
        // Parse date from ISO 8601 string
        var date: Date? = nil
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        date = formatter.date(from: dto.createdAt)

        // Try without fractional seconds if first attempt fails
        if date == nil {
            formatter.formatOptions = [.withInternetDateTime]
            date = formatter.date(from: dto.createdAt)
        }

        return AppNotification(
            id: dto.id,
            title: dto.title,
            message: dto.message,
            isRead: dto.isRead,
            createdAt: date,
            productId: dto.productId
        )
    }

    static func map(_ dtos: [NotificationDTO]) -> [AppNotification] {
        dtos.map { map($0) }
    }
}
