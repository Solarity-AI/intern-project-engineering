package com.productreview.app.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Authenticator that handles 401 responses by refreshing / re-acquiring
 * a JWT token and retrying the request exactly once.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val authTokenManager: AuthTokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Don't retry auth endpoints to avoid infinite loops
        if (response.request.url.encodedPath.startsWith("/api/auth/")) {
            return null
        }

        // Only retry once (check for our custom marker header)
        if (response.request.header("X-Auth-Retry") != null) {
            return null
        }

        val newToken = runBlocking { authTokenManager.refreshOrLogin() }
            ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .header("X-Auth-Retry", "true")
            .build()
    }
}
