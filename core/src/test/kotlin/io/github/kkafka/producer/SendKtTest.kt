package io.github.kkafka.producer

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.clients.producer.internals.FutureRecordMetadata
import org.apache.kafka.clients.producer.internals.ProduceRequestResult
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.utils.Time
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SendKtTest {
    private val someRecord = ProducerRecord("topic", 1, "hello", "world")
    private val someRecordMetadata = RecordMetadata(TopicPartition("topic", 1), 0, 0, 0, 0, 1, 1)
    private val someFutureRecordMetadata =
        FutureRecordMetadata(ProduceRequestResult(TopicPartition("topic", 1)), 0, 0, 0, 0, 1, Time.SYSTEM)

    private val producer: KafkaProducer<String, String> = mockk {
        every { send(any(), any()) } answers {
            val callback = secondArg<Callback>()
            callback.onCompletion(someRecordMetadata, null)

            someFutureRecordMetadata
        }
    }

    @Test
    fun `sendSuspending() calls send and continues`() {
        runBlocking { producer.sendSuspending(someRecord) }

        verify(exactly = 1) { producer.send(any(), any()) }
    }

    @Test
    fun `sendSuspending() returns recordMetadata if no exception`() {
        val result = runBlocking { producer.sendSuspending(someRecord) }

        result shouldBe someRecordMetadata
    }

    @Test
    fun `sendSuspending() throws whatever is passed to the callback`() {
        val someException = IllegalStateException("woopsie")

        every { producer.send(any(), any()) } answers {
            val callback = secondArg<Callback>()
            callback.onCompletion(null, someException)

            someFutureRecordMetadata
        }

        val exception = assertThrows<IllegalStateException> {
            runBlocking { producer.sendSuspending(someRecord) }
        }

        exception.message shouldBe someException.message
    }

    @Test
    fun `sendSuspending(record) calls send with passed params`() {
        val someRecord2 = ProducerRecord("topic2", 2, "hello2", "world2")
        runBlocking { producer.sendSuspending(someRecord2) }

        verify(exactly = 1) { producer.send(someRecord2, any()) }
    }
}
