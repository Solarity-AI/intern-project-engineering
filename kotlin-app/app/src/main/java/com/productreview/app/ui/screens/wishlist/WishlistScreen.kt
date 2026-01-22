package com.productreview.app.ui.screens.wishlist

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productreview.app.data.local.ThemeMode
import com.productreview.app.ui.components.LoadMoreCard
import com.productreview.app.ui.components.WishlistCard
import com.productreview.app.ui.theme.AppTheme
import com.productreview.app.ui.theme.GradientColors
import com.productreview.app.ui.viewmodel.ThemeViewModel

@Composable
fun WishlistScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductDetails: (String) -> Unit,
    onNavigateToProductList: () -> Unit,
    viewModel: WishlistViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()

    val vibrator = context.getSystemService(Vibrator::class.java)

    fun vibrateOnSelect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    val (itemCount, avgRating, totalPrice) = remember(uiState.products, uiState.totalItems) {
        viewModel.getStats()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand/Back
                Row(
                    modifier = Modifier.clickable { onNavigateToProductList() },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Brush.linearGradient(GradientColors.Primary),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = colors.primaryForeground,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "ProductReview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.foreground
                    )
                }

                // Actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Theme Toggle
                    IconButton(
                        onClick = { themeViewModel.toggleTheme() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.secondary, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (themeMode == ThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = colors.foreground,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Grid Toggle
                    IconButton(
                        onClick = { viewModel.toggleGridMode() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.secondary, CircleShape)
                    ) {
                        Icon(
                            imageVector = when (uiState.gridMode) {
                                1 -> Icons.Default.ViewList
                                2 -> Icons.Outlined.GridView
                                else -> Icons.Default.GridView
                            },
                            contentDescription = "Toggle grid",
                            tint = colors.foreground,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Clear All
                    IconButton(
                        onClick = { viewModel.clearWishlist() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.secondary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Clear wishlist",
                            tint = colors.foreground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Stats Section
            if (uiState.totalItems > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            icon = Icons.Default.Favorite,
                            value = "$itemCount",
                            label = "Items"
                        )
                        StatItem(
                            icon = Icons.Default.Star,
                            value = String.format("%.1f", avgRating),
                            label = "Avg Rating"
                        )
                        StatItem(
                            icon = Icons.Default.AttachMoney,
                            value = "$${totalPrice.toInt()}",
                            label = "Total"
                        )
                    }
                }
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colors.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading wishlist...",
                                color = colors.mutedForeground
                            )
                        }
                    }
                }

                uiState.products.isEmpty() -> {
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
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = colors.mutedForeground,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your wishlist is empty",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.foreground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Save products you love and find them here later.",
                            color = colors.mutedForeground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateToProductList,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = colors.primaryForeground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Browse products", color = colors.primaryForeground)
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(uiState.gridMode),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = if (uiState.isSelectionMode) 100.dp else 16.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.products,
                            key = { it.id }
                        ) { product ->
                            WishlistCard(
                                product = product,
                                isSelectionMode = uiState.isSelectionMode,
                                isSelected = uiState.selectedItems.contains(product.id.toString()),
                                numColumns = uiState.gridMode,
                                onClick = {
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleSelection(product.id.toString())
                                    } else {
                                        onNavigateToProductDetails(product.id.toString())
                                    }
                                },
                                onLongPress = {
                                    vibrateOnSelect()
                                    viewModel.enterSelectionMode(product.id.toString())
                                },
                                onRemove = { viewModel.removeFromWishlist(product.id.toString()) }
                            )
                        }

                        // Load More
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadMoreCard(
                                onLoadMore = { viewModel.loadMore() },
                                isLoading = uiState.isLoadingMore,
                                hasMore = uiState.hasMore,
                                currentPage = uiState.currentPage,
                                totalPages = uiState.totalPages
                            )
                        }
                    }
                }
            }
        }

        // Floating Selection Bar
        if (uiState.isSelectionMode && uiState.selectedItems.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card)
            ) {
                Button(
                    onClick = { viewModel.removeSelected() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.destructive)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Remove (${uiState.selectedItems.size})",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    val colors = AppTheme.colors

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                imageVector = icon,
                contentDescription = null,
                tint = colors.primaryForeground,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.foreground
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = colors.mutedForeground
            )
        }
    }
}
