package com.evolutiondso.androiddoctor.cli

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
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
    val checks: Checks? = null,
    val android: AndroidInfo? = null,
    val scores: ScoresInfo? = null,
    val actions: List<ActionInfo>? = null,
    val plugins: PluginsInfo? = null,
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

@Serializable
data class Checks(
    val isAndroidApplication: Boolean? = null,
    val isAndroidLibrary: Boolean? = null,
    val isAndroidProject: Boolean? = null,
    val usesKapt: Boolean? = null,
    val isRootProject: Boolean? = null,
    val moduleCount: Int? = null,
    val configurationCacheEnabled: Boolean? = null
)

@Serializable
data class AndroidInfo(
    val agpVersion: String? = null,
    val composeEnabled: Boolean? = null
)

@Serializable
data class ScoresInfo(
    val buildHealth: Int? = null,
    val modernization: Int? = null
)

@Serializable
data class ImpactInfo(
    val buildHealthDelta: Int? = null,
    val modernizationDelta: Int? = null
)

@Serializable
data class ActionInfo(
    val id: String? = null,
    val priority: Int? = null,
    val severity: String? = null, // HIGH | MEDIUM | LOW
    val effort: String? = null,   // S | M | L
    val title: String? = null,
    val why: String? = null,
    val how: String? = null,
    val impact: ImpactInfo? = null
)

@Serializable
data class PluginsInfo(
    val appliedKnownPluginIds: List<String>? = null
)

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = false
}

@OptIn(ExperimentalSerializationApi::class)
private val prettyJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    prettyPrintIndent = "  "
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
        printRawJsonPretty(content)
        return
    }

    printReportSummary(report)

    println()
    println("Raw JSON:")
    println("----------------------------------------------------")
    printRawJsonPretty(content)
    println("----------------------------------------------------")
}

