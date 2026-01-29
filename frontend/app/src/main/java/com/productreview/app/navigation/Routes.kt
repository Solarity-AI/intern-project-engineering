package com.productreview.app.navigation

import kotlinx.serialization.Serializable

// Type-safe navigation routes using Kotlin Serialization

@Serializable
sealed class Screen {
    @Serializable
    data object ProductList : Screen()

    @Serializable
    data class ProductDetails(
        val productId: String,
        val imageUrl: String? = null,
        val name: String? = null
    ) : Screen()

    @Serializable
    data object Notifications : Screen()

    @Serializable
    data class NotificationDetail(
        val notificationId: String
    ) : Screen()

    @Serializable
    data object Wishlist : Screen()

    @Serializable
    data class AIAssistant(
        val productId: String,
        val productName: String
    ) : Screen()
}

// Legacy string-based routes for compatibility
object Routes {
    const val PRODUCT_LIST = "product_list"
    const val PRODUCT_DETAILS = "product_details/{productId}"
    const val NOTIFICATIONS = "notifications"
    const val NOTIFICATION_DETAIL = "notification_detail/{notificationId}"
    const val WISHLIST = "wishlist"
    const val AI_ASSISTANT = "ai_assistant/{productId}/{productName}"

    fun productDetails(productId: String) = "product_details/$productId"
    fun notificationDetail(notificationId: String) = "notification_detail/$notificationId"
    fun aiAssistant(productId: String, productName: String) = 
        "ai_assistant/$productId/${productName.replace("/", "%2F")}"
}
