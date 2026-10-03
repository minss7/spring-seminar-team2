package com.wafflestudio.spring2026.auth.dto

data class SignupResponse(
    val id: Long,
    val status: String, //"PENDING" 고정
    val createdAt: String, //ISO 8601 방식
)