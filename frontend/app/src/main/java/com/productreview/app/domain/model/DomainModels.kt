package com.productreview.app.domain.model

import java.time.LocalDateTime

// Domain Product Model
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val categories: List<String>,
    val price: Double,
    val imageUrl: String?,
    val averageRating: Double,
    val reviewCount: Int,
    val ratingBreakdown: Map<Int, Int>?,
    val aiSummary: String?
)

// Domain Review Model
data class Review(
    val id: String,
    val productId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val createdAt: String,
    val helpfulCount: Int
)

// Domain Notification Model
data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val timestamp: LocalDateTime,
    val isRead: Boolean,
    val productId: String? = null,
    val productName: String? = null
)

enum class NotificationType {
    REVIEW, ORDER, SYSTEM
}

// Wishlist Item
data class WishlistItem(
    val id: String,
    val name: String,
    val price: Double?,
    val imageUrl: String?,
    val category: String?,
    val averageRating: Double?,
    val reviewCount: Int?,
    val addedAt: LocalDateTime
)
