package com.wafflestudio.spring2026.user.repository

import com.wafflestudio.spring2026.user.model.User
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Long> {
    fun findByEmail(email: String): User?
}