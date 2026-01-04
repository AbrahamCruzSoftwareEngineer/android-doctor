package com.evolutiondso.androiddoctor.cli.utils

import java.nio.file.Files
import java.nio.file.Path

object FileUtils {

    fun write(path: Path, content: String) {
        try {
            Files.createDirectories(path.parent ?: Path.of("."))
            Files.writeString(path, content)
            Console.success("✓ Written: $path")
        } catch (e: Exception) {
            Console.error("Failed to write to $path: ${e.message}")
        }
    }

    fun ensureDir(path: Path) {
        try {
            Files.createDirectories(path)
        } catch (_: Throwable) { }
    }
}
