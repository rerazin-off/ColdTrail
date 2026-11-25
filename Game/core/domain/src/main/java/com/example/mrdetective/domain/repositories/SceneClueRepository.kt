package com.example.mrdetective.domain.repositories

import com.example.mrdetective.domain.models.CameraChallenge

/**
 * Содержит изображение улики и его подпись для поиска на сцене.
 *
 * @property clueId Идентификатор улики. У предмета-заглушки он может отсутствовать.
 * @property imageUrl Адрес изображения, доступного для показа, если он известен.
 * @property title Подпись предмета, если она известна.
 */
data class SceneClueImage(val clueId: Int?, val imageUrl: String?, val title: String? = null)

/** Предоставляет предметы для запасной сцены и отправляет выбранную улику. */
interface SceneClueRepository {
    /** Загружает изображения других улик, исключая искомую улику. */
    suspend fun loadDecoys(targetClueId: Int): List<SceneClueImage>

    /** Отправляет искомую улику, найденную на запасной сцене. */
    suspend fun submit(
        challenge: CameraChallenge,
        selectedClueId: Int,
    ): Result<Unit>
}
