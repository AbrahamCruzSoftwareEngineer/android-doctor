package com.evolutiondso.androiddoctor.cli.render.base

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import java.nio.file.Files
import java.nio.file.Paths

abstract class HtmlRendererBase {

    abstract fun render(report: AndroidDoctorReport): String

    fun renderToFile(report: AndroidDoctorReport, path: String): String {
        val html = render(report)
        val file = Paths.get(path)

        Files.createDirectories(file.parent)
        Files.writeString(file, html)

        return file.toAbsolutePath().toString()
    }
}
