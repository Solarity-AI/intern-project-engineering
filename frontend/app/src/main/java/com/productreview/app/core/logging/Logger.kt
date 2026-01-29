package com.productreview.app.core.logging

import android.util.Log

enum class LogLevel(val value: String, val priority: Int) {
    TRACE("trace", 0),
    DEBUG("debug", 1),
    INFO("info", 2),
    WARN("warn", 3),
    ERROR("error", 4)
}

object LogCategory {
    const val NETWORK = "network"
    const val AUTH = "auth"
    const val UI = "ui"
    const val DATA = "data"
    const val PAGINATION = "pagination"
    const val LIFECYCLE = "lifecycle"
    const val ERROR = "error"
}

enum class LogKey(val value: String) {
    TRACE_ID("traceId"),
    URL("url"),
    METHOD("method"),
    STATUS("status"),
    DURATION_MS("durationMs"),
    ERROR_CODE("errorCode"),
    ERROR_MESSAGE("errorMessage"),
    USER_ID("userId"),
    PRODUCT_ID("productId"),
    REVIEW_ID("reviewId"),
    PAGE("page"),
    PAGE_SIZE("pageSize"),
    TOTAL_ITEMS("totalItems"),
    SEARCH_QUERY("searchQuery"),
    CATEGORY("category"),
    SORT("sort"),
    RETRY_ATTEMPT("retryAttempt"),
    RETRY_DELAY_MS("retryDelayMs"),
    IS_RETRIABLE("isRetriable")
}

data class LogEvent(
    val category: String,
    val name: String,
    val level: LogLevel,
    val message: String? = null,
    val traceId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val error: Throwable? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    class Builder(
        private val category: String,
        private val name: String,
        private val level: LogLevel
    ) {
        private var message: String? = null
        private var traceId: String? = null
        private val metadata = mutableMapOf<String, String>()
        private var error: Throwable? = null

        fun message(message: String) = apply { this.message = message }
        fun traceId(traceId: String) = apply { this.traceId = traceId }
        fun error(error: Throwable) = apply { this.error = error }
        fun metadata(key: LogKey, value: String) = apply { metadata[key.value] = value }
        fun metadata(key: LogKey, value: Int) = apply { metadata[key.value] = value.toString() }
        fun metadata(key: LogKey, value: Long) = apply { metadata[key.value] = value.toString() }
        fun metadata(key: LogKey, value: Boolean) = apply { metadata[key.value] = value.toString() }

        fun build() = LogEvent(
            category = category,
            name = name,
            level = level,
            message = message,
            traceId = traceId,
            metadata = metadata.toMap(),
            error = error
        )
    }

    companion object {
        fun debug(category: String, name: String) = Builder(category, name, LogLevel.DEBUG)
        fun info(category: String, name: String) = Builder(category, name, LogLevel.INFO)
        fun warn(category: String, name: String) = Builder(category, name, LogLevel.WARN)
        fun error(category: String, name: String) = Builder(category, name, LogLevel.ERROR)
    }
}

interface Logger {
    val minLevel: LogLevel
    fun log(event: LogEvent)
    fun isEnabled(level: LogLevel): Boolean = level.priority >= minLevel.priority
}

class DefaultLogger(
    override val minLevel: LogLevel = LogLevel.DEBUG,
    private val tag: String = "ProductReview"
) : Logger {

    override fun log(event: LogEvent) {
        if (!isEnabled(event.level)) return

        val message = buildString {
            append("[${event.category}] ${event.name}")
            event.message?.let { append(": $it") }
            if (event.metadata.isNotEmpty()) {
                append(" | ")
                append(event.metadata.entries.joinToString(", ") { "${it.key}=${it.value}" })
            }
        }

        when (event.level) {
            LogLevel.TRACE -> Log.v(tag, message, event.error)
            LogLevel.DEBUG -> Log.d(tag, message, event.error)
            LogLevel.INFO -> Log.i(tag, message, event.error)
            LogLevel.WARN -> Log.w(tag, message, event.error)
            LogLevel.ERROR -> Log.e(tag, message, event.error)
        }
    }
}

object NoOpLogger : Logger {
    override val minLevel: LogLevel = LogLevel.ERROR
    override fun log(event: LogEvent) = Unit
}
