package com.wafflestudio.spring2026.seminar.dto

import com.wafflestudio.spring2026.seminar.model.Seminar
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class SeminarCreateResponse(
        val id: Long,
        val createdAt: OffsetDateTime,
) {
    companion object {
        private val KST_OFFSET = ZoneOffset.ofHours(9)
        fun from(seminar: Seminar): SeminarCreateResponse {
            return SeminarCreateResponse(
                    id = seminar.id!!,
                    createdAt = seminar.createdAt?.atOffset(KST_OFFSET)
                        ?: OffsetDateTime.now(KST_OFFSET),
            )
        }
    }
}