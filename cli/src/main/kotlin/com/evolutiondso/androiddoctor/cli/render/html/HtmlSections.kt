package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.utils.DateFormatter

object HtmlSections {

    fun buildFreeBody(report: AndroidDoctorReport): String {
        return listOf(
            HtmlComponents.overviewCard(report, showGenerated = false),
            HtmlComponents.scoresCard(report),
            HtmlComponents.actionsCard(report),
            HtmlComponents.upgradeBanner()
        ).joinToString("\n")
    }

    fun buildPremiumBody(report: AndroidDoctorReport): String {
        val chartCards = listOf(
            HtmlComponents.chartsCard("Trend: Build Health vs Modernization", "trendChart"),
            HtmlComponents.chartsCard("Impact Summary", "impactChart"),
            HtmlComponents.chartsCard("Build Time Mix", "buildTimeChart"),
            HtmlComponents.chartsCard("Score Radar", "radarChart", fullWidth = true)
        )

        return listOf(
            HtmlComponents.overviewCard(report, showGenerated = true),
            HtmlComponents.scoresCard(report),
            HtmlComponents.chartsGrid(chartCards),
            HtmlComponents.actionsCard(report)
        ).joinToString("\n")
    }

    fun formattedGenerated(report: AndroidDoctorReport): String {
        return DateFormatter.pretty(report.generatedAt)
    }
}
