package com.example.mrdetective.data.network

import com.example.mrdetective.data.BuildConfig

/** Replace the placeholder with the Supabase anon/publishable key. Never put service_role here. */
object SupabaseConfig {
    val baseUrl: String =
        BuildConfig.SUPABASE_URL.trim().let {
            if (it.isEmpty()) {
                it
            } else if (it.endsWith("/")) {
                it
            } else {
                "$it/"
            }
        }
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()

    /** Checks that the Supabase endpoint and public API key are configured. */
    fun validate() {
        require(baseUrl.isNotBlank()) {
            "SUPABASE_URL не задан. Добавьте его в local.properties в корне проекта."
        }
        require(baseUrl.startsWith("https://")) { "SUPABASE_URL должен начинаться с https://" }
        require(anonKey.isNotBlank() && anonKey != "PUT_SUPABASE_ANON_KEY_HERE") {
            "SUPABASE_ANON_KEY не задан. Добавьте публичный ключ Supabase в local.properties."
        }
    }
}
