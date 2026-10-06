package com.wafflestudio.spring2026.user.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("users")
data class User(
    @Id
    val id: Long? = null,

    val email: String,

    val password: String,

    val name: String,

    @Column("github_username") val githubUsername: String,
    val role: String,

    val status: String,

    @Column("assigned_seminar_id")
    val assignedSeminarId: Long? = null,

    @Column("created_at")
    val createdAt: LocalDateTime,
)