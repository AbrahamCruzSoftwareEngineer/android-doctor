package com.evolutiondso.androiddoctor

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.time.Instant

abstract class AndroidDoctorCollectTask : DefaultTask() {

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @get:Internal
    abstract val metricsService: Property<BuildMetricsService>

    init {
        group = "verification"
        description = "Collect AndroidDoctor diagnostics (writes a basic report.json)."
    }

    @TaskAction
    fun run() {
        val file = reportFile.get().asFile
        file.parentFile.mkdirs()

        val now = Instant.now().toString()
        val projectName = project.name
        val projectPath = project.path
        val gradleVersion = project.gradle.gradleVersion
        val kotlinStdlibVersion = KotlinVersion.CURRENT.toString()
        val kotlinCompilerVersion = readKotlinCompilerVersionOrNull()
        val pluginVersion = ANDROID_DOCTOR_VERSION

        // Android detection
        val isAndroidApplication = project.plugins.hasPlugin("com.android.application")
        val isAndroidLibrary = project.plugins.hasPlugin("com.android.library")
        val isAndroidProject = isAndroidApplication || isAndroidLibrary

        // kapt usage
        val hasKaptPlugin =
            project.plugins.hasPlugin("org.jetbrains.kotlin.kapt") ||
                    project.plugins.hasPlugin("kotlin-kapt")

        val hasKaptConfiguration =
            project.configurations.names.any { name ->
                name.startsWith("kapt", ignoreCase = true)
            }

        val usesKapt = hasKaptPlugin || hasKaptConfiguration

        // Root + module count
        val isRootProject = (project == project.rootProject)
        val moduleCount = project.rootProject.allprojects.size

        // Config cache status
        val configurationCacheEnabled: Boolean? = readConfigurationCacheEnabledOrNull(project)

        // Android signals (safe reflection)
        val agpVersion = readAgpVersionOrNull()
        val composeEnabled = readComposeEnabledOrNull(project)
        val composeCompilerVersion = readComposeCompilerVersionOrNull(project)
        val composeMetricsEnabled = readComposeMetricsEnabledOrNull(project)
        val composeReportsEnabled = readComposeReportsEnabledOrNull(project)
        val compileSdkVersion = readCompileSdkOrNull(project)
        val javaToolchainVersion = readJavaToolchainVersionOrNull(project)
        val kotlinJvmTarget = readKotlinJvmTargetOrNull(project)
        val javaTargetCompatibility = readJavaTargetCompatibilityOrNull(project)
        val agpCompileTarget = readAgpCompileTargetCompatibilityOrNull(project)
        val agpCompileSource = readAgpCompileSourceCompatibilityOrNull(project)
        val mismatch =
            detectJvmTargetMismatch(kotlinJvmTarget, javaTargetCompatibility, agpCompileTarget)

        // Known plugins
        val knownPluginIds = listOf(
            "com.android.application",
            "com.android.library",
            "org.jetbrains.kotlin.android",
            "org.jetbrains.kotlin.jvm",
            "org.jetbrains.kotlin.plugin.serialization",
            "org.jetbrains.kotlin.kapt",
            "kotlin-kapt",
            "dagger.hilt.android.plugin",
            "com.google.dagger.hilt.android"
        )

        val appliedKnownPluginIds = knownPluginIds.filter { project.plugins.hasPlugin(it) }
        val appliedKnownPluginsJson = appliedKnownPluginIds.joinToString { "\"$it\"" }

        val buildMetrics = metricsService.orNull?.snapshot()
        val dependencyDiagnostics = collectDependencyDiagnostics(project)
        val moduleDiagnostics = collectModuleDiagnostics(project, buildMetrics)
        val annotationDiagnostics = collectAnnotationDiagnostics(project, buildMetrics)
        val environmentDiagnostics = collectEnvironmentDiagnostics()
        val architectureDiagnostics = ArchitectureAnalyzer().analyze(project)
        val testsDiagnostics = collectTestsDiagnostics(buildMetrics)
        val scores = computeScores(
            isAndroidProject = isAndroidProject,
            usesKapt = usesKapt,
            moduleCount = moduleCount,
            configurationCacheEnabled = configurationCacheEnabled,
            composeEnabled = composeEnabled,
            architectureDiagnostics = architectureDiagnostics
        )
        val configCacheRequested = readConfigurationCacheRequestedOrNull(project)

        // Recommended actions
        val actions = buildTopActions(
            moduleCount = moduleCount,
            usesKapt = usesKapt,
            configurationCacheEnabled = configurationCacheEnabled,
            configurationCacheRequested = configCacheRequested,
            isAndroidProject = isAndroidProject,
            composeEnabled = composeEnabled,
            composeCompilerVersion = composeCompilerVersion,
            composeMetricsEnabled = composeMetricsEnabled,
            composeReportsEnabled = composeReportsEnabled,
            toolchainMismatch = mismatch,
            javaToolchainVersion = javaToolchainVersion,
            kotlinJvmTarget = kotlinJvmTarget,
            javaTargetCompatibility = javaTargetCompatibility,
            agpVersion = agpVersion,
            compileSdkVersion = compileSdkVersion,
            dependencyDiagnostics = dependencyDiagnostics,
            annotationDiagnostics = annotationDiagnostics,
            moduleDiagnostics = moduleDiagnostics,
            buildMetrics = buildMetrics,
            environmentDiagnostics = environmentDiagnostics,
            architectureDiagnostics = architectureDiagnostics
        )

        val actionsJson = actionsToJson(actions)

        val agpVersionJson = agpVersion?.let { "\"$it\"" } ?: "null"
        val composeEnabledJson = composeEnabled?.toString() ?: "null"
        val configCacheJson = configurationCacheEnabled?.toString() ?: "null"

        // ---------- CLEAN JSON (NO LEADING INDENTATION) ----------
        val json = """
{
  "schemaVersion": 2,
  "generatedAt": "$now",
  "project": {
    "name": "$projectName",
    "path": "$projectPath"
  },
  "tooling": {
    "gradleVersion": "$gradleVersion",
    "kotlinStdlibVersion": "$kotlinStdlibVersion",
    "kotlinCompilerVersion": ${quote(kotlinCompilerVersion)},
    "androidDoctorPluginVersion": "$pluginVersion"
  },
  "status": "skeleton",
  "checks": {
    "isAndroidApplication": $isAndroidApplication,
    "isAndroidLibrary": $isAndroidLibrary,
    "isAndroidProject": $isAndroidProject,
    "usesKapt": $usesKapt,
    "isRootProject": $isRootProject,
    "moduleCount": $moduleCount,
    "configurationCacheEnabled": $configCacheJson
  },
  "android": {
    "agpVersion": $agpVersionJson,
    "composeEnabled": $composeEnabledJson,
    "compileSdk": ${quote(compileSdkVersion)},
    "composeCompilerVersion": ${quote(composeCompilerVersion)},
    "composeMetricsEnabled": ${composeMetricsEnabled?.toString() ?: "null"},
    "composeReportsEnabled": ${composeReportsEnabled?.toString() ?: "null"}
  },
  "scores": {
    "buildHealth": ${scores.buildHealth},
    "modernization": ${scores.modernization}
  },
  "performance": {
    "configurationMs": ${buildMetrics?.configurationDurationMs ?: "null"},
    "executionMs": ${buildMetrics?.executionDurationMs ?: "null"},
    "incrementalCompilation": ${buildMetrics?.incrementalCompilationUsed ?: false}
  },
  "cache": {
    "hits": ${buildMetrics?.cacheHits ?: 0},
    "misses": ${buildMetrics?.cacheMisses ?: 0}
  },
  "taskTimings": ${tasksToJson(buildMetrics?.taskDurations.orEmpty())},
  "modules": ${moduleSummariesToJson(moduleDiagnostics)},
  "compose": {
    "enabled": $composeEnabledJson,
    "compilerVersion": ${quote(composeCompilerVersion)},
    "metricsEnabled": ${composeMetricsEnabled?.toString() ?: "null"},
    "reportsEnabled": ${composeReportsEnabled?.toString() ?: "null"}
  },
  "dependencies": ${dependencyDiagnostics.toJson()},
  "environment": ${environmentDiagnostics.toJson()},
  "architecture": ${architectureDiagnostics.toJson()},
  "tests": ${testsDiagnostics.toJson()},
  "toolchain": {
    "javaToolchainVersion": ${quote(javaToolchainVersion)},
    "jvmTarget": ${quote(javaTargetCompatibility)},
    "kotlinJvmTarget": ${quote(kotlinJvmTarget)},
    "agpCompileTarget": ${quote(agpCompileTarget)},
    "agpCompileSource": ${quote(agpCompileSource)},
    "jvmTargetMismatch": ${mismatch?.toString() ?: "null"}
  },
  "diagnostics": {
    "configuration": {
      "durationMs": ${buildMetrics?.configurationDurationMs ?: "null"}
    },
    "execution": {
      "durationMs": ${buildMetrics?.executionDurationMs ?: "null"},
      "topLongestTasks": ${tasksToJson(buildMetrics?.topLongestTasks.orEmpty())}
    },
    "buildCache": {
      "enabled": ${project.gradle.startParameter.isBuildCacheEnabled},
      "hits": ${buildMetrics?.cacheHits ?: 0},
      "misses": ${buildMetrics?.cacheMisses ?: 0},
      "skipped": ${buildMetrics?.cacheSkipped ?: 0},
      "incrementalCompilationUsed": ${buildMetrics?.incrementalCompilationUsed ?: false}
    },
    "configurationCache": {
      "requested": ${configCacheRequested?.toString() ?: "null"},
      "stored": null,
      "reused": null,
      "incompatibleTasks": null
    }
  },
  "modulesDiagnostics": ${moduleDiagnostics.toJson()},
  "annotationProcessing": ${annotationDiagnostics.toJson()},
  "actions": $actionsJson,
  "plugins": {
    "appliedKnownPluginIds": [ $appliedKnownPluginsJson ]
  },
  "notes": [
    "This report is generated by the AndroidDoctor Gradle plugin skeleton.",
    "Fields and structure will evolve; do not rely on this format yet."
  ]
}
        """.trimIndent()

        // Pretty: add final newline so the terminal doesn't show "%".
        file.writeText(json + "\n")

        logger.lifecycle("AndroidDoctor: report written to ${file.absolutePath}")
    }
}

