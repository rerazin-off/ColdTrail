package com.example.mrdetective.data.remote.api

import com.example.mrdetective.data.remote.dto.CaseDto
import com.example.mrdetective.data.remote.dto.ClueDto
import com.example.mrdetective.data.remote.dto.NoteDto
import com.example.mrdetective.data.remote.dto.NoteInsertDto
import com.example.mrdetective.data.remote.dto.SuspectDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/** Содержит никнейм из записи профиля Supabase. */
data class ProfileNicknameDto(val nickname: String)

/** Интерфейс Retrofit для чтения публичных данных и работы с заметками расследований. */
interface SupabaseRestApi {
    /** Запрашивает никнейм указанного аккаунта. */
    @GET("rest/v1/profiles?select=nickname")
    suspend fun getNickname(
        @Query("id") userFilter: String,
    ): List<ProfileNicknameDto>

    /** Возвращает записи доступных дел из Supabase. */
    @GET("rest/v1/cases?select=*")
    suspend fun getCases(): List<CaseDto>

    /** Ищет дело по фильтру идентификатора. */
    @GET("rest/v1/cases?select=*")
    suspend fun getCase(
        @Query("id") idFilter: String,
    ): List<CaseDto>

    /** Ищет подозреваемых в выбранном деле. */
    @GET("rest/v1/suspects?select=id,case_id,name,profile,photo,created_at")
    suspend fun getSuspects(
        @Query("case_id") caseFilter: String,
    ): List<SuspectDto>

    /** Ищет публичный профиль подозреваемого по идентификатору. */
    @GET("rest/v1/suspects?select=id,case_id,name,profile,photo,created_at")
    suspend fun getSuspect(
        @Query("id") idFilter: String,
    ): List<SuspectDto>

    /** Ищет улику по фильтру идентификатора. */
    @GET("rest/v1/clues?select=*")
    suspend fun getClue(
        @Query("id") idFilter: String,
    ): List<ClueDto>

    /** Запрашивает последнюю заметку выбранного расследования. */
    @GET("rest/v1/notes?select=*")
    suspend fun getLatestNote(
        @Query("investigation_id") investigationFilter: String,
        @Query("order") order: String = "create_at.desc",
        @Query("limit") limit: Int = 1,
    ): List<NoteDto>

    /** Отправляет новую или изменённую заметку в REST API Supabase. */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/notes")
    suspend fun insertNote(
        @Body body: NoteInsertDto,
    ): List<NoteDto>

    /** Удаляет заметку по переданному фильтру. */
    @DELETE("rest/v1/notes")
    suspend fun deleteNote(
        @Query("id") idFilter: String,
    )
}
