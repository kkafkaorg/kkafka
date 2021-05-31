package io.github.kkafka.producer

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.coroutines.suspendCoroutine

/**
 * Like [KafkaProducer.send], except
 * - it suspends once sending is complete rather than accept a callback
 * - it returns the [RecordMetadata] specifying the partition [record] was sent to,
 * the offset it was assigned and the timestamp of the record
 * - it throws the exception that would normally be passed to the callback if sending fails
 *
 * @throws org.apache.kafka.common.errors.AuthenticationException if authentication fails. See
 * the exception for more details
 * @throws org.apache.kafka.common.errors.AuthorizationException fatal error indicating that  the
 * producer is not allowed to write
 * @throws IllegalStateException if a transactional.id has been configured and no transaction has
 * been started, or when send is invoked after producer has been closed.
 * @throws org.apache.kafka.common.errors.InterruptException If the thread is interrupted while
 * blocked
 * @throws org.apache.kafka.common.errors.SerializationException If the key or value are not
 * valid objects given the configured serializers
 * @throws org.apache.kafka.common.KafkaException If a Kafka related error occurs that
 * does not belong to the public API exceptions.
 *
 */
public suspend fun <K, V> KafkaProducer<K, V>.sendSuspending(record: ProducerRecord<K, V>): RecordMetadata =
    suspendCoroutine { continuation ->
        send(record) { metadata: RecordMetadata?, exception: Exception? ->
            val res = if (exception != null) failure(exception) else success(metadata!!)
            continuation.resumeWith(res)
        }
    }
