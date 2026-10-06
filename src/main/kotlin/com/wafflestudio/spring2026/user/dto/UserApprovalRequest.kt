package com.wafflestudio.spring2026.user.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class UserApprovalRequest(
    @field:NotBlank
    @field:Pattern(regexp = "APPROVED|REJECTED", message = "가입 신청 상태는 APPROVED, REJECTED만 가능합니다.")
    val status: String,
)