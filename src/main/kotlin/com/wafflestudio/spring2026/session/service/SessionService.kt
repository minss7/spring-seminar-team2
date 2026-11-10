package com.wafflestudio.spring2026.session.service

import com.wafflestudio.spring2026.enrollment.repository.EnrollmentRepository
import com.wafflestudio.spring2026.seminar.exception.SeminarNotFoundException
import com.wafflestudio.spring2026.session.dto.SessionCreateRequest
import com.wafflestudio.spring2026.session.dto.SessionCreateResponse
import com.wafflestudio.spring2026.session.model.Session
import com.wafflestudio.spring2026.session.repository.SessionRepository
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import com.wafflestudio.spring2026.session.dto.SessionDetailResponse
import com.wafflestudio.spring2026.session.dto.SessionListResponse
import com.wafflestudio.spring2026.session.dto.SessionUpdateRequest
import com.wafflestudio.spring2026.session.exception.SessionNotFoundException
import com.wafflestudio.spring2026.user.model.User
import jakarta.validation.constraints.Null
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import org.springframework.security.access.AccessDeniedException

@Service
class SessionService(
    private val sessionRepository: SessionRepository,
    private val seminarRepository: SeminarRepository,
    private val enrollmentRepository: EnrollmentRepository,
) {
    private val KST_OFFSET = ZoneOffset.ofHours(9)
    // createSession, updateSession 인증 검사 함수
    private fun checkManageSessionAuth(
        seminarId: Long,
        principal: User,
        ) {
        if (principal.role == "ADMIN") {
            return
        }
        if (principal.role != "STAFF" || principal.status != "APPROVED" || principal.assignedSeminarId != seminarId) {
            throw AccessDeniedException("허가되지 않은 사용자입니다.")
        }
    }

    fun createSession(
        seminarId: Long,
        request: SessionCreateRequest,
        principal: User,
    ): SessionCreateResponse {

        checkManageSessionAuth(seminarId, principal)
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
    // getSession, getSession 인증 검사 함수
    private fun checkGetSessionAuth(
        seminarId: Long,
        principal: User,
    ) {
        if (principal.role == "ADMIN") {
            return
        }
        if (principal.role == "STAFF") {

            if (principal.status != "APPROVED" || principal.assignedSeminarId != seminarId) {
                throw AccessDeniedException("허가되지 않은 사용자입니다.")
            }
        }
        else if (principal.role == "ROOKIE") {
            val userId = principal.id
                ?: throw AccessDeniedException("허가되지 않은 사용자입니다.")
            val enrollment = enrollmentRepository.findByUserIdAndSeminarId(userId, seminarId)
            if (principal.status != "APPROVED" ||
                enrollment == null
            ) {
                throw AccessDeniedException("허가되지 않은 사용자입니다.")
            }
        }
        else {
            throw AccessDeniedException("허가되지 않은 사용자입니다.")
        }

    }
    fun getSessions(
        seminarId: Long,
        principal: User,
    ): List<SessionListResponse> {
        checkGetSessionAuth(seminarId, principal)
        if (!seminarRepository.existsById(seminarId)) {
            throw SeminarNotFoundException(seminarId)
        }

        return sessionRepository
            .order(seminarId)
            .mapIndexed { index, session ->
                SessionListResponse.from(session, index + 1L)
            }
    }

    fun getSession(
        sessionId: Long,
        principal: User,
    ): SessionDetailResponse {

        val session = sessionRepository.findById(sessionId)
            .orElseThrow {SessionNotFoundException(sessionId)}

        checkGetSessionAuth(session.seminarId, principal)

        val orderedSessions =
            sessionRepository.order(session.seminarId)

        val index = orderedSessions.indexOfFirst { it.id == session.id }
        check(index >= 0) {"저장된 회차를 정렬된 목록에서 찾지 못했습니다."}

        return SessionDetailResponse.from(session, index + 1L)
    }

    fun updateSession (
        sessionId: Long,
        request: SessionUpdateRequest,
        principal: User,
    ): SessionDetailResponse {

        val session = sessionRepository.findById(sessionId)
            .orElseThrow {SessionNotFoundException(sessionId)}

        checkManageSessionAuth(session.seminarId, principal)

        if (request.isTitlePresent) {
            require(!request.title.isNullOrBlank()) {
                "title은 공백일 수 없습니다."
            }
        }

        if (request.isStartsAtPresent) {
            requireNotNull(request.startsAt) {
                "startsAt은 필수입니다."
            }
        }

        if (request.isLocationPresent) {
            require(!request.location.isNullOrBlank()) {
                "location은 공백일 수 없습니다."
            }
        }

        if (request.isAssignmentTitlePresent) {
            require(!request.assignmentTitle.isNullOrBlank()) {
                "assignmentTitle은 공백일 수 없습니다."
            }
        }
        val updatedSession = session.copy(
            title = if (request.isTitlePresent) request.title!! else session.title,
            startsAt = if (request.isStartsAtPresent) {
                request.startsAt!!
                    .withOffsetSameInstant(KST_OFFSET)
                    .toLocalDateTime()
            } else {
                session.startsAt
            },
            location = if (request.isLocationPresent) request.location!! else session.location,
            assignmentTitle = if (request.isAssignmentTitlePresent) {
                request.assignmentTitle!!
            } else {
                session.assignmentTitle
            },
            lectureContent = if (request.isLectureContentPresent) {
                request.lectureContent
            } else {
                session.lectureContent
            },
            assignmentContent = if (request.isAssignmentContentPresent) {
                request.assignmentContent
            } else {
                session.assignmentContent
            },
        )
        val savedSession = sessionRepository.save(updatedSession)
        val orderedSessions = sessionRepository.order(savedSession.seminarId)
        val index = orderedSessions.indexOfFirst { it.id == savedSession.id }

        check(index >= 0) {"수정된 회차를 정렬된 목록에서 찾지 못했습니다."}

        return SessionDetailResponse.from(savedSession, index + 1L)

    }
}