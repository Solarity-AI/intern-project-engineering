package com.productreview.app.core

/**
 * Framework error with canonical error codes.
 * All framework errors use this type.
 *
 * @param code Canonical error code (e.g., "NETWORK", "UNAUTHORIZED")
 * @param message Human-readable error message
 * @param cause Original exception that caused this error, if any
 * @param isRetriable Whether this error can be safely retried
 */
data class FWError(
    val code: String,
    override val message: String,
    override val cause: Throwable? = null,
    val isRetriable: Boolean = false
) : Exception(message, cause) {

    companion object {
        fun invalidArgument(
            message: String,
            cause: Throwable? = null
        ): FWError = FWError(
            code = "INVALID_ARGUMENT",
            message = message,
            cause = cause,
            isRetriable = false
        )

        fun network(
            message: String,
            cause: Throwable? = null
        ): FWError = FWError(
            code = "NETWORK",
            message = message,
            cause = cause,
            isRetriable = true
        )

        fun timeout(
            message: String = "Request timed out",
            cause: Throwable? = null
        ): FWError = FWError(
            code = "TIMEOUT",
            message = message,
            cause = cause,
            isRetriable = true
        )

        fun noInternet(
            message: String = "No internet connection",
            cause: Throwable? = null
        ): FWError = FWError(
            code = "NO_INTERNET",
            message = message,
            cause = cause,
            isRetriable = true
        )

        fun serverError(
            message: String,
            statusCode: Int? = null,
            cause: Throwable? = null
        ): FWError = FWError(
            code = "SERVER_ERROR",
            message = if (statusCode != null) "$message (HTTP $statusCode)" else message,
            cause = cause,
            isRetriable = true
        )

        fun clientError(
            message: String,
            statusCode: Int? = null,
            cause: Throwable? = null
        ): FWError = FWError(
            code = "CLIENT_ERROR",
            message = if (statusCode != null) "$message (HTTP $statusCode)" else message,
            cause = cause,
            isRetriable = false
        )

        fun notFound(
            message: String = "Resource not found",
            cause: Throwable? = null
        ): FWError = FWError(
            code = "NOT_FOUND",
            message = message,
            cause = cause,
            isRetriable = false
        )

        fun rateLimited(
            message: String = "Rate limited, please try again later",
            cause: Throwable? = null
        ): FWError = FWError(
            code = "RATE_LIMITED",
            message = message,
            cause = cause,
            isRetriable = true
        )

        fun decoding(
            message: String,
            cause: Throwable? = null
        ): FWError = FWError(
            code = "DECODING",
            message = message,
            cause = cause,
            isRetriable = false
        )

        fun unauthorized(
            message: String = "Unauthorized",
            cause: Throwable? = null
        ): FWError = FWError(
            code = "UNAUTHORIZED",
            message = message,
            cause = cause,
            isRetriable = false
        )

        fun forbidden(
            message: String = "Forbidden",
            cause: Throwable? = null
        ): FWError = FWError(
            code = "FORBIDDEN",
            message = message,
            cause = cause,
            isRetriable = false
        )

        fun unknown(
            message: String,
            cause: Throwable? = null
        ): FWError = FWError(
            code = "UNKNOWN",
            message = message,
            cause = cause,
            isRetriable = false
        )

        fun fromHttpStatus(statusCode: Int, message: String? = null): FWError {
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

        fun fromThrowable(throwable: Throwable): FWError {
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
    }
}
