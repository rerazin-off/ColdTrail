package com.example.mrdetective.domain.hardware

interface PuzzlePermissionsPreferences {
    suspend fun hasSeenPuzzlePermissions(userId: String): Boolean

    suspend fun markPuzzlePermissionsSeen(userId: String)
}
