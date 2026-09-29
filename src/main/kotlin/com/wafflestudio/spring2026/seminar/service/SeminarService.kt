package com.wafflestudio.spring2026.seminar.service

import com.wafflestudio.spring2026.seminar.dto.SeminarCreateRequest
import com.wafflestudio.spring2026.seminar.model.Seminar
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class SeminarService(
        private val seminarRepository: SeminarRepository,
) {
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
                applyStartAt = request.applyStartAt,
                applyEndAt = request.applyEndAt,
                totalGraceDays = request.totalGraceDays,
        )

        // 저장 처리 후 반환
        return seminarRepository.save(seminar)
    }
}