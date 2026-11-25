package com.example.mrdetective.domain.usecases.clues

import javax.inject.Inject

/** Определяет, находится ли улика в пределах заданного расстояния. */
class ConfirmClueApproachUseCase
    @Inject
    constructor() {
        /** Принимает неотрицательные расстояния и проверяет, находится ли улика в пределах досягаемости. */
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
