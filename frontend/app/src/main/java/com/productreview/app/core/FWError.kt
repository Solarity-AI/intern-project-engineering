package com.productreview.app.core

import fw.core.FWError

/**
 * App-specific FWError companion extensions.
 * The framework does not expose these factory methods.
 * Consumer code must `import fw.core.*` for the base type and
 * `import com.productreview.app.core.*` for these extensions.
 */

fun FWError.Companion.timeout(
    message: String = "Request timed out",
    cause: Throwable? = null
): FWError = FWError(
    code = "TIMEOUT",
    message = message,
    cause = cause,
    isRetriable = true
)

fun FWError.Companion.noInternet(
    message: String = "No internet connection",
    cause: Throwable? = null
): FWError = FWError(
    code = "NO_INTERNET",
    message = message,
    cause = cause,
    isRetriable = true
)

fun FWError.Companion.serverError(
    message: String,
    statusCode: Int? = null,
    cause: Throwable? = null
): FWError = FWError(
    code = "SERVER_ERROR",
    message = if (statusCode != null) "$message (HTTP $statusCode)" else message,
    cause = cause,
    isRetriable = true
)

fun FWError.Companion.clientError(
    message: String,
    statusCode: Int? = null,
    cause: Throwable? = null
): FWError = FWError(
    code = "CLIENT_ERROR",
    message = if (statusCode != null) "$message (HTTP $statusCode)" else message,
    cause = cause,
    isRetriable = false
)

fun FWError.Companion.notFound(
    message: String = "Resource not found",
    cause: Throwable? = null
): FWError = FWError(
    code = "NOT_FOUND",
    message = message,
    cause = cause,
    isRetriable = false
)

fun FWError.Companion.rateLimited(
    message: String = "Rate limited, please try again later",
    cause: Throwable? = null
): FWError = FWError(
    code = "RATE_LIMITED",
    message = message,
    cause = cause,
    isRetriable = true
)

fun FWError.Companion.forbidden(
    message: String = "Forbidden",
    cause: Throwable? = null
): FWError = FWError(
    code = "FORBIDDEN",
    message = message,
    cause = cause,
    isRetriable = false
)

fun FWError.Companion.fromHttpStatus(statusCode: Int, message: String? = null): FWError {
    val defaultMessage = message ?: "HTTP error $statusCode"
    return when (statusCode) {
        401 -> unauthorized(defaultMessage)
        403 -> forbidden(defaultMessage)
        404 -> notFound(defaultMessage)
        429 -> rateLimited(defaultMessage)
        in 400..499 -> clientError(defaultMessage, statusCode)
        in 500..599 -> serverError(defaultMessage, statusCode)
        else -> unknown(defaultMessage)
    }
}

fun FWError.Companion.fromThrowable(throwable: Throwable): FWError {
    return when (throwable) {
        is FWError -> throwable
        is java.net.UnknownHostException -> noInternet(cause = throwable)
        is java.net.SocketTimeoutException -> timeout(cause = throwable)
        is java.net.ConnectException -> network("Connection failed", throwable)
        is java.io.IOException -> network("I/O error: ${throwable.message}", throwable)
        is kotlinx.serialization.SerializationException -> decoding("Serialization error: ${throwable.message}", throwable)
        else -> unknown(throwable.message ?: "Unknown error", throwable)
    }
}
