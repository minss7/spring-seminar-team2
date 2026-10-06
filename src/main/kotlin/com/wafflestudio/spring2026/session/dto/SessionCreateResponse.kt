package com.wafflestudio.spring2026.session.dto

import com.wafflestudio.spring2026.session.model.Session

data class SessionCreateResponse(
    val id: Long,
    val seminarId: Long,
    val round: Long,
) {
    companion object {
        fun from(session: Session, round: Long) = SessionCreateResponse(
            id = requireNotNull(session.id),
            seminarId = session.seminarId,
            round = round,
        )
    }
}