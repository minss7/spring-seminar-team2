package com.wafflestudio.spring2026.seminar.service

import com.wafflestudio.spring2026.enrollment.repository.EnrollmentRepository
import com.wafflestudio.spring2026.seminar.dto.DashboardEnrollmentResponse
import com.wafflestudio.spring2026.seminar.dto.DashboardRecordResponse
import com.wafflestudio.spring2026.seminar.dto.DashboardSessionResponse
import com.wafflestudio.spring2026.seminar.dto.DashboardUserResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarCreateRequest
import com.wafflestudio.spring2026.seminar.dto.SeminarDetailResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarUpdateRequest
import com.wafflestudio.spring2026.seminar.dto.SeminarPageResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarDashboardResponse
import com.wafflestudio.spring2026.seminar.exception.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.model.Seminar
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import com.wafflestudio.spring2026.session.model.Session
import com.wafflestudio.spring2026.session.repository.AttendanceRepository
import com.wafflestudio.spring2026.session.repository.SessionRepository
import com.wafflestudio.spring2026.session.repository.SubmissionRepository
import com.wafflestudio.spring2026.user.exception.UserNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneOffset
import com.wafflestudio.spring2026.user.model.User
import com.wafflestudio.spring2026.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.AccessDeniedException
import kotlin.Long
import kotlin.math.ceil


