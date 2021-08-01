package io.github.kkafka.producer

import io.kotest.assertions.any
import io.mockk.every
import io.mockk.mockkConstructor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.apache.kafka.clients.producer.KafkaProducer

internal class ProducersTest {
    @Test
    fun `building producer with vararg`() {
        mockkConstructor(KafkaProducer::class)

        every { anyConstructed<KafkaProducer<String, String>>() } answers {
            KafkaProducer<String, String>()
        }

        val p = KafkaProducer<String, String>("bootstrap.servers" to "127.0.0.1:3131", "message.send.max.retires" to "100")
    }
}