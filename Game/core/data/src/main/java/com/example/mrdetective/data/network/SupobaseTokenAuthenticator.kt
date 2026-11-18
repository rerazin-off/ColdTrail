package com.example.mrdetective.data.network


import com.google.gson.JsonParser
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseTokenAuthenticator @Inject constructor(
    private val tokenStore: SessionTokenStore,
) : Authenticator {
    private val refreshClient = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Не допускаем бесконечный цикл повторных 401.
        if (responseCount(response) >= 2) return null
        val requestPath = response.request.url.encodedPath
        if (requestPath.endsWith("/auth/v1/token") || requestPath.endsWith("/auth/v1/signup")) return null
        val requestSession = response.request.tag(SessionTokenStore.Session::class.java) ?: return null

        synchronized(this) {
            // Другой запрос мог уже обновить токен, пока этот ожидал блокировку.
            val currentSession = tokenStore.session() ?: return null
            if (currentSession.generation != requestSession.generation) return null
            if (currentSession.access != requestSession.access) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${currentSession.access}")
                    .build()
            }

            val refreshToken = currentSession.refresh?.takeIf(String::isNotBlank) ?: return null
            val payload = """{"refresh_token":"${escapeJson(refreshToken)}"}"""
            val request = Request.Builder()
                .url(SupabaseConfig.baseUrl + "auth/v1/token?grant_type=refresh_token")
                .header("apikey", SupabaseConfig.anonKey)
                .header("Content-Type", "application/json")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            return try {
                refreshClient.newCall(request).execute().use { refreshResponse ->
                    if (!refreshResponse.isSuccessful) {
                        if (refreshResponse.code == 400 || refreshResponse.code == 401) {
                            tokenStore.updateSession(currentSession, null, null)
                        }
                        return null
                    }
                    val body = refreshResponse.body?.string() ?: return null
                    val json = JsonParser.parseString(body).asJsonObject
                    val access = json.get("access_token")?.asString ?: return null
                    val refreshed = json.get("refresh_token")?.asString ?: refreshToken
                    if (!tokenStore.updateSession(currentSession, access, refreshed)) return null
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $access")
                        .build()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++; prior = prior.priorResponse
        }
        return count
    }

    private fun escapeJson(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
}
