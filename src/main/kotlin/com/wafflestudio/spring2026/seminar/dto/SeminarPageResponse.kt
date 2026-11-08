package com.wafflestudio.spring2026.seminar.dto

data class SeminarPageResponse(
    val content: List<SeminarDetailResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
