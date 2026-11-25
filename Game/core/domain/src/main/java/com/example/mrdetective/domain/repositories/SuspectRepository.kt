package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.Suspect

/** Looks up public suspect profiles and the cast of a case. */
interface SuspectRepository {
    /** Returns public profiles of the suspects assigned to the specified case. */
    suspend fun getSuspects(caseId: Int): Result<List<Suspect>>

    /** Looks up the public profile of a suspect by identifier. */
    suspend fun getSuspect(id: Int): Result<Suspect>
}
