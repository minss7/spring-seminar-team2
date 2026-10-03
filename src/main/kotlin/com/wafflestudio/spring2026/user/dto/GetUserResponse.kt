package com.wafflestudio.spring2026.user.dto

import com.wafflestudio.spring2026.user.model.User
import java.time.ZoneOffset

data class GetUserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val githubUsername: String,
    val role: String,
    val status: String,
    val seminarId: Long?,
    val createdAt: String,
) {
    companion object{
        fun from(user: User): GetUserResponse{
            return GetUserResponse(
                id = user.id!!,
                email = user.email,
                name = user.name,
                githubUsername = user.githubUsername,
                role = user.role,
                status = user.status,
                seminarId = user.assignedSeminarId,
                createdAt = user.createdAt
                    .atOffset(ZoneOffset.ofHours(9))
                    .toString(),
            )
        }
    }
}