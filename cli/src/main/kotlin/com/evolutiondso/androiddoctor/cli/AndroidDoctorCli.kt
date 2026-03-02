package com.evolutiondso.androiddoctor.cli

import com.evolutiondso.androiddoctor.cli.capabilities.AndroidDoctorCapabilities
import com.evolutiondso.androiddoctor.cli.capabilities.CapabilityPlan
import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.report.ReportLoader

object AndroidDoctorCli {

    fun run(args: Array<String>) {
        val router = CommandRouter()

        if (args.isEmpty()) {
            router.printHelp()
            return
        }

        when (args[0]) {
            "--help", "-h" -> {
                router.printHelp()
                return
            }

            "--report", "-r" -> {
                if (args.size < 2) {
                    println("❌ Missing report file path after --report")
                    return
                }

                val reportPath = args[1]
                val report: AndroidDoctorReport = ReportLoader.load(reportPath)
                    ?: return

                val plan = detectPlan(report)
                val capabilities = AndroidDoctorCapabilities.forPlan(plan)

                router.handleReport(report, capabilities, args)
            }

            else -> {
                println("❌ Unknown command: ${args[0]}")
                router.printHelp()
            }
        }
    }

    /**
     * For now: determines free/premium based on module count.
     * Later: this will be driven by login/auth/API/etc.
     */
    private fun detectPlan(report: AndroidDoctorReport): CapabilityPlan {
        val modules = report.checks?.moduleCount ?: 0
        return if (modules > 5) CapabilityPlan.PREMIUM else CapabilityPlan.FREE
    }
}
