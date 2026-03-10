package com.evolutiondso.androiddoctor.cli

data class ParsedArgs(
    val command: String? = null,
    val subcommand: String? = null,
    val reportPath: String? = null
)

fun parseArgs(args: List<String>): ParsedArgs {
    if (args.isEmpty()) return ParsedArgs(command = "help")

    var command: String? = null
    var report: String? = null

    val iter = args.iterator()

    while (iter.hasNext()) {
        when (val arg = iter.next()) {
            "analyze" -> command = "analyze"
            "export" -> {
                if (!iter.hasNext()) error("export requires html|md")
                val format = iter.next()
                command = when (format) {
                    "html" -> "export-html"
                    "md" -> "export-md"
                    else -> error("Unknown export format: $format")
                }
            }

            "help" -> return ParsedArgs(command = "help")

            "--report", "-r" -> {
                if (!iter.hasNext()) error("--report requires a path")
                report = iter.next()
            }

            else -> error("Unknown argument: $arg")
        }
    }

    return ParsedArgs(
        command = command,
        reportPath = report
    )
}

private fun error(msg: String): Nothing {
    println("Error: $msg")
    println("Run `android-doctor help` for usage.")
    throw IllegalArgumentException(msg)
}
