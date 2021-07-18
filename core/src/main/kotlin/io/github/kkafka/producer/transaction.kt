package io.github.kkafka.producer

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.errors.TransactionAbortedException

/**
 * Produce transactionally with a given consumer on the given topics.
 * The [consumer] indicates which consumer is taking part in the transaction with the producer.
 * The [consumerTopics] indicate which of the consumer's topics are taking part in the transaction.
 * You should not include the producer's topic.
 * The [consumer] must be subscribed to and consuming from the given [consumerTopics].
 *
 * [block] is the transaction code. Here you can consume from [consumer] and send with the producer.
 * At the end of the [block], if no exceptions were thrown, the transaction is committed and the result of the
 * block is returned. Otherwise, the transaction is aborted and the exception is thrown.
 *
 * This is useful for a consume-process-produce pattern, where we commit to the consumer only if we have successfully
 * produced. This ensures exactly once processing.
 */
public fun <K, V, T> KafkaProducer<K, V>.transaction(
    consumer: KafkaConsumer<*, *>,
    consumerTopics: Set<String>,
    block: KafkaProducer<K, V>.() -> T,
): T {
    beginTransaction()
    return try {
        val res = block(this)

        // Get the TopicPartition objects given the topic names
        val consumerTopicPartitions = consumer.assignment().filter { it.topic() in consumerTopics }.map { it!! }

        // Get the offsets of the partitions so we can commit them as part of the transaction
        val offsets = consumerTopicPartitions.associateWith { OffsetAndMetadata(consumer.position(it)) }

        val groupId = consumer.groupMetadata().groupId()

        // Make sure that we got all the needed partitions
        if (offsets.size != consumerTopics.size) {
            throw TransactionAbortedException(
                "consumer in transaction is not properly subscribed to all topics given for transaction"
            )
        }

        // Commit the consumer's offsets
        sendOffsetsToTransaction(offsets, groupId)

        // Successful transaction
        commitTransaction()

        res
    } catch (e: Exception) {
        try {
            abortTransaction()
        } catch (eAbort: Exception) {
            eAbort.addSuppressed(e)
            throw eAbort
        }

        throw e
    }
}

/**
 * Overload of [transaction] with vararg.
 */
public fun <K, V, T> KafkaProducer<K, V>.transaction(
    consumer: KafkaConsumer<*, *>,
    vararg consumerTopics: String,
    block: KafkaProducer<K, V>.() -> T,
): T = transaction(consumer, consumerTopics.toSet(), block)

/**
 * Produce transactionally with an arbitrary block of code.
 * If an exception occurs during the execution of [block] or the transaction commit, the transaction is aborted,
 * otherwise it is committed and the result of [block] is returned.
 *
 * Rethrows any exceptions thrown by [block] [KafkaProducer.commitTransaction], or [KafkaProducer.abortTransaction]
 */
public fun <K, V, T> KafkaProducer<K, V>.transaction(block: KafkaProducer<K, V>.() -> T): T {
    beginTransaction()
    return try {
        val res = block(this)
        commitTransaction()
        res
    } catch (e: Exception) {
        try {
            abortTransaction()
        } catch (eAbort: Exception) {
            eAbort.addSuppressed(e)
            throw eAbort
        }
        throw e
    }
}