// ---------------------------------------------------------------------------
// Score Model
// ---------------------------------------------------------------------------

private data class Scores(
    val buildHealth: Int,
    val modernization: Int
)

private fun computeScores(
    isAndroidProject: Boolean,
    usesKapt: Boolean,
    moduleCount: Int,
    configurationCacheEnabled: Boolean?,
    composeEnabled: Boolean?,
    architectureDiagnostics: ArchitectureDiagnostics
): Scores {
    var build = 100

    when (configurationCacheEnabled) {
        true -> Unit
        false -> build -= 10
        null -> build -= 3
    }

    if (usesKapt) build -= 20
    if (moduleCount <= 1) build -= 10
    build = build.coerceIn(0, 100)

    var modern = 100

    when (configurationCacheEnabled) {
        true -> Unit
        false -> modern -= 5
        null -> modern -= 2
    }

    if (usesKapt) modern -= 10

    if (isAndroidProject) {
        when (composeEnabled) {
            true -> Unit
            false -> modern -= 10
            null -> modern -= 3
        }
    }

    if (architectureDiagnostics.mvvm > 0) {
        modern += 20
    }
    if (architectureDiagnostics.mvi > 0) {
        modern += 15
    }
    if (architectureDiagnostics.violations.any { it.type == "MissingDomainLayer" }) {
        modern -= 20
    }

    modern = modern.coerceIn(0, 100)

    return Scores(buildHealth = build, modernization = modern)
}

// ---------------------------------------------------------------------------
// Action Model
// ---------------------------------------------------------------------------

private data class Impact(val buildHealthDelta: Int, val modernizationDelta: Int)

private data class Action(
    val id: String,
    val priority: Int,
    val severity: String,
    val effort: String,
    val title: String,
    val why: String,
    val how: String,
    val impact: Impact
)

