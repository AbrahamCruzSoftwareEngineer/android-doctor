package com.evolutiondso.androiddoctor.cli.render.pdf

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import java.nio.file.Files
import java.nio.file.Paths

object PdfRenderer {

    fun renderToFile(report: AndroidDoctorReport, outputPath: String): String {
        val path = Paths.get(outputPath)
        Files.createDirectories(path.parent)

        PDDocument().use { doc ->
            val page = PDPage()
            doc.addPage(page)

            PDPageContentStream(doc, page).use { stream ->
                stream.beginText()
                stream.setFont(PDType1Font.HELVETICA_BOLD, 18f)
                stream.newLineAtOffset(50f, 750f)
                stream.showText("AndroidDoctor Report Summary")
                stream.endText()

                var y = 720f

                fun writeLine(text: String) {
                    stream.beginText()
                    stream.setFont(PDType1Font.HELVETICA, 12f)
                    stream.newLineAtOffset(50f, y)
                    stream.showText(text)
                    stream.endText()
                    y -= 20f
                }

                writeLine("Project: ${report.project?.name ?: "<unknown>"}")
                writeLine("Status: ${report.status ?: "<unknown>"}")
                writeLine("Generated At: ${report.generatedAt ?: "<unknown>"}")
                writeLine("")

                writeLine("Scores:")
                writeLine("Build Health: ${report.scores?.buildHealth ?: "?"}")
                writeLine("Modernization: ${report.scores?.modernization ?: "?"}")
                writeLine("")

                writeLine("Top Actions:")
                report.actions.orEmpty().take(5).forEach { action ->
                    writeLine("- ${action.title}")
                }
            }

            doc.save(path.toFile())
        }

        return path.toAbsolutePath().toString()
    }
}
