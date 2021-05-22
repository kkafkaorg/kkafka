import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("io.kotest:kotest-assertions-core:4.6.0")
    testImplementation("io.mockk:mockk:1.11.0")
    api("org.apache.kafka:kafka_2.12:2.8.0")


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
}


kotlin {
    explicitApi()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.allWarningsAsErrors = true
}

tasks.test {
    useJUnitPlatform()
}
