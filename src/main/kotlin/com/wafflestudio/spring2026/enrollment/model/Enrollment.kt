package com.wafflestudio.spring2026.enrollment.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("enrollments")
data class Enrollment(
    @Id
    val id: Long? = null,

    @Column("remaining_grace_days")
    val remainingGraceDays: Int,

    @Column("is_failed")
    val failed: Boolean = false,

    @Column("created_at")
    val createdAt: LocalDateTime,

    @Column("user_id")
    val userId: Long,

    @Column("seminar_id")
    val seminarId: Long,
)