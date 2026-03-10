package com.evolutiondso.androiddoctor.cli

import com.evolutiondso.androiddoctor.core.model.ActionInfo
import com.evolutiondso.androiddoctor.core.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.core.model.ImpactInfo
import com.evolutiondso.androiddoctor.core.model.ProjectInfo
import com.evolutiondso.androiddoctor.core.model.ScoresInfo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

class FreeOnlyCliRegressionTest {

    private val report = AndroidDoctorReport(
        project = ProjectInfo(name = "demo-app", path = ":app"),
        scores = ScoresInfo(buildHealth = 80, modernization = 75),
        actions = listOf(
            ActionInfo(
                title = "Enable configuration cache",
                why = "Improves configuration time",
                how = "Set org.gradle.configuration-cache=true",
                impact = ImpactInfo(buildHealthDelta = 4, modernizationDelta = 1)
            )
        )
    )

    @BeforeEach
    fun cleanOutput() {
        Path.of("build", "androidDoctor").toFile().deleteRecursively()
    }

    @Test
    fun `help text does not expose premium or pdf options`() {
        val output = captureStdout {
            CommandRouter().printHelp()
        }

        assertTrue(output.contains("--html"))
        assertTrue(output.contains("--md"))
        assertFalse(output.contains("--pdf"))
        assertFalse(output.contains("Premium"))
    }

    @Test
    fun `html export produces free report without premium markers`() {
        CommandRouter().handleReport(report, arrayOf("--report", "ignored.json", "--html"))

        val htmlPath = Path.of("build", "androidDoctor", "html", "report.html")
        assertTrue(Files.exists(htmlPath))

        val html = Files.readString(htmlPath)
        assertTrue(html.contains("AndroidDoctor Report"))
        assertFalse(html.contains("Premium"))
        assertFalse(html.contains("chart.js"))
    }

    @Test
    fun `markdown export keeps free branding`() {
        CommandRouter().handleReport(report, arrayOf("--report", "ignored.json", "--md"))

        val mdPath = Path.of("build", "androidDoctor", "markdown", "report.md")
        assertTrue(Files.exists(mdPath))

        val markdown = Files.readString(mdPath)
        assertTrue(markdown.contains("# AndroidDoctor Markdown Report"))
        assertFalse(markdown.contains("Premium"))
    }

    @Test
    fun `parseArgs rejects pdf export format`() {
        assertThrows(IllegalArgumentException::class.java) {
            parseArgs(listOf("export", "pdf"))
        }
    }

    private fun captureStdout(block: () -> Unit): String {
        val originalOut = System.out
        val out = ByteArrayOutputStream()
        try {
            System.setOut(PrintStream(out))
            block()
            return out.toString()
        } finally {
            System.setOut(originalOut)
        }
    }
}
