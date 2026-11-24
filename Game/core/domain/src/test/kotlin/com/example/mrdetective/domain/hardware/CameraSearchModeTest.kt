package com.example.mrdetective.domain.hardware

import org.junit.Assert.assertEquals
import org.junit.Test

class CameraSearchModeTest {
    @Test
    fun firstUseAsksForPermissionAndExistingAccessStartsCamera() {
        assertEquals(ClueSearchMode.AskPermission, chooseClueSearchMode(null, true, false, false))
        assertEquals(ClueSearchMode.Camera, chooseClueSearchMode(null, true, true, false))
        assertEquals(ClueSearchMode.Camera, chooseClueSearchMode(true, true, true, false))
    }

    @Test
    fun appSwitchOffOrMissingCameraOpensPictureSearch() {
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(false, true, true, false))
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(true, false, true, false))
    }

    @Test
    fun previouslyDeniedOrRevokedPermissionOpensPictureSearch() {
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(false, true, false, false))
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(true, true, false, false))
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(null, true, false, true))
    }
}
