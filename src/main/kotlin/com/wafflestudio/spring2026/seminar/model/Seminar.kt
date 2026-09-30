package com.wafflestudio.spring2026.seminar.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("seminars")
data class Seminar(
        @Id
        val id: Long? = null,
        val title: String,
        val description: String?,
        val capacity: Int,
        val applyStartAt: OffsetDateTime,
        val applyEndAt: OffsetDateTime,
        val totalGraceDays: Int,
        val createdAt: OffsetDateTime = OffsetDateTime.now()
)