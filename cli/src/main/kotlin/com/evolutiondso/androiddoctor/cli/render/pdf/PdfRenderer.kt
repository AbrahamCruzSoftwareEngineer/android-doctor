package com.evolutiondso.androiddoctor.cli.render.pdf

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport

object PdfRenderer {

    fun render(report: AndroidDoctorReport): ByteArray {
        // later: replace with Apache PDFBox or FlyingSaucer
        val text = """
            AndroidDoctor Report
            Status: ${report.status}
            Build Health: ${report.scores?.buildHealth}
            Modernization: ${report.scores?.modernization}
        """.trimIndent()

        return text.toByteArray()
    }
}
