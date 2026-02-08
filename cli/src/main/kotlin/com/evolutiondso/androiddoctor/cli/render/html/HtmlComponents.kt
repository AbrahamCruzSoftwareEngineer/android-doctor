package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.ActionInfo
import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport

object HtmlComponents {

    fun overviewCard(report: AndroidDoctorReport, showGenerated: Boolean): String {
        val name = report.project?.name ?: "Unknown"
        val path = report.project?.path
            ?.takeIf { it.isNotBlank() && it != ":" }
            ?: "Unknown"
        val status = report.status?.takeIf { it.isNotBlank() && it.lowercase() != "skeleton" } ?: "Unknown"
        val generatedMetaLine =
            if (showGenerated) "<div><strong>Generated:</strong> ${HtmlSections.formattedGenerated(report)}</div>" else ""
        val pathLine = if (path == "Unknown") "" else "<div><strong>Path:</strong> $path</div>"
        val statusLine = if (status == "Unknown") "" else "<div><strong>Status:</strong> $status</div>"

        return """
        <section class="card overview-card">
            <div class="overview-header">
                <div>
                    <p class="overview-eyebrow">Project Overview</p>
                    <h2 class="overview-title">$name</h2>
                </div>
            <div class="overview-meta">
                $generatedMetaLine
            </div>
            </div>
            <div class="overview-divider"></div>
            <div class="info-grid">
                <div><strong>Project:</strong> $name</div>
                $pathLine
                $statusLine
            </div>
        </section>
        """.trimIndent()
    }

    fun scoresCard(report: AndroidDoctorReport): String {
        val build = report.scores?.buildHealth ?: 0
        val modern = report.scores?.modernization ?: 0

        return """
        <section class="card">
            <h2>Scores</h2>
            <div class="score-grid">
                <div class="score-tile">
                    <div class="score-label">Build Health</div>
                    <div class="score-value">$build</div>
                    <div class="score-max">/ 100</div>
                </div>
                <div class="score-tile">
                    <div class="score-label">Modernization</div>
                    <div class="score-value">$modern</div>
                    <div class="score-max">/ 100</div>
                </div>
            </div>
        </section>
        """.trimIndent()
    }

    fun actionsCard(report: AndroidDoctorReport): String {
        val actions = report.actions.orEmpty()
        if (actions.isEmpty()) {
            return """
            <section class="card">
                <h2>Recommended Actions</h2>
                <p class="muted">No recommended actions were found for this report.</p>
            </section>
            """.trimIndent()
        }

        return """
        <section class="card">
            <h2>Recommended Actions</h2>
            ${actions.joinToString("\n") { actionItem(it) }}
        </section>
        """.trimIndent()
    }

    private fun actionItem(action: ActionInfo): String {
        val severity = action.severity?.uppercase() ?: "INFO"
        val severityClass = when (severity) {
            "HIGH" -> "chip--high"
            "MEDIUM" -> "chip--medium"
            "LOW" -> "chip--low"
            else -> "chip--neutral"
        }
        val effort = action.effort?.uppercase()
        val effortChip = effort?.let { """<span class="chip chip--effort">Effort: $it</span>""" } ?: ""

        return """
        <div class="action-item">
            <div class="action-header">
                <h3>${action.title}</h3>
                <div class="action-chips">
                    <span class="chip $severityClass">$severity</span>
                    $effortChip
                </div>
            </div>
            <p><strong>Why:</strong> ${action.why}</p>
            <p><strong>How:</strong> ${action.how}</p>
            <p class="impact">
                +${action.impact?.buildHealthDelta ?: 0} Build Health •
                +${action.impact?.modernizationDelta ?: 0} Modernization
            </p>
        </div>
        """.trimIndent()
    }

    fun chartsCard(title: String, canvasId: String, fullWidth: Boolean = false): String {
        val widthClass = if (fullWidth) "chart-card full" else "chart-card"

        return """
        <section class="card $widthClass">
            <h2>$title</h2>
            <div class="chart-wrapper">
                <div class="chart-empty" data-chart-empty="$canvasId">No Data Available</div>
                <canvas id="$canvasId"></canvas>
            </div>
        </section>
        """.trimIndent()
    }

    fun chartsGrid(cards: List<String>): String {
        return """
        <div class="charts-grid">
            ${cards.joinToString("\n")}
        </div>
        """.trimIndent()
    }

