package com.example.mrdetective.domain.usecases.puzzles

import javax.inject.Inject

class MatchFingerprintUseCase
    @Inject
    constructor() {
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