private fun buildTopActions(
    moduleCount: Int,
    usesKapt: Boolean,
    configurationCacheEnabled: Boolean?,
    configurationCacheRequested: Boolean?,
    isAndroidProject: Boolean,
    composeEnabled: Boolean?,
    composeCompilerVersion: String?,
    composeMetricsEnabled: Boolean?,
    composeReportsEnabled: Boolean?,
    toolchainMismatch: Boolean?,
    javaToolchainVersion: String?,
    kotlinJvmTarget: String?,
    javaTargetCompatibility: String?,
    agpVersion: String?,
    compileSdkVersion: String?,
    dependencyDiagnostics: DependencyDiagnostics,
    annotationDiagnostics: AnnotationDiagnostics,
    moduleDiagnostics: ModuleDiagnostics,
    buildMetrics: BuildMetricsSnapshot?,
    environmentDiagnostics: EnvironmentDiagnostics,
    architectureDiagnostics: ArchitectureDiagnostics
): List<Action> {

    val actions = mutableListOf<Action>()
    val executionMs = buildMetrics?.executionDurationMs
    val configMs = buildMetrics?.configurationDurationMs
    val cacheHits = buildMetrics?.cacheHits ?: 0
    val cacheMisses = buildMetrics?.cacheMisses ?: 0
    val cacheTotal = cacheHits + cacheMisses
    val cacheHitRate = if (cacheTotal > 0) cacheHits.toDouble() / cacheTotal else null
    val slowTasks = buildMetrics?.topLongestTasks.orEmpty().take(3)
    val lowRam = environmentDiagnostics.availableRamMb < 8192
    val hasTimingData = executionMs != null || configMs != null || buildMetrics?.topLongestTasks?.isNotEmpty() == true
    val hasCacheStats = cacheTotal > 0

    if (moduleCount <= 1 && (executionMs ?: 0) > 120_000) {
        actions += Action(
            id = "MODULARIZE_MONOLITH",
            priority = 1,
            severity = "HIGH",
            effort = "L",
            title = "Modularize to improve build scalability",
            why = "Long execution times and a single module reduce parallelism and cache reuse.",
            how = "Introduce :core and :feature modules, then move high-churn code first. Start with compile-time boundaries.",
            impact = Impact(18, 6)
        )
    }

    // Configuration cache
    when (configurationCacheEnabled) {
        false -> actions += Action(
            id = "ENABLE_CONFIGURATION_CACHE",
            priority = if ((configMs ?: 0) > 30_000) 1 else 2,
            severity = if ((configMs ?: 0) > 60_000) "HIGH" else "MEDIUM",
            effort = "S",
            title = "Enable Gradle configuration cache",
            why = "Configuration time is ${configMs?.let { "${it} ms" } ?: "unknown"}; cache reuse can cut it drastically.",
            how = "Enable org.gradle.configuration-cache=true, then fix reported incompatibilities and re-run to verify reuse.",
            impact = Impact(if ((configMs ?: 0) > 60_000) 16 else 10, 6)
        )

        null -> actions += Action(
            id = "VERIFY_CONFIGURATION_CACHE",
            priority = 2,
            severity = "MEDIUM",
            effort = "S",
            title = "Verify configuration cache support",
            why = "AndroidDoctor could not determine if configuration cache is enabled for this build.",
            how = "Try enabling it and confirming it’s effective; then consider opting in via gradle.properties.",
            impact = Impact(3, 2)
        )

        true -> Unit
    }

    if (configurationCacheRequested == false) {
        actions += Action(
            id = "REQUEST_CONFIGURATION_CACHE",
            priority = 2,
            severity = if ((configMs ?: 0) > 30_000) "MEDIUM" else "LOW",
            effort = "S",
            title = "Request configuration cache",
            why = "Configuration cache is not requested; configuration time may be higher than necessary.",
            how = "Enable org.gradle.configuration-cache=true and validate reuse on subsequent builds.",
            impact = Impact(if ((configMs ?: 0) > 30_000) 8 else 4, 4)
        )
    }

    if (configurationCacheRequested == true && configurationCacheEnabled != true) {
        actions += Action(
            id = "CONFIGURATION_CACHE_NOT_REUSED",
            priority = 1,
            severity = "HIGH",
            effort = "M",
            title = "Configuration cache requested but not reused",
            why = "The build asked for configuration cache, but it was not reused. This can hide configuration bottlenecks.",
            how = "Review the configuration cache report, fix incompatible tasks, and re-run the build to verify reuse.",
            impact = Impact(12, 6)
        )
    }

    if (configurationCacheRequested == true && configurationCacheEnabled == true) {
        actions += Action(
            id = "VERIFY_CONFIGURATION_CACHE_REUSE",
            priority = 2,
            severity = "MEDIUM",
            effort = "S",
            title = "Verify configuration cache reuse",
            why = "Configuration cache is enabled but reuse metrics are missing from the report.",
            how = "Run with --configuration-cache and inspect the reuse summary for incompatible tasks.",
            impact = Impact(6, 4)
        )
    }

    if (!hasTimingData) {
        actions += Action(
            id = "ENABLE_BUILD_SCAN",
            priority = 2,
            severity = "MEDIUM",
            effort = "S",
            title = "Capture build timing data",
            why = "Timing data is missing, limiting optimization accuracy.",
            how = "Run with --scan or use Gradle Profiler; ensure builds run with --profile for detailed task durations.",
            impact = Impact(3, 2)
        )
    }

    if (!hasCacheStats) {
        actions += Action(
            id = "COLLECT_BUILD_CACHE_STATS",
            priority = 2,
            severity = "MEDIUM",
            effort = "S",
            title = "Collect build cache statistics",
            why = "Build cache hit/miss data is missing, which hides cache efficiency.",
            how = "Enable build cache and run a clean build followed by an incremental build to observe hit rates.",
            impact = Impact(4, 2)
        )
    }

    if (executionMs != null && executionMs > 180_000 && slowTasks.isNotEmpty()) {
        val taskList = slowTasks.joinToString(", ") { it.path }
        actions += Action(
            id = "OPTIMIZE_LONG_TASKS",
            priority = 1,
            severity = "HIGH",
            effort = "M",
            title = "Optimize slow tasks: $taskList",
            why = "Execution time is ${executionMs} ms and these tasks dominate the build.",
            how = "Inspect task inputs/outputs, enable incremental compilation, and validate cacheability for the listed tasks.",
            impact = Impact(14, 5)
        )
    }

    if (cacheHitRate != null && cacheHitRate < 0.4) {
        val severity = if (cacheHitRate < 0.2) "HIGH" else "MEDIUM"
        actions += Action(
            id = "IMPROVE_BUILD_CACHE_HIT_RATE",
            priority = if (severity == "HIGH") 1 else 2,
            severity = severity,
            effort = "M",
            title = "Improve build cache hit rate",
            why = "Cache hit rate is ${(cacheHitRate * 100).toInt()}%, leading to repeated work.",
            how = if (environmentDiagnostics.ci) {
                "Enable remote build cache, verify cache push/pull, and align CI caches across agents."
            } else {
                "Enable local/remote build cache and check cacheability warnings for tasks with misses."
            },
            impact = Impact(if (severity == "HIGH") 12 else 8, 4)
        )
    }

    if (buildMetrics?.incrementalCompilationUsed == false && executionMs != null && executionMs > 120_000) {
        actions += Action(
            id = "ENABLE_INCREMENTAL_COMPILATION",
            priority = 2,
            severity = "MEDIUM",
            effort = "S",
            title = "Enable incremental compilation",
            why = "Incremental compilation is off and execution time is high.",
            how = "Ensure Kotlin incremental compilation is enabled and avoid disabling it in compiler flags.",
            impact = Impact(8, 4)
        )
    }

    if (buildMetrics?.incrementalCompilationUsed == null && executionMs != null && executionMs > 90_000) {
        actions += Action(
            id = "VERIFY_INCREMENTAL_COMPILATION",
            priority = 3,
            severity = "LOW",
            effort = "S",
            title = "Verify incremental compilation status",
            why = "Incremental compilation status is unknown; execution time is elevated.",
            how = "Confirm kotlin.incremental=true and inspect Kotlin compile task logs for incremental status.",
            impact = Impact(4, 2)
        )
    }

    // kapt → KSP
    if (usesKapt) {
        actions += Action(
            id = "MIGRATE_KAPT_TO_KSP",
            priority = 2,
            severity = "MEDIUM",
            effort = "M",
            title = "Consider migrating kapt to KSP",
            why = "kapt can slow builds due to Java stub generation and annotation processing overhead.",
            how = "Where supported, migrate libraries to KSP and remove kapt usage incrementally.",
            impact = Impact(20, 10)
        )
    }

    val kspCandidates = annotationDiagnostics.processors.filter { processor ->
        val lower = processor.lowercase()
        lower.contains("room") ||
            lower.contains("moshi") ||
            lower.contains("dagger") ||
            lower.contains("hilt")
    }

    if (kspCandidates.isNotEmpty()) {
        actions += Action(
            id = "KSP_SUPPORTED_PROCESSORS",
            priority = 2,
            severity = "MEDIUM",
            effort = "M",
            title = "Prioritize KSP migrations for supported processors",
            why = "Detected processors with KSP support: ${kspCandidates.joinToString(", ")}.",
            how = "Evaluate KSP migration guides for these processors and migrate incrementally to reduce kapt overhead.",
            impact = Impact(12, 8)
        )
    }

    // Compose detection
    if (isAndroidProject) {
        when (composeEnabled) {
            false -> actions += Action(
                id = "EVALUATE_COMPOSE_ADOPTION",
                priority = 3,
                severity = "LOW",
                effort = "M",
                title = "Evaluate adopting Jetpack Compose for new UI",
                why = "Compose can improve UI iteration speed and reduce XML complexity over time.",
                how = "Start with new screens or isolated components; keep migration incremental and measurable.",
                impact = Impact(0, 10)
            )

            null -> actions += Action(
                id = "DETECT_COMPOSE_CONFIGURATION",
                priority = 3,
                severity = "LOW",
                effort = "S",
                title = "Confirm Compose configuration",
                why = "AndroidDoctor could not determine whether Compose is enabled.",
                how = "Check android.buildFeatures.compose and your Compose compiler configuration.",
                impact = Impact(0, 3)
            )

            true -> Unit
        }
    }

    if (dependencyDiagnostics.outdated.isNotEmpty()) {
        actions += Action(
            id = "UPGRADE_OUTDATED_DEPENDENCIES",
            priority = 2,
            severity = if (dependencyDiagnostics.outdated.size > 5) "HIGH" else "MEDIUM",
            effort = "M",
            title = "Upgrade outdated dependencies",
            why = "Detected ${dependencyDiagnostics.outdated.size} outdated libraries, which can block modernization.",
            how = "Review the outdated list, prioritize core libraries, and upgrade versions in batches with CI validation.",
            impact = Impact(if (dependencyDiagnostics.outdated.size > 5) 10 else 6, 8)
        )
    }

    if (dependencyDiagnostics.duplicates.isNotEmpty()) {
        actions += Action(
            id = "CONSOLIDATE_DUPLICATE_DEPENDENCIES",
            priority = 2,
            severity = "MEDIUM",
            effort = "S",
            title = "Consolidate duplicate dependency versions",
            why = "Duplicate versions inflate build time and can cause runtime conflicts.",
            how = "Align dependency versions using version catalogs or resolution strategies.",
            impact = Impact(5, 4)
        )
    }

    if (dependencyDiagnostics.unused.isNotEmpty()) {
        actions += Action(
            id = "REMOVE_UNUSED_DEPENDENCIES",
            priority = 3,
            severity = "LOW",
            effort = "S",
            title = "Remove unused dependencies",
            why = "Unused dependencies add unnecessary build overhead.",
            how = "Validate unused dependencies and remove them from configurations where safe.",
            impact = Impact(4, 3)
        )
    }

    if (dependencyDiagnostics.heavy.isNotEmpty()) {
        val heaviest = dependencyDiagnostics.heavy.maxByOrNull { it.sizeBytes }
        actions += Action(
            id = "REPLACE_HEAVY_ARTIFACTS",
            priority = 3,
            severity = if ((heaviest?.sizeBytes ?: 0) > 25L * 1024 * 1024) "MEDIUM" else "LOW",
            effort = "M",
            title = "Evaluate heavy artifacts",
            why = "Large artifacts can slow dependency resolution and increase build times. Largest: ${heaviest?.group}:${heaviest?.name}.",
            how = "Consider lighter alternatives, remove unused transitive artifacts, or split heavy features behind dynamic delivery modules.",
            impact = Impact(6, 3)
        )
    }

    val compilerEmbeddable = dependencyDiagnostics.heavy.firstOrNull { artifact ->
        artifact.name.contains("kotlin-compiler-embeddable", ignoreCase = true)
    }
    if (compilerEmbeddable != null) {
        val compilerCount = dependencyDiagnostics.heavy.count {
            it.name.contains("kotlin-compiler-embeddable", ignoreCase = true)
        }
        actions += Action(
            id = "REMOVE_COMPILER_EMBEDDABLE",
            priority = 1,
            severity = if (compilerCount > 1) "HIGH" else "MEDIUM",
            effort = "S",
            title = "Remove kotlin-compiler-embeddable from runtime dependencies",
            why = "kotlin-compiler-embeddable appears ${compilerCount}x and is a large artifact that should not be on runtime classpaths.",
            how = "Move it to buildscript classpath or remove it; use the Kotlin Gradle plugin instead of embedding compiler jars.",
            impact = Impact(if (compilerCount > 1) 10 else 6, 4)
        )
    }

    if (moduleDiagnostics.modules.size >= 8 &&
        moduleDiagnostics.dependencies.size > moduleDiagnostics.modules.size * 2
    ) {
        actions += Action(
            id = "REDUCE_MODULE_COUPLING",
            priority = 2,
            severity = "MEDIUM",
            effort = "M",
            title = "Reduce module coupling",
            why = "Dense module dependencies can slow incremental builds and reduce cache effectiveness.",
            how = "Identify highly connected modules and introduce API boundaries or split shared utilities.",
            impact = Impact(8, 4)
        )
    }

    if (toolchainMismatch == true) {
        actions += Action(
            id = "ALIGN_TOOLCHAIN_TARGETS",
            priority = 1,
            severity = "HIGH",
            effort = "S",
            title = "Align Java/Kotlin target versions",
            why = "Detected mismatched JVM targets (Java: ${javaTargetCompatibility ?: "?"}, Kotlin: ${kotlinJvmTarget ?: "?"}).",
            how = "Align kotlinOptions.jvmTarget with compileOptions.targetCompatibility and toolchain version ($javaToolchainVersion).",
            impact = Impact(10, 6)
        )
    }

    if (isAndroidProject && agpVersion.isNullOrBlank()) {
        actions += Action(
            id = "DETECT_AGP_VERSION",
            priority = 2,
            severity = "MEDIUM",
            effort = "S",
            title = "Ensure AGP version is detected",
            why = "AGP version was not detected, which limits compatibility guidance.",
            how = "Verify the Android Gradle Plugin is applied in the root build and that build logic is not hiding the version.",
            impact = Impact(4, 4)
        )
    }

    if (isAndroidProject && compileSdkVersion.isNullOrBlank()) {
        actions += Action(
            id = "SET_COMPILE_SDK",
            priority = 2,
            severity = "MEDIUM",
            effort = "S",
            title = "Set compileSdk for Android modules",
            why = "compileSdk was not detected; this can block modern Android APIs and tooling alignment.",
            how = "Define compileSdk in the Android block for all modules and keep it aligned with AGP recommendations.",
            impact = Impact(5, 6)
        )
    }

    if (composeEnabled == true && composeCompilerVersion.isNullOrBlank()) {
        actions += Action(
            id = "CONFIGURE_COMPOSE_COMPILER",
            priority = 2,
            severity = "MEDIUM",
            effort = "S",
            title = "Configure Compose compiler version",
            why = "Compose is enabled but compiler version was not detected.",
            how = "Set kotlinCompilerExtensionVersion in composeOptions to match your Compose BOM.",
            impact = Impact(4, 6)
        )
    }

    if (composeEnabled == true && composeMetricsEnabled != true) {
        actions += Action(
            id = "ENABLE_COMPOSE_METRICS",
            priority = 3,
            severity = "LOW",
            effort = "S",
            title = "Enable Compose compiler metrics",
            why = "Compose metrics are disabled; insights on recomposition cost are missing.",
            how = "Enable compose.compiler.metricsDestination to generate metrics for performance reviews.",
            impact = Impact(2, 3)
        )
    }

    if (composeEnabled == true && composeReportsEnabled != true) {
        actions += Action(
            id = "ENABLE_COMPOSE_REPORTS",
            priority = 3,
            severity = "LOW",
            effort = "S",
            title = "Enable Compose compiler reports",
            why = "Compose reports are disabled; stability and restartability signals are missing.",
            how = "Enable compose.compiler.reportsDestination to generate compose compiler reports.",
            impact = Impact(2, 2)
        )
    }

    if (annotationDiagnostics.processors.size > 5 && usesKapt) {
        actions += Action(
            id = "REDUCE_PROCESSOR_OVERHEAD",
            priority = 2,
            severity = "MEDIUM",
            effort = "M",
            title = "Reduce annotation processor overhead",
            why = "Detected ${annotationDiagnostics.processors.size} processors; kapt overhead can be significant.",
            how = "Consolidate processors, remove unused ones, and migrate compatible processors to KSP.",
            impact = Impact(10, 6)
        )
    }

    if (lowRam && environmentDiagnostics.ci) {
        actions += Action(
            id = "INCREASE_CI_MEMORY",
            priority = 2,
            severity = "MEDIUM",
            effort = "M",
            title = "Increase CI memory or reduce parallelism",
            why = "CI agent has ${environmentDiagnostics.availableRamMb} MB RAM, which can slow Kotlin compilation.",
            how = "Provision larger CI agents or tune org.gradle.workers.max and Kotlin daemon settings.",
            impact = Impact(6, 2)
        )
    }

    if (architectureDiagnostics.violations.any { it.type == "MissingDomainLayer" }) {
        actions += Action(
            id = "INTRODUCE_DOMAIN_LAYER",
            priority = 1,
            severity = "HIGH",
            effort = "M",
            title = "Introduce Domain Layer",
            why = "A domain layer is missing, which increases coupling between UI and data layers.",
            how = "Add a domain module with use-cases and interfaces; migrate UI to depend on domain contracts.",
            impact = Impact(6, 12)
        )
    }

    if (architectureDiagnostics.violations.any { it.type == "GodActivity" }) {
        actions += Action(
            id = "BREAK_GOD_ACTIVITY",
            priority = 1,
            severity = "HIGH",
            effort = "M",
            title = "Break God Activity into ViewModel + UI state",
            why = "Oversized Activities/Fragments reduce maintainability and slow down feature delivery.",
            how = "Extract UI state to ViewModels and move business logic to use-cases or repositories.",
            impact = Impact(8, 10)
        )
    }

    if (architectureDiagnostics.violations.any { it.type == "RepositoryReturnsDTO" }) {
        actions += Action(
            id = "REMOVE_DTOS_FROM_UI",
            priority = 2,
            severity = "MEDIUM",
            effort = "S",
            title = "Remove DTOs from UI",
            why = "Repositories are returning DTOs directly, leaking data-layer details to UI.",
            how = "Map DTOs to domain models in repositories and expose only domain entities to UI.",
            impact = Impact(4, 8)
        )
    }

    if (architectureDiagnostics.violations.any { it.type == "ModuleCoupling" }) {
        actions += Action(
            id = "DECOUPLE_MODULES",
            priority = 2,
            severity = "MEDIUM",
            effort = "M",
            title = "Decouple modules via interfaces",
            why = "Feature modules depend on :app, which limits reuse and increases build coupling.",
            how = "Move shared contracts to a core/domain module and invert dependencies.",
            impact = Impact(6, 6)
        )
    }

    if (architectureDiagnostics.violations.any { it.type == "ArchitectureInconsistency" }) {
        actions += Action(
            id = "STANDARDIZE_ARCHITECTURE",
            priority = 2,
            severity = "MEDIUM",
            effort = "M",
            title = "Standardize architecture pattern (choose MVVM or MVI)",
            why = "Multiple patterns are present, which increases maintenance overhead and onboarding cost.",
            how = "Pick a primary pattern and migrate remaining modules incrementally.",
            impact = Impact(4, 8)
        )
    }

    return actions
        .sortedWith(compareBy<Action> { it.priority }.thenBy { it.id })
        .take(5)
}

