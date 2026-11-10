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

    @Query(
        """
        SELECT s.*
        FROM seminars s
        WHERE (:keyword IS NULL OR s.title LIKE CONCAT('%', :keyword, '%'))
          AND (
            :status IS NULL
            OR CASE
                WHEN s.apply_end_at <= :now
                  OR (
                    SELECT COUNT(*)
                    FROM enrollments e
                    WHERE e.seminar_id = s.id
                  ) >= s.capacity
                THEN 'CLOSED'
                WHEN s.apply_start_at > :now THEN 'BEFORE'
                ELSE 'OPEN'
               END = :status
          )
        ORDER BY s.id ASC
        LIMIT :size OFFSET :offset
        """
    )
    fun findSeminars(
        @Param("keyword") keyword: String?,
        @Param("status") status: String?,
        @Param("now") now: java.time.LocalDateTime,
        @Param("size") size: Int,
        @Param("offset") offset: Long,
    ): List<Seminar>

    @Query(
        """
        SELECT COUNT(*)
        FROM seminars s
        WHERE (:keyword IS NULL OR s.title LIKE CONCAT('%', :keyword, '%'))
          AND (
            :status IS NULL
            OR CASE
                WHEN s.apply_end_at <= :now
                  OR (
                    SELECT COUNT(*)
                    FROM enrollments e
                    WHERE e.seminar_id = s.id
                  ) >= s.capacity
                THEN 'CLOSED'
                WHEN s.apply_start_at > :now THEN 'BEFORE'
                ELSE 'OPEN'
               END = :status
          )
        """
    )
    fun countSeminars(
        @Param("keyword") keyword: String?,
        @Param("status") status: String?,
        @Param("now") now: java.time.LocalDateTime,
    ): Long
}