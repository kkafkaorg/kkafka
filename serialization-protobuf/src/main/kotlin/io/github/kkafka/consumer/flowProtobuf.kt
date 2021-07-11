package io.github.kkafka.consumer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

public fun <K, V> KafkaConsumer<ByteArray, ByteArray>.pollWithFlow(
    pollingPeriod: Duration,
    keyDeserializer: DeserializationStrategy<K>,
    valueDeserializer: DeserializationStrategy<V>,
    protobuf: ProtoBuf = ProtoBuf
): Flow<ConsumerRecord<K, V>> = pollWithFlow(pollingPeriod)
    .map {
        val key = protobuf.decodeFromByteArray(keyDeserializer, it.key())
        val value = protobuf.decodeFromByteArray(valueDeserializer, it.value())
        ConsumerRecord(it.topic(), it.partition(), it.offset(), key, value)
    }
