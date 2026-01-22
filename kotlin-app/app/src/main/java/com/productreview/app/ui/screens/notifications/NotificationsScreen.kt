package com.productreview.app.ui.screens.notifications

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productreview.app.domain.model.Notification
import com.productreview.app.domain.model.NotificationType
import com.productreview.app.ui.theme.AppTheme
import com.productreview.app.ui.theme.GradientColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNotificationDetail: (String) -> Unit,
    onNavigateToProductDetails: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val colors = AppTheme.colors
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp,
            color = colors.background
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.foreground
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.linearGradient(GradientColors.Primary),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = colors.primaryForeground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Notifications",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.foreground
                        )
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount unread",
                                fontSize = 12.sp,
                                color = colors.mutedForeground
                            )
                        }
                    }
                }

                // Actions
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (unreadCount > 0) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all read",
                                tint = colors.primary
                            )
                        }
                    }
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.deleteAllNotifications() }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete all",
                                tint = colors.mutedForeground
                            )
                        }
                    }
                }
            }
        }

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = colors.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading notifications...",
                            color = colors.mutedForeground
                        )
                    }
                }
            }

            notifications.isEmpty() -> {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(colors.muted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = colors.mutedForeground,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.foreground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You're all caught up!",
                        color = colors.mutedForeground
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                viewModel.markAsRead(notification.id)
                                if (notification.productId != null) {
                                    onNavigateToProductDetails(notification.productId)
                                } else {
                                    onNavigateToNotificationDetail(notification.id)
                                }
                            },
                            onDelete = { viewModel.deleteNotification(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors

    val icon = when (notification.type) {
        NotificationType.REVIEW -> Icons.Default.RateReview
        NotificationType.ORDER -> Icons.Default.ShoppingCart
        NotificationType.SYSTEM -> Icons.Default.Info
    }

    val iconColors = when (notification.type) {
        NotificationType.REVIEW -> listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
        NotificationType.ORDER -> listOf(Color(0xFF22C55E), Color(0xFF16A34A))
        NotificationType.SYSTEM -> GradientColors.Primary
    }

    val timeAgo = formatTimeAgo(notification.timestamp)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (!notification.isRead) 4.dp else 2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) colors.card else colors.secondary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.linearGradient(iconColors),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 16.sp,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        color = colors.foreground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Unread indicator
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(colors.primary, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.body,
                    fontSize = 14.sp,
                    color = colors.mutedForeground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeAgo,
                        fontSize = 12.sp,
                        color = colors.mutedForeground
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = colors.mutedForeground,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimeAgo(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(dateTime, now)
    val hours = ChronoUnit.HOURS.between(dateTime, now)
    val days = ChronoUnit.DAYS.between(dateTime, now)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}
