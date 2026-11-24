package com.wafflestudio.spring2026.seminar.dto

import com.wafflestudio.spring2026.session.model.Session
import com.wafflestudio.spring2026.user.model.User
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class SeminarDashboardResponse(//response
    //세미나의 회차, 수강생별 운영 현황 조회|행은 수강생, 열은 회차|수강생 목록은 페이지 단위
    val seminarId: Long,
    val sessions: List<DashboardSessionResponse>,
    val content: List<DashboardEnrollmentResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class DashboardSessionResponse(//response.sessions
    val id: Long,//sessionId
    val round: Long,
    val title: String,
    val startsAt: OffsetDateTime,
    val assignmentTitle: String,
){
    companion object{
        fun from(
            session: Session,
            round: Long,
        ): DashboardSessionResponse{
            return DashboardSessionResponse(
                id = requireNotNull(session.id),
                round = round,
                title = session.title,
                startsAt = session.startsAt.atOffset(ZoneOffset.ofHours(9)),
                assignmentTitle = session.assignmentTitle,
            )
        }
    }
}

data class DashboardEnrollmentResponse(//response.content
    val id: Long,//enrollmentId
    val user: DashboardUserResponse,
    val graceDaysRemaining: Int,
    val dropped: Boolean,
    val createdAt: OffsetDateTime,
    val records: List<DashboardRecordResponse>,
)

data class DashboardUserResponse(//response.content.user
    val id: Long,//userId
    val email: String,
    val name: String,
    val githubUsername: String,
){
    companion object{
        fun from(
            user: User,
        ): DashboardUserResponse{
            return DashboardUserResponse(
                id = requireNotNull(user.id),
                email = user.email,
                name = user.name,
                githubUsername = user.githubUsername,
            )
        }
    }
}

data class DashboardRecordResponse(//response.content.records
    val sessionId: Long,
    val attendance: String?,
    val assignmentStatus: String?,
)