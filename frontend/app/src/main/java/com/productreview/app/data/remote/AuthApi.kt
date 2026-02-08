package com.productreview.app.data.remote

import com.productreview.app.data.model.AuthRequest
import com.productreview.app.data.model.AuthResponse
import com.productreview.app.data.model.RefreshRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Separate Retrofit interface for authentication endpoints.
 * Uses an unauthenticated OkHttpClient (no Bearer token interceptor).
 */
interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): AuthResponse
}
