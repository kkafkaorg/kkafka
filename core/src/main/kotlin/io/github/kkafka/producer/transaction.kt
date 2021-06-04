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
 */
public fun <PK, PV, CK, CV> KafkaProducer<PK, PV>.transaction(
    consumer: KafkaConsumer<CK, CV>,
    consumerTopics: List<String>,
    block: KafkaProducer<PK, PV>.() -> Boolean,
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
 */
public fun <PK, PV, CK, CV> KafkaProducer<PK, PV>.transaction(
    consumer: KafkaConsumer<CK, CV>,
    consumerTopic: String,
    block: KafkaProducer<PK, PV>.() -> Boolean,
) {
    transaction(consumer, listOf(consumerTopic), block)
}

public fun main() {
    val producer = KafkaProducer<String, Int>(mapOf())
    val consumer = KafkaConsumer<String, String>(mapOf())

    consumer.subscribe(listOf("my-topic"))

    producer.transaction(consumer, "my-topic") {
        // Consume & produce

        true
    }
}
