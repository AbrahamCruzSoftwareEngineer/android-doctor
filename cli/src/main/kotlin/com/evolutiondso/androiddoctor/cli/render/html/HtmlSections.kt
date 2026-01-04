package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.utils.DateFormatter

object HtmlSections {

    private fun buildActions(report: AndroidDoctorReport): String {
        val actions = report.actions.orEmpty()

        if (actions.isEmpty()) return "<p class='muted'>No actions available</p>"

        return actions.joinToString("\n") { a ->
            """
            <div class="action-card">
                <h3>${a.title}</h3>
                <p class="why">${a.why}</p>
                <p class="how">${a.how}</p>
                <div class="impact">
                    +${a.impact?.buildHealthDelta ?: 0} Build • 
                    +${a.impact?.modernizationDelta ?: 0} Modernize
                </div>
            </div>
            """.trimIndent()
        }
    }

    fun buildFreeBody(report: AndroidDoctorReport): String {

        val scores = report.scores

        return """
        <section class="card">
            <h2>Project Overview</h2>
            <p><strong>Name:</strong> ${report.project?.name ?: "Unknown"}</p>
            <p><strong>Status:</strong> ${report.status}</p>
        </section>

        <section class="card">
            <h2>Scores</h2>
            <p><strong>Build Health:</strong> ${scores?.buildHealth}</p>
            <p><strong>Modernization:</strong> ${scores?.modernization}</p>
        </section>

        <section class="card">
            <h2>Top Actions</h2>
            ${buildActions(report)}
        </section>

        <section class="upgrade-banner">
            🚀 Upgrade to Premium to unlock:
            <ul>
                <li>Dark Mode + Themes</li>
                <li>Markdown & PDF Export</li>
                <li>Interactive Charts</li>
                <li>Extended Recommendations</li>
            </ul>
        </section>
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
            <h2>Score Summary</h2>
            <p><strong>Build Health:</strong> ${scores?.buildHealth} / 100</p>
            <p><strong>Modernization:</strong> ${scores?.modernization} / 100</p>
        </section>

        <section class="card">
            <h2>Recommended Actions</h2>
            ${buildActions(report)}
        </section>
        """
    }
}
