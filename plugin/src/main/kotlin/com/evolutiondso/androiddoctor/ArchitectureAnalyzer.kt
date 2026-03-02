package com.evolutiondso.androiddoctor

import org.gradle.api.Project
import java.io.File

internal data class ArchitectureDiagnostics(
    val mvc: Int,
    val mvp: Int,
    val mvvm: Int,
    val mvi: Int,
    val violations: List<ArchitectureViolation>,
    val recommendedFixes: List<ArchitectureFix>
) {
    fun toJson(): String {
        return """
        {
          "mvc": $mvc,
          "mvp": $mvp,
          "mvvm": $mvvm,
          "mvi": $mvi,
          "violations": ${architectureViolationsToJson(violations)},
          "recommendedFixes": ${architectureFixesToJson(recommendedFixes)}
        }
        """.trimIndent()
    }
}

internal data class ArchitectureViolation(
    val type: String,
    val file: String,
    val description: String
)

internal data class ArchitectureFix(
    val title: String,
    val description: String
)

internal class ArchitectureAnalyzer {

    fun analyze(project: Project): ArchitectureDiagnostics {
        val rootDir = project.rootProject.projectDir
        val sourceFiles = project.rootProject.fileTree(rootDir).apply {
            include("**/*.kt", "**/*.java")
            exclude("**/build/**", "**/.gradle/**", "**/generated/**", "**/test/**", "**/androidTest/**")
        }.files

        val activityFiles = mutableListOf<File>()
        val fragmentFiles = mutableListOf<File>()
        val presenterFiles = mutableListOf<File>()
        val viewModelFiles = mutableListOf<File>()
        val reducerFiles = mutableListOf<File>()
        val stateFiles = mutableListOf<File>()
        val actionFiles = mutableListOf<File>()

        val mvpOccurrences = mutableListOf<File>()
        val mvvmOccurrences = mutableListOf<File>()

        sourceFiles.forEach { file ->
            val name = file.name
            when {
                name.endsWith("Activity.kt") || name.endsWith("Activity.java") -> activityFiles += file
                name.endsWith("Fragment.kt") || name.endsWith("Fragment.java") -> fragmentFiles += file
            }
            if (name.contains("Presenter")) presenterFiles += file
            if (name.contains("ViewModel")) viewModelFiles += file
            if (name.contains("Reducer")) reducerFiles += file
            if (name.contains("State")) stateFiles += file
            if (name.contains("Action")) actionFiles += file

            val text = safeText(file)
            if (Regex("interface\\s+\\w*Contract").containsMatchIn(text)) {
                mvpOccurrences += file
            }
            if (Regex("class\\s+\\w*Presenter").containsMatchIn(text)) {
                mvpOccurrences += file
            }
            if (Regex("class\\s+\\w+\\s*:\\s*ViewModel").containsMatchIn(text) || text.contains("extends ViewModel")) {
                mvvmOccurrences += file
            }
            if (text.contains("LiveData") || text.contains("StateFlow") || text.contains("MutableState")) {
                mvvmOccurrences += file
            }
        }

        val mvcDetected = activityFiles.isNotEmpty() || fragmentFiles.isNotEmpty()
        val mvpDetected = presenterFiles.isNotEmpty() || mvpOccurrences.isNotEmpty()
        val mvvmDetected = viewModelFiles.isNotEmpty() || mvvmOccurrences.isNotEmpty()
        val mviDetected = reducerFiles.isNotEmpty() || stateFiles.isNotEmpty() || actionFiles.isNotEmpty()

        val mvcWeight = if (mvcDetected) 1 else 0
        val mvpWeight = if (mvpDetected) 1 else 0
        val mvvmWeight = if (mvvmDetected) 2 else 0
        val mviWeight = if (mviDetected) 1 else 0
        val total = listOf(mvcWeight, mvpWeight, mvvmWeight, mviWeight).sum().coerceAtLeast(1)

        val mvcPercent = (mvcWeight * 100) / total
        val mvpPercent = (mvpWeight * 100) / total
        val mvvmPercent = (mvvmWeight * 100) / total
        val mviPercent = (mviWeight * 100) / total

        val violations = mutableListOf<ArchitectureViolation>()
        val recommendedFixes = mutableListOf<ArchitectureFix>()

        val networkingKeywords = listOf("Retrofit", "OkHttp", "OkHttpClient", "HttpUrlConnection")
        val dbKeywords = listOf("Room", "SQLite", "SQLiteDatabase", "Realm")
        val uiKeywords = listOf("setContentView", "findViewById", "RecyclerView", "TextView", "Button", "Adapter")
        val dataManipulationKeywords = listOf("map(", "filter(", "reduce(", "MutableList", "mutableListOf")

        val activityOrFragments = activityFiles + fragmentFiles
        activityOrFragments.forEach { file ->
            val lines = safeLines(file)
            val text = safeText(file)
            val relativePath = file.relativeTo(rootDir).path
            val hasNetwork = networkingKeywords.any { text.contains(it) }
            val hasDb = dbKeywords.any { text.contains(it) }
            val hasUi = uiKeywords.any { text.contains(it) }
            val hasDataManipulation = dataManipulationKeywords.any { text.contains(it) }
            val hasRepoCalls = text.contains("Repository") || text.contains("Service")
            val hasViewModel = text.contains("ViewModel")

            if (lines.size > 400) {
                violations += ArchitectureViolation(
                    type = "GodActivity",
                    file = relativePath,
                    description = "Activity/Fragment exceeds 400 LOC. Split UI and business logic into ViewModels/use-cases."
                )
                recommendedFixes += ArchitectureFix(
                    title = "Break God Activity into ViewModel + UI state",
                    description = "Move business logic and IO into ViewModels and use-cases. Keep Activities/Fragments thin."
                )
            }

            if ((hasNetwork || hasDb) && hasUi && (hasDataManipulation || hasRepoCalls)) {
                violations += ArchitectureViolation(
                    type = "MixedResponsibilityActivity",
                    file = relativePath,
                    description = "UI, data manipulation, and IO work are mixed in the same Activity/Fragment."
                )
            }

            if ((hasNetwork || hasDb || hasRepoCalls) && !hasViewModel) {
                violations += ArchitectureViolation(
                    type = "MissingViewModel",
                    file = relativePath,
                    description = "Activity/Fragment performs business logic without a ViewModel."
                )
            }
        }

        val hasDomainLayer = project.rootProject.allprojects.any { module ->
            listOf("src/main/java", "src/main/kotlin").any { path ->
                File(module.projectDir, path).walkTopDown().any { it.isDirectory && it.name.equals("domain", ignoreCase = true) }
            }
        }

        if (!hasDomainLayer) {
            violations += ArchitectureViolation(
                type = "MissingDomainLayer",
                file = "<root>",
                description = "No domain layer detected. Add domain interfaces and use-cases to separate UI and data."
            )
            recommendedFixes += ArchitectureFix(
                title = "Introduce Domain Layer",
                description = "Create a domain module with use-cases and interfaces to decouple UI and data layers."
            )
        }

        val moduleDeps = mutableListOf<Pair<String, String>>()
        project.rootProject.allprojects.forEach { module ->
            module.configurations.filter { it.name.contains("implementation", ignoreCase = true) }
                .flatMap { it.dependencies }
                .filterIsInstance<org.gradle.api.artifacts.ProjectDependency>()
                .forEach { dep ->
                    moduleDeps += module.path to dep.dependencyProject.path
                }
        }
        val featureDependsOnApp = moduleDeps.filter { (from, to) ->
            from.contains("feature", ignoreCase = true) && to == ":app"
        }
        if (featureDependsOnApp.isNotEmpty()) {
            violations += ArchitectureViolation(
                type = "ModuleCoupling",
                file = featureDependsOnApp.joinToString(", ") { "${it.first} -> ${it.second}" },
                description = "Feature modules depend on :app. Invert dependencies or extract shared contracts."
            )
            recommendedFixes += ArchitectureFix(
                title = "Decouple modules via interfaces",
                description = "Move shared APIs to a core/domain module and make app depend on features."
            )
        }

        val uiLayerFiles = activityOrFragments + sourceFiles.filter { file ->
            file.path.contains("/ui/") || file.path.contains("/presentation/")
        }

        uiLayerFiles.forEach { file ->
            val text = safeText(file)
            if (Regex("\\bdata\\b").containsMatchIn(text) || text.contains("Repository")) {
                violations += ArchitectureViolation(
                    type = "UiCallsDataLayer",
                    file = file.relativeTo(rootDir).path,
                    description = "UI layer calls data layer directly. Introduce use-cases or ViewModels."
                )
            }
        }

        viewModelFiles.forEach { file ->
            val text = safeText(file)
            if (text.contains("Retrofit") || text.contains("OkHttp") || text.contains("Dao") || text.contains("DataSource")) {
                violations += ArchitectureViolation(
                    type = "ViewModelDependsOnDataSource",
                    file = file.relativeTo(rootDir).path,
                    description = "ViewModel depends on data sources/Retrofit directly. Inject repositories or use-cases."
                )
            }
        }

        val repositoryFiles = sourceFiles.filter { it.name.contains("Repository") }
        repositoryFiles.forEach { file ->
            val text = safeText(file)
            if (text.contains("Dto") || text.contains("Response<")) {
                violations += ArchitectureViolation(
                    type = "RepositoryReturnsDTO",
                    file = file.relativeTo(rootDir).path,
                    description = "Repository appears to return DTOs directly. Map DTOs to domain models."
                )
                recommendedFixes += ArchitectureFix(
                    title = "Remove DTOs from UI",
                    description = "Map DTOs to domain models inside repositories before exposing to UI."
                )
            }
        }

        val patternsDetected = listOf(mvcDetected, mvpDetected, mvvmDetected, mviDetected).count { it }
        if (patternsDetected > 2) {
            violations += ArchitectureViolation(
                type = "ArchitectureInconsistency",
                file = "<root>",
                description = "Multiple architecture patterns detected. Standardize on a single pattern."
            )
            recommendedFixes += ArchitectureFix(
                title = "Standardize architecture pattern (choose MVVM or MVI)",
                description = "Pick a primary architecture pattern and migrate remaining features to it."
            )
        }

        if (activityOrFragments.any { safeText(it).contains("Repository") }) {
            recommendedFixes += ArchitectureFix(
                title = "Decouple modules via interfaces",
                description = "Extract interfaces in a core/domain module and invert dependencies."
            )
        }

        return ArchitectureDiagnostics(
            mvc = mvcPercent,
            mvp = mvpPercent,
            mvvm = mvvmPercent,
            mvi = mviPercent,
            violations = violations.distinctBy { it.type + it.file + it.description },
            recommendedFixes = recommendedFixes.distinctBy { it.title + it.description }
        )
    }

    private fun safeText(file: File): String = runCatching { file.readText() }.getOrDefault("")
    private fun safeLines(file: File): List<String> = runCatching { file.readLines() }.getOrDefault(emptyList())
}

private fun architectureViolationsToJson(items: List<ArchitectureViolation>): String {
    if (items.isEmpty()) return "[]"
    val json = items.joinToString(",\n") { item ->
        """
        {
          "type": "${esc(item.type)}",
          "file": "${esc(item.file)}",
          "description": "${esc(item.description)}"
        }
        """.trimIndent()
    }
    return "[\n$json\n]"
}

private fun architectureFixesToJson(items: List<ArchitectureFix>): String {
    if (items.isEmpty()) return "[]"
    val json = items.joinToString(",\n") { item ->
        """
        {
          "title": "${esc(item.title)}",
          "description": "${esc(item.description)}"
        }
        """.trimIndent()
    }
    return "[\n$json\n]"
}

private fun esc(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
}
