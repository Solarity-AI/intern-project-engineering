package com.productreview.app.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Provider for coroutine dispatchers.
 * Makes threading contracts explicit and enables testability.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

/**
 * Default dispatcher provider using platform dispatchers.
 */
object DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher get() = Dispatchers.Main
    override val io: CoroutineDispatcher get() = Dispatchers.IO
    override val default: CoroutineDispatcher get() = Dispatchers.Default
}

/**
 * Test dispatcher provider for unit tests.
 */
class TestDispatcherProvider(
    private val testDispatcher: CoroutineDispatcher
) : DispatcherProvider {
    override val main: CoroutineDispatcher get() = testDispatcher
    override val io: CoroutineDispatcher get() = testDispatcher
    override val default: CoroutineDispatcher get() = testDispatcher
}
