package com.wafflestudio.spring2026.user.dto

data class AuthRequestListResponse(
    val content: List<GetUserResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