    fun diagnosticsSummaryCard(report: AndroidDoctorReport): String {
        val configMs = report.diagnostics?.configuration?.durationMs?.let { "${it} ms" } ?: "Unknown"
        val execMs = report.diagnostics?.execution?.durationMs?.let { "${it} ms" } ?: "Unknown"
        val cache = report.diagnostics?.buildCache
        val cacheSummary = cache?.let { "Hits ${it.hits ?: 0} / Misses ${it.misses ?: 0}" } ?: "Unknown"
        val configCache = report.diagnostics?.configurationCache
        val configCacheRequested = configCache?.requested?.let { if (it) "Requested" else "Not requested" } ?: "Unknown"
        val depSummary = report.dependencies?.let {
            val dup = it.duplicates?.size ?: 0
            val outdated = it.outdated?.size ?: 0
            "Duplicates $dup • Outdated $outdated"
        } ?: "Unknown"
        val ci = report.environment?.ci?.let { if (it) "CI" else "Local" } ?: "Unknown"

        return """
        <section class="card">
            <h2>Diagnostics Summary</h2>
            <div class="info-grid">
                <div><strong>Config Time:</strong> $configMs</div>
                <div><strong>Execution Time:</strong> $execMs</div>
                <div><strong>Build Cache:</strong> $cacheSummary</div>
                <div><strong>Config Cache:</strong> $configCacheRequested</div>
                <div><strong>Dependencies:</strong> $depSummary</div>
                <div><strong>Environment:</strong> $ci</div>
            </div>
        </section>
        """.trimIndent()
    }

    fun buildPerformanceCard(report: AndroidDoctorReport): String {
        val configMs = report.diagnostics?.configuration?.durationMs
        val execMs = report.diagnostics?.execution?.durationMs
        val cache = report.diagnostics?.buildCache
        val incremental = cache?.incrementalCompilationUsed
        val tasks = report.diagnostics?.execution?.topLongestTasks.orEmpty()
        val tasksRows = if (tasks.isEmpty()) {
            "<tr><td colspan=\"3\" class=\"muted\">No task timing data available.</td></tr>"
        } else {
            tasks.joinToString("\n") { task ->
                """
                <tr>
                    <td>${task.path ?: "Unknown"}</td>
                    <td>${task.projectPath ?: "-"}</td>
                    <td>${task.durationMs?.let { "${it} ms" } ?: "Unknown"}</td>
                </tr>
                """.trimIndent()
            }
        }

        return """
        <section class="card">
            <h2>Build Performance</h2>
            <div class="info-grid">
                <div><strong>Configuration:</strong> ${configMs?.let { "${it} ms" } ?: "Unknown"}</div>
                <div><strong>Execution:</strong> ${execMs?.let { "${it} ms" } ?: "Unknown"}</div>
                <div><strong>Build Cache:</strong> Hits ${cache?.hits ?: 0} / Misses ${cache?.misses ?: 0}</div>
                <div><strong>Incremental Compile:</strong> ${formatBoolean(incremental)}</div>
            </div>
            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th>Task</th>
                            <th>Module</th>
                            <th>Duration</th>
                        </tr>
                    </thead>
                    <tbody>
                        $tasksRows
                    </tbody>
                </table>
            </div>
        </section>
        """.trimIndent()
    }

    fun configurationCacheCard(report: AndroidDoctorReport): String {
        val config = report.diagnostics?.configurationCache
        val requested = formatBoolean(config?.requested)
        val stored = formatBoolean(config?.stored)
        val reused = formatBoolean(config?.reused)
        val incompatible = config?.incompatibleTasks?.toString() ?: "Unknown"
        val warning = if (config?.requested == true && config?.reused != true) {
            "<p class=\"warning\">Configuration cache was requested but not reused.</p>"
        } else ""

        return """
        <section class="card">
            <h2>Configuration Cache Report</h2>
            <div class="info-grid">
                <div><strong>Requested:</strong> $requested</div>
                <div><strong>Stored:</strong> $stored</div>
                <div><strong>Reused:</strong> $reused</div>
                <div><strong>Incompatible Tasks:</strong> $incompatible</div>
            </div>
            $warning
        </section>
        """.trimIndent()
    }

