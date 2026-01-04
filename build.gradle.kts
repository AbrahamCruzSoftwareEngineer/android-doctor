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

        banner("ANDROIDDOCTOR END-TO-END TEST")

        // Optional clean
        if (project.properties["clean"] == "true") {
            timed("clean") {
                step("Cleaning project (requested via -Pclean=true)")
                exec {
                    commandLine("./gradlew", "clean")
                }.exitValue
            }
            success("Project cleaned")
        }

        success("Plugin + CLI build completed")

        // Step 2 — Run androidDoctorCollect
        timed("androidDoctorCollect") {
            step("Running androidDoctorCollect in samples/sample-app")
            exec {
                workingDir = file("samples/sample-app")
                commandLine("../../gradlew", "androidDoctorCollect")
                isIgnoreExitValue = true
            }.exitValue
        }
        success("Report generated successfully")

        // Step 3 — Run CLI on report.json
        timed("CLI run") {
            step("Running CLI against generated report")
            exec {
                commandLine(
                    "./gradlew",
                    ":cli:run",
                    "--args=--report samples/sample-app/build/androidDoctor/report.json"
                )
                isIgnoreExitValue = true
            }.exitValue
        }

        banner("ALL STEPS COMPLETED")
        println("${GREEN}AndroidDoctor E2E test successful! 🎉$RESET")
    }
}
