package com.evolutiondso.androiddoctor.core.api

import com.evolutiondso.androiddoctor.core.model.AndroidDoctorReport

interface ReportRenderer {
    fun render(report: AndroidDoctorReport): String
}
