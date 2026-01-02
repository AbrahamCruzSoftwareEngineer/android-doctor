plugins {
    kotlin("jvm") version "1.9.24"
    id("com.evolutiondso.androiddoctor") version "0.0.1-SNAPSHOT"
}

version = "0.0.1-SNAPSHOT"
group = "com.evolutiondso.samples"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
}
