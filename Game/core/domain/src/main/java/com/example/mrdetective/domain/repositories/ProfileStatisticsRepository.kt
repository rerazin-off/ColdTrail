package com.example.mrdetective.domain.repositories

data class ProfileStatistics(val total: Int, val active: Int, val solved: Int)

interface ProfileStatisticsRepository {
    suspend fun load(): Result<ProfileStatistics>

    suspend fun loadNickname(): Result<String?>
}
