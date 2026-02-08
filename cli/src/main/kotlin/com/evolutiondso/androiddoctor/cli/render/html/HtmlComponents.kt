package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.ActionInfo
import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport

object HtmlComponents {

    fun overviewCard(report: AndroidDoctorReport, showGenerated: Boolean): String {
        val name = report.project?.name ?: "Unknown"
        val path = report.project?.path ?: "Unknown"
        val status = report.status ?: "Unknown"
        val generated = if (showGenerated) "<p><strong>Generated:</strong> ${HtmlSections.formattedGenerated(report)}</p>" else ""

        return """
        <section class="card">
            <h2>Project Overview</h2>
            <div class="info-grid">
                <div><strong>Project:</strong> $name</div>
                <div><strong>Path:</strong> $path</div>
                <div><strong>Status:</strong> $status</div>
            </div>
            $generated
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
        return """
        <div class="action-item">
            <h3>${action.title}</h3>
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
                <canvas id="$canvasId"></canvas>
            </div>
        </section>
        """.trimIndent()
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
