package com.productreview.app.ui.screens.productlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productreview.app.core.DispatcherProvider
import com.productreview.app.core.FWError
import com.productreview.app.core.FWResult
import com.productreview.app.core.logging.LogCategory
import com.productreview.app.core.logging.LogEvent
import com.productreview.app.core.logging.LogKey
import com.productreview.app.core.logging.Logger
import com.productreview.app.core.pagination.Page
import com.productreview.app.core.pagination.PageCursor
import com.productreview.app.core.pagination.Paginator
import com.productreview.app.data.local.UserPreferencesManager
import com.productreview.app.data.model.ApiProduct
import com.productreview.app.data.model.GlobalStats
import com.productreview.app.data.repository.ProductRepository
import com.productreview.app.data.repository.WishlistRepository
import com.productreview.app.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductListUiState(
    val products: List<ApiProduct> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: FWError? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Int = 0,
    val hasMore: Boolean = true,
    val searchQuery: String = "",
    val submittedSearchQuery: String = "",
    val selectedCategory: String = "All",
    val sortBy: String = "name,asc",
    val gridMode: Int = 2,
    val globalStats: GlobalStats? = null,
    val isSelectionMode: Boolean = false,
    val selectedItems: Set<String> = emptySet()
) {
    val canLoadMore: Boolean get() = hasMore && !isLoading && !isLoadingMore && error == null
    val isEmpty: Boolean get() = !isLoading && products.isEmpty()
}

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val wishlistRepository: WishlistRepository,
    private val notificationRepository: NotificationRepository,
    private val preferencesManager: UserPreferencesManager,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    val wishlistIds: StateFlow<Set<String>> = wishlistRepository.wishlistIds
    val wishlistCount: StateFlow<Int> = wishlistRepository.wishlistIds
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unreadNotificationCount: StateFlow<Int> = notificationRepository.unreadCount

    val searchHistory: StateFlow<List<String>> = preferencesManager.searchHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var paginator: Paginator<ApiProduct>? = null
    private var fetchJob: Job? = null
    private var searchDebounceJob: Job? = null

    init {
        viewModelScope.launch {
            preferencesManager.sortPreferenceFlow.collect { sort ->
                _uiState.update { it.copy(sortBy = sort) }
            }
        }
        viewModelScope.launch {
            preferencesManager.gridModeFlow.collect { grid ->
                _uiState.update { it.copy(gridMode = grid) }
            }
        }

        viewModelScope.launch {
            delay(100)
            initializePaginator()
            loadFirstPage()
            fetchGlobalStats()
            wishlistRepository.loadWishlist()
            notificationRepository.loadNotifications()
        }
    }

    private fun initializePaginator() {
        val state = _uiState.value
        paginator = Paginator { cursor ->
            productRepository.getProductsPage(
                cursor = cursor,
                size = 20,
                sort = state.sortBy,
                category = state.selectedCategory.takeIf { it != "All" },
                search = state.submittedSearchQuery.takeIf { it.isNotBlank() }
            )
        }

        viewModelScope.launch {
            paginator?.items?.collect { items ->
                _uiState.update { it.copy(products = items) }
            }
        }
        
        viewModelScope.launch {
            paginator?.pagingState?.collect { ps ->
                _uiState.update { it.copy(
                    isLoadingMore = ps.isLoadingMore,
                    hasMore = ps.hasMore,
                    currentPage = ps.currentPage,
                    totalPages = ps.totalPages,
                    totalElements = ps.totalElements
                )}
            }
        }
        
        viewModelScope.launch {
            paginator?.isRefreshing?.collect { ref ->
                _uiState.update { it.copy(isRefreshing = ref) }
            }
        }
    }

    private fun loadFirstPage() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            logger.log(
                LogEvent.info(LogCategory.PAGINATION, "load_first_page")
                    .metadata(LogKey.CATEGORY, _uiState.value.selectedCategory)
                    .metadata(LogKey.SORT, _uiState.value.sortBy)
                    .build()
            )
            
            reinitializePaginator()
            
            when (val result = paginator?.loadFirst()) {
                is FWResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, error = null) }
                    logger.log(
                        LogEvent.info(LogCategory.PAGINATION, "load_first_success")
                            .metadata(LogKey.TOTAL_ITEMS, result.value.totalElements)
                            .build()
                    )
                }
                is FWResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error) }
                    logger.log(
                        LogEvent.error(LogCategory.PAGINATION, "load_first_failed")
                            .metadata(LogKey.ERROR_CODE, result.error.code)
                            .build()
                    )
                }
                null -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun reinitializePaginator() {
        val state = _uiState.value
        paginator = Paginator { cursor ->
            productRepository.getProductsPage(
                cursor = cursor,
                size = 20,
                sort = state.sortBy,
                category = state.selectedCategory.takeIf { it != "All" },
                search = state.submittedSearchQuery.takeIf { it.isNotBlank() }
            )
        }

        viewModelScope.launch {
            paginator?.items?.collect { items ->
                _uiState.update { it.copy(products = items) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            logger.log(LogEvent.info(LogCategory.PAGINATION, "refresh_triggered").build())
            reinitializePaginator()
            
            when (val result = paginator?.refresh()) {
                is FWResult.Success -> _uiState.update { it.copy(error = null) }
                is FWResult.Failure -> _uiState.update { it.copy(error = result.error) }
                null -> {}
            }
            fetchGlobalStats()
        }
    }

    private fun fetchGlobalStats() {
        viewModelScope.launch {
            val state = _uiState.value
            productRepository.getGlobalStats(
                category = state.selectedCategory.takeIf { it != "All" },
                search = state.submittedSearchQuery.takeIf { it.isNotBlank() }
            ).onSuccess { stats ->
                _uiState.update { it.copy(globalStats = stats) }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (!state.canLoadMore) return

        viewModelScope.launch {
            logger.log(
                LogEvent.debug(LogCategory.PAGINATION, "load_more")
                    .metadata(LogKey.PAGE, state.currentPage + 1)
                    .build()
            )

            when (val result = paginator?.loadNext()) {
                is FWResult.Success -> _uiState.update { it.copy(error = null) }
                is FWResult.Failure -> {
                    logger.log(
                        LogEvent.warn(LogCategory.PAGINATION, "load_more_failed")
                            .metadata(LogKey.ERROR_CODE, result.error.code)
                            .build()
                    )
                }
                null -> {}
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(1000)
            if (query.isBlank() && _uiState.value.submittedSearchQuery.isNotBlank()) {
                resetFilters()
            }
        }
    }

    fun submitSearch(query: String) {
        if (query.isBlank()) {
            resetFilters()
            return
        }
        viewModelScope.launch { preferencesManager.addSearchTerm(query) }
        _uiState.update { it.copy(submittedSearchQuery = query) }
        loadFirstPage()
        fetchGlobalStats()
    }

    fun removeSearchTerm(term: String) {
        viewModelScope.launch { preferencesManager.removeSearchTerm(term) }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { preferencesManager.clearSearchHistory() }
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadFirstPage()
        fetchGlobalStats()
    }

    fun setSort(sort: String) {
        viewModelScope.launch { preferencesManager.setSortPreference(sort) }
        _uiState.update { it.copy(sortBy = sort) }
        loadFirstPage()
    }

    fun toggleGridMode() {
        val newMode = when (_uiState.value.gridMode) {
            1 -> 2
            2 -> 3
            else -> 1
        }
        viewModelScope.launch { preferencesManager.setGridMode(newMode) }
        _uiState.update { it.copy(gridMode = newMode) }
    }

    fun setGridMode(columns: Int) {
        viewModelScope.launch { preferencesManager.setGridMode(columns) }
        _uiState.update { it.copy(gridMode = columns) }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                submittedSearchQuery = "",
                selectedCategory = "All",
                sortBy = "name,asc"
            )
        }
        viewModelScope.launch { preferencesManager.setSortPreference("name,asc") }
        loadFirstPage()
        fetchGlobalStats()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun retry() {
        clearError()
        loadFirstPage()
    }

    fun enterSelectionMode(productId: String) {
        _uiState.update {
            it.copy(isSelectionMode = true, selectedItems = setOf(productId))
        }
    }

    fun toggleSelection(productId: String) {
        _uiState.update { state ->
            val newSelected = state.selectedItems.toMutableSet()
            if (newSelected.contains(productId)) {
                newSelected.remove(productId)
            } else {
                newSelected.add(productId)
            }
            state.copy(
                selectedItems = newSelected,
                isSelectionMode = newSelected.isNotEmpty()
            )
        }
    }

    fun cancelSelection() {
        _uiState.update { it.copy(isSelectionMode = false, selectedItems = emptySet()) }
    }

    fun addSelectedToWishlist() {
        val selected = _uiState.value.selectedItems
        val products = _uiState.value.products.filter {
            selected.contains(it.id.toString()) && !wishlistRepository.isInWishlist(it.id.toString())
        }
        
        if (products.isEmpty()) {
            cancelSelection()
            return
        }

        viewModelScope.launch {
            wishlistRepository.addMultipleToWishlist(products)
            cancelSelection()
        }
    }

    fun isInWishlist(productId: String): Boolean = wishlistRepository.isInWishlist(productId)

    fun toggleWishlist(product: ApiProduct) {
        viewModelScope.launch { wishlistRepository.toggleWishlist(product) }
    }
}
