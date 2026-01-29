package com.productreview.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.productreview.app.data.model.ApiProduct
import com.productreview.app.ui.theme.AppTheme

@Composable
fun WishlistCard(
    product: ApiProduct,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    numColumns: Int,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val isCompact = numColumns >= 2

    // Shake animation for selection mode
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val rotation by infiniteTransition.animateFloat(
        initialValue = if (isSelectionMode) -2f else 0f,
        targetValue = if (isSelectionMode) 2f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val imageUrl = remember(product) {
        product.imageUrl?.takeIf { it.isNotBlank() } ?: imageForCategory(product.categories)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .then(
                if (isSelectionMode) Modifier.rotate(rotation) else Modifier
            )
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, colors.primary, RoundedCornerShape(16.dp))
                } else Modifier
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card)
    ) {
        Column {
            // Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (isCompact) 1f else 16f / 9f)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Quick Delete Button (when not in selection mode)
                if (!isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .shadow(4.dp, CircleShape)
                            .background(colors.card.copy(alpha = 0.9f), CircleShape)
                            .clickable { onRemove() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = colors.mutedForeground,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Selection Indicator
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                            .background(
                                if (isSelected) colors.primary else Color.White.copy(alpha = 0.9f),
                                CircleShape
                            )
                            .border(
                                2.dp,
                                if (isSelected) colors.primary else colors.border,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Category Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(colors.secondary, RoundedCornerShape(50))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = product.categories.firstOrNull() ?: "Uncategorized",
                        fontSize = if (isCompact) 9.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.foreground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.padding(
                    if (isCompact) PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    else PaddingValues(12.dp)
                ),
                verticalArrangement = Arrangement.spacedBy(if (isCompact) 2.dp else 4.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = if (isCompact) 11.sp else 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StarRating(
                        rating = product.averageRating ?: 0.0,
                        size = StarSize.SMALL,
                        compact = isCompact
                    )
                    Text(
                        text = "(${product.reviewCount ?: 0})",
                        fontSize = if (isCompact) 9.sp else 11.sp,
                        color = colors.mutedForeground
                    )
                }

                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    fontSize = if (isCompact) 12.sp else 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.foreground
                )
            }
        }
    }
}

private fun imageForCategory(categories: List<String>): String {
    val category = categories.firstOrNull()?.lowercase() ?: ""
    return when {
        "audio" in category -> "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80"
        "smart" in category || "phone" in category -> 
            "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80"
        "camera" in category -> 
            "https://images.unsplash.com/photo-1519183071298-a2962be96cdb?w=800&q=80"
        "watch" in category || "wear" in category -> 
            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&q=80"
        "laptop" in category || "computer" in category -> 
            "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80"
        else -> "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800&q=80"
    }
}
