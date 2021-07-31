package io.github.kkafka.producer

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.TransactionAbortedException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TransactionKtTest {
    private val topic = "kf-some-topic"
    private val topics = setOf(topic, "kf-some-other-topic")

    private val consumer: KafkaConsumer<Int, String> = mockk {
        every { assignment() } answers {
            // Add a topic we don't care about to make sure it is ignored
            val allTopics = topics + setOf("kf-some-uninteresting-topic")
            allTopics.map { TopicPartition(it, 0) }.toSet()
        }
        every { groupMetadata().groupId() } answers { "someGroupId" }
        every { position(any()) } answers { 31 }
    }

    private val producer: KafkaProducer<String, String> = mockk {
        every { beginTransaction() } just runs
        every { commitTransaction() } just runs
        every { abortTransaction() } just runs
        every { sendOffsetsToTransaction(any(), "someGroupId") } just runs
    }

    //
    // Tests for transaction with consumer and set of topics
    //

    @Test
    fun `transaction with consumer commits correctly`() {
        val res = producer.transaction(consumer, topics) {
            // Block succeeds
            "someResult"
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 1) {
            producer.sendOffsetsToTransaction(
                topics.associate { TopicPartition(it, 0) to OffsetAndMetadata(31) },
                "someGroupId"
            )
        }
        verify(exactly = 1) { producer.commitTransaction() }
        verify(exactly = 0) { producer.abortTransaction() }
        res shouldBe "someResult"
    }

    @Test
    fun `transaction with consumer aborts and throws if consumer is not subscribed to given topics`() {
        assertThrows<TransactionAbortedException> {
            producer.transaction(consumer, topics + setOf("kf-some-unsubscribed-topic")) {
                // Block succeeds
            }
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 0) { producer.commitTransaction() }
        verify(exactly = 1) { producer.abortTransaction() }
    }

    @Test
    fun `transaction with consumer aborts and throws if block throws`() {
        assertThrows<Exception> {
            producer.transaction(consumer, topics) {
                // Block fails
                throw Exception()
            }
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 0) { producer.commitTransaction() }
        verify(exactly = 1) { producer.abortTransaction() }
    }

    @Test
    fun `transaction with consumer throws if abort throws`() {
        every { producer.abortTransaction() } throws (Exception())
        assertThrows<Exception> {
            producer.transaction(consumer, topics) {
                // Block fails
                throw Exception()
            }
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 0) { producer.commitTransaction() }
        verify(exactly = 1) { producer.abortTransaction() }
    }

    //
    // Tests for transaction with consumer and single topic
    //

    @Test
    fun `transaction with single topic commits correctly`() {
        val res = producer.transaction(consumer, topic) {
            // Block succeeds
            "someResult"
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 1) {
            producer.sendOffsetsToTransaction(
                mapOf(TopicPartition(topic, 0) to OffsetAndMetadata(31)),
                "someGroupId"
            )
        }
        verify(exactly = 1) { producer.commitTransaction() }
        verify(exactly = 0) { producer.abortTransaction() }
        assert(res == "someResult")
    }

    @Test
    fun `transaction with single topic aborts and throws if consumer is not subscribed to given topic`() {
        assertThrows<TransactionAbortedException> {
            producer.transaction(consumer, "kf-some-unsubscribed-topic") {
                // Block succeeds
            }
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 0) { producer.commitTransaction() }
        verify(exactly = 1) { producer.abortTransaction() }
    }

    @Test
    fun `transaction with single topic aborts and throws if block throws`() {
        assertThrows<Exception> {
            producer.transaction(consumer, topic) {
                // Block fails
                throw Exception()
            }
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 0) { producer.commitTransaction() }
        verify(exactly = 1) { producer.abortTransaction() }
    }

    @Test
    fun `transaction with single topic throws if abort throws`() {
        every { producer.abortTransaction() } throws (Exception())
        assertThrows<Exception> {
            producer.transaction(consumer, topic) {
                // Block fails
                throw Exception()
            }
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 0) { producer.commitTransaction() }
        verify(exactly = 1) { producer.abortTransaction() }
    }

    //
    // Tests for transaction with no consumer
    //

    @Test
    fun `no consumer transaction commits if block ran`() {
        producer.transaction {
            // Execution is fine
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 1) { producer.commitTransaction() }
        verify(exactly = 0) { producer.abortTransaction() }
    }

    @Test
    fun `no consumer transaction aborts and throws if block threw`() {
        assertThrows<Exception> {
            producer.transaction {
                // Execution fails
                throw Exception()
            }
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 0) { producer.commitTransaction() }
        verify(exactly = 1) { producer.abortTransaction() }
    }

    @Test
    fun `no consumer transaction throws if abort threw`() {
        every { producer.abortTransaction() } throws (Exception())
        assertThrows<Exception> {
            producer.transaction {
                // Execution fails
                throw Exception()
            }
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 0) { producer.commitTransaction() }
        verify(exactly = 1) { producer.abortTransaction() }
    }

    @Test
    fun `no consumer transaction throws if commitTransaction() failed`() {
        every { producer.commitTransaction() } throws (Exception())

        assertThrows<Exception> {
            producer.transaction {
                // Execution succeeds
            }
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 1) { producer.commitTransaction() }
        verify(exactly = 1) { producer.abortTransaction() }
    }

    @Test
    fun `no consumer transaction returns block's return value`() {
        val res = producer.transaction {
            "hello"
        }

        verify(exactly = 1) { producer.beginTransaction() }
        verify(exactly = 1) { producer.commitTransaction() }
        verify(exactly = 0) { producer.abortTransaction() }

        res shouldBe "hello"
    }
}
