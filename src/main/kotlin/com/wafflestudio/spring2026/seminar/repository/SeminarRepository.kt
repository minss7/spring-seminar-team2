package com.wafflestudio.spring2026.seminar.repository

import com.wafflestudio.spring2026.seminar.model.Seminar
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface SeminarRepository : CrudRepository<Seminar, Long> {

    @Query("SELECT COUNT(*) FROM enrollments WHERE seminar_id = :seminarId")
    fun countEnrolledUsers(@Param("seminarId") seminarId: Long): Int

    @Query("SELECT COUNT(*) FROM sessions WHERE seminar_id = :seminarId")
    fun countSessions(@Param("seminarId") seminarId: Long): Int
}