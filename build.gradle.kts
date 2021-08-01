import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintPlugin
import java.net.URL

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0" apply false
    id("org.jetbrains.dokka") version "1.4.32"
    kotlin("jvm") version "1.5.10"
    jacoco
}

allprojects {
    group = "io.github.kkafkaorg"
    repositories {
        mavenCentral()
    }
}

val jacocoVersion: String by project
jacoco {
    toolVersion = jacocoVersion
}

subprojects {
    apply<DokkaPlugin>()

    if ("docs" !in name) {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply<KtlintPlugin>()
        apply<JacocoPlugin>()

        jacoco {
            toolVersion = jacocoVersion
        }

        kotlin {
            explicitApi()
        }

        dependencies {
            testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
            testImplementation("io.kotest:kotest-assertions-core:4.6.0")
            testImplementation("io.mockk:mockk:1.11.0")
        }

        tasks {
            withType<KotlinCompile> {
                kotlinOptions.allWarningsAsErrors = true
            }

            test {
                useJUnitPlatform()
            }
            jacocoTestReport {
                reports {
                    xml.isEnabled = false
                    html.isEnabled = false
                }
                dependsOn(test)
            }
        }
    }

    tasks.withType<DokkaTaskPartial>().configureEach {
        if (project.name != "docs") dokkaSourceSets {
            named("main") {
                moduleName.set("kkafka-${project.name}")
                includes.from("Module.md")
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(URL(
                        "https://github.com/kkafkaorg/kkafka/tree/master/${project.name}/src/main/kotlin"
                    ))
                }
                externalDocumentationLink(
                    url = URL("https://javadoc.io/doc/org.apache.kafka/kafka-clients/2.8.0/")
                )
                externalDocumentationLink(
                    url = URL("https://kotlin.github.io/kotlinx.coroutines/")
                )
            }
        }
    }
}

val rootCoverageReport by tasks.registering(JacocoReport::class) {
    executionData(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))

    subprojects.filter { "docs" !in it.name }.forEach { it.sourceSets { it.sourceSets.main.get() } }

    reports {
        xml.isEnabled = true
        xml.destination = file("${buildDir}/reports/jacoco/report.xml")
        html.isEnabled = true
        csv.isEnabled = false
    }
}

tasks.check { dependsOn(rootCoverageReport) }

afterEvaluate {
    rootCoverageReport {
        val testingTasks = subprojects.flatMap { it.tasks.withType<JacocoReport>() }
        dependsOn(testingTasks)
    }
}

