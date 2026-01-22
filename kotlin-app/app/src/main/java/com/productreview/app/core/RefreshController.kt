package com.productreview.app.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller for managing refresh state.
 * Screens should NOT manage refresh state manually.
 */
class RefreshController {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun startRefresh(): Boolean {
        if (_isRefreshing.value) return false
        _isRefreshing.value = true
        return true
    }

    fun completeRefresh() {
        _isRefreshing.value = false
    }

    suspend fun <T> executeRefresh(block: suspend () -> T): T {
        return try {
            startRefresh()
            block()
        } finally {
            completeRefresh()
        }
    }

    suspend fun <T> executeRefreshWithResult(block: suspend () -> FWResult<T>): FWResult<T> {
        return try {
            startRefresh()
            block()
        } finally {
            completeRefresh()
        }
    }
}
