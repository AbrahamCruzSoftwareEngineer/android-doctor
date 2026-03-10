package com.evolutiondso.androiddoctor

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AndroidDoctorCollectTaskConfigFilterTest {

    private val method = Class
        .forName("com.evolutiondso.androiddoctor.AndroidDoctorCollectTaskKt")
        .getDeclaredMethod("shouldAnalyzeResolvedConfiguration", String::class.java)
        .apply { isAccessible = true }

    private fun shouldAnalyze(name: String): Boolean = method.invoke(null, name) as Boolean

    @Test
    fun `includes supported dependency classpaths and metadata`() {
        assertTrue(shouldAnalyze("debugCompileClasspath"))
        assertTrue(shouldAnalyze("releaseRuntimeClasspath"))
        assertTrue(shouldAnalyze("debugImplementationDependenciesMetadata"))
        assertTrue(shouldAnalyze("releaseApiDependenciesMetadata"))
    }

    @Test
    fun `excludes test lint detached and compiler plugin configurations`() {
        assertFalse(shouldAnalyze("debugAndroidTestCompileClasspath"))
        assertFalse(shouldAnalyze("testDebugUnitTestRuntimeClasspath"))
        assertFalse(shouldAnalyze("lintClassPath"))
        assertFalse(shouldAnalyze("detachedConfiguration1"))
        assertFalse(shouldAnalyze("kotlinCompilerPluginClasspathMain"))
    }

    @Test
    fun `excludes unsupported arbitrary configurations`() {
        assertFalse(shouldAnalyze("archives"))
        assertFalse(shouldAnalyze("default"))
        assertFalse(shouldAnalyze("kapt"))
    }
}
