package org.kkafka.producer

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.coroutines.suspendCoroutine

public suspend fun <K, V> KafkaProducer<K, V>.sendSuspending(record: ProducerRecord<K, V>): RecordMetadata =
    suspendCoroutine { continuation ->
        send(record) { metadata: RecordMetadata, exception: Exception? ->
            val res = if (exception != null) failure(exception) else success(metadata)
            continuation.resumeWith(res)
        }
    }
