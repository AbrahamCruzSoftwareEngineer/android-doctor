package com.evolutiondso.androiddoctor

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidDoctorPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        target.tasks.register(
            "androidDoctorCollect",
            AndroidDoctorCollectTask::class.java
        ) { task ->
            task.reportFile.set(
                target.layout.buildDirectory.file("androidDoctor/report.json")
            )
            task.outputs.upToDateWhen { false }
        }

        target.logger.lifecycle("AndroidDoctor plugin applied to project: ${target.path}")
    }
}
