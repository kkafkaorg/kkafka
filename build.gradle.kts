import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintPlugin

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0" apply false
    kotlin("jvm") version "1.5.0"
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

