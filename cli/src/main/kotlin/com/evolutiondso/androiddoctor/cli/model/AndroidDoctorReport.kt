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
