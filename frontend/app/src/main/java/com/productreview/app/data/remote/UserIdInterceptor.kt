package com.productreview.app.data.remote

import com.productreview.app.data.local.UserPreferencesManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserIdInterceptor @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val userId = runBlocking { userPreferencesManager.getUserId() }
        
        val request = chain.request().newBuilder()
            .addHeader("X-User-ID", userId)
            .build()
        
        return chain.proceed(request)
    }
}
