package com.example.mrdetective.domain.usecases.clues

import javax.inject.Inject

class ConfirmClueApproachUseCase
    @Inject
    constructor() {
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
