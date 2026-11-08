package com.wafflestudio.spring2026.enrollment.dto

import java.time.OffsetDateTime

data class MyEnrollmentListResponse(
    val content: List<MyEnrollmentItemResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class MyEnrollmentItemResponse(
    val id: Long,
    val seminar: MyEnrollmentSeminarResponse,
    val graceDaysRemaining: Int,
    val dropped: Boolean,
    val createdAt: OffsetDateTime,
)

data class MyEnrollmentSeminarResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val status: String,
    val capacity: Int,
    val enrolledCount: Int,
    val applyStartAt: OffsetDateTime,
    val applyEndAt: OffsetDateTime,
)
