package com.evolutiondso.androiddoctor.cli.capabilities

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.render.html.PremiumHtmlRenderer
import com.evolutiondso.androiddoctor.cli.render.terminal.TerminalRenderer

object PremiumCapabilities : CapabilitySet {

    override val name = "Premium"
    override val terminalRenderer = TerminalRenderer
    override val htmlRenderer = PremiumHtmlRenderer()

    override fun canExportHtml() = true
    override fun canExportMarkdown() = true
    override fun canExportPdf() = true

    override fun printSummary(report: AndroidDoctorReport) {
        terminalRenderer.render(report)
    }
}
