package com.wafflestudio.spring2026.session.dto

import com.wafflestudio.spring2026.session.model.Session
import java.time.LocalDateTime
import java.time.OffsetDateTime

data class SessionDetailResponse(
    val id: Long,
    val seminarId: Long,
    val round: Long,
    val title: String,
    val startsAt: LocalDateTime,
    val location: String,
    val assignmentTitle: String,
    val lectureContent: String?,
    val assignmentContent: String?,
) {
    companion object {
        fun from(session: Session, round: Long) = SessionDetailResponse(
            id = requireNotNull(session.id),
            seminarId = session.seminarId,
            round = round,
            title = session.title,
            startsAt = session.startsAt,
            location = session.location,
            assignmentTitle = session.assignmentTitle,
            lectureContent = session.lectureContent,
            assignmentContent = session.assignmentContent,
        )
    }
}
