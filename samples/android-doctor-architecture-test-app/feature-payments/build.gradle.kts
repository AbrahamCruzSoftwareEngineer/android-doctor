plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.example.architecturesample.feature.payments"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.5.0")
    implementation("com.squareup.retrofit2:retrofit:2.6.0")
    implementation("com.squareup.okhttp3:okhttp:4.3.1")
    implementation(project(":core-data"))
}
