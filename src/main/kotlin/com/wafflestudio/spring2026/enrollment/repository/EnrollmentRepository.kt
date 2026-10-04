package com.wafflestudio.spring2026.enrollment.repository

import com.wafflestudio.spring2026.enrollment.model.Enrollment
import org.springframework.data.repository.CrudRepository

interface EnrollmentRepository : CrudRepository<Enrollment, Long> {
    fun existsByUserIdAndSeminarId(userId: Long, seminarId: Long): Boolean
}
