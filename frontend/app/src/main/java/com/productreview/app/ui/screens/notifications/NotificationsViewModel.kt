package com.productreview.app.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productreview.app.core.logging.*
import com.productreview.app.data.repository.NotificationRepository
import com.productreview.app.domain.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val logger: Logger
) : ViewModel() {

    val notifications: StateFlow<List<Notification>> = notificationRepository.notifications
    val unreadCount: StateFlow<Int> = notificationRepository.unreadCount
    val isLoading: StateFlow<Boolean> = notificationRepository.isLoading

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            logger.log(LogEvent.debug(LogCategory.UI, "notifications_screen_load").build())
            notificationRepository.loadNotifications()
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            notificationRepository.deleteAllNotifications()
        }
    }

    fun getNotificationById(id: String): Notification? {
        return notificationRepository.getNotificationById(id)
    }
}
