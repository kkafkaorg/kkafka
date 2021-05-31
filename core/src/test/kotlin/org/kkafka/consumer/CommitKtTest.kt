package org.kkafka.consumer

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.consumer.OffsetCommitCallback
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CommitKtTest {
    private val someMap = mapOf(TopicPartition("cake", 1) to OffsetAndMetadata(0, "lies"))
    private val consumer: KafkaConsumer<String, String> = mockk {
        every { commitAsync(any()) } answers {
            val callback = firstArg<OffsetCommitCallback>()
            callback.onComplete(someMap, null)
        }

        every { commitAsync(any(), any()) } answers {
            val callback = secondArg<OffsetCommitCallback>()
            callback.onComplete(someMap, null)
        }
    }

    @Test
    fun `commit() calls commitAsync and continues`() {
        runBlocking { consumer.commitSuspending() }

        verify(exactly = 1) { consumer.commitAsync(any()) }
    }

    @Test
    fun `cover overloads`() {
        val record = record("t1", 1, 1L)
        runBlocking { consumer.commitSuspending(record.partitionAndOffset) }

        val map = mapOf(record.partitionAndOffset)
        verify(exactly = 1) { consumer.commitAsync(map, any()) }
    }

    @Test
    fun `commit() returns what commitAsync returns`() {
        val result = runBlocking { consumer.commitSuspending() }

        result shouldBe someMap
    }

    @Test
    fun `commit() throws whatever is passed to the callback`() {
        val someException = IllegalStateException("oopsie")

        every { consumer.commitAsync(any()) } answers {
            val callback = firstArg<OffsetCommitCallback>()
            callback.onComplete(someMap, someException)
        }

        val exception = assertThrows<IllegalStateException> {
            runBlocking { consumer.commitSuspending() }
        }

        exception.message shouldBe someException.message
    }

    @Test
    fun `commit(offsets) calls commitAsync with passed params`() {
        val someMap2 = mapOf(TopicPartition("cakes", 10) to OffsetAndMetadata(0, "more lies")) + someMap
        runBlocking { consumer.commitSuspending(someMap2) }

        verify(exactly = 1) { consumer.commitAsync(someMap2, any()) }
    }

    @Test
    fun `commit(offsets) throws whatever is passed to the callback`() {
        val someException = IllegalStateException("oopsie")

        every { consumer.commitAsync(someMap, any()) } answers {
            val callback = secondArg<OffsetCommitCallback>()
            callback.onComplete(someMap, someException)
        }

        val exception = assertThrows<IllegalStateException> {
            runBlocking { consumer.commitSuspending(someMap) }
        }

        exception.message shouldBe someException.message
    }
}