    fun dependencyInsightsCard(report: AndroidDoctorReport): String {
        val deps = report.dependencies
        val outdatedRows = deps?.outdated?.joinToString("\n") { item ->
            """
            <tr>
                <td>${item.group ?: "?"}:${item.name ?: "?"}</td>
                <td>${item.currentVersion ?: "?"}</td>
                <td>${item.latestVersion ?: "?"}</td>
            </tr>
            """.trimIndent()
        } ?: ""
        val outdatedBody = if (outdatedRows.isBlank()) {
            "<tr><td colspan=\"3\" class=\"muted\">No outdated libraries detected.</td></tr>"
        } else outdatedRows

        val duplicates = deps?.duplicates.orEmpty()
        val duplicatesRows = if (duplicates.isEmpty()) {
            "<tr><td colspan=\"2\" class=\"muted\">No duplicate modules detected.</td></tr>"
        } else {
            duplicates.joinToString("\n") { item ->
                """
                <tr>
                    <td>${item.group ?: "?"}:${item.name ?: "?"}</td>
                    <td>${item.versions?.joinToString(", ") ?: "?"}</td>
                </tr>
                """.trimIndent()
            }
        }

        val unused = deps?.unused.orEmpty()
        val unusedRows = if (unused.isEmpty()) {
            "<tr><td colspan=\"3\" class=\"muted\">No unused dependencies flagged.</td></tr>"
        } else {
            unused.joinToString("\n") { item ->
                """
                <tr>
                    <td>${item.group ?: "?"}:${item.name ?: "?"}</td>
                    <td>${item.version ?: "?"}</td>
                    <td>${item.configuration ?: "?"}</td>
                </tr>
                """.trimIndent()
            }
        }

        val heavy = deps?.heavy.orEmpty()
        val heavyRows = if (heavy.isEmpty()) {
            "<tr><td colspan=\"3\" class=\"muted\">No heavy artifacts flagged.</td></tr>"
        } else {
            heavy.joinToString("\n") { item ->
                """
                <tr>
                    <td>${item.group ?: "?"}:${item.name ?: "?"}</td>
                    <td>${item.version ?: "?"}</td>
                    <td>${formatBytes(item.sizeBytes)}</td>
                </tr>
                """.trimIndent()
            }
        }

        return """
        <section class="card">
            <h2>Dependency Insights</h2>
            <h3 class="section-subtitle">Outdated Libraries</h3>
            <div class="table-wrapper">
                <table>
                    <thead><tr><th>Library</th><th>Current</th><th>Latest</th></tr></thead>
                    <tbody>$outdatedBody</tbody>
                </table>
            </div>
            <h3 class="section-subtitle">Duplicate Modules</h3>
            <div class="table-wrapper">
                <table>
                    <thead><tr><th>Module</th><th>Versions</th></tr></thead>
                    <tbody>$duplicatesRows</tbody>
                </table>
            </div>
            <h3 class="section-subtitle">Unused Dependencies</h3>
            <div class="table-wrapper">
                <table>
                    <thead><tr><th>Dependency</th><th>Version</th><th>Configuration</th></tr></thead>
                    <tbody>$unusedRows</tbody>
                </table>
            </div>
            <h3 class="section-subtitle">Heavy Artifacts</h3>
            <div class="table-wrapper">
                <table>
                    <thead><tr><th>Artifact</th><th>Version</th><th>Size</th></tr></thead>
                    <tbody>$heavyRows</tbody>
                </table>
            </div>
        </section>
        """.trimIndent()
    }

    fun toolchainCard(report: AndroidDoctorReport): String {
        val toolchain = report.toolchain
        val android = report.android
        val warnings = if (toolchain?.jvmTargetMismatch == true) {
            "<p class=\"warning\">JVM targets appear mismatched across toolchains.</p>"
        } else ""

        return """
        <section class="card">
            <h2>Toolchain Diagnostics</h2>
            <div class="info-grid">
                <div><strong>Kotlin Compiler:</strong> ${report.tooling?.kotlinCompilerVersion ?: "Unknown"}</div>
                <div><strong>Kotlin JVM Target:</strong> ${toolchain?.kotlinJvmTarget ?: "Unknown"}</div>
                <div><strong>Java Toolchain:</strong> ${toolchain?.javaToolchainVersion ?: "Unknown"}</div>
                <div><strong>AGP Version:</strong> ${android?.agpVersion ?: "Unknown"}</div>
                <div><strong>compileSdk:</strong> ${android?.compileSdk ?: "Unknown"}</div>
                <div><strong>AGP Target/Source:</strong> ${toolchain?.agpCompileTarget ?: "Unknown"} / ${toolchain?.agpCompileSource ?: "Unknown"}</div>
            </div>
            $warnings
        </section>
        """.trimIndent()
    }

