package com.wafflestudio.spring2026.auth.service

import com.wafflestudio.spring2026.auth.dto.LoginRequest
import com.wafflestudio.spring2026.auth.dto.LoginResponse
import com.wafflestudio.spring2026.auth.exception.LoginFailedException
import com.wafflestudio.spring2026.auth.jwt.JwtService
import com.wafflestudio.spring2026.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
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
}