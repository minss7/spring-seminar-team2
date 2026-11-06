package com.wafflestudio.spring2026.auth.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import java.util.UUID

@Service
class JwtService(
    @Value("\${jwt.secret}") jwtSecret: String,
) {
    private val signingKey =
        Keys.hmacShaKeyFor(jwtSecret.toByteArray(Charsets.UTF_8))

    fun issue(userId: Long): String {
        val issuedAt = Instant.now()

        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(userId.toString())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(issuedAt.plusSeconds(3600)))
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact()
    }

    fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}