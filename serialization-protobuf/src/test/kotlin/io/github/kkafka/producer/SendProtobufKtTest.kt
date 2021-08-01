package io.github.kkafka.producer

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test
import kotlin.reflect.KSuspendFunction2

internal class SendProtobufKtTest {
    private val producer: KafkaProducer<ByteArray, ByteArray> = mockk {
    }

    init {
        val send: KSuspendFunction2<
            KafkaProducer<ByteArray, ByteArray>,
            ProducerRecord<ByteArray, ByteArray>,
            RecordMetadata,
            > =
            KafkaProducer<ByteArray, ByteArray>::sendSuspending
        mockkStatic(send)

        coEvery { producer.sendSuspending(any()) } returns RecordMetadata(TopicPartition("", 1), 0, 0, 0, 0, 0, 0)
    }

    @Test
    fun `send does not crash when key is null`() {
        val record = ProducerRecord<String, String>("topic", null, "value")

        runBlocking {
            producer.sendSuspending(record, String.serializer(), String.serializer())
        }

        coVerify(exactly = 1) {
            producer.sendSuspending(match { it.key() == null && it.value() != null })
        }
    }

    @Test
    fun `byte arrays match encoded values`() {
        val record = ProducerRecord<String, String>("topic", "key", "value")

        runBlocking {
            producer.sendSuspending(record, String.serializer(), String.serializer())
        }

        val encodedK = ProtoBuf.encodeToByteArray(String.serializer(), "key")
        val encodedV = ProtoBuf.encodeToByteArray(String.serializer(), "value")

        coVerify(exactly = 1) {
            producer.sendSuspending(match { it.key().contentEquals(encodedK) && it.value().contentEquals(encodedV) })
        }
    }
}
