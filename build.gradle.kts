import java.util.concurrent.TimeUnit

plugins {
    id("base")
    kotlin("jvm") version "1.9.24" apply false
    kotlin("plugin.serialization") version "1.9.24" apply false
}

allprojects {
    group = "com.evolutiondso"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
        google()
    }
}

/**
 * Full AndroidDoctor E2E test:
 * - Ensures plugin + CLI build
 * - Generates report.json
 * - Exports HTML / Markdown outputs
 * - Tests auto-open flag (no crash)
 * - Validates output file existence
 */
tasks.register("doctorTest") {
    group = "verification"
    description = "Complete end-to-end test covering plugin, CLI, HTML, Markdown, and --open behavior."

    dependsOn(":plugin:build")
    dependsOn(":cli:build")

    doLast {
        val red = "\u001B[31m"
        val green = "\u001B[32m"
        val yellow = "\u001B[33m"
        val cyan = "\u001B[36m"
        val reset = "\u001B[0m"

        fun banner(msg: String) = println("\n$cyan====================  $msg  ====================$reset\n")
        fun step(msg: String) = println("$yellow→ $msg$reset")
        fun success(msg: String) = println("$green✔ $msg$reset")
        fun fail(msg: String): Nothing {
            println("$red✘ $msg$reset")
            throw GradleException(msg)
        }

        val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
        val wrapperScript = rootProject.file(if (isWindows) "gradlew.bat" else "gradlew").absolutePath
        val timeoutMinutes = 15L

        fun runCommand(
            workingDir: File = rootProject.projectDir,
            vararg args: String,
        ): Int {
            val process = ProcessBuilder(*args)
                .apply {
                    directory(workingDir)
                    redirectErrorStream(true)
                    environment()["JAVA_HOME"] = System.getenv("JAVA_HOME") ?: ""
                    environment()["PATH"] = System.getenv("PATH") ?: ""
                }
                .start()

            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach(::println)
            }

            if (!process.waitFor(timeoutMinutes, TimeUnit.MINUTES)) {
                process.destroyForcibly()
                fail("Command timed out after $timeoutMinutes minutes: ${args.joinToString(" ")}")
            }

            return process.exitValue()
        }

        fun timed(label: String, block: () -> Int) {
            val start = System.currentTimeMillis()
            val exitCode = block()
            val duration = System.currentTimeMillis() - start

            if (exitCode == 0) {
                println("$green   (✓ Completed $label in ${duration}ms)$reset\n")
            } else {
                fail("Step '$label' failed with exit code $exitCode")
            }
        }

        banner("ANDROIDDOCTOR COMPLETE E2E TEST")

        val reportJson = "samples/android-doctor-architecture-test-app/app/build/androidDoctor/report.json"

        success("Plugin + CLI built successfully")

        timed("androidDoctorCollect") {
            step("Running androidDoctorCollect in samples/android-doctor-architecture-test-app")
            runCommand(
                rootProject.file("samples/android-doctor-architecture-test-app"),
                wrapperScript,
                "--no-daemon",
                "androidDoctorCollect",
            )
        }
        success("report.json generated")

        if (!file(reportJson).exists()) fail("report.json not created! Something is wrong.")

        fun assertExists(path: String) {
            if (!file(path).exists()) fail("Expected output file missing: $path")
            else success("Verified output exists → $path")
        }

        val htmlOut = "cli/build/androidDoctor/html/report.html"
        timed("CLI HTML export") {
            step("Exporting HTML report...")
            runCommand(
                rootProject.projectDir,
                wrapperScript,
                "--no-daemon",
                ":cli:run",
                "--args=--report $reportJson --html",
            )
        }
        assertExists(htmlOut)

        val mdOut = "cli/build/androidDoctor/markdown/report.md"
        timed("CLI Markdown export") {
            step("Exporting Markdown report...")
            runCommand(
                rootProject.projectDir,
                wrapperScript,
                "--no-daemon",
                ":cli:run",
                "--args=--report $reportJson --md",
            )
        }
        assertExists(mdOut)

        timed("CLI --open test") {
            step("Testing auto-open flag (will not fail if OS cannot open)")
            runCommand(
                rootProject.projectDir,
                wrapperScript,
                "--no-daemon",
                ":cli:run",
                "--args=--report $reportJson --html --open",
            )
        }
        success("--open flag executed without errors")

        banner("ALL STEPS COMPLETED SUCCESSFULLY")
        println("${green}AndroidDoctor full E2E test passed!${reset}")
    }
}

