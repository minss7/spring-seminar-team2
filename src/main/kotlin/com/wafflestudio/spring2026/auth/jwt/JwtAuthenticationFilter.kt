package com.wafflestudio.spring2026.auth.jwt

import com.wafflestudio.spring2026.auth.repository.LogoutTokenRepository
import com.wafflestudio.spring2026.security.SecurityErrorWriter
import com.wafflestudio.spring2026.user.repository.UserRepository
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val logoutTokenRepository: LogoutTokenRepository,
    private val securityErrorWriter: SecurityErrorWriter,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authorization = request.getHeader("Authorization")

        // 헤더가 없으면 다음 단계로 보냄.
        // 보호된 경로라면 Security 설정이 인증 실패(401)로 처리함.
        if (authorization == null) {
            filterChain.doFilter(request, response)
            return
        }

        // Authorization 헤더가 있는데 Bearer 형식이 아니면 401
        if (!authorization.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }

        val token = authorization.removePrefix("Bearer ")

        try {
            val claims = jwtService.parseClaims(token)

            val jti = requireNotNull(claims.id)
            require(jti.isNotBlank())
            requireNotNull(claims.expiration)

            val userId = claims.subject.toLong()

            if (logoutTokenRepository.exists(jti)) {
                reject(response)
                return
            }

            val user = userRepository.findById(userId).orElse(null)
            if (user == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                return
            }

            val authorities = listOf(
                SimpleGrantedAuthority("ROLE_${user.role}")
            )

            val authentication =
                UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    authorities,
                )

            SecurityContextHolder.getContext().authentication = authentication

            request.setAttribute("jwtClaims", claims)
        } catch (exception: JwtException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        } catch (exception: IllegalArgumentException) {
            // subject가 숫자가 아니거나 토큰 입력 형식이 잘못된 경우
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        filterChain.doFilter(request, response)
    }
    private fun reject(response: HttpServletResponse) {
        SecurityContextHolder.clearContext()
        securityErrorWriter.unauthorized(response)
    }
}