package com.evolutiondso.androiddoctor.cli.utils

import java.nio.file.Path

object Paths {
    fun htmlOut(): Path = Path.of("android-doctor-report.html")
    fun mdOut(): Path = Path.of("android-doctor-report.md")
    fun pdfOut(): Path = Path.of("android-doctor-report.pdf")
}
