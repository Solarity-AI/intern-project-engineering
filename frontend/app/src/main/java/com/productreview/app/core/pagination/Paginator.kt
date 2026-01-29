package com.productreview.app.core.pagination

import com.productreview.app.core.FWError
import com.productreview.app.core.FWResult
import com.productreview.app.core.RefreshController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Enhanced paginator that manages pagination state and refresh.
 * Screens should NOT manage pagination state manually.
 */
class Paginator<T>(
    private val loadPage: suspend (PageCursor?) -> FWResult<Page<T>>
) {
    private val mutex = Mutex()
    private val refreshController = RefreshController()
    private var currentCursor: PageCursor? = null

    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items.asStateFlow()

    private val _pagingState = MutableStateFlow(PagingState.Initial)
    val pagingState: StateFlow<PagingState> = _pagingState.asStateFlow()

    val isRefreshing: StateFlow<Boolean> = refreshController.isRefreshing

    suspend fun loadFirst(): FWResult<PagingResult<T>> = mutex.withLock {
        resetInternal()
        loadNextInternal(isInitialLoad = true)
    }

    suspend fun refresh(): FWResult<PagingResult<T>> {
        return refreshController.executeRefreshWithResult {
            mutex.withLock {
                resetInternal()
                loadNextInternal(isInitialLoad = true)
            }
        }
    }

    suspend fun loadNext(): FWResult<PagingResult<T>> = mutex.withLock {
        loadNextInternal(isInitialLoad = false)
    }

    private suspend fun loadNextInternal(isInitialLoad: Boolean): FWResult<PagingResult<T>> {
        val currentState = _pagingState.value

        if (currentState.isLoading || currentState.isLoadingMore) {
            return FWResult.failure(FWError.invalidArgument("Already loading"))
        }

        if (!isInitialLoad && !currentState.hasMore) {
            return FWResult.failure(FWError.invalidArgument("No more pages available"))
        }

        _pagingState.value = currentState.copy(
            isLoading = isInitialLoad,
            isLoadingMore = !isInitialLoad,
            error = null
        )

        return try {
            when (val result = loadPage(currentCursor)) {
                is FWResult.Success -> {
                    val page = result.value
                    _items.value = _items.value + page.items
                    currentCursor = page.nextCursor

                    val newState = PagingState(
                        isLoading = false,
                        isLoadingMore = false,
                        hasMore = page.hasMore,
                        currentPage = currentCursor?.toPageNumber() ?: 0,
                        totalPages = page.totalPages ?: 0,
                        totalElements = page.totalElements ?: _items.value.size,
                        error = null
                    )
                    _pagingState.value = newState

                    FWResult.success(PagingResult(
                        items = page.items,
                        hasMore = page.hasMore,
                        currentPage = newState.currentPage,
                        totalPages = newState.totalPages,
                        totalElements = newState.totalElements
                    ))
                }
                is FWResult.Failure -> {
                    _pagingState.value = currentState.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = result.error
                    )
                    result
                }
            }
        } catch (e: Exception) {
            val error = FWError.fromThrowable(e)
            _pagingState.value = currentState.copy(
                isLoading = false,
                isLoadingMore = false,
                error = error
            )
            FWResult.failure(error)
        }
    }

    fun reset() { resetInternal() }

    private fun resetInternal() {
        currentCursor = null
        _items.value = emptyList()
        _pagingState.value = PagingState.Initial
    }

    fun addItemAtStart(item: T) {
        _items.value = listOf(item) + _items.value
        _pagingState.value = _pagingState.value.copy(totalElements = _pagingState.value.totalElements + 1)
    }

    fun addItemAtEnd(item: T) {
        _items.value = _items.value + item
        _pagingState.value = _pagingState.value.copy(totalElements = _pagingState.value.totalElements + 1)
    }

    fun updateItem(predicate: (T) -> Boolean, transform: (T) -> T) {
        _items.value = _items.value.map { if (predicate(it)) transform(it) else it }
    }

    fun removeItems(predicate: (T) -> Boolean) {
        val removed = _items.value.count(predicate)
        _items.value = _items.value.filterNot(predicate)
        _pagingState.value = _pagingState.value.copy(totalElements = maxOf(0, _pagingState.value.totalElements - removed))
    }

    fun getItems(): List<T> = _items.value
    fun canLoadMore(): Boolean = _pagingState.value.canLoadMore
}
