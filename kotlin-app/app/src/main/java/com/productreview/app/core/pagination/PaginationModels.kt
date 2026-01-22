package com.productreview.app.core.pagination

import com.productreview.app.core.FWError

@JvmInline
value class PageCursor(val value: String) {
    companion object {
        fun fromPageNumber(page: Int): PageCursor = PageCursor(page.toString())
    }
    fun toPageNumber(): Int = value.toIntOrNull() ?: 0
}

data class Page<T>(
    val items: List<T>,
    val nextCursor: PageCursor?,
    val totalElements: Int? = null,
    val totalPages: Int? = null
) {
    val hasMore: Boolean get() = nextCursor != null
    val isEmpty: Boolean get() = items.isEmpty()
    val size: Int get() = items.size

    companion object {
        fun <T> empty(): Page<T> = Page(emptyList(), null)

        fun <T> fromSpringPage(
            content: List<T>,
            currentPage: Int,
            totalPages: Int,
            totalElements: Int,
            isLast: Boolean
        ): Page<T> = Page(
            items = content,
            nextCursor = if (isLast) null else PageCursor.fromPageNumber(currentPage + 1),
            totalElements = totalElements,
            totalPages = totalPages
        )
    }
}

data class PagingState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Int = 0,
    val error: FWError? = null
) {
    val canLoadMore: Boolean get() = hasMore && !isLoading && !isLoadingMore && error == null
    val isInitialLoading: Boolean get() = isLoading && currentPage == 0
    val isEmpty: Boolean get() = !isLoading && !isLoadingMore && totalElements == 0

    companion object {
        val Initial = PagingState()
        val Loading = PagingState(isLoading = true)
    }
}

data class PagingResult<T>(
    val items: List<T>,
    val hasMore: Boolean,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Int = 0
) {
    companion object {
        fun <T> fromPage(page: Page<T>, currentPage: Int = 0): PagingResult<T> = PagingResult(
            items = page.items,
            hasMore = page.hasMore,
            currentPage = currentPage,
            totalPages = page.totalPages ?: 0,
            totalElements = page.totalElements ?: 0
        )
        fun <T> empty(): PagingResult<T> = PagingResult(emptyList(), false)
    }
}
