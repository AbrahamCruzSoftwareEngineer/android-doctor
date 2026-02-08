package com.evolutiondso.androiddoctor

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class AndroidDoctorPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create(
            "androidDoctor",
            AndroidDoctorExtension::class.java,
            target.objects
        )
        val metricsService = target.gradle.sharedServices.registerIfAbsent(
            "androidDoctorMetrics",
            BuildMetricsService::class.java
        ) {}

        target.gradle.addListener(metricsService.get())
        target.rootProject.allprojects { project ->
            project.tasks.withType(Test::class.java).configureEach {
                it.addTestListener(metricsService.get())
            }
        }

        if (target == target.rootProject) {
            target.rootProject.allprojects { module ->
                val analysisTask = module.tasks.register("androidDoctorRunAnalysis") { task ->
                    task.group = "verification"
                    task.description = "Runs build/test tasks used for AndroidDoctor analysis."
                }

                val isSampleModule = module.projectDir.path.contains("/samples/")
                if (extension.autoRunBuilds.get()) {
                    module.tasks.matching { it.name == "assembleDebug" || it.name == "assemble" }
                        .configureEach { analysisTask.configure { it.dependsOn(this) } }
                }
                if (extension.autoRunTests.get()) {
                    module.tasks.matching { it.name == "testDebugUnitTest" || it.name == "test" }
                        .configureEach { analysisTask.configure { it.dependsOn(this) } }
                }
                if (extension.autoRunTests.get()) {
                    module.tasks.matching { it.name == "connectedDebugAndroidTest" }
                        .configureEach { analysisTask.configure { it.dependsOn(this) } }
                }
                if (extension.autoRunSampleApps.get() && isSampleModule) {
                    module.tasks.matching { it.name == "assembleDebug" }
                        .configureEach { analysisTask.configure { it.dependsOn(this) } }
                }
            }
        }

        target.tasks.register("androidDoctorCollect", AndroidDoctorCollectTask::class.java) { task ->
            task.reportFile.set(target.layout.buildDirectory.file("androidDoctor/report.json"))
            task.metricsService.set(metricsService)
            task.outputs.upToDateWhen { false }
        }

        target.afterEvaluate {
            val analysisTask = target.tasks.findByName("androidDoctorRunAnalysis")
            val collectTask = target.tasks.findByName("androidDoctorCollect")
            if (analysisTask != null && collectTask != null) {
                if (extension.autoRunBuilds.get() || extension.autoRunTests.get() || extension.autoRunSampleApps.get()) {
                    collectTask.dependsOn(analysisTask)
                }
            }
        }

        target.logger.lifecycle("AndroidDoctor plugin applied to project: ${target.path}")
    }
}
