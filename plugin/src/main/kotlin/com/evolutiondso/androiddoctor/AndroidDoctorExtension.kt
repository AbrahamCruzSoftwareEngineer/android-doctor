package com.evolutiondso.androiddoctor

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class AndroidDoctorExtension(objects: ObjectFactory) {
    val autoRunBuilds: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val autoRunTests: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val autoRunSampleApps: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
}
