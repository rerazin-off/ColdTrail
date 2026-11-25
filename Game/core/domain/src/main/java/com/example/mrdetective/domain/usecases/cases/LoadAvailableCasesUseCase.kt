package com.example.mrdetective.domain.usecases.cases

import com.example.mrdetective.domain.models.Case
import com.example.mrdetective.domain.repositories.CaseRepository
import javax.inject.Inject

/**
 * Загружает список дел через доменный репозиторий.
 *
 * @param repository Репозиторий данных дела и запуска расследования.
 */
class LoadAvailableCasesUseCase
    @Inject
    constructor(
        private val repository: CaseRepository,
    ) {
        /**
         * Возвращает дела, полученные от репозитория.
         *
         * @return Список доступных дел или ошибка загрузки.
         */
        suspend operator fun invoke(): Result<List<Case>> {
            return repository.getAvailableCases()
        }
    }
