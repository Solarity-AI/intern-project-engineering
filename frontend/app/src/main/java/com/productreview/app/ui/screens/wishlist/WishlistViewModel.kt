package com.productreview.app.ui.screens.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productreview.app.core.FWError
import com.productreview.app.core.FWResult
import com.productreview.app.core.logging.*
import com.productreview.app.data.local.UserPreferencesManager
import com.productreview.app.data.model.ApiProduct
import com.productreview.app.data.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WishlistUiState(
    val products: List<ApiProduct> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: FWError? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalItems: Int = 0,
    val hasMore: Boolean = true,
    val gridMode: Int = 2,
    val isSelectionMode: Boolean = false,
    val selectedItems: Set<String> = emptySet()
) {
    val canLoadMore: Boolean get() = hasMore && !isLoading && !isLoadingMore
    val isEmpty: Boolean get() = !isLoading && products.isEmpty()
}

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val preferencesManager: UserPreferencesManager,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    val wishlistCount: StateFlow<Int> = wishlistRepository.wishlistIds
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            preferencesManager.gridModeFlow.collect { grid ->
                _uiState.update { it.copy(gridMode = grid) }
            }
        }
        loadWishlist()
    }

    fun loadWishlist(page: Int = 0, append: Boolean = false) {
        viewModelScope.launch {
            if (!append) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            when (val result = wishlistRepository.getWishlistProducts(page = page, size = 10)) {
                is FWResult.Success -> {
                    val pageData = result.value
                    _uiState.update { state ->
                        state.copy(
                            products = if (append) state.products + pageData.items else pageData.items,
                            isLoading = false, isLoadingMore = false,
                            currentPage = page,
                            totalPages = pageData.totalPages ?: 0,
                            totalItems = pageData.totalElements ?: pageData.items.size,
                            hasMore = pageData.hasMore
                        )
                    }
                }
                is FWResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, isLoadingMore = false, error = result.error) }
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (!state.canLoadMore) return
        loadWishlist(page = state.currentPage + 1, append = true)
    }

    fun refresh() { loadWishlist(page = 0, append = false) }

    fun toggleGridMode() {
        val newMode = when (_uiState.value.gridMode) { 1 -> 2; 2 -> 3; else -> 1 }
        viewModelScope.launch { preferencesManager.setGridMode(newMode) }
        _uiState.update { it.copy(gridMode = newMode) }
    }

    fun enterSelectionMode(productId: String) {
        _uiState.update { it.copy(isSelectionMode = true, selectedItems = setOf(productId)) }
    }

    fun toggleSelection(productId: String) {
        _uiState.update { state ->
            val newSelected = state.selectedItems.toMutableSet()
            if (newSelected.contains(productId)) newSelected.remove(productId) else newSelected.add(productId)
            state.copy(selectedItems = newSelected, isSelectionMode = newSelected.isNotEmpty())
        }
    }

    fun cancelSelection() {
        _uiState.update { it.copy(isSelectionMode = false, selectedItems = emptySet()) }
    }

    fun removeFromWishlist(productId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    products = state.products.filter { it.id.toString() != productId },
                    totalItems = maxOf(0, state.totalItems - 1)
                )
            }
            wishlistRepository.removeFromWishlist(productId)
        }
    }

    fun removeSelected() {
        val selected = _uiState.value.selectedItems.toList()
        if (selected.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    products = state.products.filter { it.id.toString() !in selected },
                    totalItems = maxOf(0, state.totalItems - selected.size),
                    isSelectionMode = false, selectedItems = emptySet()
                )
            }
            wishlistRepository.removeMultipleFromWishlist(selected)
        }
    }

    fun clearWishlist() {
        viewModelScope.launch {
            _uiState.update { it.copy(products = emptyList(), totalItems = 0) }
            wishlistRepository.clearWishlist()
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }

    fun getStats(): Triple<Int, Double, Double> {
        val products = _uiState.value.products
        val totalPrice = products.sumOf { it.price }
        val avgRating = if (products.isNotEmpty()) products.mapNotNull { it.averageRating }.average() else 0.0
        return Triple(_uiState.value.totalItems, avgRating, totalPrice)
    }
}
