package com.wafflestudio.spring2026.seminar.dto
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class SeminarCreateRequest(
        @field:NotBlank
        val title: String,

        val description: String?,

        @field:NotNull
        @field:Min(1)
        val capacity: Int,

        @field:NotNull
        val applyStartAt: OffsetDateTime,

        @field:NotNull
        val applyEndAt: OffsetDateTime,

        @field:NotNull
        @field:Min(0)
        val totalGraceDays: Int
)
