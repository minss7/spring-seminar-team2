package com.wafflestudio.spring2026.seminar.service

import com.wafflestudio.spring2026.seminar.dto.SeminarCreateRequest
import com.wafflestudio.spring2026.seminar.dto.SeminarDetailResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarUpdateRequest
import com.wafflestudio.spring2026.seminar.exception.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.model.Seminar
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset



@Service
class SeminarService(
        private val seminarRepository: SeminarRepository,
) {
    private val KST_OFFSET = ZoneOffset.ofHours(9)
    @Transactional
    fun createSeminar(request: SeminarCreateRequest): Seminar {
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
                createdAt = java.time.LocalDateTime.now()
        )

        // 저장 처리 후 반환
        return seminarRepository.save(seminar)
    }

    @Transactional(readOnly = true)
    fun getSeminar(seminarId: Long): SeminarDetailResponse {
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
    ): SeminarDetailResponse {
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
}