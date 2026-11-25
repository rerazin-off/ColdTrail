import org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
}
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    // This ktlint plugin version does not discover Kotlin sources from AGP's built-in Kotlin.
    listOf("com.android.application", "com.android.library").forEach { androidPlugin ->
        plugins.withId(androidPlugin) {
            val kotlinSources = fileTree("src") { include("**/*.kt") }
            tasks.withType<BaseKtLintCheckTask>().configureEach {
                if (name.endsWith("OverKotlinScripts")) source(kotlinSources)
            }
        }
    }
}
