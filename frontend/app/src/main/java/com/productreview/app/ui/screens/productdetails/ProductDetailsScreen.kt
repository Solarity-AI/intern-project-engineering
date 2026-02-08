package com.productreview.app.ui.screens.productdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.productreview.app.ui.components.*
import com.productreview.app.ui.theme.AppTheme
import com.productreview.app.ui.theme.GradientColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAIAssistant: (String, String) -> Unit,
    viewModel: ProductDetailsViewModel = hiltViewModel()
) {
    val colors = AppTheme.colors
    val uiState by viewModel.uiState.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    
    val isInWishlist = wishlistIds.contains(productId)

    // Load product on first composition
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    // Review Modal
    if (uiState.isReviewModalOpen) {
        AddReviewModal(
            productName = uiState.product?.name ?: "Product",
            isSubmitting = uiState.isSubmittingReview,
            onDismiss = { viewModel.closeReviewModal() },
            onSubmit = { userName, rating, comment ->
                viewModel.submitReview(userName, rating, comment) {
                    // Success callback
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.product == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            uiState.error != null && uiState.product == null -> {
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
                        text = "Product not found",
                        fontSize = 20.sp,
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
            }

            uiState.product != null -> {
                val product = uiState.product!!
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Back Button
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateBack() }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Back",
                                tint = colors.foreground,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Back",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.foreground
                            )
                        }
                    }

                    // Product Image
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            AsyncImage(
                                model = product.imageUrl ?: "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800&q=80",
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Wishlist Button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .size(48.dp)
                                    .shadow(4.dp, CircleShape)
                                    .background(
                                        if (isInWishlist) colors.primary else colors.card.copy(alpha = 0.9f),
                                        CircleShape
                                    )
                                    .clickable { viewModel.toggleWishlist() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isInWishlist) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Wishlist",
                                    tint = if (isInWishlist) Color.White else colors.foreground,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Product Info
                    item {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Categories
                            if (product.categories.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    product.categories.forEach { category ->
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = colors.secondary
                                        ) {
                                            Text(
                                                text = category.uppercase(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.foreground,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Name
                            Text(
                                text = product.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.foreground,
                                lineHeight = 32.sp
                            )

                            // Price
                            Text(
                                text = "$${String.format("%.2f", product.price)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )

                            // Rating
                            product.averageRating?.let { rating ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    StarRating(rating = rating, size = StarSize.MEDIUM)
                                    Text(
                                        text = String.format("%.1f", rating),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.foreground
                                    )
                                    Text(
                                        text = "(${product.reviewCount ?: 0} reviews)",
                                        fontSize = 16.sp,
                                        color = colors.mutedForeground
                                    )
                                }
                            }

                            // Description
                            if (product.description?.isNotBlank() == true) {
                                Text(
                                    text = product.description ?: "",
                                    fontSize = 16.sp,
                                    lineHeight = 26.sp,
                                    color = colors.foreground,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    }

                    // AI Summary
                    product.aiSummary?.let { summary ->
                        item {
                            AISummaryCard(
                                summary = summary,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Rating Breakdown
                    item {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Rating Breakdown",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.foreground
                            )
                            RatingBreakdown(
                                breakdown = product.ratingBreakdown?.mapValues { it.value.toInt() },
                                totalCount = product.reviewCount ?: 0,
                                selectedRating = uiState.selectedRatingFilter,
                                onSelectRating = { viewModel.setRatingFilter(it) }
                            )
                        }
                    }

                    // Reviews Section Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reviews${uiState.selectedRatingFilter?.let { " ($it★)" } ?: ""}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.foreground
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // AI Chat Button
                                IconButton(
                                    onClick = { onNavigateToAIAssistant(productId, product.name) },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .shadow(4.dp, CircleShape)
                                        .background(
                                            Brush.linearGradient(GradientColors.AI),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Forum,
                                        contentDescription = "AI Chat",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Add Review Button
                                Button(
                                    onClick = { viewModel.openReviewModal() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Brush.linearGradient(GradientColors.Primary),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = "Add Review",
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.primaryForeground
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Reviews List
                    if (uiState.reviews.isEmpty() && !uiState.isLoadingReviews) {
                        item {
                            Text(
                                text = if (uiState.selectedRatingFilter != null) 
                                    "No ${uiState.selectedRatingFilter}★ reviews found."
                                else 
                                    "No reviews yet. Be the first to review!",
                                color = colors.mutedForeground,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        items(
                            items = uiState.reviews,
                            key = { it.id }
                        ) { review ->
                            ReviewCard(
                                review = review,
                                isHelpful = uiState.helpfulReviewIds.contains(review.id),
                                onHelpfulClick = { viewModel.toggleHelpful(review.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Load More Reviews
                        item {
                            LoadMoreCard(
                                onLoadMore = { viewModel.loadMoreReviews() },
                                isLoading = uiState.isLoadingReviews,
                                hasMore = uiState.hasMoreReviews,
                                currentPage = uiState.currentReviewPage,
                                totalPages = uiState.totalReviewPages,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
