package com.wafflestudio.spring2026.session.dto

import java.time.OffsetDateTime

data class SessionUpdateRequest(
    val isTitlePresent: Boolean,
    val title: String?,
    val isStartsAtPresent: Boolean,
    val startsAt: OffsetDateTime?,
    val isLocationPresent: Boolean,
    val location: String?,
    val isAssignmentTitlePresent: Boolean,
    val assignmentTitle: String?,
    val isLectureContentPresent: Boolean,
    val lectureContent: String?,
    val isAssignmentContentPresent: Boolean,
    val assignmentContent: String?,
) {
    companion object {
        fun fromMap(body: Map<String, Any?>): SessionUpdateRequest {
            val startsAt = (body["startsAt"] as? String)
                ?.let(OffsetDateTime::parse)

            return SessionUpdateRequest(
                isTitlePresent = body.containsKey("title"),
                title = body["title"] as? String,
                isStartsAtPresent = body.containsKey("startsAt"),
                startsAt = startsAt,
                isLocationPresent = body.containsKey("location"),
                location = body["location"] as? String,
                isAssignmentTitlePresent = body.containsKey("assignmentTitle"),
                assignmentTitle = body["assignmentTitle"] as? String,
                isLectureContentPresent = body.containsKey("lectureContent"),
                lectureContent = body["lectureContent"] as? String,
                isAssignmentContentPresent = body.containsKey("assignmentContent"),
                assignmentContent = body["assignmentContent"] as? String,
            )
        }
    }
}