private fun actionsToJson(actions: List<Action>): String {
    if (actions.isEmpty()) return "[]"

    fun esc(s: String): String =
        s.replace("\\", "\\\\").replace("\"", "\\\"")

    val items = actions.joinToString(",\n") { a ->
        """
        {
          "id": "${esc(a.id)}",
          "priority": ${a.priority},
          "severity": "${esc(a.severity)}",
          "effort": "${esc(a.effort)}",
          "title": "${esc(a.title)}",
          "why": "${esc(a.why)}",
          "how": "${esc(a.how)}",
          "impact": {
            "buildHealthDelta": ${a.impact.buildHealthDelta},
            "modernizationDelta": ${a.impact.modernizationDelta}
          }
        }
        """.trimIndent()
    }

    return "[\n$items\n]"
}

private fun tasksToJson(tasks: List<TaskTiming>): String {
    if (tasks.isEmpty()) return "[]"

    fun esc(s: String): String =
        s.replace("\\", "\\\\").replace("\"", "\\\"")

    val items = tasks.joinToString(",\n") { task ->
        """
        {
          "path": "${esc(task.path)}",
          "projectPath": "${esc(task.projectPath)}",
          "durationMs": ${task.durationMs},
          "didWork": ${task.didWork},
          "skipped": ${task.skipped},
          "skipMessage": ${task.skipMessage?.let { "\"${esc(it)}\"" } ?: "null"}
        }
        """.trimIndent()
    }

    return "[\n$items\n]"
}

