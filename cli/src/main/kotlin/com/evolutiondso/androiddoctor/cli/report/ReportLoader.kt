package com.evolutiondso.androiddoctor.cli.report

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object ReportLoader {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    fun load(path: String): AndroidDoctorReport? {
        val resolved = resolvePath(path)

        return try {
            val text = Files.readString(resolved)
            json.decodeFromString<AndroidDoctorReport>(text)
        } catch (e: Exception) {
            println("❌ Error reading report: $resolved")
            println("   Reason: ${e.message}")
            null
        }
    }

    /**
     * Resolves a path safely, even when Gradle changes the working directory.
     */
    private fun resolvePath(path: String): Path {
        val p = Paths.get(path)

        // If absolute → OK
        if (p.isAbsolute) return p.normalize()

        // Try repo root JVM flag passed via build.gradle (best option)
        val repoRoot = System.getProperty("androiddoctor.repoRoot")
        if (!repoRoot.isNullOrEmpty()) {
            return Paths.get(repoRoot).resolve(path).normalize()
        }

        // Fallback: use current working directory
        return Paths.get("").toAbsolutePath().resolve(path).normalize()
    }
}
