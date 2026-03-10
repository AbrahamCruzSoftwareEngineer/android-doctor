plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.example.architecturesample.core.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation("androidx.room:room-runtime:2.2.6")
    implementation("androidx.room:room-ktx:2.2.6")
}
