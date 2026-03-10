plugins {
    kotlin("jvm")
    application
    kotlin("plugin.serialization")
    jacoco
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("com.evolutiondso.androiddoctor.cli.MainKt")
    applicationDefaultJvmArgs = listOf(
        "-Dandroiddoctor.repoRoot=${rootProject.rootDir.absolutePath}"
    )
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.apache.pdfbox:pdfbox:2.0.30")

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit4)
}

tasks.jacocoTestCoverageVerification {
    description = "Verifies minimum unit test coverage for CLI mocked report tests."
    group = "verification"
    dependsOn(tasks.test)

    violationRules {
        rule {
            element = "PACKAGE"
            includes = listOf("com.evolutiondso.androiddoctor.cli.report", "com.evolutiondso.androiddoctor.cli.render.html")
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}


tasks.test {
    description = "Runs unit tests for CLI report loading and rendering."
    group = "verification"
    useJUnitPlatform()
}
