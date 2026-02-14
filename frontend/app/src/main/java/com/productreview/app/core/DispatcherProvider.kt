package com.productreview.app.core

import fw.core.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher

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
