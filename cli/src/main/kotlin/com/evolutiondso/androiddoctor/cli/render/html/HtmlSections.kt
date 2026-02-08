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
        return listOf(
            HtmlComponents.overviewCard(report, showGenerated = true),
            HtmlComponents.scoresCard(report),
            HtmlComponents.chartsCard("Trend Chart", "trendChart"),
            HtmlComponents.chartsCard("Impact Chart", "impactChart"),
            HtmlComponents.chartsCard("Radar Chart", "radarChart", fullWidth = true),
            HtmlComponents.actionsCard(report)
        ).joinToString("\n")
    }

    fun formattedGenerated(report: AndroidDoctorReport): String {
        return DateFormatter.pretty(report.generatedAt)
    }
}
