

plugins {
    kotlin("jvm")
    `java-library`
    jacoco
}

dependencies {
    api("org.apache.kafka:kafka_2.12:2.8.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
}
