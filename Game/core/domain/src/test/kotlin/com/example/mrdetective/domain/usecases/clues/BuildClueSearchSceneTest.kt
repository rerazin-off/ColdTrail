package com.example.mrdetective.domain.usecases.clues

import com.example.mrdetective.domain.repositories.SceneClueImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.random.Random

/** Tests selection, uniqueness, and placeholder handling in the fallback scene. */
class BuildClueSearchSceneTest {
    private val target = SceneClueImage(10, "target.webp")

    /** Uses three distinct pictures including the target. */
    @Test
    fun usesThreeDistinctPicturesIncludingTheTarget() {
        val candidates =
            listOf(
                target,
                SceneClueImage(11, "target.webp"),
                SceneClueImage(12, "note.webp"),
                SceneClueImage(13, "note.webp"),
                SceneClueImage(14, "card.webp"),
            )

        val items = buildClueSearchScene(target, candidates, Random(1))

        assertEquals(setOf("target.webp", "note.webp", "card.webp"), items.map { it.imageUrl }.toSet())
        assertEquals(3, items.size)
        assertEquals(1, items.count { it.clueId == target.clueId })
        assertFalse(items.any { it.clueId == 11 })
    }

    /** Missing artwork still leaves three tappable objects. */
    @Test
    fun missingArtworkStillLeavesThreeTappableObjects() {
        val items = buildClueSearchScene(target.copy(imageUrl = null), emptyList(), Random(1))

        assertEquals(3, items.size)
        assertEquals(2, items.count { it.clueId == null })
        assertEquals(1, items.count { it.clueId == target.clueId })
    }
}