    fun moduleGraphCard(report: AndroidDoctorReport): String {
        val modules = report.modules?.modules.orEmpty()
        val composeFlag = report.android?.composeEnabled
        val rows = if (modules.isEmpty()) {
            "<tr><td colspan=\"5\" class=\"muted\">No module data available.</td></tr>"
        } else {
            modules.joinToString("\n") { module ->
                """
                <tr>
                    <td>${module.path ?: "?"}</td>
                    <td>${module.taskCount ?: 0}</td>
                    <td>${module.executionMs?.let { "${it} ms" } ?: "Unknown"}</td>
                    <td>${formatBoolean(module.usesKapt)}</td>
                    <td>${formatBoolean(composeFlag)}</td>
                    <td>${formatBoolean(module.buildCacheEnabled)}</td>
                </tr>
                """.trimIndent()
            }
        }

        return """
        <section class="card">
            <h2>Module Graph Summary</h2>
            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th>Module</th>
                            <th>Tasks</th>
                            <th>Build Time</th>
                            <th>kapt</th>
                            <th>Compose</th>
                            <th>Build Cache</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rows
                    </tbody>
                </table>
            </div>
        </section>
        """.trimIndent()
    }

    fun annotationProcessingCard(report: AndroidDoctorReport): String {
        val annotation = report.annotationProcessing
        val processors = annotation?.processors?.joinToString(", ") ?: "Unknown"

        return """
        <section class="card">
            <h2>Annotation Processing Metrics</h2>
            <div class="info-grid">
                <div><strong>Processors:</strong> $processors</div>
                <div><strong>Total Time:</strong> ${annotation?.totalProcessingMs?.let { "${it} ms" } ?: "Unknown"}</div>
                <div><strong>Kapt Stub Overhead:</strong> ${annotation?.kaptStubGenerationMs?.let { "${it} ms" } ?: "Unknown"}</div>
            </div>
        </section>
        """.trimIndent()
    }

    fun composeCompilerCard(report: AndroidDoctorReport): String {
        val android = report.android
        val enabled = formatBoolean(android?.composeEnabled)
        val compiler = android?.composeCompilerVersion ?: "Unknown"
        val metrics = formatBoolean(android?.composeMetricsEnabled)
        val reports = formatBoolean(android?.composeReportsEnabled)
        val warning = if (android?.composeEnabled == true && android.composeCompilerVersion.isNullOrBlank()) {
            "<p class=\"warning\">Compose is enabled but compiler version was not detected.</p>"
        } else ""

        return """
        <section class="card">
            <h2>Compose Compiler Insights</h2>
            <div class="info-grid">
                <div><strong>Compose Enabled:</strong> $enabled</div>
                <div><strong>Compiler Version:</strong> $compiler</div>
                <div><strong>Metrics Enabled:</strong> $metrics</div>
                <div><strong>Reports Enabled:</strong> $reports</div>
            </div>
            $warning
        </section>
        """.trimIndent()
    }

    fun environmentCard(report: AndroidDoctorReport): String {
        val env = report.environment
        return """
        <section class="card">
            <h2>Environment Metadata</h2>
            <div class="info-grid">
                <div><strong>OS:</strong> ${env?.os ?: "Unknown"}</div>
                <div><strong>Architecture:</strong> ${env?.arch ?: "Unknown"}</div>
                <div><strong>CI:</strong> ${formatBoolean(env?.ci)}</div>
                <div><strong>RAM:</strong> ${env?.availableRamMb?.let { "${it} MB" } ?: "Unknown"}</div>
            </div>
        </section>
        """.trimIndent()
    }

    private fun formatBoolean(value: Boolean?): String = when (value) {
        true -> "Yes"
        false -> "No"
        null -> "Unknown"
    }

    private fun formatBytes(value: Long?): String {
        if (value == null) return "Unknown"
        val kb = value / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1) String.format("%.1f MB", mb) else String.format("%.0f KB", kb)
    }

    fun upgradeBanner(): String = """
        <section class="upgrade-banner">
            <div class="upgrade-title">Upgrade to Premium</div>
            <p>Unlock charts, insights, and advanced exports for your AndroidDoctor reports.</p>
            <ul>
                <li>Interactive charts and trends</li>
                <li>PDF + Markdown exports</li>
                <li>Theme toggle and premium styling</li>
            </ul>
        </section>
    """.trimIndent()
}
