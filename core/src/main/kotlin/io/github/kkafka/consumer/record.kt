package io.github.kkafka.consumer

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

/**
 * Returns the [Pair] of [TopicPartition] and [OffsetAndMetadata] corresponding to this
 * [ConsumerRecord]
 */
public val ConsumerRecord<*, *>.partitionAndOffset: Pair<TopicPartition, OffsetAndMetadata>
    get() = TopicPartition(topic(), partition()) to OffsetAndMetadata(offset())

/**
 * Returns the latest offset for all the partitions in this [ConsumerRecords]
 * as a [Map] of [TopicPartition] to [OffsetAndMetadata]
 */
public val ConsumerRecords<*, *>.partitionAndLatestOffsets: Map<TopicPartition, OffsetAndMetadata>
    get() = partitions().associateWith { partition ->
        val last = records(partition).reduce { acc, consumerRecord ->
            if (acc.offset() >= consumerRecord.offset()) acc else consumerRecord
        }
        OffsetAndMetadata(last.offset())
    }

public operator fun <K> ConsumerRecord<K, *>.component1(): K = key()

public operator fun <V> ConsumerRecord<*, V>.component2(): V = value()
