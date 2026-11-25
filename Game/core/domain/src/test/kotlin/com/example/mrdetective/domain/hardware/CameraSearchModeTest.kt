package com.example.mrdetective.domain.hardware

import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests camera permission and fallback decisions for clue puzzles. */
class CameraSearchModeTest {
    /** First use asks for permission and existing access starts camera. */
    @Test
    fun firstUseAsksForPermissionAndExistingAccessStartsCamera() {
        assertEquals(ClueSearchMode.AskPermission, chooseClueSearchMode(null, true, false, false))
        assertEquals(ClueSearchMode.Camera, chooseClueSearchMode(null, true, true, false))
        assertEquals(ClueSearchMode.Camera, chooseClueSearchMode(true, true, true, false))
    }

    /** App switch off or missing camera opens picture search. */
    @Test
    fun appSwitchOffOrMissingCameraOpensPictureSearch() {
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(false, true, true, false))
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(true, false, true, false))
    }

    /** Previously denied or revoked permission opens picture search. */
    @Test
    fun previouslyDeniedOrRevokedPermissionOpensPictureSearch() {
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(false, true, false, false))
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(true, true, false, false))
        assertEquals(ClueSearchMode.Picture, chooseClueSearchMode(null, true, false, true))
    }
}
