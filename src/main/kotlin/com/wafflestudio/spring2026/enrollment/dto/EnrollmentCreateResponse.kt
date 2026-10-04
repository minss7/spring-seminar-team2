package com.wafflestudio.spring2026.enrollment.dto

import java.time.OffsetDateTime

data class EnrollmentCreateResponse(
    val id: Long,
    val graceDaysRemaining: Int,
    val createdAt: OffsetDateTime
)
