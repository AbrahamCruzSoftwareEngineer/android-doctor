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
        // Colors
        val RED = "\u001B[31m"
        val GREEN = "\u001B[32m"
        val YELLOW = "\u001B[33m"
        val CYAN = "\u001B[36m"
        val RESET = "\u001B[0m"

        fun banner(msg: String) = println("\n$CYAN====================  $msg  ====================${RESET}\n")
        fun step(msg: String) = println("$YELLOW→ $msg$RESET")
        fun success(msg: String) = println("$GREEN✔ $msg$RESET")
        fun fail(msg: String): Nothing {
            println("$RED✘ $msg$RESET")
            throw GradleException(msg)
        }

        fun timed(label: String, block: () -> Int) {
            val start = System.currentTimeMillis()
            val exitCode = block()
            val duration = System.currentTimeMillis() - start

            if (exitCode == 0) {
                println("$GREEN   (✓ Completed $label in ${duration}ms)$RESET\n")
            } else {
                fail("Step '$label' failed with exit code $exitCode")
            }
        }

        fun runCommand(vararg args: String): Int {
            val process = ProcessBuilder(*args)
                .apply { redirectErrorStream(true) }
                .start()

            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { println(it) }
            }

            return process.waitFor()
        }

        fun runCommandInDir(workingDir: File, vararg args: String): Int {
            val process = ProcessBuilder(*args)
                .apply {
                    directory(workingDir)
                    redirectErrorStream(true)
                }
                .start()

            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { println(it) }
            }

            return process.waitFor()
        }

        banner("ANDROIDDOCTOR COMPLETE E2E TEST")

        val reportJson = "samples/android-doctor-architecture-test-app/app/build/androidDoctor/report.json"

        // Step 1 — Basic build success (already handled by dependsOn)
        success("Plugin + CLI built successfully")

        // Step 2 — Generate report.json
        timed("androidDoctorCollect") {
            step("Running androidDoctorCollect in samples/android-doctor-architecture-test-app")
            runCommandInDir(
                file("samples/android-doctor-architecture-test-app"),
                "../../gradlew", "androidDoctorCollect"
            )
        }
        success("report.json generated")

        if (!file(reportJson).exists()) fail("report.json not created! Something is wrong.")

        // --- Helper to assert created files ---
        fun assertExists(path: String) {
            if (!file(path).exists()) fail("Expected output file missing: $path")
            else success("Verified output exists → $path")
        }

        // -----------------------------
        // Step 3 — HTML Export Test
        // -----------------------------
        val htmlOut = "cli/build/androidDoctor/html/report.html"
        timed("CLI HTML export") {
            step("Exporting HTML report...")
            runCommand(
                "./gradlew", ":cli:run",
                "--args=--report $reportJson --html"
            )
        }
        assertExists(htmlOut)

        // -----------------------------
        // Step 4 — Markdown Export
        // -----------------------------
        val mdOut = "cli/build/androidDoctor/markdown/report.md"
        timed("CLI Markdown export") {
            step("Exporting Markdown report...")
            runCommand(
                "./gradlew", ":cli:run",
                "--args=--report $reportJson --md"
            )
        }
        assertExists(mdOut)

        // -----------------------------
        // Step 5 — Auto-open Test (HTML)
        // -----------------------------
        timed("CLI --open test") {
            step("Testing auto-open flag (will not fail if OS cannot open)")
            runCommand(
                "./gradlew", ":cli:run",
                "--args=--report $reportJson --html --open"
            )
        }
        success("--open flag executed without errors")

        banner("ALL STEPS COMPLETED SUCCESSFULLY")
        println("${GREEN}AndroidDoctor full E2E test passed!${RESET}")
    }
}

tasks.register("verifyPublicFreeOnly") {
    group = "verification"
    description = "Fails if premium/licensing source markers are present in public CLI sources."

    val forbidden = listOf("premium", "licensevalidator", "useridentity", "licensing", "entitlement")
    val sourceRoots = listOf(file("cli/src/main/kotlin"), file("cli/src/main/resources"))

    doLast {
        val violations = mutableListOf<String>()
        sourceRoots.filter { it.exists() }.forEach { root ->
            root.walkTopDown()
                .filter { it.isFile && (it.extension == "kt" || it.extension == "md" || it.extension == "js" || it.extension == "css" || it.extension == "html") }
                .forEach { file ->
                    val text = file.readText().lowercase()
                    forbidden.forEach { token ->
                        if (text.contains(token)) {
                            violations += "${file.relativeTo(rootProject.projectDir)} contains forbidden token '$token'"
                        }
                    }
                }
        }

        if (violations.isNotEmpty()) {
            throw GradleException("Public/free guardrail failed:\n" + violations.joinToString("\n"))
        }
    }
}


tasks.named("check") {
    dependsOn("verifyPublicFreeOnly")
    dependsOn("verifyNoPrivateCoordinates")
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
        val violations = mutableListOf<String>()
        filesToScan.filter { it.exists() }.forEach { file ->
            val text = file.readText().lowercase()
            forbidden.forEach { token ->
                if (text.contains(token)) {
                    violations += "${file.relativeTo(rootProject.projectDir)} contains forbidden token '$token'"
                }
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException("Private coordinate guard failed:\n" + violations.joinToString("\n"))
        }
    }
}
