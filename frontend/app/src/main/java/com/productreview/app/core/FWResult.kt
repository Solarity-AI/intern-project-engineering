package com.productreview.app.core

import fw.core.FWError
import fw.core.FWResult

/**
 * App-specific FWResult extension methods.
 * The framework's FWResult does not include these convenience methods.
 */
inline fun <T> FWResult<T>.onSuccess(action: (T) -> Unit): FWResult<T> {
    if (this is FWResult.Success) action(value)
    return this
}

inline fun <T> FWResult<T>.onFailure(action: (FWError) -> Unit): FWResult<T> {
    if (this is FWResult.Failure) action(error)
    return this
}
