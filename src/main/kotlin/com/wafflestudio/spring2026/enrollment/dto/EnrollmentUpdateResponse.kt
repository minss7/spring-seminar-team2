package com.wafflestudio.spring2026.enrollment.dto

import java.time.OffsetDateTime

data class EnrollmentUpdateResponse(
    val id: Long,
    val graceDaysRemaining: Int,
    val dropped: Boolean,
    val createdAt: OffsetDateTime
)
