package com.evolutiondso.androiddoctor.cli.utils

object Console {

    private const val RESET = "\u001B[0m"
    private const val RED = "\u001B[31m"
    private const val GREEN = "\u001B[32m"
    private const val YELLOW = "\u001B[33m"
    private const val BLUE = "\u001B[34m"
    private const val CYAN = "\u001B[36m"
    private const val BOLD = "\u001B[1m"

    fun title(text: String) {
        println("\n$BOLD$BLUE$text$RESET")
    }

    fun subtitle(text: String) {
        println("\n$CYAN$text$RESET")
    }

    fun success(msg: String) {
        println("$GREEN$msg$RESET")
    }

    fun warn(msg: String) {
        println("$YELLOW⚠ $msg$RESET")
    }

    fun error(msg: String) {
        println("$RED✗ $msg$RESET")
    }

    fun info(msg: String) {
        println("$CYAN$msg$RESET")
    }
}
