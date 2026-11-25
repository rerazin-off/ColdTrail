package com.example.mrdetective.domain.hardware

/** Отслеживает, был ли показан экран разрешений каждому игроку. */
interface PuzzlePermissionsPreferences {
    /** Проверяет, был ли показан экран разрешений указанному игроку. */
    suspend fun hasSeenPuzzlePermissions(userId: String): Boolean

    /** Отмечает, что указанный игрок увидел экран разрешений. */
    suspend fun markPuzzlePermissionsSeen(userId: String)
}
