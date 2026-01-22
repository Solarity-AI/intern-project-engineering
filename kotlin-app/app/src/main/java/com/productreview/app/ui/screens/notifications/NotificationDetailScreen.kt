package com.productreview.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productreview.app.domain.model.NotificationType
import com.productreview.app.ui.theme.AppTheme
import com.productreview.app.ui.theme.GradientColors
import java.time.format.DateTimeFormatter

@Composable
fun NotificationDetailScreen(
    notificationId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProductDetails: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val colors = AppTheme.colors
    val notification = remember(notificationId) {
        viewModel.getNotificationById(notificationId)
    }

    // Mark as read when viewing
    LaunchedEffect(notificationId) {
        viewModel.markAsRead(notificationId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(colors.secondary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.foreground
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Notification",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.foreground
            )
        }

        if (notification == null) {
            // Not Found
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = colors.mutedForeground,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Notification not found",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.foreground
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Go Back", color = colors.primaryForeground)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon and Type Badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(4.dp, CircleShape)
                            .background(
                                Brush.linearGradient(GradientColors.Primary),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (notification.type) {
                                NotificationType.REVIEW -> Icons.Default.RateReview
                                NotificationType.ORDER -> Icons.Default.ShoppingBag
                                NotificationType.SYSTEM -> Icons.Default.Notifications
                            },
                            contentDescription = null,
                            tint = colors.primaryForeground,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = colors.secondary
                        ) {
                            Text(
                                text = notification.type.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.foreground,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = notification.timestamp.format(
                                DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")
                            ),
                            fontSize = 14.sp,
                            color = colors.mutedForeground,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = colors.border)

                // Title
                Text(
                    text = notification.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.foreground,
                    lineHeight = 32.sp
                )

                // Body
                Text(
                    text = notification.body,
                    fontSize = 16.sp,
                    color = colors.foreground,
                    lineHeight = 26.sp
                )

                // Product Link (if available)
                notification.productId?.let { productId ->
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToProductDetails(productId) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.secondary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = colors.primary
                                )
                                Column {
                                    Text(
                                        text = "View Product",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.foreground
                                    )
                                    notification.productName?.let { name ->
                                        Text(
                                            text = name,
                                            fontSize = 14.sp,
                                            color = colors.mutedForeground
                                        )
                                    }
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = colors.mutedForeground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delete Button
                OutlinedButton(
                    onClick = {
                        viewModel.deleteNotification(notificationId)
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.destructive
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Notification")
                }
            }
        }
    }
}