private fun quote(value: String?): String = value?.let { "\"${esc(it)}\"" } ?: "null"

private fun esc(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"")

// ---------------------------------------------------------------------------
// Reflection helpers
// ---------------------------------------------------------------------------

private fun readAgpVersionOrNull(): String? {
    return try {
        val clazz = Class.forName("com.android.Version")
        val field = clazz.getField("ANDROID_GRADLE_PLUGIN_VERSION")
        field[null]?.toString()
    } catch (_: Throwable) {
        null
    }
}

private fun readComposeEnabledOrNull(project: Project): Boolean? {
    val androidExt = project.extensions.findByName("android") ?: return null

    return try {
        val getBuildFeatures =
            androidExt.javaClass.methods.firstOrNull { it.name == "getBuildFeatures" }
                ?: return null

        val buildFeatures = getBuildFeatures.invoke(androidExt) ?: return null

        val getCompose =
            buildFeatures.javaClass.methods.firstOrNull { it.name == "getCompose" }
                ?: return null

        getCompose.invoke(buildFeatures) as? Boolean
    } catch (_: Throwable) {
        null
    }
}

private fun readComposeCompilerVersionOrNull(project: Project): String? {
    val androidExt = project.extensions.findByName("android") ?: return null

    return try {
        val getComposeOptions =
            androidExt.javaClass.methods.firstOrNull { it.name == "getComposeOptions" }
                ?: return null
        val composeOptions = getComposeOptions.invoke(androidExt) ?: return null
        val getCompilerVersion =
            composeOptions.javaClass.methods.firstOrNull { it.name == "getKotlinCompilerExtensionVersion" }
                ?: return null
        getCompilerVersion.invoke(composeOptions) as? String
    } catch (_: Throwable) {
        null
    }
}

