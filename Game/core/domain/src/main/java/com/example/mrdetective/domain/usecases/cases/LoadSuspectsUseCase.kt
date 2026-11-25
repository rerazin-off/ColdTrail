package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.models.DomainError
import com.example.mrdetective.domain.models.Suspect
import com.example.mrdetective.domain.repositories.SuspectRepository
import javax.inject.Inject

/**
 * Загружает подозреваемых, связанных с делом.
 *
 * @param repository Источник публичных профилей подозреваемых.
 */
class LoadSuspectsUseCase
    @Inject
    constructor(
        private val repository: SuspectRepository,
    ) {
        /**
         * Возвращает публичные профили подозреваемых по делу.
         *
         * @param caseId Идентификатор выбранного дела.
         * @return Список подозреваемых, в том числе пустой, или ошибка загрузки.
         */
        suspend operator fun invoke(caseId: Int): Result<List<Suspect>> {
            if (caseId <= 0) {
                return Result.failure(
                    DomainError.Validation("Некорректный ID дела"),
                )
            }

            return repository.getSuspects(caseId)
        }
    }
