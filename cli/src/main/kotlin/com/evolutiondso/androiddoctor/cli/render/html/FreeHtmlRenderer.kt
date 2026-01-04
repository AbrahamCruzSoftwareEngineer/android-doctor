package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.render.base.HtmlRendererBase

/**
 * Renderer for free-tier HTML reports.
 */
class FreeHtmlRenderer : HtmlRendererBase() {

    override fun render(report: AndroidDoctorReport): String {
        val body = HtmlSections.buildFreeBody(report)

        return HtmlTemplates.page(
            report = report,
            bodyContent = body,
            premium = false
        )
    }
}
