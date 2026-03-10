package com.evolutiondso.androiddoctor.core.api

import com.evolutiondso.androiddoctor.core.model.AndroidDoctorReport

interface Exporter {
    fun export(report: AndroidDoctorReport, outputPath: String): String
}
