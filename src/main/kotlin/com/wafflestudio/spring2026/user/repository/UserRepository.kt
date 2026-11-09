package com.wafflestudio.spring2026.user.repository

import com.wafflestudio.spring2026.user.model.User
import org.springframework.data.repository.CrudRepository
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param

interface UserRepository : CrudRepository<User, Long> {
    fun findByEmail(email: String): User?

    @Query(
        """
        SELECT * FROM users
        WHERE `role` IN ('STAFF', 'ROOKIE')
          AND (:status = 'ALL STATUS' OR `status` = :status)
          AND (:role = 'ALL ROLE' OR `role` = :role)
        ORDER BY `created_at` DESC, `id` DESC
        LIMIT :size OFFSET :offset
        """
    )
    fun getAuthRequestPage( //GET /users 페이지
        @Param("status") queryStatus: String,
        @Param("role") queryRole: String,
        @Param("size") size: Int,
        @Param("offset") pageOffset: Long
    ):List<User>

    @Query(
        """
        SELECT COUNT(*) FROM users
        WHERE `role` IN ('STAFF', 'ROOKIE')
          AND (:status IS NULL OR `status` = :status)
          AND (:role IS NULL OR `role` = :role)
        """
    )
    fun countTotalAuthRequestList( //GET /users totalElements
        @Param("status") status: String?,
        @Param("role") role: String?,
    ): Long
}