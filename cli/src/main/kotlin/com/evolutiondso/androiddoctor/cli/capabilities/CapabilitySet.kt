package com.evolutiondso.androiddoctor.cli.capabilities

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.render.base.HtmlRendererBase
import com.evolutiondso.androiddoctor.cli.render.terminal.TerminalRenderer

interface CapabilitySet {
    val name: String
    val terminalRenderer: TerminalRenderer
    val htmlRenderer: HtmlRendererBase?

    fun canExportHtml(): Boolean
    fun canExportMarkdown(): Boolean
    fun canExportPdf(): Boolean

    fun printSummary(report: AndroidDoctorReport)
}
