import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val generatedVersionDir = layout.buildDirectory.dir("generated/sources/androiddoctorVersion/kotlin")
val generateAndroidDoctorVersion = tasks.register("generateAndroidDoctorVersion") {
    val outDir = generatedVersionDir.get().asFile
    outputs.dir(outDir)

    doLast {
        val pkgDir = outDir.resolve("com/evolutiondso/androiddoctor")
        pkgDir.mkdirs()

        val versionFile = pkgDir.resolve("AndroidDoctorVersion.kt")
        versionFile.writeText(
            """
            package com.evolutiondso.androiddoctor

            internal const val ANDROID_DOCTOR_VERSION = "${project.version}"
            """.trimIndent()
        )
    }
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generateAndroidDoctorVersion)
}

extensions.configure<KotlinJvmProjectExtension>("kotlin") {
    sourceSets.named("main") {
        kotlin.srcDir(generatedVersionDir)
    }
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
}

gradlePlugin {
    plugins {
        create("androidDoctor") {
            id = "com.evolutiondso.androiddoctor"
            implementationClass = "com.evolutiondso.androiddoctor.AndroidDoctorPlugin"
            displayName = "AndroidDoctor Gradle Plugin"
            description = "Advisory tool for Android build health & Compose modernization."
        }
    }
}
