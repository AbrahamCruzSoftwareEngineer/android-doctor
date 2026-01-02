plugins {
    kotlin("jvm") version "1.9.24"
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("com.evolutiondso.androiddoctor.cli.AndroidDoctorCliKt")
}

dependencies {
}