@Service
class SeminarService(
    private val seminarRepository: SeminarRepository,
    private val sessionRepository: SessionRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository,
    private val submissionRepository: SubmissionRepository,
) {
    private val KST_OFFSET = ZoneOffset.ofHours(9)
    @Transactional
    fun createSeminar(request: SeminarCreateRequest,
                      principal: User,
                      ): Seminar {
        if (principal.role != "ADMIN") {
            throw AccessDeniedException("세미나 개설 권한이 없습니다.")
        }
        // 애플리케이션 비즈니스 규칙 검증
        require(request.applyEndAt.isAfter(request.applyStartAt)) {
            "applyEndAt은 applyStartAt보다 뒤여야 합니다."
        }

        // 내부 데이터(Model) 생성
        val seminar = Seminar(
                title = request.title,
                description = request.description,
                capacity = request.capacity,
                applyStartAt = request.applyStartAt.withOffsetSameInstant(KST_OFFSET).toLocalDateTime(),
                applyEndAt = request.applyEndAt.withOffsetSameInstant(KST_OFFSET).toLocalDateTime(),
                totalGraceDays = request.totalGraceDays,
                createdAt = java.time.LocalDateTime.now(KST_OFFSET)
        )

        // 저장 처리 후 반환
        return seminarRepository.save(seminar)
    }

    @Transactional(readOnly = true)
    fun getSeminars(
        keyword : String?,
        status : String?,
        page : Int,
        size : Int,
    ): SeminarPageResponse {
        require(status == null || status in setOf("BEFORE", "OPEN", "CLOSED")) {
            "올바르지 않은 status 값입니다."
        }
        require(page >= 0) {
            "page 값이 올바르지 않습니다."
        }

        require(size in 1..100) {
            "size가 올바르지 않습니다."
        }

        val normalizedKeyword: String? =
            if (keyword == null) {
                null
            } else {
                val trimmedKeyword = keyword.trim()

                if (trimmedKeyword.isNotEmpty()) {
                    trimmedKeyword
                } else {
                    null
                }
            }
        val now = java.time.LocalDateTime.now(KST_OFFSET)
        val offset = page.toLong() * size

        val seminars = seminarRepository.findSeminars(
            keyword = normalizedKeyword,
            status = status,
            now = now,
            size = size,
            offset = offset,
        )

        val totalElements = seminarRepository.countSeminars(
            keyword = normalizedKeyword,
            status = status,
            now = now,
        )

        val content = seminars.map { seminar ->
            val seminarId = requireNotNull(seminar.id)

            SeminarDetailResponse.from(
                seminar = seminar,
                enrolledCount = seminarRepository.countEnrolledUsers(seminarId),
                sessionCount = seminarRepository.countSessions(seminarId),
            )
        }

        val totalPages =
            if (totalElements == 0L) 0
            else ((totalElements + size - 1) / size).toInt()

        return SeminarPageResponse(
            content = content,
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }

    @Transactional(readOnly = true)
    fun getSeminar(seminarId: Long,
                   principal: User,
                   ): SeminarDetailResponse {

        if (principal.role != "ADMIN") {
            if (
                principal.status != "APPROVED" ||
                principal.role !in setOf("STAFF", "ROOKIE")
            ) {
                throw AccessDeniedException("세미나 조회 권한이 없습니다.")
            }
        }

        // DB에 세미나가 없으면 SeminarNotFoundException 발생 -> GlobalExceptionHandler에서 404 응답 처리
        val seminar = seminarRepository.findById(seminarId)
            .orElseThrow { SeminarNotFoundException(seminarId) }

        val enrolledCount = seminarRepository.countEnrolledUsers(seminarId)
        val sessionCount = seminarRepository.countSessions(seminarId)

        return SeminarDetailResponse.from(
            seminar = seminar,
            enrolledCount = enrolledCount,
            sessionCount = sessionCount,
        )
    }

    @Transactional
    fun updateSeminar(
        seminarId: Long,
        request: SeminarUpdateRequest,
        principal: User,
    ): SeminarDetailResponse {
        if (principal.role != "ADMIN") {
            if (
                principal.status != "APPROVED" ||
                principal.role != "STAFF" ||
                principal.assignedSeminarId != seminarId
            ) {
                throw AccessDeniedException("세미나 수정 권한이 없습니다.")
            }
        }

        // 1. 애플리케이션 비즈니스 규칙 검증 (Service 책임)
        if (request.isTitlePresent) {
            require(!request.title.isNullOrBlank()) { "title은 공백이거나 null일 수 없습니다." }
        }

        // 2. 데이터 조회 및 예외 처리
        val seminar = seminarRepository.findById(seminarId)
            .orElseThrow { SeminarNotFoundException(seminarId) }

        // 3. 엔티티 상태 변경
        val updatedSeminar = seminar.copy(
            title = request.title ?: seminar.title,
            description = if (request.isDescriptionPresent) request.description else seminar.description,
            createdAt = seminar.createdAt,
        )

        // 4. 영속화 및 응답 생성
        val savedSeminar = seminarRepository.save(updatedSeminar)
        val enrolledCount = seminarRepository.countEnrolledUsers(seminarId)
        val sessionCount = seminarRepository.countSessions(seminarId)
        return SeminarDetailResponse.from(
            seminar = savedSeminar,
            enrolledCount = enrolledCount,
            sessionCount = sessionCount,
        )
    }

    fun getDashboard(
        principal: User,
        seminarId: Long,
        page: Int,
        size: Int,
    ): SeminarDashboardResponse{
        //1.세미나 존재하는지 확인
        if(!seminarRepository.existsById(seminarId)){
            throw SeminarNotFoundException(seminarId)
        }

        //2.권한 검사
        checkGetDashboardAuth(principal, seminarId)

        //3.조립해서 반환
        //response.sessions
        val sessions: List<Session> = sessionRepository.order(seminarId)
        val sessionResponses: List<DashboardSessionResponse> =
            sessions.mapIndexed { index, session ->
                DashboardSessionResponse.from(session, index+1L) }

        //response.content
        val pageable = PageRequest.of(
            page, size, Sort.by("id").ascending()
        )
        val enrollments = enrollmentRepository.findAllBySeminarId(seminarId, pageable)
        val contentResponses: List<DashboardEnrollmentResponse> =
            enrollments.map { enrollment ->//->DashboardEnrollmentResponse
            val enrollmentId = requireNotNull(enrollment.id)
            val user = userRepository.findById(enrollment.userId)
                .orElseThrow { UserNotFoundException() }
            val records = sessions.map { session ->//->DashboardRecordResponse
                val sessionId = requireNotNull(session.id)
                val attendance =
                    attendanceRepository.findBySessionIdAndEnrollmentId(
                        sessionId, enrollmentId
                    )
                val submission =
                    submissionRepository.findBySessionIdAndEnrollmentId(
                        sessionId, enrollmentId
                    )
                DashboardRecordResponse(
                    sessionId = sessionId,
                    attendance = attendance?.status,
                    assignmentStatus = submission?.status,
                )
            }
            DashboardEnrollmentResponse(
                id = enrollmentId,
                user = DashboardUserResponse.from(user),
                graceDaysRemaining = enrollment.remainingGraceDays,
                dropped = enrollment.failed,
                createdAt = enrollment.createdAt.atOffset(KST_OFFSET),
                records = records,
            )
        }

        val totalElements = seminarRepository.countEnrolledUsers(seminarId).toLong()

        val totalPages = ceil(totalElements.toDouble() / size).toInt()

        //response
        return SeminarDashboardResponse(
            seminarId= seminarId,
            sessions = sessionResponses,
            content = contentResponses,
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }

    private fun checkGetDashboardAuth(//getDashboard() 인증 검사
        principal: User,
        seminarId: Long,
    ) {
        //SessionService.checkGetSessionAuth 똑같아서 복붙&수정함
        //와장, 담당 운영진, 해당 세미나를 수강 중인 루키|아니면 403
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
}