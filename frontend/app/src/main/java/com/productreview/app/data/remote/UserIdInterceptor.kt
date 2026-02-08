package com.productreview.app.data.remote

import com.productreview.app.data.local.UserPreferencesManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserIdInterceptor @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val authTokenManager: AuthTokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val builder = original.newBuilder()

        // Always add X-User-ID
        val userId = runBlocking { userPreferencesManager.getUserId() }
        builder.addHeader("X-User-ID", userId)

        // Add Bearer token for non-auth endpoints
        val path = original.url.encodedPath
        if (!path.startsWith("/api/auth/")) {
            val token = runBlocking { authTokenManager.getAccessToken() }
            if (token != null) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(builder.build())
    }
}
