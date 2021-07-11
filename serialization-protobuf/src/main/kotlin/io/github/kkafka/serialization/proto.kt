package io.github.kkafka.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

/**
 * Implementation of Kafka's [Serializer] that uses a Kotlinx-serialization
 * [strategy] to encode
 * Kotlin classes into [ByteArray]s with [protobuf].
 */
@ExperimentalSerializationApi
public class ProtobufSerializer<T>(
    private val strategy: SerializationStrategy<T>,
    private val protobuf: ProtoBuf = ProtoBuf,
) : Serializer<T> {
    override fun serialize(topic: String?, data: T): ByteArray =
        protobuf.encodeToByteArray(strategy, data)
}

/**
 * Implementation of Kafka's [Deserializer] that uses a Kotlinx-serialization
 * [strategy] to decode
 * Kotlin classes from [ByteArray]s with [protobuf].
 *
 * For deserializing into Kotlin classes with an existing
 * [KafkaConsumer][org.apache.kafka.clients.consumer.KafkaConsumer]<[ByteArray], [ByteArray]>,
 * see the corresponding overload of [io.github.kkafka.consumer.pollWithFlow]
 */
@ExperimentalSerializationApi
public class ProtobufDeserializer<T>(
    private val strategy: DeserializationStrategy<T>,
    private val protobuf: ProtoBuf = ProtoBuf,
) : Deserializer<T> {
    override fun deserialize(topic: String?, data: ByteArray): T =
        protobuf.decodeFromByteArray(strategy, data)
}
