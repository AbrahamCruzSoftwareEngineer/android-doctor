package com.evolutiondso.androiddoctor.core.api

import com.evolutiondso.androiddoctor.core.model.ActionInfo
import com.evolutiondso.androiddoctor.core.model.AndroidDoctorReport

interface RecommendationProvider {
    fun recommendationsFor(report: AndroidDoctorReport): List<ActionInfo>
}
