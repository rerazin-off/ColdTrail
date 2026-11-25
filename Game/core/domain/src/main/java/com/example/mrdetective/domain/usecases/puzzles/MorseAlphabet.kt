package com.example.mrdetective.domain.usecases.puzzles

/** Provides the alphabet used to display and decode Morse puzzles. */
object MorseAlphabet {
    val letters =
        ('A'..'Z').zip(
            listOf(
                ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--",
                "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..",
            ),
        )
    val digits =
        ('0'..'9').zip(
            listOf("-----", ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----."),
        )
}
