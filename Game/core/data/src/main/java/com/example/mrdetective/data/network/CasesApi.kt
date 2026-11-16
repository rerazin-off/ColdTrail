package com.example.mrdetective.data.network

import com.example.mrdetective.data.remote.dto.CaseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CasesApi {

    @GET("rest/v1/cases")
    suspend fun getCases(
        @Query("select") select: String = "*"
    ): List<CaseDto>

    @GET("rest/v1/cases")
    suspend fun getCaseById(
        @Query("id") id: String,
        @Query("select") select: String = "*"
    ): List<CaseDto>
}