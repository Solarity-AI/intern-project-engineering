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

    suspend fun getProduct(id: String): FWResult<ApiProduct> {
        logger.log(LogEvent(
            category = LogCategory.NETWORK,
            name = "get_product",
            level = LogLevel.DEBUG,
            metadata = mapOf("productId" to id)
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
        productId: String,
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
                "productId" to productId,
                "page" to page.toString()
            )
        ))

        return safeApiCall {
            val response = api.getReviews(productId, page, size, sort, rating)
            response.toPage()
        }
    }

    suspend fun getReviewsPage(
        productId: String,
        cursor: PageCursor?,
        size: Int = 10,
        rating: Int? = null
    ): FWResult<Page<ApiReview>> {
        val page = cursor?.toPageNumber() ?: 0
        return getReviews(productId, page, size, rating = rating)
    }

    suspend fun postReview(
        productId: String,
        reviewerName: String?,
        rating: Int,
        comment: String
    ): FWResult<ApiReview> {
        val sanitizedName = reviewerName?.trim()?.takeIf { it.isNotBlank() }
            ?: "Anonymous"
        val sanitizedComment = comment.trim()
        val clampedRating = rating.coerceIn(1, 5)

        // Client-side validation matching backend @Valid constraints
        if (sanitizedName.length < 2 || sanitizedName.length > 50) {
            return FWResult.failure(FWError.invalidArgument("Reviewer name must be 2-50 characters"))
        }
        if (sanitizedComment.length < 10 || sanitizedComment.length > 500) {
            return FWResult.failure(FWError.invalidArgument("Comment must be 10-500 characters"))
        }

        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "post_review",
            level = LogLevel.INFO,
            metadata = mapOf(
                "productId" to productId,
                "nameLen" to sanitizedName.length.toString(),
                "commentLen" to sanitizedComment.length.toString(),
                "rating" to clampedRating.toString()
            )
        ))

        return try {
            FWResult.success(
                api.postReview(productId, ReviewRequest(sanitizedName, clampedRating, sanitizedComment))
            )
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 400) {
                val body = e.response()?.errorBody()?.string()
                logger.log(LogEvent(
                    category = LogCategory.NETWORK,
                    name = "REVIEW_400",
                    level = LogLevel.ERROR,
                    metadata = mapOf(
                        "productId" to productId,
                        "status" to "400",
                        "errorBody" to (body ?: "empty")
                    )
                ))
            }
            val error = FWError.fromHttpStatus(e.code(), e.message())
            FWResult.failure(error)
        } catch (e: Exception) {
            val error = FWError.fromThrowable(e)
            FWResult.failure(error)
        }
    }

    suspend fun markReviewAsHelpful(reviewId: String): FWResult<ApiReview> {
        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "mark_helpful",
            level = LogLevel.INFO,
            metadata = mapOf("reviewId" to reviewId)
        ))

        return safeApiCall { api.markReviewAsHelpful(reviewId) }
    }

    suspend fun getUserVotedReviews(): FWResult<List<String>> {
        return safeApiCall { api.getUserVotedReviews() }
    }

    // ==================== AI CHAT ====================

    suspend fun chatWithAI(productId: String, question: String): FWResult<ChatResponse> {
        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "ai_chat",
            level = LogLevel.INFO,
            metadata = mapOf("productId" to productId)
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
    val items = content.ifEmpty { data }
    val pageNumber = page?.number ?: number
    val isLast = if (page != null) {
        pageNumber + 1 >= page.totalPages || items.isEmpty()
    } else {
        last
    }
    val nextCursor = if (isLast) null else PageCursor((pageNumber + 1).toString())
    return Page(
        items = items,
        nextCursor = nextCursor,
        totalElements = page?.totalElements?.toInt() ?: totalElements,
        totalPages = page?.totalPages ?: totalPages
    )
}




fun ApiReview.toDomain(productId: String): Review = Review(
    id = id ?: System.currentTimeMillis().toString(),
    productId = productId,
    userName = reviewerName ?: "Anonymous",
    rating = rating,
    comment = comment,
    createdAt = createdAt ?: "",
    helpfulCount = helpfulCount ?: 0
)
