package com.example.mrdetective.data.remote.dto

/**
 * Идентификатор, путь к изображению и название улики для поиска на сцене.
 *
 * @property id Идентификатор улики на сцене.
 * @property image Путь к изображению в хранилище, если оно задано.
 * @property title Название дела, улики или головоломки для отображения.
 */
data class SceneClueImageDto(val id: Int, val image: String?, val title: String)