private fun resolveReportPath(reportPathArg: String): Path {
    val candidate = Paths.get(reportPathArg)
    if (candidate.isAbsolute) return candidate.normalize()

    val repoRootProp = System.getProperty("androiddoctor.repoRoot")
    val repoRoot = if (!repoRootProp.isNullOrBlank()) Paths.get(repoRootProp) else Paths.get("").toAbsolutePath()
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

    val isAndroidProject = report.checks?.isAndroidProject == true
    val isAndroidApp = report.checks?.isAndroidApplication == true
    val isAndroidLib = report.checks?.isAndroidLibrary == true
    val usesKapt = report.checks?.usesKapt == true
    val isRootProject = report.checks?.isRootProject == true
    val moduleCount = report.checks?.moduleCount
    val configCacheEnabled = report.checks?.configurationCacheEnabled

    val targetType = when {
        isAndroidApp -> "Android Application"
        isAndroidLib -> "Android Library"
        isAndroidProject -> "Android (unknown type)"
        else -> "Non-Android (no AGP plugins detected)"
    }

    val kaptLabel = if (usesKapt) "Yes (annotation processing via kapt)" else "No"
    val moduleCountLabel = moduleCount?.toString() ?: "<unknown>"

    val structureAssessment = when {
        moduleCount == null -> "Unknown"
        moduleCount <= 1 -> "Monolith risk (single-module build)"
        moduleCount in 2..5 -> "Small modular build"
        moduleCount in 6..20 -> "Modular build"
        else -> "Large modular build"
    }

    val configCacheLabel = when (configCacheEnabled) {
        true -> "Enabled"
        false -> "Disabled"
        null -> "<unknown>"
    }

    val agpVersion = report.android?.agpVersion
    val composeEnabled = report.android?.composeEnabled

    val buildHealthScore = report.scores?.buildHealth
    val modernizationScore = report.scores?.modernization

    val knownPlugins = (report.plugins?.appliedKnownPluginIds).orEmpty()

    println("Project        : $projectName ($projectPath)")
    println("Generated At   : $generatedAt")
    println("Status         : $status")
    println("Target Type    : $targetType")
    println("Uses Kapt      : $kaptLabel")
    println("Is Root        : ${if (isRootProject) "Yes (this is the build root)" else "No (subproject)"}")
    println("Module Count   : $moduleCountLabel")
    println("Structure      : $structureAssessment")
    println("Config Cache   : $configCacheLabel")

    // Compose check surfaced (Android-only)
    if (isAndroidProject || agpVersion != null || composeEnabled != null) {
        println()
        println("Android")
        println("  AGP Version  : ${agpVersion ?: "<unknown>"}")
        val composeLabel = composeEnabled?.let { if (it) "Enabled" else "Disabled" } ?: "<unknown>"
        println("  Compose      : $composeLabel")
    }

    if (buildHealthScore != null || modernizationScore != null) {
        println()
        println("Scores")
        println("  Build Health : ${buildHealthScore ?: "<unknown>"} / 100")
        println("  Modernize    : ${modernizationScore ?: "<unknown>"} / 100")
    }

    val actions = report.actions.orEmpty()
    if (actions.isNotEmpty()) {
        println()
        println("Top Actions")

        val sorted = actions.sortedWith(
            compareBy<ActionInfo> { it.priority ?: Int.MAX_VALUE }.thenBy { it.id ?: "" }
        )

        var totalBuildDelta = 0
        var totalModernDelta = 0

        sorted.forEachIndexed { index, a ->
            val title = a.title ?: a.id ?: "Action"
            val priority = a.priority?.toString() ?: "?"
            val severity = a.severity ?: "?"
            val effort = a.effort ?: "?"
            val why = a.why ?: ""
            val how = a.how ?: ""

            val buildDelta = a.impact?.buildHealthDelta ?: 0
            val modernDelta = a.impact?.modernizationDelta ?: 0

            totalBuildDelta += buildDelta
            totalModernDelta += modernDelta

            val impactLabel = buildString {
                if (buildDelta != 0) append("${formatDelta(buildDelta)} Build Health")
                if (modernDelta != 0) {
                    if (isNotEmpty()) append(", ")
                    append("${formatDelta(modernDelta)} Modernize")
                }
            }.ifBlank { "no score estimate" }

            println("  ${index + 1}. [P$priority][$severity][$effort] $title ($impactLabel)")
            if (why.isNotBlank()) println("     Why: $why")
            if (how.isNotBlank()) println("     How: $how")
        }

        println()
        println("Estimated score gain (if completed):")
        println("  Build Health : ${formatDelta(totalBuildDelta)}")
        println("  Modernize    : ${formatDelta(totalModernDelta)}")

        // 7/30/90 roadmap derived from effort
        printRoadmap(sorted)
    }

    println()
    println("Tooling")
    println("  Gradle       : $gradleVersion")
    println("  Kotlin       : $kotlinVersion")
    println("  Plugin       : $pluginVersion")

    if (knownPlugins.isNotEmpty()) {
        println()
        println("Known Plugins")
        knownPlugins.forEach { id ->
            println("  - $id")
        }
    }

    val notes = report.notes
    if (!notes.isNullOrEmpty()) {
        println()
        println("Notes:")
        notes.forEach { note ->
            println("  - $note")
        }
    }
}

private fun printRoadmap(actions: List<ActionInfo>) {
    fun effortBucket(effort: String?): String = when (effort) {
        "S" -> "Next 7 days"
        "M" -> "Next 30 days"
        "L" -> "Next 90 days"
        else -> "Unscheduled"
    }

    val grouped = actions.groupBy { effortBucket(it.effort) }

    val order = listOf("Next 7 days", "Next 30 days", "Next 90 days", "Unscheduled")

    println()
    println("Suggested Roadmap (7/30/90)")
    println("---------------------------")

    order.forEach { bucket ->
        val items = grouped[bucket].orEmpty()
        if (items.isEmpty()) return@forEach

        println(bucket + ":")

        items.sortedWith(compareBy<ActionInfo> { it.priority ?: Int.MAX_VALUE }.thenBy { it.id ?: "" })
            .forEach { a ->
                val title = a.title ?: a.id ?: "Action"
                val severity = a.severity ?: "?"
                val effort = a.effort ?: "?"
                println("  - [$severity][$effort] $title")
            }

        println()
    }
}

private fun formatDelta(value: Int): String = if (value >= 0) "+$value" else value.toString()

private fun printRawJsonPretty(content: String) {
    val pretty = try {
        val element: JsonElement = Json.parseToJsonElement(content)
        prettyJson.encodeToString(element)
    } catch (_: Throwable) {
        content.trimIndent()
    }
    println(pretty)
}
