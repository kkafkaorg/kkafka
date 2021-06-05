# Index

[![Check](https://github.com/kkafkaorg/kkafka/actions/workflows/main.yml/badge.svg)](https://github.com/kkafkaorg/kkafka/actions/workflows/main.yml)
[![codecov](https://codecov.io/gh/kkafkaorg/kkafka/branch/master/graph/badge.svg?token=IEGAM53Q19)](https://codecov.io/gh/kkafkaorg/kkafka)

**KKafka** is a Kotlin library around the 
[Kafka Clients](https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients)
java library, with a focus on providing an idiomatic Kotlin asynchronous API thanks
to [coroutines](https://kotlinlang.org/docs/coroutines-overview.html), rather
than using callbacks Java-style.


## Quick start

### Gradle

```kotlin
depndencies {
    implementation("TODO")
}
```

### A consumer

```kotlin
val props = Properties().apply {
    setProperty("bootstrap.servers", "localhost:9092")
    setProperty("group.id", "test")
    setProperty("enable.auto.commit", "true")
    setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
}
val consumer = KafkaConsumer<String, String>(props)
consumer.subscribe(listOf("foo", "bar"))
consumer.pollWithFlow
    .collect { record ->
        val (key, value) = record
        updateDb(key, value)
    }
```
