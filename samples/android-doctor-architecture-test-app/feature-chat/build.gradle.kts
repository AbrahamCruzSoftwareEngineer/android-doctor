plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.example.architecturesample.feature.chat"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation(project(":app"))
}
