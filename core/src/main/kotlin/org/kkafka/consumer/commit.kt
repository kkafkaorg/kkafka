package org.kkafka.consumer

import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.coroutines.suspendCoroutine
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

/**
 * Like [KafkaConsumer.commitAsync], except
 * - It suspends rather than take a callback
 * - It throws rather than pass a nullable exception in the callback
 * - It returns the offsets passed to the callback
 */
public suspend fun <K, V> KafkaConsumer<K, V>.commit(): Map<TopicPartition, OffsetAndMetadata> =
    suspendCoroutine { continuation ->
        commitAsync { nullableOffsets: Map<TopicPartition, OffsetAndMetadata>?, exception: Exception? ->
            val offsets = nullableOffsets ?: emptyMap()
            val result = if (exception != null) failure(exception) else success(offsets)
            continuation.resumeWith(result)
        }
    }

/**
 * Like [KafkaConsumer.commitAsync(offsets)], except
 * - It suspends rather than take a callback
 * - It throws rather than pass a nullable exception in the callback
 * - It returns the offsets passed to the callback
 */
public suspend fun <K, V> KafkaConsumer<K, V>.commit(
    offsets: Map<TopicPartition, OffsetAndMetadata>,
): Map<TopicPartition, OffsetAndMetadata> = suspendCoroutine { continuation ->
    commitAsync(offsets) {
            nullableOffsets: Map<TopicPartition, OffsetAndMetadata>?,
            exception: Exception?,
        ->
        val newOffsets = nullableOffsets ?: emptyMap()
        val result = if (exception != null) failure(exception) else success(newOffsets)
        continuation.resumeWith(result)
    }
}


public fun hello(): Int = 2
