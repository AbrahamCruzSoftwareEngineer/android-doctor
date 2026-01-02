package com.evolutiondso.androiddoctor

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class AndroidDoctorCollectTask : DefaultTask() {

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    init {
        group = "verification"
        description = "Collect AndroidDoctor diagnostics (skeleton, writes a stub report.json)."
    }

    @TaskAction
    fun run() {
        val file = reportFile.get().asFile
        file.parentFile.mkdirs()

        val stubJson = """
            {
              "schemaVersion": 1,
              "status": "skeleton",
              "message": "AndroidDoctor report stub. No real checks implemented yet.",
              "generatedBy": "AndroidDoctor Gradle Plugin",
              "version": "0.0.1-SNAPSHOT"
            }
        """.trimIndent()

        file.writeText(stubJson)

        logger.lifecycle("AndroidDoctor: stub report written to ${file.absolutePath}")
    }
}
