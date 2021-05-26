package org.kkafka.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor

/**
 * Runs [continuationCallback] in this [Continuation]'s [CoroutineDispatcher] if present (or in
 * [Dispatchers.Default] if not present)
 *
 * Used in order to run blocks of code in threads different than those provided by kafka's
 * callbacks, so we do not block them under any circumstances.
 */
internal inline fun <T> Continuation<T>.submitToContextDispatcher(
    crossinline continuationCallback: () -> Result<T>,
) {
    val dispatcher = context[ContinuationInterceptor] as? CoroutineDispatcher
        ?: Dispatchers.Default

    // we really cannot throw in here, or the continuation won't resume at all
    dispatcher.dispatch(context) {
        val result = try {
            continuationCallback()
        } catch (e: Exception) {
            Result.failure(e)
        }
        resumeWith(result)
    }
}
