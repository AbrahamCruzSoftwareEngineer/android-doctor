pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.jetbrains.kotlin.jvm",
                "org.jetbrains.kotlin.plugin.serialization" ->
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "android-doctor"

include(":core")
include(":plugin")
include(":cli")
