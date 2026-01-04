package com.evolutiondso.androiddoctor.cli.identity

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

object LicenseValidator {

    private val licensePath: Path =
        Path.of(System.getProperty("user.home"), ".androiddoctor", "license.key")

    fun findLicenseKey(): String? {
        // 1. ENV variable
        val env = System.getenv("ANDROID_DOCTOR_LICENSE")
        if (!env.isNullOrBlank()) return env.trim()

        // 2. Local license file
        return if (licensePath.exists()) {
            Files.readString(licensePath).trim().ifBlank { null }
        } else null
    }
}
