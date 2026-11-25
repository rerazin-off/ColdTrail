package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.repositories.SceneClueImage
import kotlin.random.Random

/** Builds a shuffled set of scene items containing the target and distinct decoys. */
fun buildClueSearchScene(
    target: SceneClueImage,
    candidates: List<SceneClueImage>,
    random: Random,
): List<SceneClueImage> {
    val decoys =
        candidates
            .filter { it.clueId != target.clueId && it.imageUrl != null && it.imageUrl != target.imageUrl }
            .distinctBy { it.clueId }
            .distinctBy { it.imageUrl }
            .shuffled(random)
            .take(2)
    val missing = List(2 - decoys.size) { SceneClueImage(clueId = null, imageUrl = null) }
    return (decoys + missing + target).shuffled(random)
}
