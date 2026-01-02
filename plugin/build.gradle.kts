plugins {
    kotlin("jvm") version "1.9.24"
    `java-gradle-plugin`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
}

gradlePlugin {
    plugins {
        create("androidDoctor") {
            id = "com.evolutiondso.androiddoctor"
            implementationClass = "com.evolutiondso.androiddoctor.AndroidDoctorPlugin"
            displayName = "AndroidDoctor Gradle Plugin"
            description = "Advisory tool for Android build health & Compose modernization (skeleton, no analysis yet)."
        }
    }
}
