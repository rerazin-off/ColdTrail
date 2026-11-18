package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.models.Clue
import javax.inject.Inject

class FindClueByAzimuthUseCase
    @Inject
    constructor() {
        operator fun invoke(
            clues: List<Clue>,
            currentAzimuth: Float,
            clueAzimuths: Map<Int, Float>,
        ): Clue? {
            return clues
                .filter { it.id in clueAzimuths }
                .minByOrNull { clue ->
                    val difference =
                        kotlin.math.abs(
                            currentAzimuth - clueAzimuths.getValue(clue.id),
                        )

                    minOf(difference, 360f - difference)
                }
        }
    }
