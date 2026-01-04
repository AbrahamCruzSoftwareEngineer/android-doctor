package com.evolutiondso.androiddoctor.cli.report

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

object ReportLoader {

    private val json = Json { ignoreUnknownKeys = true }

    fun load(pathString: String): AndroidDoctorReport? {
        return try {
            val path = Path.of(pathString)
            val text = Files.readString(path)
            json.decodeFromString<AndroidDoctorReport>(text)
        } catch (e: Exception) {
            println("Error reading report: ${e.message}")
            null
        }
    }
}
