package com.evolutiondso.androiddoctor.cli.capabilities

object AndroidDoctorCapabilities {

    fun forPlan(plan: CapabilityPlan): CapabilitySet =
        when (plan) {
            CapabilityPlan.FREE -> FreeCapabilities
            CapabilityPlan.PREMIUM -> PremiumCapabilities
        }
}
