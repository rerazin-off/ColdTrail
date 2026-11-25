package com.example.mrdetective.domain.usecases.puzzles

import javax.inject.Inject

/** Compares the selected fingerprint with the expected fingerprint. */
class MatchFingerprintUseCase
    @Inject
    constructor() {
        /** Compares non-blank fingerprint strings after trimming whitespace. */
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
