plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.5.10"
    `java-library`
    jacoco
}

dependencies {
    implementation(project(":core"))
    val serializationLibVersion: String by project
    api("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$serializationLibVersion")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
}
