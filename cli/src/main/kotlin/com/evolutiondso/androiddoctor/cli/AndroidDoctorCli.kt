package com.evolutiondso.androiddoctor.cli

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

data class CliConfig(
    val reportPath: String?
)

@Serializable
data class AndroidDoctorReport(
    val schemaVersion: Int? = null,
    val generatedAt: String? = null,
    val project: ProjectInfo? = null,
    val tooling: ToolingInfo? = null,
    val status: String? = null,
    val notes: List<String>? = null
)

@Serializable
data class ProjectInfo(
    val name: String? = null,
    val path: String? = null
)

@Serializable
data class ToolingInfo(
    val gradleVersion: String? = null,
    val kotlinStdlibVersion: String? = null,
    val androidDoctorPluginVersion: String? = null
)

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = false
}

fun main(args: Array<String>) {
    val config = parseArgs(args.toList())

    println("AndroidDoctor CLI")
    println("================================")
    println()

    val reportPathArg = config.reportPath ?: "report.json"
    val path = resolveReportPath(reportPathArg)

    println("Looking for report at: $path")
    println()

    if (!Files.exists(path)) {
        println("❌ Report file not found.")
        println("   Make sure the Gradle plugin has generated a report.json first.")
        println("   Example: ./gradlew androidDoctorCollect")
        println()
        println("You can also pass a path explicitly:")
        println("   ./gradlew :cli:run --args=\"--report samples/sample-app/build/androidDoctor/report.json\"")
        return
    }

    val content = Files.readString(path)

    val report = try {
        json.decodeFromString<AndroidDoctorReport>(content)
    } catch (e: Exception) {
        println("⚠️  Failed to parse report.json as AndroidDoctorReport.")
        println("    Falling back to raw JSON output.")
        println()
        printRawJson(content)
        return
    }

    printReportSummary(report)
    println()
    println("Raw JSON:")
    println("----------------------------------------------------")
    println(content)
    println("----------------------------------------------------")
}

private fun resolveReportPath(reportPathArg: String): Path {
    val candidate = Paths.get(reportPathArg)

    // 1) If already absolute, use directly
    if (candidate.isAbsolute) {
        return candidate.normalize()
    }

    // 2) Look for the repo root passed from Gradle (--Dandroiddoctor.repoRoot)
    val repoRootProp = System.getProperty("androiddoctor.repoRoot")
    val repoRoot = if (!repoRootProp.isNullOrBlank()) {
        Paths.get(repoRootProp)
    } else {
        // fallback (less ideal, but safe)
        Paths.get("").toAbsolutePath()
    }

    return repoRoot.resolve(reportPathArg).normalize()
}

private fun parseArgs(args: List<String>): CliConfig {
    var reportPath: String? = null

    val iter = args.iterator()
    while (iter.hasNext()) {
        when (val arg = iter.next()) {
            "--report", "-r" -> {
                if (!iter.hasNext()) {
                    println("Error: --report requires a path.")
                    printUsage()
                    exitProcess(1)
                }
                reportPath = iter.next()
            }

            "--help", "-h" -> {
                printUsage()
                exitProcess(0)
            }

            else -> {
                println("Unknown argument: $arg")
                printUsage()
                exitProcess(1)
            }
        }
    }

    return CliConfig(reportPath = reportPath)
}

private fun printUsage() {
    println(
        """
        AndroidDoctor CLI

        Usage:
          ./gradlew :cli:run --args="--report samples/sample-app/build/androidDoctor/report.json"

        Options:
          -r, --report <path>   Path to report.json (repo-root-relative or absolute)
          -h, --help            Show this help message
        """.trimIndent()
    )
}

private fun printReportSummary(report: AndroidDoctorReport) {
    println("AndroidDoctor Report Summary")
    println("----------------------------")

    val projectName = report.project?.name ?: "<unknown>"
    val projectPath = report.project?.path ?: "<unknown>"
    val gradleVersion = report.tooling?.gradleVersion ?: "<unknown>"
    val kotlinVersion = report.tooling?.kotlinStdlibVersion ?: "<unknown>"
    val pluginVersion = report.tooling?.androidDoctorPluginVersion ?: "<unknown>"
    val generatedAt = report.generatedAt ?: "<unknown>"
    val status = report.status ?: "<unknown>"

    println("Project        : $projectName ($projectPath)")
    println("Generated At   : $generatedAt")
    println("Status         : $status")
    println()
    println("Tooling")
    println("  Gradle       : $gradleVersion")
    println("  Kotlin       : $kotlinVersion")
    println("  Plugin       : $pluginVersion")

    val notes = report.notes
    if (!notes.isNullOrEmpty()) {
        println()
        println("Notes:")
        notes.forEach { note ->
            println("  - $note")
        }
    }
}

private fun printRawJson(content: String) {
    println("Raw report.json:")
    println("----------------------------------------------------")
    println(content)
    println("----------------------------------------------------")
}
