package com.example.mrdetective.data.repository

import com.example.mrdetective.data.local.dao.InvestigationDao
import com.example.mrdetective.data.local.datastore.SettingsData
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.remote.api.SupabaseRestApi
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.ProfileStatistics
import com.example.mrdetective.domain.repositories.ProfileStatisticsRepository
import javax.inject.Inject

/** Computes profile statistics from investigation records and fetches the account nickname. */
class ProfileStatisticsRepositoryImpl
    @Inject
    constructor(
        private val auth: AuthRepository,
        private val investigations: InvestigationDao,
        private val api: OfflineApi,
        private val connectivity: ConnectivityChecker,
        private val profiles: SupabaseRestApi,
        private val settings: SettingsData,
    ) : ProfileStatisticsRepository {
        /** Looks up the nickname of the current player. */
        override suspend fun loadNickname(): Result<String?> =
            apiCall {
                val user = auth.currentUser() ?: throw DomainError.Unauthorized()
                if (!connectivity.isNetworkAvailable()) {
                    return@apiCall settings.getNickname(user) ?: throw DomainError.Network(kind = DomainError.Network.Kind.Offline)
                }
                apiCall {
                    profiles.getNickname("eq.$user").firstOrNull()?.nickname?.trim()?.takeIf { it.isNotBlank() }
                }.fold(
                    onSuccess = { nickname -> nickname?.also { settings.setNickname(user, it) } },
                    onFailure = { error -> settings.getNickname(user) ?: throw error },
                )
            }

        /** Loads investigation statistics for the current player. */
        override suspend fun load(): Result<ProfileStatistics> =
            apiCall {
                val userId = auth.currentUser() ?: throw DomainError.Unauthorized()
                val local =
                    investigations.getForUser(userId).distinctBy { it.caseId }
                        .associate { it.caseId to it.status }
                val remote =
                    if (connectivity.isNetworkAvailable()) {
                        val response = apiCall { api.getInvestigations(caseId = null, userId = "eq.$userId") }
                        val rows = if (local.isEmpty()) response.getOrThrow() else response.getOrDefault(emptyList())
                        rows.filter { it.userId == userId }.associate { it.caseId to it.status }
                    } else {
                        if (local.isEmpty()) throw DomainError.Network(kind = DomainError.Network.Kind.Offline)
                        emptyMap()
                    }
                val statuses = (local + remote).values
                ProfileStatistics(
                    total = statuses.size,
                    active = statuses.count { it == "in_progress" || it == "in_investigation" },
                    solved = statuses.count { it == "solved" || it == "closed" },
                )
            }
    }
