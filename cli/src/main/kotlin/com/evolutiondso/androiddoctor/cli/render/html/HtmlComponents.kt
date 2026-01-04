package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.ActionInfo
import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport

object HtmlComponents {

    fun themeToggle(): String = """
        <label class="theme-switch">
            <input type="checkbox" id="themeToggle">
            <span class="slider"></span>
        </label>
    """.trimIndent()

    fun themeUpgradeNotice(): String = """
        <div class="upgrade-box">
            <p>✨ Dark theme available in Premium</p>
        </div>
    """.trimIndent()

    fun projectSummary(report: AndroidDoctorReport): String = """
        <section class="card">
            <h2>Project Overview</h2>
            <p><b>Name:</b> ${report.project?.name ?: "Unknown"}</p>
            <p><b>Path:</b> ${report.project?.path ?: "Unknown"}</p>
            <p><b>Generated:</b> ${report.generatedAt ?: "Unknown"}</p>
        </section>
    """

    fun scores(report: AndroidDoctorReport): String {
        val b = report.scores?.buildHealth ?: 0
        val m = report.scores?.modernization ?: 0

        return """
        <section class="card">
            <h2>Scores</h2>
            <div class="score-box">
                <div class="score">
                    <span class="label">Build Health</span>
                    <span class="value">$b / 100</span>
                </div>
                <div class="score">
                    <span class="label">Modernization</span>
                    <span class="value">$m / 100</span>
                </div>
            </div>
        </section>
        """
    }

    fun actions(actions: List<ActionInfo>): String {
        if (actions.isEmpty()) return ""

        val rendered = actions.joinToString("\n") { a ->
            """
            <div class="action">
                <h3>${a.title}</h3>
                <p><b>Why:</b> ${a.why}</p>
                <p><b>How:</b> ${a.how}</p>

                <p class="impact">
                    +${a.impact?.buildHealthDelta ?: 0} Build Health • 
                    +${a.impact?.modernizationDelta ?: 0} Modernization
                </p>
            </div>
            """.trimIndent()
        }

        return """
        <section class="card">
            <h2>Top Actions</h2>
            $rendered
        </section>
        """
    }

    fun chartsSection(): String = """
        <section class="card">
            <h2>Build Metrics</h2>
            <canvas id="scoreChart" width="400" height="200"></canvas>
        </section>
    """
}
