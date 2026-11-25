package com.example.mrdetective.domain.hardware

/** Tracks whether the puzzle permissions introduction has been shown per player. */
interface PuzzlePermissionsPreferences {
    /** Checks whether the permissions introduction has been shown to the specified player. */
    suspend fun hasSeenPuzzlePermissions(userId: String): Boolean

    /** Records that the specified player has seen the permissions introduction. */
    suspend fun markPuzzlePermissionsSeen(userId: String)
}
