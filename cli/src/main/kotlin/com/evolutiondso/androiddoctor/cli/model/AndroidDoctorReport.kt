package com.evolutiondso.androiddoctor.cli.model

import kotlinx.serialization.Serializable

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
    val diagnostics: DiagnosticsInfo? = null,
    val dependencies: DependencyDiagnosticsInfo? = null,
    val toolchain: ToolchainInfo? = null,
    val modules: List<ModuleSummaryInfo>? = null,
    val modulesDiagnostics: ModulesInfo? = null,
    val performance: PerformanceInfo? = null,
    val cache: CacheStatsInfo? = null,
    val taskTimings: List<TaskTimingSimpleInfo>? = null,
    val compose: ComposeInfo? = null,
    val annotationProcessing: AnnotationProcessingInfo? = null,
    val environment: EnvironmentInfo? = null,
    val architecture: ArchitectureDiagnosticsInfo? = null,
    val actions: List<ActionInfo>? = null,
    val plugins: PluginsInfo? = null,
    val notes: List<String>? = null
)

@Serializable
data class ProjectInfo(val name: String? = null, val path: String? = null)

@Serializable
data class ToolingInfo(
    val gradleVersion: String? = null,
    val kotlinStdlibVersion: String? = null,
    val kotlinCompilerVersion: String? = null,
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
    val composeEnabled: Boolean? = null,
    val compileSdk: String? = null,
    val composeCompilerVersion: String? = null,
    val composeMetricsEnabled: Boolean? = null,
    val composeReportsEnabled: Boolean? = null
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
    val severity: String? = null,
    val effort: String? = null,
    val title: String? = null,
    val why: String? = null,
    val how: String? = null,
    val impact: ImpactInfo? = null
)

@Serializable
data class PluginsInfo(
    val appliedKnownPluginIds: List<String>? = null
)

@Serializable
data class PerformanceInfo(
    val configurationMs: Long? = null,
    val executionMs: Long? = null,
    val incrementalCompilation: Boolean? = null
)

@Serializable
data class CacheStatsInfo(
    val hits: Int? = null,
    val misses: Int? = null
)

@Serializable
data class TaskTimingSimpleInfo(
    val path: String? = null,
    val ms: Long? = null
)

@Serializable
data class ModuleSummaryInfo(
    val name: String? = null,
    val tasks: Int? = null,
    val totalMs: Long? = null,
    val usesKapt: Boolean? = null,
    val buildCacheEnabled: Boolean? = null
)

@Serializable
data class ComposeInfo(
    val enabled: Boolean? = null,
    val compilerVersion: String? = null,
    val metricsEnabled: Boolean? = null,
    val reportsEnabled: Boolean? = null
)

@Serializable
data class DiagnosticsInfo(
    val configuration: PhaseDurationInfo? = null,
    val execution: ExecutionInfo? = null,
    val buildCache: BuildCacheInfo? = null,
    val configurationCache: ConfigurationCacheInfo? = null
)

@Serializable
data class PhaseDurationInfo(val durationMs: Long? = null)

@Serializable
data class ExecutionInfo(
    val durationMs: Long? = null,
    val topLongestTasks: List<TaskTimingInfo>? = null
)

@Serializable
data class TaskTimingInfo(
    val path: String? = null,
    val projectPath: String? = null,
    val durationMs: Long? = null,
    val didWork: Boolean? = null,
    val skipped: Boolean? = null,
    val skipMessage: String? = null
)

@Serializable
data class BuildCacheInfo(
    val enabled: Boolean? = null,
    val hits: Int? = null,
    val misses: Int? = null,
    val skipped: Int? = null,
    val incrementalCompilationUsed: Boolean? = null
)

@Serializable
data class ConfigurationCacheInfo(
    val requested: Boolean? = null,
    val stored: Boolean? = null,
    val reused: Boolean? = null,
    val incompatibleTasks: Int? = null
)

@Serializable
data class DependencyDiagnosticsInfo(
    val duplicates: List<DependencyDuplicateInfo>? = null,
    val outdated: List<DependencyOutdatedInfo>? = null,
    val unused: List<DependencyUnusedInfo>? = null,
    val heavy: List<DependencyHeavyInfo>? = null
)

@Serializable
data class DependencyDuplicateInfo(
    val group: String? = null,
    val name: String? = null,
    val versions: List<String>? = null
)

@Serializable
data class DependencyOutdatedInfo(
    val group: String? = null,
    val name: String? = null,
    val currentVersion: String? = null,
    val latestVersion: String? = null
)

@Serializable
data class DependencyUnusedInfo(
    val group: String? = null,
    val name: String? = null,
    val version: String? = null,
    val configuration: String? = null
)

@Serializable
data class DependencyHeavyInfo(
    val group: String? = null,
    val name: String? = null,
    val version: String? = null,
    val sizeBytes: Long? = null
)

@Serializable
data class ToolchainInfo(
    val javaToolchainVersion: String? = null,
    val jvmTarget: String? = null,
    val kotlinJvmTarget: String? = null,
    val agpCompileTarget: String? = null,
    val agpCompileSource: String? = null,
    val jvmTargetMismatch: Boolean? = null
)

@Serializable
data class ArchitectureDiagnosticsInfo(
    val mvc: Int? = null,
    val mvp: Int? = null,
    val mvvm: Int? = null,
    val mvi: Int? = null,
    val violations: List<ArchitectureViolationInfo>? = null,
    val recommendedFixes: List<ArchitectureFixInfo>? = null
)

@Serializable
data class ArchitectureViolationInfo(
    val type: String? = null,
    val file: String? = null,
    val description: String? = null
)

@Serializable
data class ArchitectureFixInfo(
    val title: String? = null,
    val description: String? = null
)

@Serializable
data class ModulesInfo(
    val count: Int? = null,
    val modules: List<ModuleInfo>? = null,
    val dependencies: List<ModuleDependencyInfo>? = null
)

@Serializable
data class ModuleInfo(
    val path: String? = null,
    val taskCount: Int? = null,
    val executionMs: Long? = null,
    val usesKapt: Boolean? = null,
    val buildCacheEnabled: Boolean? = null
)

@Serializable
data class ModuleDependencyInfo(
    val from: String? = null,
    val to: String? = null
)

@Serializable
data class AnnotationProcessingInfo(
    val processors: List<String>? = null,
    val totalProcessingMs: Long? = null,
    val kaptStubGenerationMs: Long? = null
)

@Serializable
data class EnvironmentInfo(
    val os: String? = null,
    val arch: String? = null,
    val ci: Boolean? = null,
    val availableRamMb: Long? = null
)
