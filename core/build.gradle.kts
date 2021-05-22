plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.0"
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("io.kotest:kotest-runner-junit5:4.6.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
}
