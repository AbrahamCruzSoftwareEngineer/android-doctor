plugins {
    kotlin("jvm")
    application
    kotlin("plugin.serialization")
    jacoco
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("com.evolutiondso.androiddoctor.cli.MainKt")
    applicationDefaultJvmArgs = listOf(
        "-Dandroiddoctor.repoRoot=${rootProject.rootDir.absolutePath}"
    )
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit)
}

tasks.jacocoTestCoverageVerification {
    description = "Verifies minimum unit test coverage for core free CLI report/render classes."
    group = "verification"
    dependsOn(tasks.test)

    violationRules {
        rule {
            element = "CLASS"
            includes = listOf(
                "com.evolutiondso.androiddoctor.cli.report.ReportLoader",
                "com.evolutiondso.androiddoctor.cli.report.ReportAnalyzer",
                "com.evolutiondso.androiddoctor.cli.render.html.FreeHtmlRenderer",
                "com.evolutiondso.androiddoctor.cli.render.html.HtmlSections",
                "com.evolutiondso.androiddoctor.cli.render.html.HtmlTemplates",
                "com.evolutiondso.androiddoctor.cli.render.html.HtmlGauge",
                "com.evolutiondso.androiddoctor.cli.render.html.HtmlAssets"
            )
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}


tasks.test {
    description = "Runs unit tests for CLI report loading and rendering."
    group = "verification"
    useJUnitPlatform()
}
