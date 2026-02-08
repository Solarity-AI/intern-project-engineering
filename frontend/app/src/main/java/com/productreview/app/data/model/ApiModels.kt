package com.productreview.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Page Response wrapper
@Serializable
data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val size: Int = 0,
    val last: Boolean = true
)

// Product DTO
@Serializable
data class ApiProduct(
    val id: Long,
    val name: String,
    val description: String? = null,
    val categories: List<String> = emptyList(),
    val price: Double = 0.0,
    val averageRating: Double? = null,
    val reviewCount: Int? = null,
    val ratingBreakdown: Map<Int, Long>? = null,
    val imageUrl: String? = null,
    val aiSummary: String? = null
)


// Review DTO
@Serializable
data class ApiReview(
    val id: Long? = null,
    val reviewerName: String? = null,
    val rating: Int,
    val comment: String,
    val helpfulCount: Int? = null,
    val createdAt: String? = null
)

// Review Request DTO (for POST)
@Serializable
data class ReviewRequest(
    val reviewerName: String?,
    val rating: Int,
    val comment: String
)

// Notification DTO
@Serializable
data class ApiNotification(
    val id: Long,
    val title: String,
    val message: String,
    @SerialName("read")
    val isRead: Boolean = false,
    val createdAt: String,
    val productId: Long? = null
)

// Global Stats DTO
@Serializable
data class GlobalStats(
    val totalProducts: Int = 0,
    val totalReviews: Int = 0,
    val averageRating: Double = 0.0
)

// Chat Request/Response
@Serializable
data class ChatRequest(
    val question: String
)

@Serializable
data class ChatResponse(
    val answer: String
)

// Unread Count Response
@Serializable
data class UnreadCountResponse(
    val count: Int
)

// Notification Create Request
@Serializable
data class NotificationCreateRequest(
    val title: String,
    val message: String,
    val productId: Long? = null
)
