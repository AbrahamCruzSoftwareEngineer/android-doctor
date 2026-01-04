package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.utils.DateFormatter

object HtmlSections {

    fun buildFreeBody(report: AndroidDoctorReport): String {
        val scores = report.scores

        return """
        <section class="card">
            <h2>Project Overview</h2>
            <p><strong>Project:</strong> ${report.project?.name}</p>
            <p><strong>Status:</strong> ${report.status}</p>
        </section>

        <section class="card">
            <h2>Scores</h2>
            <p><strong>Build Health:</strong> ${scores?.buildHealth}</p>
            <p><strong>Modernization:</strong> ${scores?.modernization}</p>
        </section>

        <section class="card">
            <h2>Recommended Actions</h2>
            ${buildActions(report)}
        </section>

        <div class="upgrade-banner">
            Upgrade to Premium for charts, insights, and PDF/Markdown exports!
        </div>
        """
    }

    fun buildPremiumBody(report: AndroidDoctorReport): String {
        val scores = report.scores

        return """
        <section class="card">
            <h2>Project Overview</h2>
            <p><strong>Name:</strong> ${report.project?.name ?: "Unknown"}</p>
            <p><strong>Generated:</strong> ${DateFormatter.pretty(report.generatedAt)}</p>
        </section>

        <section class="card">
            <h2>Scores Overview</h2>
            <p><strong>Build Health:</strong> ${scores?.buildHealth}</p>
            <p><strong>Modernization:</strong> ${scores?.modernization}</p>
        </section>

        <section class="charts-grid">
            <div class="chart-card">
                <h3>Trend Overview</h3>
                <canvas id="trendChart"></canvas>
            </div>

            <div class="chart-card">
                <h3>Action Impact</h3>
                <canvas id="impactChart"></canvas>
            </div>

            <div class="chart-card" style="grid-column: span 2;">
                <h3>Score Radar</h3>
                <canvas id="radarChart"></canvas>
            </div>
        </section>

        <section class="card">
            <h2>Recommended Actions</h2>
            ${buildActions(report)}
        </section>
        """
    }

    fun buildActions(report: AndroidDoctorReport): String {
        val actions = report.actions.orEmpty()

        return actions.joinToString("\n") { a ->
            """
            <div class="action-item">
                <h4>${a.title}</h4>
                <p>${a.why}</p>
                <p class="how">${a.how}</p>
                <p class="impact">
                    +${a.impact?.buildHealthDelta ?: 0} Build •
                    +${a.impact?.modernizationDelta ?: 0} Modernize
                </p>
            </div>
            """.trimIndent()
        }
    }
}
