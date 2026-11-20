package com.wafflestudio.spring2026.session.repository

import com.wafflestudio.spring2026.session.model.Attendance
import org.springframework.data.repository.CrudRepository

interface AttendanceRepository : CrudRepository<Attendance, Long> {
    fun findBySessionIdAndEnrollmentId(
        sessionId: Long,
        enrollmentId: Long,
    ): Attendance?
}