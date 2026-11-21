package com.example.mrdetective.data.repository

import com.example.mrdetective.data.local.dao.InvestigationDao
import com.example.mrdetective.data.local.entity.InvestigationEntity
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.remote.dto.AccusationRequestDto
import com.example.mrdetective.data.remote.dto.AccusationResponseDto
import com.example.mrdetective.domain.models.Accusation
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AccusationRepository
import com.example.mrdetective.domain.repositories.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccusationRepositoryImpl @Inject constructor(
    private val api: OfflineApi,
    private val progress: InvestigationDao,
    private val auth: AuthRepository,
    private val connectivity: ConnectivityChecker,
) : AccusationRepository {
    override suspend fun getResult(caseId: Int): Result<Accusation> = apiCall {
        val investigation = syncedInvestigation(caseId)
        val response = api.getAccusation(investigation.id)
        saveResult(investigation, response, response.suspectId ?: 0)
    }

    override suspend fun submitAccusation(caseId: Int, suspectId: Int, clueIds: List<Int>): Result<Accusation> = apiCall {
        val investigation = syncedInvestigation(caseId)
        val response = api.submitAccusation(AccusationRequestDto(investigation.id, suspectId))
        saveResult(investigation, response, suspectId)
    }

    private suspend fun syncedInvestigation(caseId: Int): InvestigationEntity {
        val userId = auth.currentUser() ?: throw DomainError.Unauthorized()
        val investigation = progress.getByCaseForUser(caseId, userId)
            ?: throw DomainError.NotFound("Сначала начните расследование.")
        if (investigation.id <= 0 || !connectivity.isNetworkAvailable()) {
            throw DomainError.Network()
        }
        return investigation
    }

    private suspend fun saveResult(
        investigation: InvestigationEntity,
        response: AccusationResponseDto,
        selectedSuspectId: Int,
    ): Accusation {
        val status = if (response.isCorrect) "solved" else "failed"
        progress.upsertInvestigation(investigation.copy(status = status, updatedAt = System.currentTimeMillis()))
        val suspectId = response.suspectId ?: selectedSuspectId
        return Accusation(
            id = response.id,
            caseId = investigation.caseId,
            suspectId = suspectId,
            isCorrect = response.isCorrect,
            explanation = response.explanation,
            culpritId = response.culpritId ?: suspectId.takeIf { response.isCorrect },
        )
    }
}