private fun readComposeMetricsEnabledOrNull(project: Project): Boolean? {
    return readComposeCompilerFlag(project, "metricsDestination")
}

private fun readComposeReportsEnabledOrNull(project: Project): Boolean? {
    return readComposeCompilerFlag(project, "reportsDestination")
}

private fun readComposeCompilerFlag(project: Project, flag: String): Boolean? {
    return try {
        val task = findKotlinCompileTask(project) ?: return null
        val kotlinOptions = task.javaClass.methods.firstOrNull { it.name == "getKotlinOptions" }
            ?.invoke(task) ?: return null
        val argsMethod = kotlinOptions.javaClass.methods.firstOrNull { it.name == "getFreeCompilerArgs" }
            ?: return null
        val args = argsMethod.invoke(kotlinOptions) as? Iterable<*> ?: return null
        args.any { it?.toString()?.contains(flag) == true }
    } catch (_: Throwable) {
        null
    }
}

private fun readCompileSdkOrNull(project: Project): String? {
    val androidExt = project.extensions.findByName("android") ?: return null
    return try {
        val method = androidExt.javaClass.methods.firstOrNull { it.name == "getCompileSdkVersion" }
            ?: androidExt.javaClass.methods.firstOrNull { it.name == "getCompileSdk" }
            ?: return null
        method.invoke(androidExt)?.toString()
    } catch (_: Throwable) {
        null
    }
}

private fun readJavaToolchainVersionOrNull(project: Project): String? {
    val javaExtension = project.extensions.findByType(JavaPluginExtension::class.java) ?: return null
    return javaExtension.toolchain.languageVersion.orNull?.toString()
}

private fun readKotlinCompilerVersionOrNull(): String? {
    return try {
        val clazz = Class.forName("org.jetbrains.kotlin.config.KotlinCompilerVersion")
        val field = clazz.getField("VERSION")
        field[null]?.toString()
    } catch (_: Throwable) {
        null
    }
}

private fun readKotlinJvmTargetOrNull(project: Project): String? {
    return try {
        val task = findKotlinCompileTask(project) ?: return null
        val kotlinOptions = task.javaClass.methods.firstOrNull { it.name == "getKotlinOptions" }
            ?.invoke(task) ?: return null
        val jvmTargetMethod = kotlinOptions.javaClass.methods.firstOrNull { it.name == "getJvmTarget" }
            ?: return null
        jvmTargetMethod.invoke(kotlinOptions)?.toString()
    } catch (_: Throwable) {
        null
    }
}

private fun findKotlinCompileTask(project: Project): Any? {
    return project.tasks.firstOrNull { it.javaClass.name.contains("KotlinCompile") }
}

private fun readJavaTargetCompatibilityOrNull(project: Project): String? {
    return try {
        val task = project.tasks.withType(org.gradle.api.tasks.compile.JavaCompile::class.java)
            .firstOrNull() ?: return null
        task.targetCompatibility
    } catch (_: Throwable) {
        null
    }
}

private fun readAgpCompileTargetCompatibilityOrNull(project: Project): String? {
    val androidExt = project.extensions.findByName("android") ?: return null
    return try {
        val method = androidExt.javaClass.methods.firstOrNull { it.name == "getCompileOptions" }
            ?: return null
        val compileOptions = method.invoke(androidExt) ?: return null
        val targetMethod =
            compileOptions.javaClass.methods.firstOrNull { it.name == "getTargetCompatibility" }
                ?: return null
        targetMethod.invoke(compileOptions)?.toString()
    } catch (_: Throwable) {
        null
    }
}

private fun readAgpCompileSourceCompatibilityOrNull(project: Project): String? {
    val androidExt = project.extensions.findByName("android") ?: return null
    return try {
        val method = androidExt.javaClass.methods.firstOrNull { it.name == "getCompileOptions" }
            ?: return null
        val compileOptions = method.invoke(androidExt) ?: return null
        val sourceMethod =
            compileOptions.javaClass.methods.firstOrNull { it.name == "getSourceCompatibility" }
                ?: return null
        sourceMethod.invoke(compileOptions)?.toString()
    } catch (_: Throwable) {
        null
    }
}

private fun detectJvmTargetMismatch(
    kotlinTarget: String?,
    javaTarget: String?,
    agpTarget: String?
): Boolean? {
    val targets = listOfNotNull(kotlinTarget, javaTarget, agpTarget).map { it.trim() }.distinct()
    if (targets.isEmpty()) return null
    return targets.size > 1
}


private fun readConfigurationCacheEnabledOrNull(project: Project): Boolean? {
    return try {
        val gradle = project.gradle

        val getBuildFeatures =
            gradle.javaClass.methods.firstOrNull { it.name == "getBuildFeatures" }
                ?: return null

        val buildFeatures = getBuildFeatures.invoke(gradle) ?: return null

        val getConfigurationCache =
            buildFeatures.javaClass.methods.firstOrNull { it.name == "getConfigurationCache" }
                ?: return null

        val configCacheFeature = getConfigurationCache.invoke(buildFeatures) ?: return null

        val enabledMethod =
            configCacheFeature.javaClass.methods.firstOrNull {
                it.parameterCount == 0 &&
                        (it.name == "isEnabled" || it.name == "getEnabled")
            } ?: return null

        enabledMethod.invoke(configCacheFeature) as? Boolean
    } catch (_: Throwable) {
        null
    }
}

private fun readConfigurationCacheRequestedOrNull(project: Project): Boolean? {
    return try {
        val startParameter = project.gradle.startParameter
        val method = startParameter.javaClass.methods.firstOrNull { it.name == "isConfigurationCacheRequested" }
            ?: return null
        method.invoke(startParameter) as? Boolean
    } catch (_: Throwable) {
        null
    }
}

