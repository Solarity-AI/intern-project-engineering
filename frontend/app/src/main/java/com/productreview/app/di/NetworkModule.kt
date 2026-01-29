package com.productreview.app.di

import com.productreview.app.BuildConfig
import com.productreview.app.core.DispatcherProvider
import com.productreview.app.core.DefaultDispatcherProvider
import com.productreview.app.core.RetryPolicy
import com.productreview.app.core.logging.DefaultLogger
import com.productreview.app.core.logging.LogLevel
import com.productreview.app.core.logging.Logger
import com.productreview.app.data.remote.ProductReviewApi
import com.productreview.app.data.remote.UserIdInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider

    @Provides
    @Singleton
    fun provideLogger(): Logger = DefaultLogger(
        minLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.INFO,
        tag = "ProductReview"
    )

    @Provides
    @Singleton
    fun provideRetryPolicy(): RetryPolicy = RetryPolicy.Default

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        userIdInterceptor: UserIdInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(userIdInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + "/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideProductReviewApi(retrofit: Retrofit): ProductReviewApi {
        return retrofit.create(ProductReviewApi::class.java)
    }
}
