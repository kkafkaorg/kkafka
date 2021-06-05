package io.github.kkafka.consumer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

/**
 * Runs a polling loop returned as a cold [Flow] of [ConsumerRecords]s.
 *
 * If you are using auto-commit, then this is a great way to consume your records,
 * because they will not get committed until you [collect][Flow.collect] this [Flow].
 *
 * This is also why this is a flow of [ConsumerRecords] rather than a flow
 * of [ConsumerRecord]: if
 * that was the case, then it would be possible to consume a single
 * [ConsumerRecord] without having committed the rest of the batch that [KafkaConsumer.poll]
 * returns.
 *
 * If you do not use auto-commit and would like a flow that emits one [ConsumerRecord]
 * at a time, see [pollWithFlow].
 *
 * **For example, with auto-commit:**
 *
 * ```kotlin
 * val consumer: KafkaConsumer<String, String>
 *
 * consumer.pollWithFlow()
 *   .collect { records -> database.insertBatch(records.toList()) }
 *
 * ```
 *
 * **With manual commit control:**
 *
 * ```kotlin
 * val consumer: KafkaConsumer<String, String>
 *
 * consumer.pollWithFlow()
 *   .onEach { records -> database.insertBatch(records.toList()) }
 *   .collect { records ->
 *      consumer.commitSuspending(records.partitionAndLatestOffsets)
 *   }
 * ```
 */
public fun <K, V> KafkaConsumer<K, V>.pollWithFlowByBatches(pollingPeriod: Duration): Flow<ConsumerRecords<K, V>> =
    flow {
        while (true) emit(poll(pollingPeriod))
    }

/**
 * Runs a polling loop returned as a cold [Flow] of [ConsumerRecord]s
 *
 * When consuming
 * this flattened flow with auto-commit on, there is a scenario where if you crash while
 * collecting this flow it is possible some records you have not handled can get committed
 * (this is not a concern at all if you are not using auto-commit).
 *
 * For example, with manual committing:
 *
 * ```kotlin
 * val consumer: KafkaConsumer<String, String>
 *
 * consumer.pollWithFlattenedFlow()
 *   .onEach { database.insert(it) }
 *   .collect {
 *      consumer.commitSuspending(it.partitionAndOffset)
 *   }
 * ```
 *
 * For a [Flow] that returns [ConsumerRecords] in batches, more closely resembling [KafkaConsumer.poll], see
 * [pollWithFlowByBatches]
 */
public fun <K, V> KafkaConsumer<K, V>.pollWithFlow(pollingPeriod: Duration): Flow<ConsumerRecord<K, V>> =
    flow {
        pollWithFlowByBatches(pollingPeriod).collect { records -> for (record in records) emit(record) }
    }