// ---------------------------------------------------------------------------
// Diagnostics collection
// ---------------------------------------------------------------------------

private data class DependencyDuplicate(val group: String, val name: String, val versions: List<String>)
private data class DependencyOutdated(val group: String, val name: String, val currentVersion: String, val latestVersion: String)
private data class DependencyUnused(val group: String, val name: String, val version: String?, val configuration: String)
private data class DependencyHeavy(val group: String, val name: String, val version: String?, val sizeBytes: Long)

private data class DependencyDiagnostics(
    val duplicates: List<DependencyDuplicate>,
    val outdated: List<DependencyOutdated>,
    val unused: List<DependencyUnused>,
    val heavy: List<DependencyHeavy>
) {
    fun toJson(): String {
        return """
        {
          "duplicates": ${duplicatesToJson(duplicates)},
          "outdated": ${outdatedToJson(outdated)},
          "unused": ${unusedToJson(unused)},
          "heavy": ${heavyToJson(heavy)}
        }
        """.trimIndent()
    }
}

private data class ModuleDiagnostics(
    val modules: List<ModuleInfo>,
    val dependencies: List<ModuleDependency>
) {
    fun toJson(): String {
        return """
        {
          "count": ${modules.size},
          "modules": ${modulesToJson(modules)},
          "dependencies": ${moduleDepsToJson(dependencies)}
        }
        """.trimIndent()
    }
}

private data class ModuleInfo(
    val path: String,
    val taskCount: Int,
    val executionMs: Long?,
    val usesKapt: Boolean,
    val buildCacheEnabled: Boolean
)

private data class ModuleDependency(val from: String, val to: String)

private data class AnnotationDiagnostics(
    val processors: List<String>,
    val totalProcessingMs: Long?,
    val kaptStubGenerationMs: Long?
) {
    fun toJson(): String {
        val processorsJson = processors.joinToString(prefix = "[", postfix = "]") { "\"${esc(it)}\"" }
        return """
        {
          "processors": $processorsJson,
          "totalProcessingMs": ${totalProcessingMs ?: "null"},
          "kaptStubGenerationMs": ${kaptStubGenerationMs ?: "null"}
        }
        """.trimIndent()
    }
}

private data class EnvironmentDiagnostics(
    val os: String,
    val arch: String,
    val ci: Boolean,
    val availableRamMb: Long
) {
    fun toJson(): String {
        return """
        {
          "os": "${esc(os)}",
          "arch": "${esc(arch)}",
          "ci": $ci,
          "availableRamMb": $availableRamMb
        }
        """.trimIndent()
    }
}

private data class TestsDiagnostics(
    val total: Int,
    val passed: Int,
    val failed: Int,
    val skipped: Int,
    val durationMs: Long?,
    val uiTestDurationMs: Long?,
    val slowest: List<TestTiming>,
    val failures: List<TestFailure>
) {
    fun toJson(): String {
        return """
        {
          "total": $total,
          "passed": $passed,
          "failed": $failed,
          "skipped": $skipped,
          "durationMs": ${durationMs ?: "null"},
          "uiTestDurationMs": ${uiTestDurationMs ?: "null"},
          "slowest": ${slowestTestsToJson(slowest)},
          "failures": ${testFailuresToJson(failures)}
        }
        """.trimIndent()
    }
}


private fun collectDependencyDiagnostics(project: Project): DependencyDiagnostics {
    val duplicates = mutableListOf<DependencyDuplicate>()
    val outdated = mutableListOf<DependencyOutdated>()
    val unused = mutableListOf<DependencyUnused>()
    val heavy = mutableListOf<DependencyHeavy>()
    val resolvedVersions = mutableMapOf<String, MutableSet<String>>()

    project.rootProject.allprojects.forEach { module ->
        module.configurations.filter { it.isCanBeResolved }.forEach { configuration ->
            val declared = configuration.dependencies
                .filterIsInstance<org.gradle.api.artifacts.ExternalModuleDependency>()
                .mapNotNull { dep ->
                    val group = dep.group ?: return@mapNotNull null
                    val name = dep.name
                    val version = dep.version
                    Triple(group, name, version)
                }

            val resolved = configuration.incoming.resolutionResult.allComponents
                .mapNotNull { component ->
                    val id = component.id as? org.gradle.api.artifacts.component.ModuleComponentIdentifier
                        ?: return@mapNotNull null
                    Triple(id.group, id.module, id.version)
                }

            resolved.forEach { (group, name, version) ->
                resolvedVersions.getOrPut("$group:$name") { mutableSetOf() }.add(version)
            }

            val resolvedKeys = resolved.map { "${it.first}:${it.second}" }.toSet()
            declared.forEach { (group, name, version) ->
                if ("$group:$name" !in resolvedKeys) {
                    unused += DependencyUnused(group, name, version, configuration.name)
                }
            }

            configuration.resolvedConfiguration.lenientConfiguration.artifacts.forEach { artifact ->
                val id = artifact.moduleVersion.id
                val size = artifact.file.length()
                if (size > 5L * 1024 * 1024) {
                    heavy += DependencyHeavy(id.group, id.name, id.version, size)
                }
            }
        }
    }

    resolvedVersions.forEach { (module, versions) ->
        if (versions.size > 1) {
            val parts = module.split(":", limit = 2)
            duplicates += DependencyDuplicate(parts[0], parts[1], versions.sorted())
            val latest = versions.maxOrNull().orEmpty()
            versions.filter { it != latest }.forEach { version ->
                outdated += DependencyOutdated(parts[0], parts[1], version, latest)
            }
        }
    }

    return DependencyDiagnostics(duplicates, outdated, unused, heavy)
}

private fun collectModuleDiagnostics(project: Project, metrics: BuildMetricsSnapshot?): ModuleDiagnostics {
    val modules = mutableListOf<ModuleInfo>()
    val deps = mutableListOf<ModuleDependency>()
    val taskTimes = metrics?.taskDurations.orEmpty()

    project.rootProject.allprojects.forEach { module ->
        val usesKapt = module.plugins.hasPlugin("org.jetbrains.kotlin.kapt") ||
            module.plugins.hasPlugin("kotlin-kapt") ||
            module.configurations.names.any { it.startsWith("kapt", ignoreCase = true) }

        val moduleTasks = taskTimes.filter { it.projectPath == module.path }
        val moduleDuration = moduleTasks.sumOf { it.durationMs }.takeIf { it > 0 }

        modules += ModuleInfo(
            path = module.path,
            taskCount = module.tasks.size,
            executionMs = moduleDuration,
            usesKapt = usesKapt,
            buildCacheEnabled = module.gradle.startParameter.isBuildCacheEnabled
        )

        module.configurations.filter { it.name.contains("implementation", ignoreCase = true) }
            .flatMap { it.dependencies }
            .filterIsInstance<org.gradle.api.artifacts.ProjectDependency>()
            .forEach { dep ->
                deps += ModuleDependency(module.path, dep.dependencyProject.path)
            }
    }

    return ModuleDiagnostics(modules, deps)
}

