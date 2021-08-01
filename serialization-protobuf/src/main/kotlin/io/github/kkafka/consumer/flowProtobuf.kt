package io.github.kkafka.consumer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

/**
 * Runs a polling loop returned as a cold [Flow] of [ConsumerRecord]s.
 *
 * For use on a [KafkaConsumer] that does not have serialization, this
 * extension deserializes [ConsumerRecord]s as they come in thanks to
 * [keyDeserializer] and [valueDeserializer].
 *
 * To create a [KafkaConsumer] that
 * does have the right generic parameters and uses Kotlinx Serialization
 * with Protobuf (rather than deserializing with this extension) see
 * [ProtobufDeserializer][io.github.kkafka.serialization.ProtobufDeserializer].
 */
public fun <K, V> KafkaConsumer<ByteArray, ByteArray>.pollWithFlow(
    pollingPeriod: Duration,
    keyDeserializer: DeserializationStrategy<K>,
    valueDeserializer: DeserializationStrategy<V>,
    protobuf: ProtoBuf = ProtoBuf,
): Flow<ConsumerRecord<K, V>> = pollWithFlow(pollingPeriod)
    .map {
        val key = protobuf.decodeFromByteArray(keyDeserializer, it.key())
        val value = protobuf.decodeFromByteArray(valueDeserializer, it.value())
        ConsumerRecord(it.topic(), it.partition(), it.offset(), key, value)
    }
