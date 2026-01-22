package com.productreview.app.data.repository

import com.productreview.app.fw.core.FWError
import com.productreview.app.fw.core.FWResult
import com.productreview.app.fw.logging.*
import com.productreview.app.fw.pagination.Page
import com.productreview.app.fw.pagination.PageCursor
import com.productreview.app.data.model.*
import com.productreview.app.data.remote.ProductReviewApi
import com.productreview.app.domain.model.Product
import com.productreview.app.domain.model.Review
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for product-related data operations.
 * 
 * All methods return FWResult for consistent error handling.
 * Uses structured logging for observability.
 */
@Singleton
class ProductRepository @Inject constructor(
    private val api: ProductReviewApi,
    private val logger: Logger
) {
    // ==================== PRODUCTS ====================
    
    suspend fun getProducts(
        page: Int = 0,
        size: Int = 10,
        sort: String = "name,asc",
        category: String? = null,
        search: String? = null
    ): FWResult<Page<ApiProduct>> {
        logger.log(
            LogEvent.debug(LogCategory.NETWORK, "get_products")
                .metadata(LogKey.PAGE, page)
                .metadata(LogKey.PAGE_SIZE, size)
                .metadata(LogKey.SORT, sort)
                .apply {
                    category?.let { metadata(LogKey.CATEGORY, it) }
                    search?.let { metadata(LogKey.SEARCH_QUERY, it) }
                }
                .build()
        )
        
        return safeApiCall {
            val response = api.getProducts(
                page = page,
                size = size,
                sort = sort,
                category = category?.takeIf { it != "All" },
                search = search?.takeIf { it.isNotBlank() }
            )
            response.toPage()
        }
    }
    
    suspend fun getProductsPage(
        cursor: PageCursor?,
        size: Int = 20,
        sort: String = "name,asc",
        category: String? = null,
        search: String? = null
    ): FWResult<Page<ApiProduct>> {
        val page = cursor?.toPageNumber() ?: 0
        return getProducts(page, size, sort, category, search)
    }
    
    suspend fun getProduct(id: Long): FWResult<ApiProduct> {
        logger.log(
            LogEvent.debug(LogCategory.NETWORK, "get_product")
                .metadata(LogKey.PRODUCT_ID, id.toString())
                .build()
        )
        
        return safeApiCall { api.getProduct(id) }
    }
    
    suspend fun getGlobalStats(
        category: String? = null,
        search: String? = null
    ): FWResult<GlobalStats> {
        return safeApiCall {
            api.getGlobalStats(
                category = category?.takeIf { it != "All" },
                search = search?.takeIf { it.isNotBlank() }
            )
        }
    }
    
    // ==================== REVIEWS ====================
    
    suspend fun getReviews(
        productId: Long,
        page: Int = 0,
        size: Int = 10,
        sort: String = "createdAt,desc",
        rating: Int? = null
    ): FWResult<Page<ApiReview>> {
        logger.log(
            LogEvent.debug(LogCategory.NETWORK, "get_reviews")
                .metadata(LogKey.PRODUCT_ID, productId.toString())
                .metadata(LogKey.PAGE, page)
                .build()
        )
        
        return safeApiCall {
            val response = api.getReviews(productId, page, size, sort, rating)
            response.toPage()
        }
    }
    
    suspend fun getReviewsPage(
        productId: Long,
        cursor: PageCursor?,
        size: Int = 10,
        rating: Int? = null
    ): FWResult<Page<ApiReview>> {
        val page = cursor?.toPageNumber() ?: 0
        return getReviews(productId, page, size, rating = rating)
    }
    
    suspend fun postReview(
        productId: Long,
        reviewerName: String?,
        rating: Int,
        comment: String
    ): FWResult<ApiReview> {
        logger.log(
            LogEvent.info(LogCategory.DATA, "post_review")
                .metadata(LogKey.PRODUCT_ID, productId.toString())
                .build()
        )
        
        return safeApiCall {
            api.postReview(productId, ReviewRequest(reviewerName, rating, comment))
        }
    }
    
    suspend fun markReviewAsHelpful(reviewId: Long): FWResult<ApiReview> {
        logger.log(
            LogEvent.info(LogCategory.DATA, "mark_helpful")
                .metadata(LogKey.REVIEW_ID, reviewId.toString())
                .build()
        )
        
        return safeApiCall { api.markReviewAsHelpful(reviewId) }
    }
    
    suspend fun getUserVotedReviews(): FWResult<List<Long>> {
        return safeApiCall { api.getUserVotedReviews() }
    }
    
    // ==================== AI CHAT ====================
    
    suspend fun chatWithAI(productId: Long, question: String): FWResult<ChatResponse> {
        logger.log(
            LogEvent.info(LogCategory.DATA, "ai_chat")
                .metadata(LogKey.PRODUCT_ID, productId.toString())
                .build()
        )
        
        return safeApiCall { api.chatWithAI(productId, ChatRequest(question)) }
    }
    
    // ==================== HELPERS ====================
    
    private suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): FWResult<T> {
        return try {
            FWResult.success(block())
        } catch (e: retrofit2.HttpException) {
            val error = FWError.fromHttpStatus(e.code(), e.message())
            logger.log(
                LogEvent.error(LogCategory.NETWORK, "api_error")
                    .metadata(LogKey.STATUS, e.code())
                    .metadata(LogKey.ERROR_CODE, error.code)
                    .metadata(LogKey.IS_RETRIABLE, error.isRetriable)
                    .error(e)
                    .build()
            )
            FWResult.failure(error)
        } catch (e: Exception) {
            val error = FWError.fromThrowable(e)
            logger.log(
                LogEvent.error(LogCategory.NETWORK, "api_error")
                    .metadata(LogKey.ERROR_CODE, error.code)
                    .metadata(LogKey.IS_RETRIABLE, error.isRetriable)
                    .error(e)
                    .build()
            )
            FWResult.failure(error)
        }
    }
}

// ==================== EXTENSIONS ====================

fun <T> PageResponse<T>.toPage(): Page<T> = Page.fromSpringPage(
    content = content,
    currentPage = number,
    totalPages = totalPages,
    totalElements = totalElements,
    isLast = last
)

fun ApiProduct.toDomain(): Product = Product(
    id = id.toString(),
    name = name,
    description = description,
    categories = categories,
    price = price,
    imageUrl = imageUrl,
    averageRating = averageRating ?: 0.0,
    reviewCount = reviewCount ?: 0,
    ratingBreakdown = ratingBreakdown,
    aiSummary = aiSummary
)

fun ApiReview.toDomain(productId: String): Review = Review(
    id = (id ?: System.currentTimeMillis()).toString(),
    productId = productId,
    userName = reviewerName ?: "Anonymous",
    rating = rating,
    comment = comment,
    createdAt = createdAt ?: "",
    helpfulCount = helpfulCount ?: 0
)