private fun collectAnnotationDiagnostics(
    project: Project,
    buildMetrics: BuildMetricsSnapshot?
): AnnotationDiagnostics {
    val processors = mutableSetOf<String>()

    project.rootProject.allprojects.forEach { module ->
        module.configurations
            .filter { it.name.startsWith("kapt", ignoreCase = true) }
            .flatMap { it.dependencies }
            .filterIsInstance<org.gradle.api.artifacts.ExternalModuleDependency>()
            .forEach depLoop@{ dep ->
                val group = dep.group ?: return@depLoop
                processors += "$group:${dep.name}"
            }
    }

    val taskDurations = buildMetrics?.taskDurations.orEmpty()
    val totalProcessingMs = taskDurations
        .filter { it.path.contains("kapt", ignoreCase = true) || it.path.contains("ksp", ignoreCase = true) }
        .sumOf { it.durationMs }
        .takeIf { it > 0 }
    val kaptStubMs = taskDurations
        .filter { it.path.contains("kaptGenerateStubs", ignoreCase = true) }
        .sumOf { it.durationMs }
        .takeIf { it > 0 }

    return AnnotationDiagnostics(
        processors = processors.sorted(),
        totalProcessingMs = totalProcessingMs,
        kaptStubGenerationMs = kaptStubMs
    )
}

private fun collectEnvironmentDiagnostics(): EnvironmentDiagnostics {
    val os = System.getProperty("os.name") ?: "unknown"
    val arch = System.getProperty("os.arch") ?: "unknown"
    val env = System.getenv()
    val ci = env["CI"]?.toBoolean() == true ||
        env.containsKey("GITHUB_ACTIONS") ||
        env.containsKey("JENKINS_URL") ||
        env.containsKey("BUILDKITE") ||
        env.containsKey("TEAMCITY_VERSION")
    val ramMb = Runtime.getRuntime().maxMemory() / (1024 * 1024)

    return EnvironmentDiagnostics(
        os = os,
        arch = arch,
        ci = ci,
        availableRamMb = ramMb
    )
}

private fun collectTestsDiagnostics(buildMetrics: BuildMetricsSnapshot?): TestsDiagnostics {
    val tests = buildMetrics?.tests
    val uiTestDuration = buildMetrics?.taskDurations.orEmpty()
        .filter { it.path.contains("connectedDebugAndroidTest", ignoreCase = true) }
        .sumOf { it.durationMs }
        .takeIf { it > 0 }

    return TestsDiagnostics(
        total = tests?.total ?: 0,
        passed = tests?.passed ?: 0,
        failed = tests?.failed ?: 0,
        skipped = tests?.skipped ?: 0,
        durationMs = tests?.durationMs,
        uiTestDurationMs = uiTestDuration,
        slowest = tests?.slowest.orEmpty(),
        failures = tests?.failures.orEmpty()
    )
}


private fun duplicatesToJson(items: List<DependencyDuplicate>): String {
    if (items.isEmpty()) return "[]"
    val json = items.joinToString(",\n") { item ->
        val versions = item.versions.joinToString(prefix = "[", postfix = "]") { "\"${esc(it)}\"" }
        """
        {
          "group": "${esc(item.group)}",
          "name": "${esc(item.name)}",
          "versions": $versions
        }
        """.trimIndent()
    }
    return "[\n$json\n]"
}

private fun outdatedToJson(items: List<DependencyOutdated>): String {
    if (items.isEmpty()) return "[]"
    val json = items.joinToString(",\n") { item ->
        """
        {
          "group": "${esc(item.group)}",
          "name": "${esc(item.name)}",
          "currentVersion": "${esc(item.currentVersion)}",
          "latestVersion": "${esc(item.latestVersion)}"
        }
        """.trimIndent()
    }
    return "[\n$json\n]"
}

private fun unusedToJson(items: List<DependencyUnused>): String {
    if (items.isEmpty()) return "[]"
    val json = items.joinToString(",\n") { item ->
        """
        {
          "group": "${esc(item.group)}",
          "name": "${esc(item.name)}",
          "version": ${item.version?.let { "\"${esc(it)}\"" } ?: "null"},
          "configuration": "${esc(item.configuration)}"
        }
        """.trimIndent()
    }
    return "[\n$json\n]"
}

private fun heavyToJson(items: List<DependencyHeavy>): String {
    if (items.isEmpty()) return "[]"
    val json = items.joinToString(",\n") { item ->
        """
        {
          "group": "${esc(item.group)}",
          "name": "${esc(item.name)}",
          "version": ${item.version?.let { "\"${esc(it)}\"" } ?: "null"},
          "sizeBytes": ${item.sizeBytes}
        }
        """.trimIndent()
    }
    return "[\n$json\n]"
}

private fun modulesToJson(items: List<ModuleInfo>): String {
    if (items.isEmpty()) return "[]"
    val json = items.joinToString(",\n") { item ->
        """
        {
          "path": "${esc(item.path)}",
          "taskCount": ${item.taskCount},
          "executionMs": ${item.executionMs ?: "null"},
          "usesKapt": ${item.usesKapt},
          "buildCacheEnabled": ${item.buildCacheEnabled}
        }
        """.trimIndent()
    }
    return "[\n$json\n]"
}

private fun moduleDepsToJson(items: List<ModuleDependency>): String {
    if (items.isEmpty()) return "[]"
    val json = items.joinToString(",\n") { item ->
        """
        {
          "from": "${esc(item.from)}",
          "to": "${esc(item.to)}"
        }
        """.trimIndent()
    }
    return "[\n$json\n]"
}

private fun moduleSummariesToJson(modules: ModuleDiagnostics): String {
    if (modules.modules.isEmpty()) return "[]"
    val json = modules.modules.joinToString(",\n") { module ->
        """
        {
          "name": "${esc(module.path)}",
          "tasks": ${module.taskCount},
          "totalMs": ${module.executionMs ?: "null"},
          "usesKapt": ${module.usesKapt},
          "buildCacheEnabled": ${module.buildCacheEnabled}
        }
        """.trimIndent()
    }
    return "[\n$json\n]"
}

private fun slowestTestsToJson(items: List<TestTiming>): String {
    if (items.isEmpty()) return "[]"
    val json = items.joinToString(",\n") { item ->
        """
        {
          "className": "${esc(item.className)}",
          "name": "${esc(item.name)}",
          "durationMs": ${item.durationMs}
        }
        """.trimIndent()
    }
    return "[\n$json\n]"
}

private fun testFailuresToJson(items: List<TestFailure>): String {
    if (items.isEmpty()) return "[]"
    val json = items.joinToString(",\n") { item ->
        """
        {
          "className": "${esc(item.className)}",
          "name": "${esc(item.name)}",
          "message": "${esc(item.message)}",
          "stackTrace": "${esc(item.stackTrace)}"
        }
        """.trimIndent()
    }
    return "[\n$json\n]"
}
