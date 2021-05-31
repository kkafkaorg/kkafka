package org.kkafka.consumer

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

internal class FlowKtTest {
    private fun consumer(returns: Sequence<ConsumerRecords<String, String>>): KafkaConsumer<String, String> =
        mockk {
            val iter = returns.iterator()
            every { poll(any<Duration>()) } answers { iter.next() }
        }

    fun recordBatch(
        topic: String = "21",
        partition: Int = 1,
        batchSize: Int = 5,
        batches: Int = 10,
    ) = List(batchSize * batches) { record(topic, partition, offset = it.toLong()) }
        .windowed(batchSize, step = batchSize, partialWindows = true)
        .map { list ->
            ConsumerRecords(list.groupBy { it.partitionAndOffset.first })
        }

    @Test
    fun `collected flow is equal to what poll returns`() = runBlocking {
        val batch = recordBatch(batchSize = 11, batches = 10)
        val collected = consumer(batch.asSequence()).pollWithFlow(Duration.ZERO).take(10).toList()

        collected shouldBe batch
    }

    @Test
    fun `flattened flow corresponds to concatenated batches`() = runBlocking {
        val batch = recordBatch(batchSize = 11, batches = 10)
        val flattened = batch.flatMap { it.toList() }

        val collected =
            consumer(batch.asSequence()).pollWithFlattenedFlow(Duration.ZERO).take(10 * 11).toList()

        collected shouldBe flattened
    }

    @Test
    fun `flow throws on collect when poll() throws`() {
        val batches = recordBatch(batchSize = 1, batches = 2)
        val iter = batches.iterator()
        val c: KafkaConsumer<String, String> = mockk {
            every { poll(any<Duration>()) } answers { iter.next() }
        }

        val collected = mutableListOf<ConsumerRecords<*, *>>()

        runBlocking {
            val first10 = c.pollWithFlow(Duration.ZERO)
                .onEach { collected += it }
                .take(10)

            assertThrows<NoSuchElementException> { first10.collect() }
        }

        collected.size shouldBe 2
        collected shouldBe batches
    }
}
