package com.evolutiondso.androiddoctor.cli.capabilities

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.render.html.FreeHtmlRenderer
import com.evolutiondso.androiddoctor.cli.render.terminal.TerminalRenderer

object FreeCapabilities : CapabilitySet {

    override val name = "Free"
    override val terminalRenderer = TerminalRenderer
    override val htmlRenderer = FreeHtmlRenderer()

    override fun canExportHtml() = true
    override fun canExportMarkdown() = false
    override fun canExportPdf() = false

    override fun printSummary(report: AndroidDoctorReport) {
        terminalRenderer.render(report)
    }
}
