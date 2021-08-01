package io.github.kkafka.producer

import org.apache.kafka.clients.producer.KafkaProducer

public object Producers {
    public val idempotent: Map<String, String> = mapOf(
        "message.send.max.retries" to "10000000",
        "enable.idempotence" to "true",
    )
}

public fun <K, V> KafkaProducer(vararg props: Pair<String, String>): KafkaProducer<K, V> =
    KafkaProducer<K, V>(props.toList().toMap())
