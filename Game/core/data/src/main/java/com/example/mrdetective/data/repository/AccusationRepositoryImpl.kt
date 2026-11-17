package com.example.mrdetective.data.repository

import android.content.Context
import com.example.mrdetective.data.local.dao.InvestigationDao
import com.example.mrdetective.data.local.dao.SyncQueueDao
import com.example.mrdetective.data.local.entity.SyncQueueEntity
import com.example.mrdetective.data.network.ConnectivityChecker
import com.example.mrdetective.data.remote.api.OfflineApi
import com.example.mrdetective.data.remote.dto.AccusationRequestDto
import com.example.mrdetective.data.sync.SyncScheduler
import com.example.mrdetective.domain.models.Accusation
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.AccusationRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccusationRepositoryImpl @Inject constructor(
    private val api: OfflineApi,
    private val progress: InvestigationDao,
    private val outbox: SyncQueueDao,
    private val connectivity: ConnectivityChecker,
    private val gson: Gson,
    @ApplicationContext private val context: Context,
) : AccusationRepository {
    override suspend fun submitAccusation(caseId: Int, suspectId: Int, clueIds: List<Int>): Result<Accusation> = try {
        val investigation = progress.getByCase(caseId) ?: throw DomainError.NotFound("Расследование не начато")
        // Server contract accepts investigation_id + suspect_id. clueIds are not part of the published Edge Function payload.
        val request = AccusationRequestDto(investigation.id, suspectId)
        if (connectivity.isNetworkAvailable()) {
            val response = api.submitAccusation(request)
            Result.success(Accusation(response.id, caseId, suspectId, response.isCorrect, response.explanation))
        } else {
            outbox.enqueue(SyncQueueEntity(
                entityType = "accusation", entityId = investigation.id.toString(),
                operation = "submit", payload = gson.toJson(request),
            ))
            SyncScheduler.enqueue(context)
            Result.failure(DomainError.Network(IllegalStateException("Обвинение сохранено и будет отправлено после восстановления сети. Результат пока неизвестен.")))
        }
    } catch (error: Throwable) { Result.failure(error) }
}
