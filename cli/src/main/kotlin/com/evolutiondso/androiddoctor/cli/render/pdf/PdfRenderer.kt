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

                writeLine("Build Performance:")
                writeLine("Config: ${report.performance?.configurationMs?.let { "${it} ms" } ?: report.diagnostics?.configuration?.durationMs?.let { "${it} ms" } ?: "Unknown"}")
                writeLine("Execution: ${report.performance?.executionMs?.let { "${it} ms" } ?: report.diagnostics?.execution?.durationMs?.let { "${it} ms" } ?: "Unknown"}")
                val cache = report.cache ?: report.diagnostics?.buildCache
                writeLine("Build Cache: Hits ${cache?.hits ?: 0} / Misses ${cache?.misses ?: 0}")
                report.diagnostics?.execution?.topLongestTasks.orEmpty().take(5).forEach { task ->
                    writeLine("  - ${task.path ?: "<task>"} (${task.durationMs ?: "?"} ms)")
                }
                writeLine("")

                writeLine("Configuration Cache:")
                writeLine("Requested: ${report.diagnostics?.configurationCache?.requested ?: "Unknown"}")
                writeLine("Stored: ${report.diagnostics?.configurationCache?.stored ?: "Unknown"}")
                writeLine("Reused: ${report.diagnostics?.configurationCache?.reused ?: "Unknown"}")
                writeLine("Incompatible Tasks: ${report.diagnostics?.configurationCache?.incompatibleTasks ?: "Unknown"}")
                writeLine("")

                writeLine("Toolchain:")
                writeLine("Kotlin Compiler: ${report.tooling?.kotlinCompilerVersion ?: "Unknown"}")
                writeLine("Kotlin JVM Target: ${report.toolchain?.kotlinJvmTarget ?: "Unknown"}")
                writeLine("Java Toolchain: ${report.toolchain?.javaToolchainVersion ?: "Unknown"}")
                writeLine("AGP: ${report.android?.agpVersion ?: "Unknown"}")
                writeLine("compileSdk: ${report.android?.compileSdk ?: "Unknown"}")
                writeLine("")

                writeLine("Dependencies:")
                writeLine("Outdated: ${report.dependencies?.outdated?.size ?: 0}")
                writeLine("Duplicates: ${report.dependencies?.duplicates?.size ?: 0}")
                writeLine("Unused: ${report.dependencies?.unused?.size ?: 0}")
                writeLine("Heavy: ${report.dependencies?.heavy?.size ?: 0}")
                writeLine("")

                writeLine("Modules:")
                val moduleDiagnostics = report.modulesDiagnostics
                val moduleSummaries = report.modules
                writeLine("Count: ${moduleDiagnostics?.count ?: moduleDiagnostics?.modules?.size ?: moduleSummaries?.size ?: 0}")
                moduleDiagnostics?.modules.orEmpty().take(5).forEach { module ->
                    writeLine("  - ${module.path ?: "<module>"} (${module.taskCount ?: 0} tasks)")
                }
                moduleSummaries?.take(5)?.forEach { module ->
                    writeLine("  - ${module.name ?: "<module>"} (${module.tasks ?: 0} tasks)")
                }
                writeLine("")

                writeLine("Annotation Processing:")
                writeLine("Processors: ${report.annotationProcessing?.processors?.size ?: 0}")
                writeLine("Total Time: ${report.annotationProcessing?.totalProcessingMs?.let { "${it} ms" } ?: "Unknown"}")
                writeLine("")

                writeLine("Compose:")
                writeLine("Enabled: ${report.android?.composeEnabled ?: "Unknown"}")
                writeLine("Compiler: ${report.android?.composeCompilerVersion ?: "Unknown"}")
                writeLine("")

                writeLine("Environment:")
                writeLine("OS: ${report.environment?.os ?: "Unknown"}")
                writeLine("Arch: ${report.environment?.arch ?: "Unknown"}")
                writeLine("CI: ${report.environment?.ci ?: "Unknown"}")
                writeLine("RAM: ${report.environment?.availableRamMb?.let { "${it} MB" } ?: "Unknown"}")
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
