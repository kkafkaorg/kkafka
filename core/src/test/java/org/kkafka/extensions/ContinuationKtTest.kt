package org.kkafka.extensions

import io.kotest.matchers.shouldBe
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.Executors.newCachedThreadPool
import kotlin.coroutines.suspendCoroutine

internal class ContinuationKtTest {

    private val dispatcher: CoroutineDispatcher =
        spyk(newCachedThreadPool().asCoroutineDispatcher())

    @Test
    fun `submits to Dispatcher in context`() {
        runBlocking(dispatcher) {

            suspendCoroutine<Int> { cont ->
                cont.submitToContextDispatcher {
                    Result.success(1)
                }
            }
        }

        verify(atLeast = 1) { dispatcher.dispatch(any(), any()) }
    }

    @Test
    fun `propagates exceptions safely if the submitted result throws`() {
        val e = assertThrows<IllegalStateException> {
            runBlocking(dispatcher) {

                suspendCoroutine<Int> { cont ->
                    cont.submitToContextDispatcher {
                        error("oopsie")
                    }
                }
            }
        }

        e.message shouldBe "oopsie"

        verify(atLeast = 1) { dispatcher.dispatch(any(), any()) }
    }
}
