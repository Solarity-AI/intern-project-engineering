package com.productreview.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.productreview.app.domain.model.Review
import com.productreview.app.ui.theme.AppTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ReviewCard(
    review: Review,
    isHelpful: Boolean,
    onHelpfulClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    val formattedDate = try {
        val dateTime = LocalDateTime.parse(review.createdAt.replace(" ", "T"))
        dateTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    } catch (e: Exception) {
        review.createdAt.take(10)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, colors.border, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.secondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = colors.mutedForeground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = review.userName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.foreground
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 14.sp,
                            color = colors.mutedForeground
                        )
                    }
                }
                StarRating(
                    rating = review.rating.toDouble(),
                    size = StarSize.SMALL
                )
            }

            // Comment
            Text(
                text = review.comment,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = colors.foreground
            )

            // Helpful Button
            Row(
                modifier = Modifier
                    .background(
                        if (isHelpful) colors.primary.copy(alpha = 0.1f) else colors.card,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onHelpfulClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isHelpful) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = "Helpful",
                    tint = if (isHelpful) colors.primary else colors.mutedForeground,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Helpful (${review.helpfulCount})",
                    fontSize = 14.sp,
                    color = if (isHelpful) colors.primary else colors.mutedForeground
                )
            }
        }
    }
}
