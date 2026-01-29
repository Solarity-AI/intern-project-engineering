package com.productreview.app.ui.screens.productlist

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productreview.app.data.local.ThemeMode
import com.productreview.app.ui.components.*
import com.productreview.app.ui.theme.AppTheme
import com.productreview.app.ui.theme.GradientColors
import com.productreview.app.ui.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onNavigateToProductDetails: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    viewModel: ProductListViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val wishlistCount by viewModel.wishlistCount.collectAsState()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()

    val gridState = rememberLazyGridState()

    // Vibrate on entering selection mode
    val vibrator = context.getSystemService(Vibrator::class.java)

    fun vibrateOnSelect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(uiState.gridMode),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = if (uiState.isSelectionMode) 100.dp else 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section (spans full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo
                        Row(
                            modifier = Modifier.clickable { viewModel.resetFilters() },
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

                        // Header Actions
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Theme Toggle
                            HeaderIconButton(
                                icon = if (themeMode == ThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                                onClick = { themeViewModel.toggleTheme() }
                            )

                            // Grid Toggle
                            HeaderIconButton(
                                icon = when (uiState.gridMode) {
                                    1 -> Icons.Default.ViewList
                                    2 -> Icons.Outlined.GridView
                                    else -> Icons.Default.GridView
                                },
                                onClick = { viewModel.toggleGridMode() }
                            )

                            // Wishlist
                            HeaderIconButton(
                                icon = Icons.Outlined.FavoriteBorder,
                                onClick = onNavigateToWishlist,
                                badgeCount = wishlistCount
                            )

                            // Notifications
                            HeaderIconButton(
                                icon = Icons.Outlined.Notifications,
                                onClick = onNavigateToNotifications,
                                badgeCount = unreadCount
                            )
                        }
                    }

                    // Hero Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.secondary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Find Products You'll ",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.foreground
                            )
                            Text(
                                text = "Love",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                uiState.globalStats?.let { stats ->
                                    StatItem(
                                        icon = Icons.Default.Star,
                                        value = String.format("%.1f", stats.averageRating),
                                        label = "Avg Rating"
                                    )
                                    StatItem(
                                        icon = Icons.Default.ChatBubble,
                                        value = "${stats.totalReviews}",
                                        label = "Reviews"
                                    )
                                    StatItem(
                                        icon = Icons.Default.Inventory2,
                                        value = "${stats.totalProducts}",
                                        label = "Products"
                                    )
                                }
                            }
                        }
                    }

                    // Search Section
                    SearchBar(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        onSearch = { viewModel.submitSearch(it) },
                        searchHistory = searchHistory,
                        onHistoryItemClick = { viewModel.submitSearch(it) },
                        onRemoveHistoryItem = { viewModel.removeSearchTerm(it) },
                        onClearHistory = { viewModel.clearSearchHistory() },
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Filter Section
                    Text(
                        text = "Explore Products",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.foreground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    CategoryFilter(
                        selectedCategory = uiState.selectedCategory,
                        onCategoryChange = { viewModel.setCategory(it) },
                        modifier = Modifier.padding(horizontal = (-16).dp) // Extend to edges
                    )

                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort by:",
                            fontSize = 14.sp,
                            color = colors.mutedForeground
                        )
                    }

                    SortFilter(
                        selectedSort = uiState.sortBy,
                        onSortChange = { viewModel.setSort(it) },
                        modifier = Modifier.padding(horizontal = (-16).dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Loading State
            if (uiState.isLoading && uiState.products.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colors.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading products...",
                                color = colors.mutedForeground
                            )
                        }
                    }
                }
            }

            // Error State
            uiState.error?.let { error ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.destructive.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = colors.destructive
                        )
                        Text(
                            text = error,
                            color = colors.destructive,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.refresh() }) {
                            Text("Retry", color = colors.primary)
                        }
                    }
                }
            }

            // Empty State
            if (!uiState.isLoading && uiState.products.isEmpty() && uiState.error == null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(colors.muted, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = colors.mutedForeground,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No products found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.foreground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try adjusting your search or filters",
                            color = colors.mutedForeground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.resetFilters() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Text("Clear all filters", color = colors.primaryForeground)
                        }
                    }
                }
            }

            // Product Grid
            items(
                items = uiState.products,
                key = { it.id }
            ) { product ->
                ProductCard(
                    product = product,
                    isInWishlist = wishlistIds.contains(product.id.toString()),
                    onProductClick = {
                        if (uiState.isSelectionMode) {
                            viewModel.toggleSelection(product.id.toString())
                        } else {
                            onNavigateToProductDetails(product.id.toString())
                        }
                    },
                    onWishlistClick = { viewModel.toggleWishlist(product) },
                    numColumns = uiState.gridMode,
                    isSelectionMode = uiState.isSelectionMode,
                    isSelected = uiState.selectedItems.contains(product.id.toString()),
                    onLongPress = {
                        vibrateOnSelect()
                        viewModel.enterSelectionMode(product.id.toString())
                    }
                )
            }

            // Load More
            if (uiState.products.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LoadMoreCard(
                        onLoadMore = { viewModel.loadMore() },
                        isLoading = uiState.isLoadingMore,
                        hasMore = uiState.hasMore,
                        currentPage = uiState.currentPage,
                        totalPages = uiState.totalPages,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Floating Selection Bar
        if (uiState.isSelectionMode && uiState.selectedItems.isNotEmpty()) {
            val newToWishlistCount = uiState.selectedItems.count { !wishlistIds.contains(it) }

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
                    onClick = { viewModel.addSelectedToWishlist() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (newToWishlistCount > 0) colors.primary else colors.muted
                    ),
                    enabled = newToWishlistCount > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (newToWishlistCount > 0) 
                            "Add to Wishlist ($newToWishlistCount)" 
                        else 
                            "Already in Wishlist",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    val colors = AppTheme.colors

    Box {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(36.dp)
                .background(colors.secondary, CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.foreground,
                modifier = Modifier.size(20.dp)
            )
        }
        if (badgeCount > 0) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp),
                containerColor = colors.destructive
            ) {
                Text(
                    text = if (badgeCount > 99) "99+" else "$badgeCount",
                    fontSize = 10.sp,
                    color = androidx.compose.ui.graphics.Color.White
                )
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
                modifier = Modifier.size(18.dp)
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
