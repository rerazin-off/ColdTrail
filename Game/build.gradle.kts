import org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask

// Общие настройки сборки для всех модулей проекта.
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

    // Эта версия плагина ktlint не находит исходники Kotlin, подключённые через встроенную поддержку Kotlin в AGP.
    listOf("com.android.application", "com.android.library").forEach { androidPlugin ->
        plugins.withId(androidPlugin) {
            val kotlinSources = fileTree("src") { include("**/*.kt") }
            tasks.withType<BaseKtLintCheckTask>().configureEach {
                if (name.endsWith("OverKotlinScripts")) source(kotlinSources)
            }
        }
    }
}
