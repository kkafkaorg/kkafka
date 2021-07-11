package io.github.kkafka.consumer

import io.kotest.matchers.collections.shouldBeSameSizeAs
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.jupiter.api.Test
import java.time.Duration

internal class FlowProtobufKtTest {
    @Test
    fun `pollWithFlow deserializes`() = runBlocking {
        val batch = recordBatch(
            batchSize = 11, batches = 10,
            key = ProtoBuf.encodeToByteArray(Int.serializer(), 2),
            value = ProtoBuf.encodeToByteArray(Int.serializer(), 5)
        )
        val collected = consumer(batch.asSequence())
            .pollWithFlow(Duration.ZERO, Int.serializer(), Int.serializer())
            .take(10).toList()

        val expected = batch
            .flatMap { it.toList() }
            .map { ConsumerRecord(it.topic(), it.partition(), it.offset(), 2, 5) }
            .take(10)

        collected shouldBeSameSizeAs expected

        for ((ac, ex) in collected zip expected) {
            ac.value() shouldBe ex.value()
            ac.key() shouldBe ex.key()
        }
    }
}

internal fun record(
    topic: String,
    partition: Int,
    offset: Long,
    key: ByteArray,
    value: ByteArray,
): ConsumerRecord<ByteArray, ByteArray> =
    ConsumerRecord(
        topic,
        partition,
        offset,
        key,
        value
    )

private fun recordBatch(
    topic: String = "21",
    partition: Int = 1,
    batchSize: Int = 5,
    batches: Int = 10,
    key: ByteArray,
    value: ByteArray,
) = List(batchSize * batches) { record(topic, partition, offset = it.toLong(), key, value) }
    .windowed(batchSize, step = batchSize, partialWindows = true)
    .map { list ->
        ConsumerRecords(list.groupBy { it.partitionAndOffset.first })
    }

private fun consumer(returns: Sequence<ConsumerRecords<ByteArray, ByteArray>>): KafkaConsumer<ByteArray, ByteArray> =
    mockk {
        val iter = returns.iterator()
        every { poll(any<Duration>()) } answers { iter.next() }
    }
