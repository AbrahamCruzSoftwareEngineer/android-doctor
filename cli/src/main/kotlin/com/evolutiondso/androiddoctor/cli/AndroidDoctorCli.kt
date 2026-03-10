package com.evolutiondso.androiddoctor.cli

import com.evolutiondso.androiddoctor.core.model.AndroidDoctorReport
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

                router.handleReport(report, args)
            }

            else -> {
                println("❌ Unknown command: ${args[0]}")
                router.printHelp()
            }
        }
    }
}
