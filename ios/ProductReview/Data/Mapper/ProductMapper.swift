//
//  ProductMapper.swift
//  ProductReview
//
//  Maps ProductDTO to Product domain model
//

import Foundation

enum ProductMapper {
    static func map(_ dto: ProductDTO) -> Product {
        // Convert rating breakdown from String keys to Int keys
        var breakdown: [Int: Int] = [:]
        if let rawBreakdown = dto.ratingBreakdown {
            for (key, value) in rawBreakdown {
                if let intKey = Int(key) {
                    breakdown[intKey] = value
                }
            }
        }

        return Product(
            id: dto.id,
            name: dto.name,
            description: dto.description,
            categories: dto.categories,
            price: dto.price,
            imageUrl: dto.imageUrl,
            averageRating: dto.averageRating ?? 0.0,
            reviewCount: dto.reviewCount ?? 0,
            ratingBreakdown: breakdown,
            aiSummary: dto.aiSummary
        )
    }

    static func map(_ dtos: [ProductDTO]) -> [Product] {
        dtos.map { map($0) }
    }
}
