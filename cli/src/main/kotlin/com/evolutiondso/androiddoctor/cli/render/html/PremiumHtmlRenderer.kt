package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.render.base.HtmlRendererBase

class PremiumHtmlRenderer : HtmlRendererBase() {
    override fun render(report: AndroidDoctorReport): String {
        val body = HtmlSections.buildPremiumBody(report)
        return HtmlTemplates.page(report, body, premium = true)
    }
}
