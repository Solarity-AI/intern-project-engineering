//
//  ReviewMapper.swift
//  ProductReview
//
//  Maps ReviewDTO to Review domain model
//

import Foundation

enum ReviewMapper {
    static func map(_ dto: ReviewDTO) -> Review? {
        guard let id = dto.id else { return nil }

        // Parse date from ISO 8601 string
        var date: Date? = nil
        if let dateString = dto.createdAt {
            let formatter = ISO8601DateFormatter()
            formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
            date = formatter.date(from: dateString)

            // Try without fractional seconds if first attempt fails
            if date == nil {
                formatter.formatOptions = [.withInternetDateTime]
                date = formatter.date(from: dateString)
            }
        }

        return Review(
            id: id,
            reviewerName: dto.reviewerName ?? "Anonymous",
            rating: dto.rating,
            comment: dto.comment,
            helpfulCount: dto.helpfulCount ?? 0,
            createdAt: date,
            productId: dto.productId
        )
    }

    static func map(_ dtos: [ReviewDTO]) -> [Review] {
        dtos.compactMap { map($0) }
    }
}
