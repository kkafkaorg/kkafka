plugins {
    kotlin("jvm")
    `java-library`
    jacoco
}

dependencies {
    val kafkaVersion: String by project
    api("org.apache.kafka:kafka_2.12:$kafkaVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
}
