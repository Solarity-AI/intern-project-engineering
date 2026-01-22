package com.productreview.app.core

/**
 * Result type for operations that can fail.
 * Similar to Kotlin's Result but uses FWError.
 */
sealed class FWResult<out T> {
    data class Success<out T>(val value: T) : FWResult<T>()
    data class Failure(val error: FWError) : FWResult<Nothing>()

    companion object {
        fun <T> success(value: T): FWResult<T> = Success(value)
        fun failure(error: FWError): FWResult<Nothing> = Failure(error)

        suspend fun <T> catching(block: suspend () -> T): FWResult<T> {
            return try {
                Success(block())
            } catch (e: FWError) {
                Failure(e)
            } catch (e: Exception) {
                Failure(FWError.fromThrowable(e))
            }
        }
    }

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw error
    }

    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> default
    }

    inline fun getOrElse(default: (FWError) -> @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> default(error)
    }

    fun errorOrNull(): FWError? = when (this) {
        is Success -> null
        is Failure -> error
    }

    inline fun <R> map(transform: (T) -> R): FWResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    inline fun <R> flatMap(transform: (T) -> FWResult<R>): FWResult<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }

    inline fun onSuccess(action: (T) -> Unit): FWResult<T> {
        if (this is Success) action(value)
        return this
    }

    inline fun onFailure(action: (FWError) -> Unit): FWResult<T> {
        if (this is Failure) action(error)
        return this
    }

    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (FWError) -> R
    ): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(error)
    }
}

fun <T> retrofit2.Response<T>.toFWResult(): FWResult<T> {
    return if (isSuccessful) {
        body()?.let { FWResult.success(it) }
            ?: FWResult.failure(FWError.decoding("Response body is null"))
    } else {
        FWResult.failure(FWError.fromHttpStatus(code(), message()))
    }
}
