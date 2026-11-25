package com.example.mrdetective.domain.repositories

/** Summarizes the total, active, and solved investigations for the current player. */
data class ProfileStatistics(val total: Int, val active: Int, val solved: Int)

/** Provides player statistics and the account nickname. */
interface ProfileStatisticsRepository {
    /** Loads investigation statistics for the current player. */
    suspend fun load(): Result<ProfileStatistics>

    /** Looks up the nickname of the current player. */
    suspend fun loadNickname(): Result<String?>
}
