package com.example.mrdetective.data.network

import com.example.mrdetective.data.remote.dto.CaseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit contract for reading case records. */
interface CasesApi {
    /** Queries the list of case records from Supabase. */
    @GET("rest/v1/cases")
    suspend fun getCases(
        @Query("select") select: String = "*",
    ): List<CaseDto>

    /** Queries case records matching the supplied filter. */
    @GET("rest/v1/cases")
    suspend fun getCaseById(
        @Query("id") id: String,
        @Query("select") select: String = "*",
    ): List<CaseDto>
}
