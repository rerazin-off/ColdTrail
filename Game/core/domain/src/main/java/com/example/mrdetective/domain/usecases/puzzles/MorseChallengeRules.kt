package com.example.mrdetective.domain.usecases.puzzles

import com.example.mrdetective.domain.models.MorseChallenge
import kotlin.random.Random

fun chooseMorseChallenge(
    puzzles: List<MorseChallenge>,
    restoredId: Int?,
    random: Random = Random.Default,
): MorseChallenge? =
    puzzles.firstOrNull { it.id == restoredId && (it.solved || !it.rewardClaimed) }
        ?: puzzles.filter { !it.solved && !it.rewardClaimed }.randomOrNull(random)

fun validateMorseChallengeAnswer(challenge: MorseChallenge, answer: String): Boolean =
    answer.length == challenge.answerLength && answer.all {
        it in '0'..'9' || (!challenge.numericOnly && (it in 'a'..'z' || it in 'A'..'Z'))
    }
