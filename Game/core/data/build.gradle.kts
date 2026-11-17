
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.secrets)
}

// Читаем local.properties из корня проекта
val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")

        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }

android {
    namespace = "com.example.mrdetective.data"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        // Создаём поля в BuildConfig
        val supabaseUrl = localProperties.getProperty("SUPABASE_URL", "").trim().trimEnd('/')
        val supabaseKey = localProperties.getProperty("SUPABASE_ANON_KEY", "").trim()
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl/\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseKey\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/NOTICE"
        }
    }
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.room3.common)
    implementation(libs.androidx.room3.runtime)
    implementation(libs.material)

    implementation("com.google.dagger:hilt-android:2.58")
    ksp(libs.hilt.compiler)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp — HTTP-клиент Retrofit
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Gson
    implementation("com.google.code.gson:gson:2.11.0")

    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    coreLibraryDesugaring(
        "com.android.tools:desugar_jdk_libs:2.0.4",
    )

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

secrets {
    propertiesFileName = "local.properties"
}
