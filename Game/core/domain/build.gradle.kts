plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
dependencies {
    // нужен для @Inject в use case'ах (в domain нет android.*)
    implementation("javax.inject:javax.inject:1")

    // только для тестов (src/test); тесты используют свои Fake-классы, MockK не нужен
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

// Проверка: domain — чистый Kotlin/JVM и не импортирует android.* / androidx.*
// Запуск: ./gradlew :core:domain:checkNoAndroidImports (также входит в :core:domain:check)
val checkNoAndroidImports by tasks.registering {
    group = "verification"
    description = "Падает, если в domain есть import android.* или androidx.*"
    val sources = layout.projectDirectory.dir("src").asFile
    doLast {
        val violations =
            sources.walkTopDown()
                .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
                .flatMap { file ->
                    file.readLines().withIndex()
                        .filter { (_, line) ->
                            val l = line.trim()
                            l.startsWith("import android.") || l.startsWith("import androidx.")
                        }
                        .map { (i, line) -> "${file.relativeTo(sources)}:${i + 1}: ${line.trim()}" }
                }
                .toList()
        if (violations.isNotEmpty()) {
            throw GradleException("domain не должен импортировать android.*:\n" + violations.joinToString("\n"))
        }
    }
}
tasks.named("check") { dependsOn(checkNoAndroidImports) }
