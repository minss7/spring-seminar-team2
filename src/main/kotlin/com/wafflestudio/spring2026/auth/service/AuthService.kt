package com.wafflestudio.spring2026.auth.service

import com.wafflestudio.spring2026.auth.dto.LoginRequest
import com.wafflestudio.spring2026.auth.dto.LoginResponse
import com.wafflestudio.spring2026.auth.exception.LoginFailedException
import com.wafflestudio.spring2026.auth.jwt.JwtService
import com.wafflestudio.spring2026.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import com.wafflestudio.spring2026.auth.exception.InvalidTokenException
import com.wafflestudio.spring2026.auth.repository.LogoutTokenRepository
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val logoutTokenRepository: LogoutTokenRepository,
) {
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw LoginFailedException()

        val passwordMatches =
            passwordEncoder.matches(request.password, user.password)

        if (!passwordMatches) {
            throw LoginFailedException()
        }

        val userId = requireNotNull(user.id)
        val token = jwtService.issue(userId)

        return LoginResponse(accessToken = token)
    }

    fun logout(jti: String, expiresAt: Instant) {
        val inserted = logoutTokenRepository.insert(jti, expiresAt)

        // 동시에 같은 토큰으로 로그아웃한 경우도 처리
        if (!inserted) {
            throw InvalidTokenException()
        }
    }
}