package com.example.mrdetective.data.mapper

import com.example.mrdetective.data.remote.dto.CaseDto
import com.example.mrdetective.data.remote.dto.ClueDto
import com.example.mrdetective.data.remote.dto.NoteDto
import com.example.mrdetective.data.remote.dto.SuspectDto
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifies how the four remote DTOs map to their domain models. */
class DtoMappersTest {
    /** Case dto maps only fields represented in domain. */
    @Test fun `case dto maps only fields represented in domain`() {
        val result = CaseDto(7, "Дело", "Вступление", 3, "2026-01-01T10:00:00Z").toDomain()
        assertEquals(7, result.id)
        assertEquals("Дело", result.title)
        assertEquals("Вступление", result.introduction)
        assertEquals("", result.objective)
        assertEquals("available", result.status)
    }

    /** Suspect dto maps public fields. */
    @Test fun `suspect dto maps public fields`() {
        val result = SuspectDto(2, 5, "Анна", "Реставратор", "photo.webp", "date").toDomain()
        assertEquals(2, result.id)
        assertEquals(5, result.caseId)
        assertEquals("Анна", result.name)
        assertEquals("Реставратор", result.profile)
    }

    /** Clue dto maps image and timestamp. */
    @Test fun `clue dto maps image and timestamp`() {
        val result = ClueDto(1, 4, "Записка", "Описание", "clues/a.webp", "created").toDomain()
        assertEquals("clues/a.webp", result.image)
        assertEquals("created", result.createdAt)
    }

    /** Note dto maps timestamps. */
    @Test fun `note dto maps timestamps`() {
        val result = NoteDto(9, 3, "Текст", "created", "updated").toDomain()
        assertEquals(3, result.investigationId)
        assertEquals("Текст", result.text)
        assertEquals("created", result.createdAt)
        assertEquals("updated", result.updatedAt)
    }
}
