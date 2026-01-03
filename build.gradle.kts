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
 * Enhanced AndroidDoctor end-to-end test task.
 */
tasks.register("doctorTest") {
    group = "verification"
    description = "End-to-end test: build plugin+CLI, generate report, run CLI with colors and timing."

    dependsOn(":plugin:build")
    dependsOn(":cli:build")

    doLast {
        // Colors
        val RED = "\u001B[31m"
        val GREEN = "\u001B[32m"
        val YELLOW = "\u001B[33m"
        val CYAN = "\u001B[36m"
        val RESET = "\u001B[0m"

        fun banner(msg: String) {
            println("\n$CYAN====================  $msg  ====================${RESET}\n")
        }

        fun step(msg: String) = println("$YELLOW→ $msg$RESET")
        fun success(msg: String) = println("$GREEN✔ $msg$RESET")
        fun fail(msg: String): Nothing {
            println("$RED✘ $msg$RESET")
            throw GradleException(msg)
        }

        fun timed(label: String, block: () -> Unit) {
            val start = System.currentTimeMillis()
            block()
            val duration = System.currentTimeMillis() - start
            println("$GREEN   (✓ Completed $label in ${duration}ms) $RESET\n")
        }

        banner("ANDROIDDOCTOR END-TO-END TEST")

        // Optional clean
        if (project.properties["clean"] == "true") {
            timed("clean") {
                step("Cleaning project (requested via -Pclean=true)")
                exec {
                    commandLine("bash", "-c", "./gradlew clean")
                }
                success("Project cleaned")
            }
        }

        // Step 1 — Build plugin & CLI (dependsOn ensures this)
        success("Plugin + CLI build completed")

        // Step 2 — Run androidDoctorCollect
        timed("androidDoctorCollect") {
            step("Running androidDoctorCollect inside samples/sample-app")
            val result = exec {
                workingDir = file("samples/sample-app")
                commandLine("../../gradlew", "androidDoctorCollect")
                isIgnoreExitValue = true
            }
            if (result.exitValue != 0) fail("androidDoctorCollect failed!")
            success("Report generated successfully")
        }

        // Step 3 — Run CLI on the json report
        timed("CLI run") {
            step("Running CLI to read generated report.json")
            val result = exec {
                commandLine(
                    "bash", "-c",
                    "./gradlew :cli:run --args=\"--report samples/sample-app/build/androidDoctor/report.json\""
                )
                isIgnoreExitValue = true
            }
            if (result.exitValue != 0) fail("CLI execution failed!")
            success("CLI executed successfully")
        }

        banner("ALL STEPS COMPLETED")
        println("${GREEN}AndroidDoctor E2E test successful.${RESET}")
    }
}
