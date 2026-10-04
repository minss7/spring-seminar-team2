package com.wafflestudio.spring2026.session.service

import com.wafflestudio.spring2026.seminar.exception.SeminarNotFoundException
import com.wafflestudio.spring2026.session.dto.SessionCreateRequest
import com.wafflestudio.spring2026.session.dto.SessionCreateResponse
import com.wafflestudio.spring2026.session.model.Session
import com.wafflestudio.spring2026.session.repository.SessionRepository
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import com.wafflestudio.spring2026.session.dto.SessionDetailResponse
import com.wafflestudio.spring2026.session.dto.SessionListResponse
import com.wafflestudio.spring2026.session.exception.SessionNotFoundException
import org.springframework.stereotype.Service
import java.time.ZoneOffset

@Service
class SessionService(
    private val sessionRepository: SessionRepository,
    private val seminarRepository: SeminarRepository,
) {
    private val KST_OFFSET = ZoneOffset.ofHours(9)
    fun createSession(
        seminarId: Long,
        request: SessionCreateRequest,
    ): SessionCreateResponse {
        if (!seminarRepository.existsById(seminarId)) {
            throw SeminarNotFoundException(seminarId)
        }

        val savedSession = sessionRepository.save(
            Session(
                seminarId = seminarId,
                title = request.title,
                startsAt = request.startsAt.withOffsetSameInstant(KST_OFFSET).toLocalDateTime(),
                location = request.location,
                assignmentTitle = request.assignmentTitle,
                lectureContent = request.lectureContent,
                assignmentContent= request.assignmentContent,
            )
        )

        val orderedSessions =
            sessionRepository.order(seminarId)

        val index = orderedSessions.indexOfFirst { it.id == savedSession.id }
        check(index >= 0) { "저장된 회차를 정렬된 목록에서 찾지 못했습니다." }

        return SessionCreateResponse.from(savedSession, index + 1L)
    }

    fun getSessions(seminarId: Long): List<SessionListResponse> {
        if (!seminarRepository.existsById(seminarId)) {
            throw SeminarNotFoundException(seminarId)
        }

        return sessionRepository
            .order(seminarId)
            .mapIndexed { index, session ->
                SessionListResponse.from(session, index + 1L)
            }
    }

    fun getSession(sessionId: Long): SessionDetailResponse {
        val session = sessionRepository.findById(sessionId)
            .orElseThrow { SessionNotFoundException(sessionId) }

        val orderedSessions =
            sessionRepository.order(session.seminarId)

        val index = orderedSessions.indexOfFirst { it.id == session.id }
        check(index >= 0) { "저장된 회차를 정렬된 목록에서 찾지 못했습니다." }

        return SessionDetailResponse.from(session, index + 1L)
    }
}