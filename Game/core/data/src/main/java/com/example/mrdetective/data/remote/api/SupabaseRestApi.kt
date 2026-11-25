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

/** Represents the nickname selected from a Supabase profile row. */
data class ProfileNicknameDto(val nickname: String)

/** Retrofit contract for querying public game data and updating investigation notes. */
interface SupabaseRestApi {
    /** Queries the nickname of the selected account profile. */
    @GET("rest/v1/profiles?select=nickname")
    suspend fun getNickname(
        @Query("id") userFilter: String,
    ): List<ProfileNicknameDto>

    /** Returns the available case rows from Supabase. */
    @GET("rest/v1/cases?select=*")
    suspend fun getCases(): List<CaseDto>

    /** Looks up a case using its identifier filter. */
    @GET("rest/v1/cases?select=*")
    suspend fun getCase(
        @Query("id") idFilter: String,
    ): List<CaseDto>

    /** Looks up the suspects in the selected case. */
    @GET("rest/v1/suspects?select=id,case_id,name,profile,photo,created_at")
    suspend fun getSuspects(
        @Query("case_id") caseFilter: String,
    ): List<SuspectDto>

    /** Looks up a public suspect profile by its identifier. */
    @GET("rest/v1/suspects?select=id,case_id,name,profile,photo,created_at")
    suspend fun getSuspect(
        @Query("id") idFilter: String,
    ): List<SuspectDto>

    /** Looks up a clue record using its identifier filter. */
    @GET("rest/v1/clues?select=*")
    suspend fun getClue(
        @Query("id") idFilter: String,
    ): List<ClueDto>

    /** Queries the latest note belonging to the investigation. */
    @GET("rest/v1/notes?select=*")
    suspend fun getLatestNote(
        @Query("investigation_id") investigationFilter: String,
        @Query("order") order: String = "create_at.desc",
        @Query("limit") limit: Int = 1,
    ): List<NoteDto>

    /** Sends a new or updated note to the Supabase REST API. */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/notes")
    suspend fun insertNote(
        @Body body: NoteInsertDto,
    ): List<NoteDto>

    /** Deletes the note matching the supplied filter. */
    @DELETE("rest/v1/notes")
    suspend fun deleteNote(
        @Query("id") idFilter: String,
    )
}
