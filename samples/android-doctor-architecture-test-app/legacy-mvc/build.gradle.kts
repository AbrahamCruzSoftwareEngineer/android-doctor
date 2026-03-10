plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.example.architecturesample.legacy"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("com.squareup.okhttp3:okhttp:3.12.0")
    implementation(project(":core-data"))
}
