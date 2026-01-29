package com.productreview.app.ui.screens.productdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productreview.app.core.FWError
import com.productreview.app.core.FWResult
import com.productreview.app.core.logging.LogCategory
import com.productreview.app.core.logging.LogEvent
import com.productreview.app.core.logging.LogKey
import com.productreview.app.core.logging.Logger
import com.productreview.app.data.model.ApiProduct
import com.productreview.app.data.model.ApiReview
import com.productreview.app.data.repository.NotificationRepository
import com.productreview.app.data.repository.ProductRepository
import com.productreview.app.data.repository.WishlistRepository
import com.productreview.app.data.repository.toDomain
import com.productreview.app.domain.model.NotificationType
import com.productreview.app.domain.model.Review
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailsUiState(
    val product: ApiProduct? = null,
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingReviews: Boolean = false,
    val error: FWError? = null,
    val selectedRatingFilter: Int? = null,
    val currentReviewPage: Int = 0,
    val totalReviewPages: Int = 0,
    val hasMoreReviews: Boolean = true,
    val helpfulReviewIds: Set<String> = emptySet(),
    val isReviewModalOpen: Boolean = false,
    val isSubmittingReview: Boolean = false,
    val submitError: FWError? = null
)

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val wishlistRepository: WishlistRepository,
    private val notificationRepository: NotificationRepository,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    val wishlistIds: StateFlow<Set<String>> = wishlistRepository.wishlistIds

    private var productId: Long = 0

    fun loadProduct(id: String) {
        productId = id.toLongOrNull() ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            logger.log(
                LogEvent.info(LogCategory.DATA, "load_product")
                    .metadata(LogKey.PRODUCT_ID, id)
                    .build()
            )

            when (val result = productRepository.getProduct(productId)) {
                is FWResult.Success -> {
                    _uiState.update { it.copy(product = result.value, isLoading = false) }
                }
                is FWResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error) }
                    logger.log(
                        LogEvent.error(LogCategory.DATA, "product_load_failed")
                            .metadata(LogKey.PRODUCT_ID, id)
                            .metadata(LogKey.ERROR_CODE, result.error.code)
                            .build()
                    )
                }
            }

            loadReviews(page = 0, append = false)
            loadUserVotes()
        }
    }

    private fun loadReviews(page: Int, append: Boolean) {
        viewModelScope.launch {
            if (!append) {
                _uiState.update { it.copy(isLoadingReviews = true) }
            }

            logger.log(
                LogEvent.debug(LogCategory.PAGINATION, "load_reviews")
                    .metadata(LogKey.PRODUCT_ID, productId.toString())
                    .metadata(LogKey.PAGE, page)
                    .build()
            )

            when (val result = productRepository.getReviews(
                productId = productId,
                page = page,
                size = 10,
                rating = _uiState.value.selectedRatingFilter
            )) {
                is FWResult.Success -> {
                    val pageData = result.value
                    val newReviews = pageData.items.map { it.toDomain(productId.toString()) }
                    _uiState.update { state ->
                        state.copy(
                            reviews = if (append) state.reviews + newReviews else newReviews,
                            isLoadingReviews = false,
                            currentReviewPage = page,
                            totalReviewPages = pageData.totalPages ?: 0,
                            hasMoreReviews = pageData.hasMore
                        )
                    }
                }
                is FWResult.Failure -> {
                    _uiState.update { it.copy(isLoadingReviews = false) }
                    logger.log(
                        LogEvent.warn(LogCategory.PAGINATION, "reviews_load_failed")
                            .metadata(LogKey.ERROR_CODE, result.error.code)
                            .build()
                    )
                }
            }
        }
    }

    private fun loadUserVotes() {
        viewModelScope.launch {
            productRepository.getUserVotedReviews().onSuccess { votes ->
                _uiState.update {
                    it.copy(helpfulReviewIds = votes.map { v -> v.toString() }.toSet())
                }
            }
        }
    }

    fun loadMoreReviews() {
        val state = _uiState.value
        if (state.isLoadingReviews || !state.hasMoreReviews) return
        loadReviews(page = state.currentReviewPage + 1, append = true)
    }

    fun setRatingFilter(rating: Int?) {
        _uiState.update { it.copy(selectedRatingFilter = rating) }
        loadReviews(page = 0, append = false)
    }

    fun toggleHelpful(reviewId: String) {
        viewModelScope.launch {
            val currentlyHelpful = _uiState.value.helpfulReviewIds.contains(reviewId)
            
            // Optimistic update
            _uiState.update { state ->
                val newHelpfulIds = if (currentlyHelpful) {
                    state.helpfulReviewIds - reviewId
                } else {
                    state.helpfulReviewIds + reviewId
                }
                val newReviews = state.reviews.map { review ->
                    if (review.id == reviewId) {
                        review.copy(
                            helpfulCount = if (currentlyHelpful) 
                                maxOf(0, review.helpfulCount - 1) 
                            else 
                                review.helpfulCount + 1
                        )
                    } else review
                }
                state.copy(helpfulReviewIds = newHelpfulIds, reviews = newReviews)
            }
            
            logger.log(
                LogEvent.info(LogCategory.DATA, "toggle_helpful")
                    .metadata(LogKey.REVIEW_ID, reviewId)
                    .build()
            )

            productRepository.markReviewAsHelpful(reviewId.toLong())
        }
    }

    fun openReviewModal() {
        _uiState.update { it.copy(isReviewModalOpen = true, submitError = null) }
    }

    fun closeReviewModal() {
        _uiState.update { it.copy(isReviewModalOpen = false, submitError = null) }
    }

    fun submitReview(userName: String?, rating: Int, comment: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true, submitError = null) }
            
            logger.log(
                LogEvent.info(LogCategory.DATA, "submit_review")
                    .metadata(LogKey.PRODUCT_ID, productId.toString())
                    .build()
            )

            when (val result = productRepository.postReview(
                productId = productId,
                reviewerName = userName,
                rating = rating,
                comment = comment
            )) {
                is FWResult.Success -> {
                    _uiState.update { it.copy(isSubmittingReview = false, isReviewModalOpen = false) }
                    
                    val productName = _uiState.value.product?.name ?: "Product"
                    notificationRepository.addNotification(
                        type = NotificationType.REVIEW,
                        title = "Review Posted",
                        body = "Your review for $productName has been published.",
                        productId = productId.toString(),
                        productName = productName
                    )
                    
                    logger.log(
                        LogEvent.info(LogCategory.DATA, "review_submitted")
                            .metadata(LogKey.PRODUCT_ID, productId.toString())
                            .build()
                    )
                    
                    loadProduct(productId.toString())
                    onSuccess()
                }
                is FWResult.Failure -> {
                    _uiState.update { it.copy(isSubmittingReview = false, submitError = result.error) }
                    logger.log(
                        LogEvent.error(LogCategory.DATA, "review_submit_failed")
                            .metadata(LogKey.ERROR_CODE, result.error.code)
                            .build()
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSubmitError() {
        _uiState.update { it.copy(submitError = null) }
    }

    fun isInWishlist(): Boolean = wishlistRepository.isInWishlist(productId.toString())

    fun toggleWishlist() {
        val product = _uiState.value.product ?: return
        viewModelScope.launch { wishlistRepository.toggleWishlist(product) }
    }
}

private fun ApiReview.toDomain(productId: String): Review = Review(
    id = (id ?: System.currentTimeMillis()).toString(),
    productId = productId,
    userName = reviewerName ?: "Anonymous",
    rating = rating,
    comment = comment,
    createdAt = createdAt ?: "",
    helpfulCount = helpfulCount ?: 0
)
