package io.github.kkafka.producer

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

/**
 * Overload of [sendSuspending].
 *
 * Like [KafkaProducer.send], except
 * - it suspends once sending is complete rather than accept a callback
 * - it returns the [RecordMetadata] specifying the partition [record] was sent to,
 * the offset it was assigned and the timestamp of the record
 * - it throws the exception that would normally be passed to the callback if sending fails
 * - it accepts [keySerializer], [valueSerializer] and [protobuf] in order
 * to serialise from [ByteArray]s.
 */
public suspend fun <K, V> KafkaProducer<ByteArray, ByteArray>.sendSuspending(
    record: ProducerRecord<K, V>,
    keySerializer: SerializationStrategy<K>,
    valueSerializer: SerializationStrategy<V>,
    protobuf: ProtoBuf = ProtoBuf,
): RecordMetadata {
    val encoded: ProducerRecord<ByteArray, ByteArray> = with(record) {
        val key = key()?.let { protobuf.encodeToByteArray(keySerializer, it) }
        val value = protobuf.encodeToByteArray(valueSerializer, value())
        ProducerRecord(topic(), partition(), timestamp(), key, value, headers())
    }
    return sendSuspending(encoded)
}
