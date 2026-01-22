package com.productreview.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.productreview.app.ui.theme.AppTheme

enum class StarSize(val size: Dp) {
    SMALL(16.dp),
    MEDIUM(20.dp),
    LARGE(24.dp)
}

@Composable
fun StarRating(
    rating: Double,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    size: StarSize = StarSize.MEDIUM,
    interactive: Boolean = false,
    onRatingChange: ((Int) -> Unit)? = null,
    showValue: Boolean = false,
    compact: Boolean = false
) {
    val colors = AppTheme.colors
    val iconSize = if (compact) 12.dp else size.size

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(maxRating) { index ->
                val starValue = index + 1
                val isFull = starValue <= rating.toInt()
                val isHalf = !isFull && starValue == rating.toInt() + 1 && rating % 1 >= 0.2 && rating % 1 < 0.8
                val isActuallyFull = !isFull && starValue == rating.toInt() + 1 && rating % 1 >= 0.8

                val icon = when {
                    isFull || isActuallyFull -> Icons.Filled.Star
                    isHalf -> Icons.Filled.StarHalf
                    else -> Icons.Filled.StarOutline
                }

                val color = if (isFull || isActuallyFull || isHalf) colors.starFilled else colors.starEmpty

                Icon(
                    imageVector = icon,
                    contentDescription = "Star $starValue",
                    tint = color,
                    modifier = Modifier
                        .size(iconSize)
                        .then(
                            if (interactive && onRatingChange != null) {
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onRatingChange(starValue)
                                }
                            } else Modifier
                        )
                )
            }
        }

        if (showValue) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%.1f", rating),
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = colors.foreground
            )
        }
    }
}
