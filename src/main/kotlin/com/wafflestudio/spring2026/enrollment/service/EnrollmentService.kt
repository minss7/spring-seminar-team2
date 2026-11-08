package com.wafflestudio.spring2026.enrollment.service

import com.wafflestudio.spring2026.enrollment.dto.EnrollmentCreateResponse
import com.wafflestudio.spring2026.enrollment.dto.MyEnrollmentListResponse
import com.wafflestudio.spring2026.enrollment.dto.MyEnrollmentItemResponse
import com.wafflestudio.spring2026.enrollment.dto.MyEnrollmentSeminarResponse
import com.wafflestudio.spring2026.enrollment.exception.EnrollmentNotFoundException
import com.wafflestudio.spring2026.enrollment.model.Enrollment
import com.wafflestudio.spring2026.enrollment.repository.EnrollmentRepository
import com.wafflestudio.spring2026.seminar.dto.SeminarDetailResponse
import com.wafflestudio.spring2026.seminar.exception.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import com.wafflestudio.spring2026.user.model.User
import org.springframework.data.domain.Sort
import org.springframework.data.domain.PageRequest
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
) {
    private val kst = ZoneOffset.ofHours(9)

    @Transactional
    fun enroll(seminarId: Long, principal: User): EnrollmentCreateResponse {

        val userId = requireNotNull(principal.id)
        if (principal.role != "ROOKIE" || principal.status != "APPROVED") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        val seminar = seminarRepository.findById(seminarId)
            .orElseThrow { SeminarNotFoundException(seminarId) }

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

    fun delete(seminarId: Long, principal: User){
        if (principal.role != "ROOKIE" || principal.status != "APPROVED") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        val userId = requireNotNull(principal.id)

        val seminar = seminarRepository.findById(seminarId)
            .orElseThrow { SeminarNotFoundException(seminarId) }

        val enrollment = enrollmentRepository.findByUserIdAndSeminarId(userId, seminarId)
            ?: throw EnrollmentNotFoundException(userId, seminarId)

        val now = LocalDateTime.now(kst)
        val isWithinApplicationPeriod =
            !now.isBefore(seminar.applyStartAt) && now.isBefore(seminar.applyEndAt)

        if (!isWithinApplicationPeriod) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "신청 기간에만 취소할 수 있습니다.")
        }

        enrollmentRepository.delete(enrollment)
    }

    fun getMyEnrollments(
        principal: User,
        page: Int,
        size: Int,
    ): MyEnrollmentListResponse {
        if (principal.role != "ROOKIE" || principal.status != "APPROVED") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        if (page < 0 || size !in 1..100) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }

        val userId = requireNotNull(principal.id)
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id"),
            ),
        )

        val enrollments = enrollmentRepository.findAllByUserId(userId, pageable)
        val totalElements = enrollmentRepository.countByUserId(userId)

        val content = enrollments.map { enrollment ->
            val seminar = seminarRepository.findById(enrollment.seminarId)
                .orElseThrow { SeminarNotFoundException(enrollment.seminarId) }

            val enrolledCount = seminarRepository.countEnrolledUsers(enrollment.seminarId)
            val seminarResponse = SeminarDetailResponse.from(
                seminar,
                enrolledCount,
                seminarRepository.countSessions(enrollment.seminarId),
            )

            MyEnrollmentItemResponse(
                id = requireNotNull(enrollment.id),
                seminar = MyEnrollmentSeminarResponse(
                    id = seminarResponse.id,
                    title = seminarResponse.title,
                    description = seminarResponse.description,
                    status = seminarResponse.status,
                    capacity = seminarResponse.capacity,
                    enrolledCount = seminarResponse.enrolledCount,
                    applyStartAt = seminarResponse.applyStartAt,
                    applyEndAt = seminarResponse.applyEndAt,
                ),
                graceDaysRemaining = enrollment.remainingGraceDays,
                dropped = enrollment.failed,
                createdAt = enrollment.createdAt.atOffset(ZoneOffset.ofHours(9)),
            )
        }

        val totalPages =
            if (totalElements == 0L) 0
            else ((totalElements + size - 1) / size).toInt()

        return MyEnrollmentListResponse(
            content = content,
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }

}