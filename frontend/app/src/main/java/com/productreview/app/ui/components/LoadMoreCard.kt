package com.productreview.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.productreview.app.ui.theme.AppTheme
import com.productreview.app.ui.theme.GradientColors

@Composable
fun LoadMoreCard(
    onLoadMore: () -> Unit,
    isLoading: Boolean,
    hasMore: Boolean,
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    if (!hasMore) {
        // End State
        Card(
            modifier = modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.muted, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "You've reached the end",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.mutedForeground
                )
                Text(
                    text = "Showing all products",
                    fontSize = 12.sp,
                    color = colors.mutedForeground
                )
            }
        }
    } else {
        // Load More Button
        Card(
            modifier = modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clickable(enabled = !isLoading) { onLoadMore() },
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(colors.primary, colors.accent))
                    )
                    .padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colors.primaryForeground,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Loading more products...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.primaryForeground
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = colors.primaryForeground,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Load More Products",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryForeground
                            )
                            Text(
                                text = "Page ${currentPage + 1} of $totalPages",
                                fontSize = 12.sp,
                                color = colors.primaryForeground.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.primaryForeground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
