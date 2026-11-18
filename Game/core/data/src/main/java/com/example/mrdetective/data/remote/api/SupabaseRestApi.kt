package com.example.mrdetective.data.remote.api

import com.example.mrdetective.data.remote.dto.CaseDto
import com.example.mrdetective.data.remote.dto.ClueDto
import com.example.mrdetective.data.remote.dto.NoteDto
import com.example.mrdetective.data.remote.dto.NoteInsertDto
import com.example.mrdetective.data.remote.dto.SuspectDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query

interface SupabaseRestApi {
    @GET("rest/v1/cases?select=*")
    suspend fun getCases(): List<CaseDto>

    @GET("rest/v1/cases?select=*")
    suspend fun getCase(@Query("id") idFilter: String): List<CaseDto>

    @GET("rest/v1/suspects?select=id,case_id,name,profile,photo,created_at")
    suspend fun getSuspects(@Query("case_id") caseFilter: String): List<SuspectDto>

    @GET("rest/v1/suspects?select=id,case_id,name,profile,photo,created_at")
    suspend fun getSuspect(@Query("id") idFilter: String): List<SuspectDto>

    @GET("rest/v1/clues?select=*")
    suspend fun getClue(@Query("id") idFilter: String): List<ClueDto>

    @GET("rest/v1/notes?select=*")
    suspend fun getLatestNote(
        @Query("investigation_id") investigationFilter: String,
        @Query("order") order: String = "create_at.desc",
        @Query("limit") limit: Int = 1,
    ): List<NoteDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/notes")
    suspend fun insertNote(@Body body: NoteInsertDto): List<NoteDto>

    @DELETE("rest/v1/notes")
    suspend fun deleteNote(@Query("id") idFilter: String)
}
