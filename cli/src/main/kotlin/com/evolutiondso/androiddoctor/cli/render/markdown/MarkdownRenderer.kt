package com.evolutiondso.androiddoctor.cli.render.markdown

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport

object MarkdownRenderer {

    fun render(report: AndroidDoctorReport): String {
        return """
            # AndroidDoctor Report

            **Status:** ${report.status}

            ## Scores
            - Build Health: ${report.scores?.buildHealth}
            - Modernization: ${report.scores?.modernization}
        """.trimIndent()
    }
}
