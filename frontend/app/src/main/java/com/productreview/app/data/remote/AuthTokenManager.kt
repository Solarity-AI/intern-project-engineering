package com.productreview.app.data.remote

import com.productreview.app.data.local.UserPreferencesManager
import com.productreview.app.data.model.AuthRequest
import com.productreview.app.data.model.RefreshRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages JWT tokens: storage, retrieval, login, and refresh.
 * Uses a default test user for auto-login when no tokens are present.
 */
@Singleton
class AuthTokenManager @Inject constructor(
    private val authApi: AuthApi,
    private val prefs: UserPreferencesManager
) {
    companion object {
        private const val DEFAULT_USERNAME = "alice"
        private const val DEFAULT_PASSWORD = "password123"
    }

    /**
     * Returns a valid access token, performing login if needed.
     */
    suspend fun getAccessToken(): String? {
        return prefs.getAccessToken() ?: login()
    }

    /**
     * Tries to refresh the token; falls back to a fresh login.
     * Returns the new access token or null on failure.
     */
    suspend fun refreshOrLogin(): String? {
        return tryRefresh() ?: login()
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
            prefs.saveTokens(response.accessToken, response.refreshToken)
            response.accessToken
        } catch (_: Exception) {
            null
        }
    }
}
