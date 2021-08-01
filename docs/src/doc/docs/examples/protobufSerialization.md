# Protobuf with kotlinx serialization

KKafka offers a very simple way to serialize and de-serialize classes to 
[Protocol Buffers](https://developers.google.com/protocol-buffers) without generating java code 
from
proto files, by using
[`kotlinx.serialization`](https://kotlin.github.io/kotlinx.serialization/kotlinx-serialization-protobuf/kotlinx-serialization-protobuf/kotlinx.serialization.protobuf/index.html).

Make sure you take a look at the 
[kotlinx serialization protobuf readme](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/formats.md#protobuf-experimental).

In a producer:

```kotlin
val serializers = mapOf(
    "key.deserializer" to "org.apache.kafka.common.serialization.ByteArrayDeserializer",
    "value.deserializer" to "org.apache.kafka.common.serialization.ByteArrayDeserializer",
)

val producer = KafkaProducer<ByteArray, ByteArray>(serializers)
val record = ProducerRecord("myTopic", "myKey", MyValue("cake", 42))

@Serializable
data class MyValue(val a: String, val i: Int) {
    val myMember = 2
}

producer.sendSuspending(
    record,
    keySerializer = String.serializer(),
    valueSerializer = MyValue.serializer()
)
```

In a consumer:

```kotlin
val consumer = KafkaConsumer<ByteArray, ByteArray>(serializers)

val flow: Flow<ConsumerRecord<String, MyValue>> = consumer.pollWithFlow(
    Duration.ofMillis(100),
    keyDeserializer = String.serializer(),
    valueDeserializer = MyValue.serializer(),
)

flow.collect { record ->
    val key = record.key()
    val value = record.value()
    println("Collected $key with value (${value.a}, ${value.i})")
}

```

This is useful for Kotlin applications that share code between the producers
and the consumers (with a library, for example). It avoids writing
and generating code from proto files, and having to deal with the java
classes generated from them.

To make a proto API that can evolve over time, make sure you take look at
[kotlinx serialization's docs](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/formats.md#field-numbers).
