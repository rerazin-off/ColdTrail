package com.example.mrdetective.domain.repositories

interface SceneRepository  {
    suspend fun getScene(caseId: Int, sceneId: Int): Result<String>
}