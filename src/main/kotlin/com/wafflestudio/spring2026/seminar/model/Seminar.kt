package com.wafflestudio.spring2026.seminar.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("seminars")
data class Seminar(
        @Id
        val id: Long? = null,
        val title: String,
        val description: String?,
        val capacity: Int,
        @Column("apply_start_at")
        val applyStartAt: LocalDateTime,
        @Column("apply_end_at")
        val applyEndAt: LocalDateTime,
        @Column("total_grace_days")
        val totalGraceDays: Int,
        @Column("created_at")
        val createdAt: LocalDateTime = LocalDateTime.now(),
)