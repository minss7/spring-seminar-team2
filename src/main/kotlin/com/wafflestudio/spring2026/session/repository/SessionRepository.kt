package com.wafflestudio.spring2026.session.repository

import com.wafflestudio.spring2026.session.model.Session
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface SessionRepository : CrudRepository<Session, Long> {
    @Query("""
        SELECT * FROM sessions
        WHERE seminar_id = :seminarId
        ORDER BY starts_at ASC, id ASC
    """)
    fun order(@Param("seminarId") seminarId: Long): List<Session>
}