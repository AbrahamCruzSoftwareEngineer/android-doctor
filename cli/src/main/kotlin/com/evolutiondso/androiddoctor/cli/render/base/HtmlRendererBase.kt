package com.evolutiondso.androiddoctor.cli.render.base

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class HtmlRendererBase {

    abstract fun render(report: AndroidDoctorReport): String

    fun renderToFile(report: AndroidDoctorReport, outputPath: String): String {
        val html = render(report)
        val path: Path = Paths.get(outputPath)

        Files.createDirectories(path.parent)
        Files.writeString(path, html)

        return path.toAbsolutePath().toString()
    }
}
