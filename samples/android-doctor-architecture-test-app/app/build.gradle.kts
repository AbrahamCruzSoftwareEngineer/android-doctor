plugins {
    id("com.android.application")
    kotlin("android")
    id("com.evolutiondso.androiddoctor")
}

android {
    namespace = "com.example.architecturesample"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.architecturesample"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("com.google.android.material:material:1.2.1")
    implementation("com.squareup.retrofit2:retrofit:2.7.2")
    implementation("com.squareup.okhttp3:okhttp:4.4.0")

    implementation(project(":feature-chat"))
}
