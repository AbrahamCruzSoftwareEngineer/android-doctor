package com.evolutiondso.androiddoctor

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class AndroidDoctorPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val metricsService = target.gradle.sharedServices.registerIfAbsent(
            "androidDoctorMetrics",
            BuildMetricsService::class.java
        ) {}

        target.gradle.addListener(metricsService.get())

        val extension = target.extensions.create(
            "androidDoctor",
            AndroidDoctorExtension::class.java,
            target.objects
        )

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

    private fun registerBuildOperationListener(target: Project, metricsService: BuildMetricsService) {
        try {
            val listenerClass = Class.forName("org.gradle.internal.operations.BuildOperationListener")
            val managerClass = Class.forName("org.gradle.internal.operations.BuildOperationListenerManager")
            val services = target.gradle.javaClass.methods.firstOrNull { method ->
                method.name == "getServices" && method.parameterCount == 0
            }?.invoke(target.gradle) ?: return
            val getService = services.javaClass.methods.firstOrNull { method ->
                method.name == "get" && method.parameterTypes.size == 1
            }
                ?: return
            val manager = getService.invoke(services, managerClass) ?: return
            val proxy = java.lang.reflect.Proxy.newProxyInstance(
                listenerClass.classLoader,
                arrayOf(listenerClass)
            ) { _, method, _ ->
                when (method.returnType) {
                    java.lang.Boolean.TYPE -> false
                    java.lang.Integer.TYPE -> 0
                    java.lang.Long.TYPE -> 0L
                    java.lang.Double.TYPE -> 0.0
                    java.lang.Float.TYPE -> 0f
                    java.lang.Short.TYPE -> 0.toShort()
                    java.lang.Byte.TYPE -> 0.toByte()
                    java.lang.Character.TYPE -> 0.toChar()
                    else -> null
                }
            }
            val addListener = managerClass.methods.firstOrNull { it.name == "addListener" && it.parameterTypes.size == 1 }
                ?: return
            addListener.invoke(manager, proxy)
        } catch (_: Throwable) {
            // Best-effort: BuildOperationListener is internal and may not be available.
        }
    }
}
