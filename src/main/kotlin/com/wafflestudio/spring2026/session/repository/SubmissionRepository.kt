package com.wafflestudio.spring2026.session.repository

import com.wafflestudio.spring2026.session.model.Submission
import org.springframework.data.repository.CrudRepository

interface SubmissionRepository : CrudRepository<Submission, Long> {
    fun findBySessionIdAndEnrollmentId(
        sessionId: Long,
        enrollmentId: Long,
    ): Submission?
}