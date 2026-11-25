package com.example.mrdetective.domain.usecases.clues

import javax.inject.Inject

/** Determines whether the measured distance to a clue is within the required range. */
class ConfirmClueApproachUseCase
    @Inject
    constructor() {
        /** Accepts nonnegative distances only and checks whether the clue is within reach. */
        operator fun invoke(
            distanceInMeters: Float,
            requiredDistance: Float,
        ): Boolean {
            if (distanceInMeters < 0 || requiredDistance < 0) {
                return false
            }

            return distanceInMeters <= requiredDistance
        }
    }
