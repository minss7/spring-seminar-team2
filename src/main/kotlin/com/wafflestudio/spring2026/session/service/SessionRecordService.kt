package com.wafflestudio.spring2026.session.service

import com.wafflestudio.spring2026.enrollment.repository.EnrollmentRepository
import com.wafflestudio.spring2026.session.dto.SessionRecordsPatchRequest
import com.wafflestudio.spring2026.session.exception.SessionNotFoundException
import com.wafflestudio.spring2026.session.model.Attendance
import com.wafflestudio.spring2026.session.model.Submission
import com.wafflestudio.spring2026.session.repository.AttendanceRepository
import com.wafflestudio.spring2026.session.repository.SessionRepository
import com.wafflestudio.spring2026.session.repository.SubmissionRepository
import com.wafflestudio.spring2026.user.model.User
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Service
class SessionRecordService(
    private val sessionRepository: SessionRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val submissionRepository: SubmissionRepository,
) {
    @Transactional
    fun updateRecords(
        sessionId: Long,
        request: SessionRecordsPatchRequest,
        principal: User,
    ) {
        val session = sessionRepository.findById(sessionId)
            .orElseThrow { SessionNotFoundException(sessionId) }

        checkManagePermission(session.seminarId, principal)

        // 저장하기 전에 모든 수강 신청을 검증한다.
        request.records.forEach { record ->
            val enrollment = enrollmentRepository.findById(record.enrollmentId)
                .orElseThrow {
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "수강 신청을 찾을 수 없습니다.",
                    )
                }

            if (enrollment.seminarId != session.seminarId) {
                throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 세미나의 수강 신청이 아닙니다.",
                )
            }
        }

        request.records.forEach { record ->
            if (record.attendancePresent) {
                updateAttendance(
                    sessionId = sessionId,
                    enrollmentId = record.enrollmentId,
                    status = record.attendance,
                )
            }

            if (record.assignmentStatusPresent) {
                updateSubmission(
                    sessionId = sessionId,
                    enrollmentId = record.enrollmentId,
                    status = record.assignmentStatus,
                )
            }
        }
    }

    private fun checkManagePermission(
        seminarId: Long,
        principal: User,
    ) {
        if (principal.role == "ADMIN") {
            return
        }

        if (
            principal.role != "STAFF" ||
            principal.status != "APPROVED" ||
            principal.assignedSeminarId != seminarId
        ) {
            throw AccessDeniedException("기록을 수정할 권한이 없습니다.")
        }
    }

    private fun updateAttendance(
        sessionId: Long,
        enrollmentId: Long,
        status: String?,
    ) {
        val existing = attendanceRepository.findBySessionIdAndEnrollmentId(
            sessionId,
            enrollmentId,
        )

        if (status == null) {
            if (existing != null) {
                attendanceRepository.delete(existing)
            }
            return
        }

        if (existing == null) {
            attendanceRepository.save(
                Attendance(
                    sessionId = sessionId,
                    enrollmentId = enrollmentId,
                    status = status,
                ),
            )
        } else {
            attendanceRepository.save(existing.copy(status = status))
        }
    }

    private fun updateSubmission(
        sessionId: Long,
        enrollmentId: Long,
        status: String?,
    ) {
        val existing = submissionRepository.findBySessionIdAndEnrollmentId(
            sessionId,
            enrollmentId,
        )

        if (status == null) {
            if (existing != null) {
                submissionRepository.delete(existing)
            }
            return
        }

        if (existing == null) {
            submissionRepository.save(
                Submission(
                    sessionId = sessionId,
                    enrollmentId = enrollmentId,
                    status = status,
                ),
            )
        } else {
            submissionRepository.save(existing.copy(status = status))
        }
    }
}