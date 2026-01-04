package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.render.base.HtmlRendererBase

/**
 * Renderer for premium-tier HTML reports.
 */
class PremiumHtmlRenderer : HtmlRendererBase() {

    override fun render(report: AndroidDoctorReport): String {
        val body = HtmlSections.buildPremiumBody(report)

        return HtmlTemplates.page(
            report = report,
            bodyContent = body,
            premium = true
        )
    }
}
