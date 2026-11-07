package com.wafflestudio.spring2026.auth.controller

import com.wafflestudio.spring2026.auth.dto.SignupRequest
import com.wafflestudio.spring2026.auth.dto.SignupResponse
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.wafflestudio.spring2026.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.net.URI
import com.wafflestudio.spring2026.auth.dto.LoginRequest
import com.wafflestudio.spring2026.auth.dto.LoginResponse
import com.wafflestudio.spring2026.auth.service.AuthService
import io.jsonwebtoken.Claims
import org.springframework.web.bind.annotation.RequestAttribute

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService,
    private val authService: AuthService,
) {
    @PostMapping("/signup")
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): ResponseEntity<SignupResponse> {
        val response: SignupResponse = userService.signup(request)

        return ResponseEntity
            .created(URI.create("/auth/signup/${response.id}"))
            .body(response)
    }
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): LoginResponse =
        authService.login(request)

    @PostMapping("/logout")
    fun logout(
        @RequestAttribute("jwtClaims") claims: Claims,
    ): ResponseEntity<Void> {
        authService.logout(
            jti = requireNotNull(claims.id),
            expiresAt = requireNotNull(claims.expiration).toInstant(),
        )

        return ResponseEntity.noContent().build()
    }
}