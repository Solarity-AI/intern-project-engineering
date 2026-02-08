package com.productreview.app.data.remote

import com.productreview.app.data.model.*
import retrofit2.http.*

interface ProductReviewApi {

    // ==================== PRODUCTS ====================

    @GET("api/products")
    suspend fun getProducts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "name,asc",
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): PageResponse<ApiProduct>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: String): ApiProduct

    @GET("api/products/stats")
    suspend fun getGlobalStats(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): GlobalStats

    // ==================== REVIEWS ====================

    @GET("api/products/{productId}/reviews")
    suspend fun getReviews(
        @Path("productId") productId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createdAt,desc",
        @Query("rating") rating: Int? = null
    ): PageResponse<ApiReview>

    @POST("api/products/{productId}/reviews")
    suspend fun postReview(
        @Path("productId") productId: String,
        @Body review: ReviewRequest
    ): ApiReview

    @PUT("api/products/reviews/{reviewId}/helpful")
    suspend fun markReviewAsHelpful(@Path("reviewId") reviewId: Long): ApiReview

    @GET("api/products/reviews/voted")
    suspend fun getUserVotedReviews(): List<Long>

    // ==================== AI CHAT ====================

    @POST("api/products/{productId}/chat")
    suspend fun chatWithAI(
        @Path("productId") productId: String,
        @Body request: ChatRequest
    ): ChatResponse

    // ==================== WISHLIST ====================

    @GET("api/user/wishlist")
    suspend fun getWishlist(): List<String>

    @GET("api/user/wishlist/products")
    suspend fun getWishlistProducts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "id,desc"
    ): PageResponse<ApiProduct>

    @POST("api/user/wishlist/{productId}")
    suspend fun toggleWishlist(@Path("productId") productId: String)

    // ==================== NOTIFICATIONS ====================

    @GET("api/user/notifications")
    suspend fun getNotifications(): List<ApiNotification>

    @GET("api/user/notifications/unread-count")
    suspend fun getUnreadCount(): UnreadCountResponse

    @PUT("api/user/notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: Long)

    @PUT("api/user/notifications/read-all")
    suspend fun markAllNotificationsAsRead()

    @POST("api/user/notifications")
    suspend fun createNotification(@Body request: NotificationCreateRequest)

    @DELETE("api/user/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: Long)

    @DELETE("api/user/notifications")
    suspend fun deleteAllNotifications()
}
