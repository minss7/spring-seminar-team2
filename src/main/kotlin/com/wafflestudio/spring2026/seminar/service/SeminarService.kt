package com.wafflestudio.spring2026.seminar.service

import com.wafflestudio.spring2026.seminar.dto.SeminarCreateRequest
import com.wafflestudio.spring2026.seminar.dto.SeminarDetailResponse
import com.wafflestudio.spring2026.seminar.exception.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.model.Seminar
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
}