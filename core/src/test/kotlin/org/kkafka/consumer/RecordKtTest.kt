package org.kkafka.consumer

import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test

internal class RecordKtTest {
    @Test
    fun `extension builds topicPartition that matches underlying`() {
        val (topicPartition, offsetAndMetadata) =
            record(topic = "updates", partition = 2, offset = 42)
                .partitionAndOffset

        topicPartition.topic() shouldBe "updates"
        topicPartition.partition() shouldBe 2
        offsetAndMetadata.offset() shouldBe 42
    }

    @Test
    fun `extension map returns latest offsets`() {
        val tp1 = TopicPartition("topic1", 1)
        val tp2 = TopicPartition("topic2", 132)
        val tp3 = TopicPartition("topic3", 33)
        val records = ConsumerRecords(
            mapOf(
                tp1 to List(3) { record("topic1", 1, it.toLong()) },
                tp2 to List(1) { record("topic1", 1, 21) },
                tp3 to List(10) { record("topic1", 1, it.toLong() + 100) }.reversed(),
            )
        )

        val map = records.partitionAndLatestOffsets

        println(map)

        map[tp1]?.offset() shouldBe 2
        map[tp2]?.offset() shouldBe 21
        map[tp3]?.offset() shouldBe 109
    }
}

fun record(
    topic: String,
    partition: Int,
    offset: Long,
): ConsumerRecord<String, String> =
    ConsumerRecord(topic, partition, offset, "", "")
