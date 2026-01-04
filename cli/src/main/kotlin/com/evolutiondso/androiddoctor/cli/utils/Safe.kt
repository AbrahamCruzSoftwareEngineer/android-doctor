package com.evolutiondso.androiddoctor.cli.utils

inline fun <T> safe(block: () -> T): T? {
    return try {
        block()
    } catch (e: Throwable) {
        Console.error("Error: ${e.message}")
        null
    }
}
