package com.wafflestudio.spring2026.session.dto

import com.wafflestudio.spring2026.session.model.Session
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class SessionListResponse(
    val id: Long,
    val seminarId: Long,
    val round: Long,
    val title: String,
    val startsAt: OffsetDateTime,
    val location: String,
    val assignmentTitle: String,
) {
    companion object {
        fun from(session: Session, round: Long) = SessionListResponse(
            id = requireNotNull(session.id),
            seminarId = session.seminarId,
            round = round,
            title = session.title,
            startsAt = session.startsAt.atOffset(ZoneOffset.ofHours(9)),
            location = session.location,
            assignmentTitle = session.assignmentTitle,
        )
    }
}
