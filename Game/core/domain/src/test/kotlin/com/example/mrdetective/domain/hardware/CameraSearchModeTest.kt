package com.example.mrdetective.domain.hardware

import org.junit.Assert.assertEquals
import org.junit.Test

/** Проверяет выбор между запросом разрешения, камерой и поиском на сцене. */
class CameraSearchModeTest {
    /** Проверяет запрос разрешения при первом запуске и запуск камеры при наличии доступа. */
    @Test
    fun firstUseAsksForPermissionAndExistingAccessStartsCamera() {
        assertEquals(ClueSearchMode.AskPermission, chooseClueSearchMode(null, true, false, false))
        assertEquals(ClueSearchMode.Camera, chooseClueSearchMode(null, true, true, false))
        assertEquals(ClueSearchMode.Camera, chooseClueSearchMode(true, true, true, false))
    }

    /** Проверяет переход к поиску на картинке при отключённой или отсутствующей камере. */
    @Test
    fun appSwitchOffOrMissingCameraOpensPictureSearch() {
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(false, true, true, false))
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(true, false, true, false))
    }

    /** Проверяет переход к поиску на картинке, если в доступе отказали или разрешение отозвали. */
    @Test
    fun previouslyDeniedOrRevokedPermissionOpensPictureSearch() {
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(false, true, false, false))
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(true, true, false, false))
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(null, true, false, true))
    }
}
