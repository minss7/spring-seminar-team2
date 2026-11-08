package com.wafflestudio.spring2026.enrollment.repository

import com.wafflestudio.spring2026.enrollment.model.Enrollment
import org.springframework.data.repository.CrudRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface EnrollmentRepository : CrudRepository<Enrollment, Long>, PagingAndSortingRepository<Enrollment, Long> {
    fun existsByUserIdAndSeminarId(userId: Long, seminarId: Long): Boolean
    fun findByUserIdAndSeminarId(userId: Long, seminarId: Long): Enrollment?
    fun findAllByUserId(userId: Long, pageable: Pageable): List<Enrollment>
    fun countByUserId(userId: Long): Long
}
