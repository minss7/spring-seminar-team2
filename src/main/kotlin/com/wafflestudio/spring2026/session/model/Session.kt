package com.wafflestudio.spring2026.session.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import org.springframework.data.relational.core.mapping.Table

@Table("sessions")
data class Session(
    @Id
    val id: Long? = null,

    @Column("seminar_id")
    val seminarId : Long,

    val title: String,

    @Column("starts_at")
    val startsAt: LocalDateTime,

    val location : String,

    @Column("assignment_title")
    val assignmentTitle : String,

    @Column("lecture_content")
    val lectureContent: String?,

    @Column("assignment_content")
    val assignmentContent: String?,
)
