package com.wafflestudio.spring2026.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SignupRequest(
    @field:NotBlank(message = "이메일은 비어있을 수 없습니다.")
    @field:Email
    val email: String,

    @field:NotBlank(message = "비밀번호는 비어있을 수 없습니다.")
    val password: String,

    @field:NotBlank(message = "이름은 비어있을 수 없습니다.")
    val name: String,

    @field:NotBlank(message = "github 아이디는 비어있을 수 없습니다.")
    val githubUsername: String,

    @field:NotBlank(message = "역할은 비어있을 수 없습니다.")
    @field:Pattern(regexp = "ROOKIE|STAFF", message = "역할은 ROOKIE, STAFF만 가능합니다.")
    val role: String,

    val seminarId: Long? = null,
)