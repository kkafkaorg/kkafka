package io.github.kkafka.producer

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.KafkaProducer

/**
 * Produce transactionally with a given consumer on the given topics.
 * The [consumer] indicates which consumer is taking part in the transaction with the producer.
 * The [consumerTopics] indicate which of the consumer's topics are taking part in the transaction.
 * You should not include the producer's topic.
 * The [consumer] must be subscribed to and consuming from the given [consumerTopics].
 *
 * [block] is the transaction code. Here you can consume from [consumer] and send with the producer.
 * At the end of the [block], if true is returned, then the transaction is committed, otherwise if false is returned,
 * it is aborted.
 *
 * This is useful for a consume-process-produce pattern, where we commit to the consumer only if we have successfully
 * produced. This ensures exactly once processing.
 */
public fun <K, V> KafkaProducer<K, V>.transaction(
    consumer: KafkaConsumer<*, *>,
    consumerTopics: List<String>,
    block: KafkaProducer<K, V>.() -> Boolean,
) {
    beginTransaction()

    // Run the transaction's block of code
    if (block(this)) {
        // Get the TopicPartition objects given the topic names
        val consumerTopicPartitions = consumer.assignment().filter { it.topic() in consumerTopics }.map { it!! }

        // Get the offsets of the partitions so we can commit them as part of the transaction
        val offsets = consumerTopicPartitions.associateWith { OffsetAndMetadata(consumer.position(it)) }

        val groupId = consumer.groupMetadata().groupId()

        // Commit the consumer's offsets
        sendOffsetsToTransaction(offsets, groupId)

        // Successful transaction
        commitTransaction()
    } else {
        // If the transaction failed, abort
        abortTransaction()
    }
}

/**
 * Consume and produce transactionally on a single topic.
 * See [transaction].
 */
public fun <K, V> KafkaProducer<K, V>.transaction(
    consumer: KafkaConsumer<*, *>,
    consumerTopic: String,
    block: KafkaProducer<K, V>.() -> Boolean,
) {
    transaction(consumer, listOf(consumerTopic), block)
}

/**
 * Produce transactionally with an arbitrary block of code.
 * If an exception occurs during execution of [block], the transaction is aborted, otherwise it is committed.
 */
public fun <K, V, T> KafkaProducer<K, V>.transaction(block: KafkaProducer<K, V>.() -> T) : T {
    beginTransaction()
    return try {
        val res = block(this)
        try {
            commitTransaction()
            res
        } catch (onCommit: Exception) {
            throw onCommit
        }
    } catch (e: Exception) {
        abortTransaction()
        throw e
    }
}
