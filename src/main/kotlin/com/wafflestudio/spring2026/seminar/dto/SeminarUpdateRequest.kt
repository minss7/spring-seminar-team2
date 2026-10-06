package com.wafflestudio.spring2026.seminar.dto


data class SeminarUpdateRequest(
    val isTitlePresent: Boolean,
    val title: String?,
    val isDescriptionPresent: Boolean,
    val description: String?,
) {
    companion object {
        fun fromMap(body: Map<String, Any?>): SeminarUpdateRequest {
            val hasTitle = body.containsKey("title")
            val title = body["title"] as? String

            val hasDesc = body.containsKey("description")
            val description = body["description"] as? String

            return SeminarUpdateRequest(
                isTitlePresent = hasTitle,
                title = title,
                isDescriptionPresent = hasDesc,
                description = description,
            )
        }
    }
}