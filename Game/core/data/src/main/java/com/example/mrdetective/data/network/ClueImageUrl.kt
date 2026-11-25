package com.example.mrdetective.data.network

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal fun String?.toClueImageUrl(): String? {
    val path = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val base = SupabaseConfig.baseUrl.toHttpUrlOrNull() ?: return null
    val prefix = "storage/v1/object/public/clue-images/"
    val absolute = path.toHttpUrlOrNull()
    if (absolute != null) {
        return path.takeIf { absolute.host == base.host && absolute.encodedPath.startsWith("/$prefix") }
    }
    return base.newBuilder()
        .addPathSegments(prefix + path.trimStart('/').removePrefix("clue-images/"))
        .build().toString()
}
