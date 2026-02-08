package com.productreview.app.data.repository

import com.productreview.app.core.FWError
import com.productreview.app.core.FWResult
import com.productreview.app.core.logging.LogCategory
import com.productreview.app.core.logging.LogEvent
import com.productreview.app.core.logging.LogLevel
import com.productreview.app.core.logging.Logger
import com.productreview.app.data.model.ApiNotification
import com.productreview.app.data.model.NotificationCreateRequest
import com.productreview.app.data.remote.ProductReviewApi
import com.productreview.app.domain.model.Notification
import com.productreview.app.domain.model.NotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for notification operations.
 * Uses optimistic updates for better UX.
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val api: ProductReviewApi,
    private val logger: Logger
) {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun loadNotifications(): FWResult<Unit> {
        _isLoading.value = true
        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "load_notifications",
            level = LogLevel.DEBUG
        ))

        return safeApiCall {
            val apiNotifications = api.getNotifications()
            _notifications.value = apiNotifications.map { it.toDomain() }
            _unreadCount.value = _notifications.value.count { !it.isRead }
            logger.log(LogEvent(
                category = LogCategory.DATA,
                name = "notifications_loaded",
                level = LogLevel.INFO,
                metadata = mapOf("totalItems" to apiNotifications.size.toString())
            ))
        }.also { _isLoading.value = false }
    }

    suspend fun refreshUnreadCount(): FWResult<Unit> {
        return safeApiCall {
            val response = api.getUnreadCount()
            _unreadCount.value = response.count
        }
    }

    suspend fun markAsRead(notificationId: String): FWResult<Unit> {
        _notifications.value = _notifications.value.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        _unreadCount.value = _notifications.value.count { !it.isRead }

        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "notification_read",
            level = LogLevel.INFO,
            metadata = mapOf("notificationId" to notificationId)
        ))

        return safeApiCall { api.markNotificationAsRead(notificationId.toLong()) }
    }

    suspend fun markAllAsRead(): FWResult<Unit> {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        _unreadCount.value = 0

        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "notifications_all_read",
            level = LogLevel.INFO
        ))

        return safeApiCall { api.markAllNotificationsAsRead() }
    }

    suspend fun addNotification(
        type: NotificationType,
        title: String,
        body: String,
        productId: String? = null,
        productName: String? = null
    ): FWResult<Unit> {
        val tempNotification = Notification(
            id = "local-${System.currentTimeMillis()}",
            type = type,
            title = title,
            body = body,
            timestamp = LocalDateTime.now(),
            isRead = false,
            productId = productId,
            productName = productName
        )
        _notifications.value = listOf(tempNotification) + _notifications.value
        _unreadCount.value = _unreadCount.value + 1

        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "notification_added",
            level = LogLevel.INFO,
            metadata = mapOf("title" to title)
        ))

        return safeApiCall {
            api.createNotification(
                NotificationCreateRequest(
                    title = title,
                    message = body,
                    productId = productId
                )
            )
            loadNotifications()
        }
    }

    suspend fun deleteNotification(notificationId: String): FWResult<Unit> {
        val wasUnread = _notifications.value.find { it.id == notificationId }?.isRead == false

        _notifications.value = _notifications.value.filter { it.id != notificationId }
        if (wasUnread) {
            _unreadCount.value = maxOf(0, _unreadCount.value - 1)
        }

        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "notification_deleted",
            level = LogLevel.INFO,
            metadata = mapOf("notificationId" to notificationId)
        ))

        if (notificationId.startsWith("local-")) return FWResult.success(Unit)

        return safeApiCall { api.deleteNotification(notificationId.toLong()) }
    }

    suspend fun deleteAllNotifications(): FWResult<Unit> {
        _notifications.value = emptyList()
        _unreadCount.value = 0

        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "notifications_all_deleted",
            level = LogLevel.INFO
        ))

        return safeApiCall { api.deleteAllNotifications() }
    }

    fun getNotificationById(id: String): Notification? {
        return _notifications.value.find { it.id == id }
    }

    private suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): FWResult<T> {
        return try {
            FWResult.success(block())
        } catch (e: retrofit2.HttpException) {
            FWResult.failure(FWError.fromHttpStatus(e.code(), e.message()))
        } catch (e: Exception) {
            FWResult.failure(FWError.fromThrowable(e))
        }
    }
}

private fun ApiNotification.toDomain(): Notification = Notification(
    id = id.toString(),
    type = NotificationType.SYSTEM,
    title = title,
    body = message,
    timestamp = parseDateTime(createdAt),
    isRead = isRead,
    productId = productId?.toString(),
    productName = null
)

private fun parseDateTime(dateString: String): LocalDateTime {
    return try {
        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: Exception) {
        try {
            LocalDateTime.parse(dateString.replace(" ", "T"))
        } catch (e2: Exception) {
            LocalDateTime.now()
        }
    }
}
