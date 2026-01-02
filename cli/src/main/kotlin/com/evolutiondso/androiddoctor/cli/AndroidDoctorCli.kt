package com.evolutiondso.androiddoctor.cli

fun main(args: Array<String>) {
    println("AndroidDoctor CLI (skeleton)")
    println("================================")
    println("This is a placeholder CLI entry point.")
    println("Eventually, this will read report.json and generate reports.")
    if (args.isNotEmpty()) {
        println()
        println("Args passed: ${args.joinToString(" ")}")
    }
}
