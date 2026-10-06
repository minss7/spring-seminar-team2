package com.wafflestudio.spring2026.seminar.dto

import com.wafflestudio.spring2026.seminar.model.Seminar
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class SeminarDetailResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val capacity: Int,
    val enrolledCount: Int,
    val applyStartAt: OffsetDateTime,
    val applyEndAt: OffsetDateTime,
    val totalGraceDays: Int,
    val status: String,
    val sessionCount: Int,
) {
    companion object {
        private val KST_OFFSET = ZoneOffset.ofHours(9)
        fun from(seminar: Seminar, enrolledCount: Int, sessionCount: Int): SeminarDetailResponse {
            val now = OffsetDateTime.now(KST_OFFSET)

            val applyStartAtOffset = seminar.applyStartAt.atOffset(KST_OFFSET)
            val applyEndAtOffset = seminar.applyEndAt.atOffset(KST_OFFSET)

            // 1. CLOSED 조건: 정원이 찼거나 종료 시각 지남
            val isClosed = enrolledCount >= seminar.capacity || !now.isBefore(applyEndAtOffset)

            // 2. BEFORE 조건: 아직 시작 전 (정원은 남아있음)
            val isBefore = now.isBefore(applyStartAtOffset)

            val status = when {
                isClosed -> "CLOSED"
                isBefore -> "BEFORE"
                else -> "OPEN"
            }
            return SeminarDetailResponse(
                id = seminar.id!!,
                title = seminar.title,
                description = seminar.description,
                capacity = seminar.capacity,
                enrolledCount = enrolledCount,
                applyStartAt =  applyStartAtOffset,
                applyEndAt = applyEndAtOffset,
                totalGraceDays = seminar.totalGraceDays,
                status = status,
                sessionCount = sessionCount,
            )
        }
    }
}