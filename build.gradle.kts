plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    /**
     * Remove once Dagger and Hilt support for KSP is stable
     *   https://kotlinlang.org/docs/ksp-overview.html#supported-libraries
     */
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.compose.compiler) apply false
}
