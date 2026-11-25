package com.example.mrdetective.data.repository

import android.content.Context
import com.example.mrdetective.data.R
import com.example.mrdetective.data.local.dao.CaseDao
import com.example.mrdetective.data.network.apiCall
import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.repositories.SceneRepository
import com.example.mrdetective.domain.repositories.WAREHOUSE_SCENE_ID
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Предоставляет описание встроенной сцены склада для загруженного дела.
 *
 * @param cases DAO кеша дел, по которому проверяется наличие загруженного дела.
 * @param context Контекст Android для доступа к ресурсам и системным службам.
 */
@Singleton
class SceneRepositoryImpl
    @Inject
    constructor(
        private val cases: CaseDao,
        @param:ApplicationContext private val context: Context,
    ) : SceneRepository {
        /** Возвращает локальное описание склада для поиска улик без камеры. */
        override suspend fun getScene(
            caseId: Int,
            sceneId: Int,
        ): Result<String> =
            apiCall {
                if (sceneId != WAREHOUSE_SCENE_ID || cases.getById(caseId) == null) {
                    throw DomainError.NotFound("Сцена не найдена")
                }
                context.getString(R.string.warehouse_scene_instruction)
            }
    }
