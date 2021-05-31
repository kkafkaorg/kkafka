package io.github.kkafka.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

@ExperimentalSerializationApi
public class ProtobufSerializer<T>(
    private val strategy: SerializationStrategy<T>,
    private val protobuf: ProtoBuf = ProtoBuf,
) : Serializer<T> {
    override fun serialize(topic: String?, data: T): ByteArray =
        protobuf.encodeToByteArray(strategy, data)
}

@ExperimentalSerializationApi
public class ProtobufDeserializer<T>(
    private val strategy: DeserializationStrategy<T>,
    private val protobuf: ProtoBuf = ProtoBuf,
) : Deserializer<T> {
    override fun deserialize(topic: String?, data: ByteArray): T =
        protobuf.decodeFromByteArray(strategy, data)
}
