package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.utils.DateFormatter

object HtmlSections {

    fun buildFreeBody(report: AndroidDoctorReport): String {
        return listOf(
            HtmlComponents.overviewCard(report, showGenerated = false),
            HtmlComponents.scoresCard(report),
            HtmlComponents.diagnosticsSummaryCard(report),
            HtmlComponents.architectureCard(report),
            HtmlComponents.actionsCard(report),
            HtmlComponents.upgradeBanner()
        ).joinToString("\n")
    }

    fun buildPremiumBody(report: AndroidDoctorReport): String {
        val chartCards = listOf(
            HtmlComponents.chartsCard("Trend: Build Health vs Modernization", "trendChart"),
            HtmlComponents.chartsCard("Impact Summary", "impactChart"),
            HtmlComponents.chartsCard("Build Time Mix", "buildTimeChart"),
            HtmlComponents.chartsCard("Build Cache Activity", "buildCacheChart"),
            HtmlComponents.chartsCard("Architecture Violations", "architectureViolationsChart"),
            HtmlComponents.chartsCard("Score Radar", "radarChart", fullWidth = true)
        )

        return listOf(
            HtmlComponents.overviewCard(report, showGenerated = true),
            HtmlComponents.scoresCard(report),
            HtmlComponents.buildPerformanceCard(report),
            HtmlComponents.configurationCacheCard(report),
            HtmlComponents.actionsCard(report),
            HtmlComponents.chartsGrid(chartCards),
            HtmlComponents.dependencyInsightsCard(report),
            HtmlComponents.toolchainCard(report),
            HtmlComponents.moduleGraphCard(report),
            HtmlComponents.annotationProcessingCard(report),
            HtmlComponents.composeCompilerCard(report),
            HtmlComponents.environmentCard(report),
            HtmlComponents.architectureCard(report)
        ).joinToString("\n")
    }

    fun formattedGenerated(report: AndroidDoctorReport): String {
        return DateFormatter.pretty(report.generatedAt)
    }
}
