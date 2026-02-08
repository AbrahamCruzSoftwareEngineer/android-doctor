package com.evolutiondso.androiddoctor.cli.analysis

import java.io.File

class ArchitectureAnalyzer {

    data class ArchitectureDiagnostics(
        val mvc: Int,
        val mvp: Int,
        val mvvm: Int,
        val mvi: Int,
        val violations: List<ArchitectureViolation>,
        val recommendedFixes: List<ArchitectureFix>
    )

    data class ArchitectureViolation(
        val type: String,
        val file: String,
        val description: String,
        val suggestedFix: String
    )

    data class ArchitectureFix(
        val title: String,
        val description: String
    )

    fun analyze(rootDir: File): ArchitectureDiagnostics {
        val sourceFiles = rootDir.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
            .filterNot { it.path.contains("/build/") || it.path.contains("/.gradle/") || it.path.contains("/generated/") }
            .toList()

        val activityFiles = sourceFiles.filter { it.name.endsWith("Activity.kt") || it.name.endsWith("Activity.java") }
        val fragmentFiles = sourceFiles.filter { it.name.endsWith("Fragment.kt") || it.name.endsWith("Fragment.java") }
        val presenterFiles = sourceFiles.filter { it.name.contains("Presenter") }
        val viewModelFiles = sourceFiles.filter { it.name.contains("ViewModel") }
        val reducerFiles = sourceFiles.filter { it.name.contains("Reducer") }
        val stateFiles = sourceFiles.filter { it.name.contains("State") }
        val actionFiles = sourceFiles.filter { it.name.contains("Action") }

        val mvpOccurrences = sourceFiles.filter { file ->
            val text = safeText(file)
            Regex("interface\\s+\\w*Contract").containsMatchIn(text) ||
                Regex("class\\s+\\w*Presenter").containsMatchIn(text)
        }

        val mvvmOccurrences = sourceFiles.filter { file ->
            val text = safeText(file)
            Regex("class\\s+\\w+\\s*:\\s*ViewModel").containsMatchIn(text) ||
                text.contains("LiveData") || text.contains("StateFlow") || text.contains("MutableState")
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

        val violations = mutableListOf<ArchitectureViolation>()
        val fixes = mutableListOf<ArchitectureFix>()

        val networkingKeywords = listOf("Retrofit", "OkHttp", "OkHttpClient", "HttpUrlConnection")
        val dbKeywords = listOf("Room", "SQLite", "SQLiteDatabase", "Realm")
        val uiKeywords = listOf("setContentView", "findViewById", "RecyclerView", "TextView", "Button", "Adapter")
        val dataManipulationKeywords = listOf("map(", "filter(", "reduce(", "MutableList", "mutableListOf")

        (activityFiles + fragmentFiles).forEach { file ->
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
                    description = "Activity/Fragment exceeds 400 LOC.",
                    suggestedFix = "Split UI and business logic into ViewModels and use-cases."
                )
            }
            if ((hasNetwork || hasDb) && hasUi && (hasDataManipulation || hasRepoCalls)) {
                violations += ArchitectureViolation(
                    type = "MixedResponsibilityActivity",
                    file = relativePath,
                    description = "UI and data logic are mixed in one file.",
                    suggestedFix = "Move data logic to ViewModel or repository."
                )
            }
            if ((hasNetwork || hasDb || hasRepoCalls) && !hasViewModel) {
                violations += ArchitectureViolation(
                    type = "MissingViewModel",
                    file = relativePath,
                    description = "Activity/Fragment performs business logic without a ViewModel.",
                    suggestedFix = "Introduce a ViewModel for business logic."
                )
            }
        }

        val hasDomainLayer = sourceFiles.any { it.path.contains("/domain/") }
        if (!hasDomainLayer) {
            violations += ArchitectureViolation(
                type = "MissingDomainLayer",
                file = "<root>",
                description = "No domain layer detected.",
                suggestedFix = "Add a domain layer with use-cases and interfaces."
            )
            fixes += ArchitectureFix(
                title = "Introduce Domain Layer",
                description = "Create domain interfaces and use-cases to separate UI and data."
            )
        }

        val uiLayerFiles = activityFiles + fragmentFiles + sourceFiles.filter { it.path.contains("/ui/") || it.path.contains("/presentation/") }
        uiLayerFiles.forEach { file ->
            val text = safeText(file)
            if (text.contains("Repository") || Regex("\\bdata\\b").containsMatchIn(text)) {
                violations += ArchitectureViolation(
                    type = "UiCallsDataLayer",
                    file = file.relativeTo(rootDir).path,
                    description = "UI layer calls data layer directly.",
                    suggestedFix = "Introduce use-cases and keep UI thin."
                )
            }
        }

        viewModelFiles.forEach { file ->
            val text = safeText(file)
            if (text.contains("Retrofit") || text.contains("OkHttp") || text.contains("Dao") || text.contains("DataSource")) {
                violations += ArchitectureViolation(
                    type = "ViewModelDependsOnDataSource",
                    file = file.relativeTo(rootDir).path,
                    description = "ViewModel depends on data sources/Retrofit directly.",
                    suggestedFix = "Inject repositories or use-cases."
                )
            }
        }

        sourceFiles.filter { it.name.contains("Repository") }.forEach { file ->
            val text = safeText(file)
            if (text.contains("Dto") || text.contains("Response<")) {
                violations += ArchitectureViolation(
                    type = "RepositoryReturnsDTO",
                    file = file.relativeTo(rootDir).path,
                    description = "Repository returns DTOs directly.",
                    suggestedFix = "Map DTOs to domain models inside repositories."
                )
                fixes += ArchitectureFix(
                    title = "Remove DTOs from UI",
                    description = "Map DTOs to domain models in repositories before exposing to UI."
                )
            }
        }

        val patternsDetected = listOf(mvcDetected, mvpDetected, mvvmDetected, mviDetected).count { it }
        if (patternsDetected > 2) {
            violations += ArchitectureViolation(
                type = "ArchitectureInconsistency",
                file = "<root>",
                description = "Multiple architecture patterns detected.",
                suggestedFix = "Standardize on MVVM or MVI."
            )
            fixes += ArchitectureFix(
                title = "Standardize architecture pattern (choose MVVM or MVI)",
                description = "Pick a primary pattern and migrate remaining features."
            )
        }

        return ArchitectureDiagnostics(
            mvc = (mvcWeight * 100) / total,
            mvp = (mvpWeight * 100) / total,
            mvvm = (mvvmWeight * 100) / total,
            mvi = (mviWeight * 100) / total,
            violations = violations.distinctBy { it.type + it.file },
            recommendedFixes = fixes.distinctBy { it.title }
        )
    }

    private fun safeText(file: File): String = runCatching { file.readText() }.getOrDefault("")
    private fun safeLines(file: File): List<String> = runCatching { file.readLines() }.getOrDefault(emptyList())
}
