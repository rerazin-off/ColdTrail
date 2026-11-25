package com.example.mrdetective.data

import com.example.mrdetective.data.hardware.morseWaveform
import org.junit.Assert.assertArrayEquals
import org.junit.Test

/** Checks the vibration waveform generated from a Morse sequence. */
class MorseWaveformTest {
    /** Проверяет сценарий «точки тире и паузы переводятся во включение и выключение мотора». */
    @Test
    fun `точки тире и паузы переводятся во включение и выключение мотора`() {
        assertArrayEquals(
            longArrayOf(0, 140, 140, 420, 420, 420, 980, 140),
            morseWaveform(".- - / ."),
        )
    }
}
