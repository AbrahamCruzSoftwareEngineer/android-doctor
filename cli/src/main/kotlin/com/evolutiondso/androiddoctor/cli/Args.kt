package com.evolutiondso.androiddoctor.cli

data class ParsedArgs(
    val command: String? = null,
    val subcommand: String? = null,
    val reportPath: String? = null,
    val premium: Boolean = false
)

fun parseArgs(args: List<String>): ParsedArgs {
    if (args.isEmpty()) return ParsedArgs(command = "help")

    var command: String? = null
    var report: String? = null
    var premium = false

    val iter = args.iterator()

    while (iter.hasNext()) {
        when (val arg = iter.next()) {

            // Commands
            "analyze" -> command = "analyze"
            "export" -> {
                if (!iter.hasNext()) error("export requires html|md|pdf")
                val format = iter.next()
                command = when (format) {
                    "html" -> "export-html"
                    "md" -> "export-md"
                    "pdf" -> "export-pdf"
                    else -> error("Unknown export format: $format")
                }
            }

            "help" -> return ParsedArgs(command = "help")

            // Flags
            "--report", "-r" -> {
                if (!iter.hasNext()) error("--report requires a path")
                report = iter.next()
            }

            "--premium" -> premium = true

            else -> error("Unknown argument: $arg")
        }
    }

    return ParsedArgs(
        command = command,
        reportPath = report,
        premium = premium
    )
}

private fun error(msg: String): Nothing {
    println("Error: $msg")
    println("Run `android-doctor help` for usage.")
    throw IllegalArgumentException(msg)
}
