package com.example.mrdetective.domain.usecases.puzzles

enum class MorseCodeCheck { Incomplete, Incorrect, Correct }

object DemoMorsePuzzle {
    private const val Answer = "2214"
    const val CodeLength = 4
    const val Signal = "..--- / ..--- / .---- / ....-"

    fun check(input: String): MorseCodeCheck = when {
        input.length != CodeLength || input.any { it !in '0'..'9' } -> MorseCodeCheck.Incomplete
        input == Answer -> MorseCodeCheck.Correct
        else -> MorseCodeCheck.Incorrect
    }
}
