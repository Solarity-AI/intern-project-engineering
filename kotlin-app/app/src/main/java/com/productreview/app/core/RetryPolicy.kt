package com.productreview.app.core

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Policy for retry operations with exponential backoff.
 */
data class RetryPolicy(
    val maxAttempts: Int = 3,
    val baseDelayMs: Long = 1000,
    val maxDelayMs: Long = 30000,
    val backoffMultiplier: Double = 2.0,
    val useJitter: Boolean = true,
    val retryOnNetworkError: Boolean = true,
    val retryOnServerError: Boolean = true,
    val retryOnTimeout: Boolean = true,
    val retryOnRateLimit: Boolean = true,
    val retryOnClientError: Boolean = false
) {
    companion object {
        val Default = RetryPolicy()
        val NoRetry = RetryPolicy(
            maxAttempts = 0,
            retryOnNetworkError = false,
            retryOnServerError = false,
            retryOnTimeout = false,
            retryOnRateLimit = false,
            retryOnClientError = false
        )
        val Aggressive = RetryPolicy(maxAttempts = 5, baseDelayMs = 2000, maxDelayMs = 60000)
        val Quick = RetryPolicy(maxAttempts = 2, baseDelayMs = 500, maxDelayMs = 2000)
    }

    fun shouldRetry(error: FWError, attempt: Int): Boolean {
        if (attempt >= maxAttempts) return false
        if (!error.isRetriable) return false

        return when (error.code) {
            "NETWORK", "NO_INTERNET" -> retryOnNetworkError
            "TIMEOUT" -> retryOnTimeout
            "SERVER_ERROR" -> retryOnServerError
            "RATE_LIMITED" -> retryOnRateLimit
            "CLIENT_ERROR" -> retryOnClientError
            else -> retryOnNetworkError
        }
    }

    fun getDelayMs(attempt: Int): Long {
        val exponentialDelay = (baseDelayMs * Math.pow(backoffMultiplier, attempt.toDouble())).toLong()
        val cappedDelay = minOf(exponentialDelay, maxDelayMs)
        return if (useJitter) {
            val jitterRange = (cappedDelay * 0.25).toLong()
            maxOf(0, cappedDelay + Random.nextLong(-jitterRange, jitterRange + 1))
        } else cappedDelay
    }

    suspend fun <T> executeWithRetry(block: suspend () -> FWResult<T>): FWResult<T> {
        var attempt = 0
        while (true) {
            when (val result = block()) {
                is FWResult.Success -> return result
                is FWResult.Failure -> {
                    if (!shouldRetry(result.error, attempt)) return result
                    delay(getDelayMs(attempt))
                    attempt++
                }
            }
        }
    }
}
