package com.evolutiondso.androiddoctor.cli.render.base

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport

abstract class HtmlRendererBase {
    abstract fun render(report: AndroidDoctorReport): String

    protected fun wrapHtml(content: String): String = """
        <html>
        <head>
            <meta charset="utf-8" />
            <style>
                body { font-family: Arial, sans-serif; padding: 24px; }
                h1 { color: #2b4eff; }
                .summary { margin-bottom: 24px; }
                .section { margin-top: 32px; }
                .action { border: 1px solid #ddd; padding: 12px; margin-bottom: 12px; border-radius: 6px; }
            </style>
        </head>
        <body>
            $content
        </body>
        </html>
    """.trimIndent()
}
