package com.productreview.app.data.remote

import com.productreview.app.data.local.UserPreferencesManager
import com.productreview.app.data.model.AuthRequest
import com.productreview.app.data.model.RefreshRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages JWT tokens: storage, retrieval, login, and refresh.
 * Uses a default test user for auto-login when no tokens are present.
 *
 * If the backend has no auth endpoints (dev mode), login fails once and
 * is not retried until [resetBackoff] is called, avoiding per-request latency.
 */
@Singleton
class AuthTokenManager @Inject constructor(
    private val authApi: AuthApi,
    private val prefs: UserPreferencesManager
) {
    companion object {
        private const val DEFAULT_USERNAME = "alice"
        private const val DEFAULT_PASSWORD = "password123"
        private const val BACKOFF_MS = 60_000L // retry login at most once per minute
    }

    @Volatile
    private var lastLoginFailure: Long = 0L

    /**
     * Returns a valid access token, performing login if needed.
     * Returns null without a network call when a recent login already failed.
     */
    suspend fun getAccessToken(): String? {
        return prefs.getAccessToken() ?: loginIfAllowed()
    }

    /**
     * Tries to refresh the token; falls back to a fresh login.
     * Returns the new access token or null on failure.
     */
    suspend fun refreshOrLogin(): String? {
        return tryRefresh() ?: loginIfAllowed()
    }

    fun resetBackoff() {
        lastLoginFailure = 0L
    }

    private suspend fun loginIfAllowed(): String? {
        if (System.currentTimeMillis() - lastLoginFailure < BACKOFF_MS) return null
        return login()
    }

    private suspend fun tryRefresh(): String? {
        val refreshToken = prefs.getRefreshToken() ?: return null
        return try {
            val response = authApi.refreshToken(RefreshRequest(refreshToken))
            prefs.saveTokens(response.accessToken, response.refreshToken)
            response.accessToken
        } catch (_: Exception) {
            prefs.clearTokens()
            null
        }
    }

    private suspend fun login(): String? {
        return try {
            val response = authApi.login(
                AuthRequest(DEFAULT_USERNAME, DEFAULT_PASSWORD)
            )
            lastLoginFailure = 0L
            prefs.saveTokens(response.accessToken, response.refreshToken)
            response.accessToken
        } catch (_: Exception) {
            lastLoginFailure = System.currentTimeMillis()
            null
        }
    }
}
