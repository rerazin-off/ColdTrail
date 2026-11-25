package com.example.mrdetective.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Инструментальный тест, выполняемый на Android-устройстве. См. [документацию по
 * тестированию](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    /** Проверяет имя пакета приложения, доступного инструментальному тесту. */
    @Test
    fun useAppContext() {
        // Контекст тестируемого приложения.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.mrdetective.presentation.test", appContext.packageName)
    }
}
