package com.wafflestudio.spring2026.auth.repository

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant

@Repository
class LogoutTokenRepository(
    private val jdbcClient: JdbcClient,
) {
    fun exists(jti: String): Boolean =
        jdbcClient.sql(
            "SELECT COUNT(*) FROM logout_tokens WHERE jti = :jti"
        )
            .param("jti", jti)
            .query(Long::class.java)
            .single() > 0

    fun insert(jti: String, expiresAt: Instant): Boolean =
        jdbcClient.sql(
            """
            INSERT IGNORE INTO logout_tokens (jti, expires_at)
            VALUES (:jti, :expiresAt)
            """.trimIndent()
        )
            .param("jti", jti)
            .param("expiresAt", Timestamp.from(expiresAt))
            .update() == 1
}