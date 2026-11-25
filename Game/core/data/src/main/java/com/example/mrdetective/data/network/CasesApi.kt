package com.example.mrdetective.data.network

import com.example.mrdetective.data.remote.dto.CaseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** Интерфейс Retrofit для чтения записей дел. */
interface CasesApi {
    /** Запрашивает список дел в Supabase. */
    @GET("rest/v1/cases")
    suspend fun getCases(
        @Query("select") select: String = "*",
    ): List<CaseDto>

    /** Запрашивает дела, подходящие под переданный фильтр. */
    @GET("rest/v1/cases")
    suspend fun getCaseById(
        @Query("id") id: String,
        @Query("select") select: String = "*",
    ): List<CaseDto>
}
