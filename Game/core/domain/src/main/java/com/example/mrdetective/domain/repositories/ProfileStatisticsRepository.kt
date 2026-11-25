package com.example.mrdetective.domain.repositories

/** Содержит число всех, активных и раскрытых расследований игрока. */
data class ProfileStatistics(val total: Int, val active: Int, val solved: Int)

/** Предоставляет статистику игрока и никнейм аккаунта. */
interface ProfileStatisticsRepository {
    /** Загружает статистику расследований текущего игрока. */
    suspend fun load(): Result<ProfileStatistics>

    /** Ищет никнейм текущего игрока. */
    suspend fun loadNickname(): Result<String?>
}
