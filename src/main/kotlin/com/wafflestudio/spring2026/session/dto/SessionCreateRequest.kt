package com.wafflestudio.spring2026.session.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class SessionCreateRequest(
    @field:NotBlank
    val title: String,

    @field:NotNull
    val startsAt: OffsetDateTime,

    @field:NotBlank
    val location: String,

    @field:NotBlank
    val assignmentTitle: String,

    val lectureContent: String? = null,

    val assignmentContent : String? = null,
)