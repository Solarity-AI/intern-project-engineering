package com.productreview.app.data.repository

import com.productreview.app.core.FWError
import com.productreview.app.core.FWResult
import com.productreview.app.core.pagination.Page
import com.productreview.app.core.pagination.PageCursor
import com.productreview.app.core.logging.LogCategory
import com.productreview.app.core.logging.LogEvent
import com.productreview.app.core.logging.LogKey
import com.productreview.app.core.logging.LogLevel
import com.productreview.app.core.logging.Logger
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
        val metadata = mutableMapOf(
            "page" to page.toString(),
            "pageSize" to size.toString(),
            "sort" to sort
        )
        category?.let { metadata["category"] = it }
        search?.let { metadata["searchQuery"] = it }

        logger.log(LogEvent(
            category = LogCategory.NETWORK,
            name = "get_products",
            level = LogLevel.DEBUG,
            metadata = metadata
        ))

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
        logger.log(LogEvent(
            category = LogCategory.NETWORK,
            name = "get_product",
            level = LogLevel.DEBUG,
            metadata = mapOf("productId" to id.toString())
        ))

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
        logger.log(LogEvent(
            category = LogCategory.NETWORK,
            name = "get_reviews",
            level = LogLevel.DEBUG,
            metadata = mapOf(
                "productId" to productId.toString(),
                "page" to page.toString()
            )
        ))

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
        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "post_review",
            level = LogLevel.INFO,
            metadata = mapOf("productId" to productId.toString())
        ))

        return safeApiCall {
            api.postReview(productId, ReviewRequest(reviewerName, rating, comment))
        }
    }

    suspend fun markReviewAsHelpful(reviewId: Long): FWResult<ApiReview> {
        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "mark_helpful",
            level = LogLevel.INFO,
            metadata = mapOf("reviewId" to reviewId.toString())
        ))

        return safeApiCall { api.markReviewAsHelpful(reviewId) }
    }

    suspend fun getUserVotedReviews(): FWResult<List<Long>> {
        return safeApiCall { api.getUserVotedReviews() }
    }

    // ==================== AI CHAT ====================

    suspend fun chatWithAI(productId: Long, question: String): FWResult<ChatResponse> {
        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "ai_chat",
            level = LogLevel.INFO,
            metadata = mapOf("productId" to productId.toString())
        ))

        return safeApiCall { api.chatWithAI(productId, ChatRequest(question)) }
    }

    // ==================== HELPERS ====================

    private suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): FWResult<T> {
        return try {
            FWResult.success(block())
        } catch (e: retrofit2.HttpException) {
            val error = FWError.fromHttpStatus(e.code(), e.message())
            logger.log(LogEvent(
                category = LogCategory.NETWORK,
                name = "api_error",
                level = LogLevel.ERROR,
                metadata = mapOf(
                    LogKey.STATUS.value to e.code().toString(),
                    LogKey.ERROR_CODE.value to error.code
                )
            ))
            FWResult.failure(error)
        } catch (e: Exception) {
            val error = FWError.fromThrowable(e)
            logger.log(LogEvent(
                category = LogCategory.NETWORK,
                name = "api_error",
                level = LogLevel.ERROR,
                metadata = mapOf(
                    LogKey.ERROR_CODE.value to error.code
                )
            ))
            FWResult.failure(error)
        }
    }
}

// ==================== EXTENSIONS ====================

fun <T> PageResponse<T>.toPage(): Page<T> {
    val nextCursor = if (last) null else PageCursor((number + 1).toString())
    return Page(
        items = content,
        nextCursor = nextCursor
    )
}

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
