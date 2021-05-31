package io.github.kkafka.consumer

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.coroutines.suspendCoroutine

/**
 * Like [KafkaConsumer.commitAsync], except
 * - It suspends rather than take a callback
 * - It throws rather than pass a nullable exception in the callback
 * - It returns the offsets passed to the callback
 */
public suspend fun <K, V> KafkaConsumer<K, V>.commitSuspending(): Map<TopicPartition, OffsetAndMetadata> =
    suspendCoroutine { continuation ->
        commitAsync { nullableOffsets: Map<TopicPartition, OffsetAndMetadata>?, exception: Exception? ->
            val offsets = nullableOffsets ?: emptyMap()
            val result = if (exception != null) failure(exception) else success(offsets)
            continuation.resumeWith(result)
        }
    }

/**
 * Like [KafkaConsumer.commitAsync] (offsets), except
 * - It suspends rather than take a callback
 * - It throws rather than pass a nullable exception in the callback
 * - It returns the offsets passed to the callback
 */
public suspend fun <K, V> KafkaConsumer<K, V>.commitSuspending(
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

/**
 * Overload of [commitSuspending] that can be used with
 * [ConsumerRecord.partitionAndOffset]
 * [org.apache.kafka.clients.consumer.ConsumerRecord.partitionAndOffset]
 */
public suspend fun <K, V> KafkaConsumer<K, V>.commitSuspending(
    offset: Pair<TopicPartition, OffsetAndMetadata>,
): Pair<TopicPartition, OffsetAndMetadata> =
    commitSuspending(mapOf(offset)).entries.first().toPair()
