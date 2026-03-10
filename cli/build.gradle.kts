plugins {
    kotlin("jvm")
    application
    kotlin("plugin.serialization")
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
}

tasks.test {
    description = "Runs unit tests for CLI report loading and rendering."
    group = "verification"
    useJUnitPlatform()
}
