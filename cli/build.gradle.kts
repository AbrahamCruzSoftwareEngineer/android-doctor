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
}
