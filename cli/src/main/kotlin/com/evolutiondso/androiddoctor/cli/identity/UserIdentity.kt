package com.evolutiondso.androiddoctor.cli.identity

sealed class UserIdentity(val isPremium: Boolean) {

    object Free : UserIdentity(false)

    data class Premium(
        val licenseKey: String
    ) : UserIdentity(true)

    companion object {
        fun detect(): UserIdentity {
            val key = LicenseValidator.findLicenseKey() ?: return Free
            return Premium(key)
        }
    }
}
