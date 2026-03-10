plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.evolutiondso.androiddoctor")
}

android {
    namespace = "com.example.architecturesample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.architecturesample"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("com.google.android.material:material:1.4.0")

    implementation(project(":feature-chat"))
    implementation(project(":feature-payments"))
    implementation(project(":legacy-mvc"))
    implementation(project(":core-data"))

    kapt("com.google.dagger:dagger-compiler:2.44")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.24")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
