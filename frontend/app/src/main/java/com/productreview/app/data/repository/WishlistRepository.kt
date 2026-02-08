package com.productreview.app.data.repository

import com.productreview.app.core.FWError
import com.productreview.app.core.FWResult
import com.productreview.app.core.pagination.Page
import com.productreview.app.core.pagination.PageCursor
import com.productreview.app.core.logging.LogCategory
import com.productreview.app.core.logging.LogEvent
import com.productreview.app.core.logging.LogLevel
import com.productreview.app.core.logging.Logger
import com.productreview.app.data.model.ApiProduct
import com.productreview.app.data.remote.ProductReviewApi
import com.productreview.app.domain.model.WishlistItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for wishlist operations.
 * Uses optimistic updates for better UX.
 */
@Singleton
class WishlistRepository @Inject constructor(
    private val api: ProductReviewApi,
    private val logger: Logger
) {
    private val _wishlistIds = MutableStateFlow<Set<String>>(emptySet())
    val wishlistIds: StateFlow<Set<String>> = _wishlistIds.asStateFlow()

    private val _wishlistItems = MutableStateFlow<List<WishlistItem>>(emptyList())
    val wishlistItems: StateFlow<List<WishlistItem>> = _wishlistItems.asStateFlow()

    val wishlistCount: Int get() = _wishlistIds.value.size

    fun isInWishlist(productId: String): Boolean = _wishlistIds.value.contains(productId)

    suspend fun loadWishlist(): FWResult<Unit> {
        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "load_wishlist",
            level = LogLevel.DEBUG
        ))

        return safeApiCall {
            val ids = api.getWishlist()
            _wishlistIds.value = ids.map { it.toString() }.toSet()
            logger.log(LogEvent(
                category = LogCategory.DATA,
                name = "wishlist_loaded",
                level = LogLevel.INFO,
                metadata = mapOf("totalItems" to ids.size.toString())
            ))
        }
    }

    suspend fun getWishlistProducts(
        page: Int = 0,
        size: Int = 10,
        sort: String = "id,desc"
    ): FWResult<Page<ApiProduct>> {
        return safeApiCall {
            val response = api.getWishlistProducts(page, size, sort)
            response.toPage()
        }
    }

    suspend fun getWishlistProductsPage(
        cursor: PageCursor?,
        size: Int = 10
    ): FWResult<Page<ApiProduct>> {
        val page = cursor?.toPageNumber() ?: 0
        return getWishlistProducts(page, size)
    }

    suspend fun toggleWishlist(product: ApiProduct): FWResult<Unit> {
        val productId = product.id.toString()
        val wasInWishlist = _wishlistIds.value.contains(productId)

        // Optimistic update
        if (wasInWishlist) {
            _wishlistIds.value = _wishlistIds.value - productId
            _wishlistItems.value = _wishlistItems.value.filter { it.id != productId }
        } else {
            _wishlistIds.value = _wishlistIds.value + productId
            _wishlistItems.value = _wishlistItems.value + product.toWishlistItem()
        }

        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = if (wasInWishlist) "wishlist_remove" else "wishlist_add",
            level = LogLevel.INFO,
            metadata = mapOf("productId" to productId)
        ))

        val result = safeApiCall {
            api.toggleWishlist(product.id)
        }

        if (result.isFailure) {
            // Rollback on failure
            if (wasInWishlist) {
                _wishlistIds.value = _wishlistIds.value + productId
            } else {
                _wishlistIds.value = _wishlistIds.value - productId
            }
        }

        return result
    }

    suspend fun addToWishlist(product: ApiProduct): FWResult<Unit> {
        val productId = product.id.toString()
        if (_wishlistIds.value.contains(productId)) return FWResult.success(Unit)

        _wishlistIds.value = _wishlistIds.value + productId
        _wishlistItems.value = _wishlistItems.value + product.toWishlistItem()

        val result = safeApiCall { api.toggleWishlist(product.id) }
        if (result.isFailure) {
            _wishlistIds.value = _wishlistIds.value - productId
        }
        return result
    }

    suspend fun removeFromWishlist(productId: String): FWResult<Unit> {
        if (!_wishlistIds.value.contains(productId)) return FWResult.success(Unit)

        val numericId = productId.toLongOrNull()
            ?: return FWResult.failure(FWError.invalidArgument("Invalid product ID: $productId"))

        val oldItems = _wishlistItems.value
        _wishlistIds.value = _wishlistIds.value - productId
        _wishlistItems.value = _wishlistItems.value.filter { it.id != productId }

        val result = safeApiCall { api.toggleWishlist(numericId) }
        if (result.isFailure) {
            _wishlistIds.value = _wishlistIds.value + productId
            _wishlistItems.value = oldItems
        }
        return result
    }

    suspend fun addMultipleToWishlist(products: List<ApiProduct>): FWResult<Unit> {
        val newProducts = products.filter { !_wishlistIds.value.contains(it.id.toString()) }
        if (newProducts.isEmpty()) return FWResult.success(Unit)

        val newIds = newProducts.map { it.id.toString() }.toSet()
        _wishlistIds.value = _wishlistIds.value + newIds
        _wishlistItems.value = _wishlistItems.value + newProducts.map { it.toWishlistItem() }

        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "wishlist_batch_add",
            level = LogLevel.INFO,
            metadata = mapOf("totalItems" to newProducts.size.toString())
        ))

        return try {
            newProducts.forEach { api.toggleWishlist(it.id) }
            FWResult.success(Unit)
        } catch (e: Exception) {
            FWResult.failure(FWError.fromThrowable(e))
        }
    }

    suspend fun removeMultipleFromWishlist(productIds: List<String>): FWResult<Unit> {
        val toRemove = productIds.filter { _wishlistIds.value.contains(it) }
        if (toRemove.isEmpty()) return FWResult.success(Unit)

        val oldIds = _wishlistIds.value
        val oldItems = _wishlistItems.value
        _wishlistIds.value = _wishlistIds.value - toRemove.toSet()
        _wishlistItems.value = _wishlistItems.value.filter { it.id !in toRemove }

        return try {
            toRemove.forEach {
                val numericId = it.toLongOrNull()
                    ?: return FWResult.failure(FWError.invalidArgument("Invalid product ID: $it"))
                api.toggleWishlist(numericId)
            }
            FWResult.success(Unit)
        } catch (e: Exception) {
            _wishlistIds.value = oldIds
            _wishlistItems.value = oldItems
            FWResult.failure(FWError.fromThrowable(e))
        }
    }

    suspend fun clearWishlist(): FWResult<Unit> {
        val currentIds = _wishlistIds.value.toList()
        if (currentIds.isEmpty()) return FWResult.success(Unit)

        val oldIds = _wishlistIds.value
        val oldItems = _wishlistItems.value
        _wishlistIds.value = emptySet()
        _wishlistItems.value = emptyList()

        logger.log(LogEvent(
            category = LogCategory.DATA,
            name = "wishlist_clear",
            level = LogLevel.INFO,
            metadata = mapOf("totalItems" to currentIds.size.toString())
        ))

        return try {
            currentIds.forEach {
                val numericId = it.toLongOrNull()
                    ?: return FWResult.failure(FWError.invalidArgument("Invalid product ID: $it"))
                api.toggleWishlist(numericId)
            }
            FWResult.success(Unit)
        } catch (e: Exception) {
            _wishlistIds.value = oldIds
            _wishlistItems.value = oldItems
            FWResult.failure(FWError.fromThrowable(e))
        }
    }

    private suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): FWResult<T> {
        return try {
            FWResult.success(block())
        } catch (e: retrofit2.HttpException) {
            FWResult.failure(FWError.fromHttpStatus(e.code(), e.message()))
        } catch (e: Exception) {
            FWResult.failure(FWError.fromThrowable(e))
        }
    }
}

private fun ApiProduct.toWishlistItem(): WishlistItem = WishlistItem(
    id = id.toString(),
    name = name,
    price = price,
    imageUrl = imageUrl,
    category = categories.firstOrNull(),
    averageRating = averageRating,
    reviewCount = reviewCount,
    addedAt = LocalDateTime.now()
)
