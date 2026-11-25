package com.example.mrdetective.domain.usecases.dialogue

import com.example.mrdetective.domain.models.DialogueMessage
import com.example.mrdetective.domain.repositories.DialogueRepository
import javax.inject.Inject

/**
 * Загружает предыдущие сообщения диалога с подозреваемым.
 *
 * @param repository Репозиторий истории и обмена репликами.
 */
class LoadDialogueHistoryUseCase
    @Inject
    constructor(
        private val repository: DialogueRepository,
    ) {
        /**
         * Возвращает сохранённый диалог с выбранным подозреваемым.
         *
         * @param caseId Идентификатор выбранного дела.
         * @param suspectId Идентификатор подозреваемого в выбранном деле.
         * @return Список сохранённых реплик или ошибка загрузки.
         */
        suspend operator fun invoke(
            caseId: Int,
            suspectId: Int,
        ): Result<List<DialogueMessage>> {
            return repository.getHistory(caseId, suspectId)
        }
    }
