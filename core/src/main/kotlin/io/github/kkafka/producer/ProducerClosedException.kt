package io.github.kkafka.producer

import org.apache.kafka.common.KafkaException

public class ProducerClosedException(msg: String) : KafkaException(msg)
