package com.productreview.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.productreview.app.ui.theme.AppTheme
import com.productreview.app.ui.theme.GradientColors

@Composable
fun RatingBreakdown(
    breakdown: Map<Int, Int>?,
    totalCount: Int,
    selectedRating: Int?,
    onSelectRating: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val ratings = listOf(5, 4, 3, 2, 1)
    val safeBreakdown = breakdown ?: emptyMap()
    val safeTotal = if (totalCount > 0) totalCount else safeBreakdown.values.sum().coerceAtLeast(1)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ratings.forEach { rating ->
            val count = safeBreakdown[rating] ?: 0
            val percent = if (safeTotal > 0) (count.toFloat() / safeTotal) * 100 else 0f
            val isSelected = selectedRating == rating

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) colors.secondary else colors.card)
                    .clickable {
                        onSelectRating(if (isSelected) null else rating)
                    }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Star label
                Text(
                    text = "$rating★",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.foreground,
                    modifier = Modifier.width(38.dp)
                )

                // Progress bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.border)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (percent / 100f).coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(GradientColors.Primary),
                                alpha = if (safeTotal == 0) 0.3f else 1f
                            )
                    )
                }

                // Count
                Text(
                    text = "$count",
                    fontSize = 14.sp,
                    color = if (isSelected) colors.foreground else colors.mutedForeground,
                    modifier = Modifier.width(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }

        // Helper text when filtered
        if (selectedRating != null) {
            Text(
                text = "Showing $selectedRating★ reviews — tap again to clear.",
                fontSize = 12.sp,
                color = colors.mutedForeground,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
