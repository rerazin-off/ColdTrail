package com.example.mrdetective.domain.usecases.puzzles

import javax.inject.Inject

/** Сравнивает выбранный отпечаток с ожидаемым. */
class MatchFingerprintUseCase
    @Inject
    constructor() {
        /**
         * Сравнивает непустые строки отпечатков после удаления пробелов по краям.
         *
         * @param selectedFingerprint Выбранный игроком образец.
         * @param correctFingerprint Образец, с которым сравнивается результат.
         * @return `true`, если непустые значения совпадают после удаления пробелов по краям.
         */
        operator fun invoke(
            selectedFingerprint: String,
            correctFingerprint: String,
        ): Boolean {
            if (
                selectedFingerprint.isBlank() ||
                correctFingerprint.isBlank()
            ) {
                return false
            }

            return selectedFingerprint.trim() ==
                correctFingerprint.trim()
        }
    }