fun String.toTokenRegex(): Regex {
    val escaped = Regex.escape(lowercase())
    val usesWordBoundaries = all { it.isLetterOrDigit() || it == '_' || it == '-' }
    return if (usesWordBoundaries) {
        Regex("(?<![a-z0-9])$escaped(?![a-z0-9])")
    } else {
        Regex(escaped)
    }
}

fun collectViolations(files: Collection<File>, tokens: List<String>): List<String> {
    val patterns = tokens.associateWith { it.toTokenRegex() }
    return files
        .filter { it.exists() }
        .flatMap { file ->
            file.readLines().mapIndexedNotNull { index, line ->
                val normalized = line.lowercase()
                val matched = patterns.entries
                    .firstOrNull { (_, regex) -> regex.containsMatchIn(normalized) }
                    ?.key
                matched?.let { "${file.relativeTo(rootProject.projectDir)}:${index + 1} contains forbidden token '$it'" }
            }
        }
}

tasks.register("verifyPublicFreeOnly") {
    group = "verification"
    description = "Fails if premium/licensing source markers are present in public CLI sources."

    val forbidden = listOf("premium", "licensevalidator", "useridentity", "licensing", "entitlement")
    val sourceRoots = listOf(file("cli/src/main/kotlin"), file("cli/src/main/resources"))
    val allowedExtensions = setOf("kt", "md", "js", "css", "html")

    doLast {
        val sourceFiles = sourceRoots
            .filter { it.exists() }
            .flatMap { root ->
                root.walkTopDown()
                    .filter { it.isFile && it.extension in allowedExtensions }
                    .toList()
            }
        val violations = collectViolations(sourceFiles, forbidden)

        if (violations.isNotEmpty()) {
            throw GradleException("Public/free guardrail failed:\n" + violations.joinToString("\n"))
        }
    }
}

tasks.register("verifyNoPrivateCoordinates") {
    group = "verification"
    description = "Fails if public build scripts reference private premium coordinates."

    val forbidden = file("config/guardrails/private-coordinate-tokens.txt")
        .readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }

    val filesToScan = (listOf(
        file("settings.gradle.kts"),
        file("build.gradle.kts"),
        file("gradle/libs.versions.toml")
    ) + fileTree(rootDir) {
        include("**/build.gradle.kts")
        exclude("**/.gradle/**")
        exclude("**/build/**")
        exclude("samples/**")
    }.files).distinct()

    doLast {
        val violations = collectViolations(filesToScan, forbidden)

        if (violations.isNotEmpty()) {
            throw GradleException("Private coordinate guard failed:\n" + violations.joinToString("\n"))
        }
    }
}

tasks.register("verifyWrapperConsistency") {
    group = "verification"
    description = "Verifies Gradle wrapper properties are pinned for reproducible CI."

    val wrapperProperties = file("gradle/wrapper/gradle-wrapper.properties")
    val requiredDistribution = "https\://services.gradle.org/distributions/gradle-8.5-bin.zip"

    doLast {
        if (!wrapperProperties.exists()) {
            throw GradleException("Missing ${wrapperProperties.path}.")
        }

        val props = java.util.Properties().apply {
            wrapperProperties.inputStream().use(::load)
        }

        val distributionUrl = props.getProperty("distributionUrl")?.trim()
        val validateDistributionUrl = props.getProperty("validateDistributionUrl")?.trim()?.lowercase()

        if (distributionUrl != requiredDistribution) {
            throw GradleException(
                "Unexpected wrapper distributionUrl '$distributionUrl'. Expected '$requiredDistribution'.",
            )
        }

        if (validateDistributionUrl != "true") {
            throw GradleException("gradle-wrapper.properties must set validateDistributionUrl=true.")
        }
    }
}

tasks.named("check") {
    dependsOn("verifyPublicFreeOnly")
    dependsOn("verifyNoPrivateCoordinates")
    dependsOn("verifyWrapperConsistency")
}
