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
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply<KtlintPlugin>()
    apply<JacocoPlugin>()
    apply<DokkaPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")

    kotlin {
        explicitApi()
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

        withType<DokkaTaskPartial>().configureEach {
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
                        url = URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/")
                    )
                }
            }
        }
    }
}

allprojects {
    jacoco {
        toolVersion = "0.8.7"
    }
}

val rootCoverageReport by tasks.registering(JacocoReport::class) {
    executionData(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))

    subprojects { sourceSets(sourceSets.main.get()) }

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

