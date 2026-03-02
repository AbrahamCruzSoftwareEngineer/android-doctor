plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.example.architecturesample.feature.chat"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation(project(":core-data"))
}
