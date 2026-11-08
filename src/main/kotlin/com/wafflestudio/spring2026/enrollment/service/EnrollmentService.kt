package com.wafflestudio.spring2026.enrollment.service

import com.wafflestudio.spring2026.enrollment.dto.EnrollmentCreateResponse
import com.wafflestudio.spring2026.enrollment.exception.EnrollmentNotFoundException
import com.wafflestudio.spring2026.enrollment.model.Enrollment
import com.wafflestudio.spring2026.enrollment.repository.EnrollmentRepository
import com.wafflestudio.spring2026.seminar.exception.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import com.wafflestudio.spring2026.user.repository.UserRepository
import com.wafflestudio.spring2026.user.model.User
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class EnrollmentService(
    private val enrollmentRepository: EnrollmentRepository,
    private val seminarRepository: SeminarRepository,
    private val userRepository: UserRepository,
) {
    private val kst = ZoneOffset.ofHours(9)

    @Transactional
    fun enroll(seminarId: Long, principal: User): EnrollmentCreateResponse {

        val seminar = seminarRepository.findById(seminarId)
            .orElseThrow { SeminarNotFoundException(seminarId) }
        val userId = requireNotNull(principal.id)

        if (principal.role != "ROOKIE" || principal.status != "APPROVED") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        if (enrollmentRepository.existsByUserIdAndSeminarId(userId, seminarId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT)
        }

        val now = LocalDateTime.now(kst)
        val enrolledCount = seminarRepository.countEnrolledUsers(seminarId)
        val isOpen = !now.isBefore(seminar.applyStartAt) &&
                now.isBefore(seminar.applyEndAt) &&
                enrolledCount < seminar.capacity
        if (!isOpen) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "현재 신청할 수 없는 세미나입니다.")
        }

        val saved = enrollmentRepository.save(
            Enrollment(
                remainingGraceDays = seminar.totalGraceDays,
                createdAt = now,
                userId = userId,
                seminarId = seminarId,
            ),
        )

        return EnrollmentCreateResponse(
            id = requireNotNull(saved.id),
            graceDaysRemaining = saved.remainingGraceDays,
            createdAt = saved.createdAt.atOffset(kst),
        )
    }

    fun delete(seminarId: Long, enrollmentId: Long){
        val enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow { EnrollmentNotFoundException(enrollmentId) }

        // 다른 세미나의 enrollment ID를 넣어도 삭제되지 않도록 확인
        if (enrollment.seminarId != seminarId) {
            throw EnrollmentNotFoundException(enrollmentId)
        }

        val seminar = seminarRepository.findById(seminarId)
            .orElseThrow { SeminarNotFoundException(seminarId) }

        val now = LocalDateTime.now(kst)
        val isWithinApplicationPeriod =
            !now.isBefore(seminar.applyStartAt) && now.isBefore(seminar.applyEndAt)

        if (!isWithinApplicationPeriod) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "신청 기간에만 취소할 수 있습니다.")
        }

        enrollmentRepository.deleteById(enrollmentId)
    }

}