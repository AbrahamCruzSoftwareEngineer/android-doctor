package com.evolutiondso.androiddoctor

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidDoctorPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val metricsService = target.gradle.sharedServices.registerIfAbsent(
            "androidDoctorMetrics",
            BuildMetricsService::class.java
        ) {}

        target.gradle.addListener(metricsService.get())

        target.tasks.register(
            "androidDoctorCollect",
            AndroidDoctorCollectTask::class.java
        ) { task ->
            task.reportFile.set(
                target.layout.buildDirectory.file("androidDoctor/report.json")
            )
            task.metricsService.set(metricsService)
            task.outputs.upToDateWhen { false }
        }

        target.logger.lifecycle("AndroidDoctor plugin applied to project: ${target.path}")
    }
}
