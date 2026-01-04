plugins {
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
 * - Exports HTML / MD / PDF outputs
 * - Tests auto-open flag (no crash)
 * - Validates output file existence
 */
tasks.register("doctorTest") {
    group = "verification"
    description = "Complete end-to-end test covering plugin, CLI, HTML, Markdown, PDF, and --open behavior."

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

        banner("ANDROIDDOCTOR COMPLETE E2E TEST")

        val reportJson = "samples/sample-app/build/androidDoctor/report.json"

        // Step 1 — Basic build success (already handled by dependsOn)
        success("Plugin + CLI built successfully")

        // Step 2 — Generate report.json
        timed("androidDoctorCollect") {
            step("Running androidDoctorCollect in samples/sample-app")
            exec {
                workingDir = file("samples/sample-app")
                commandLine("../../gradlew", "androidDoctorCollect")
                isIgnoreExitValue = true
            }.exitValue
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
            exec {
                commandLine(
                    "./gradlew", ":cli:run",
                    "--args=--report $reportJson --html"
                )
                isIgnoreExitValue = true
            }.exitValue
        }
        assertExists(htmlOut)

        // -----------------------------
        // Step 4 — Markdown Export (Premium only)
        // -----------------------------
        val mdOut = "cli/build/androidDoctor/markdown/report.md"
        timed("CLI Markdown export") {
            step("Exporting Markdown report...")
            exec {
                commandLine(
                    "./gradlew", ":cli:run",
                    "--args=--report $reportJson --md"
                )
                isIgnoreExitValue = true
            }.exitValue
        }
        assertExists(mdOut)

        // -----------------------------
        // Step 5 — PDF Export (Premium only)
        // -----------------------------
        val pdfOut = "cli/build/androidDoctor/pdf/report.pdf"
        timed("CLI PDF export") {
            step("Exporting PDF report...")
            exec {
                commandLine(
                    "./gradlew", ":cli:run",
                    "--args=--report $reportJson --pdf"
                )
                isIgnoreExitValue = true
            }.exitValue
        }
        assertExists(pdfOut)

        // -----------------------------
        // Step 6 — Auto-open Test (HTML)
        // -----------------------------
        timed("CLI --open test") {
            step("Testing auto-open flag (will not fail if OS cannot open)")
            exec {
                commandLine(
                    "./gradlew", ":cli:run",
                    "--args=--report $reportJson --html --open"
                )
                isIgnoreExitValue = true
            }.exitValue
        }
        success("--open flag executed without errors")

        banner("ALL STEPS COMPLETED SUCCESSFULLY")
        println("${GREEN}AndroidDoctor full E2E test passed!${RESET}")
    }
}
