package com.wafflestudio.spring2026.session.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("attendances")
data class Attendance(
    @Id
    val id: Long? = null,

    val status: String,

    @Column("session_id")
    val sessionId: Long,

    @Column("enrollment_id")
    val enrollmentId: Long,
)