package com.wafflestudio.spring2026.security

import com.wafflestudio.spring2026.ApiErrorResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class SecurityErrorWriter(
    private val objectMapper: ObjectMapper,
) {
    fun unauthorized(response: HttpServletResponse) {
        write(response, 401, "UNAUTHORIZED", "인증에 실패했습니다.")
    }

    fun forbidden(response: HttpServletResponse) {
        write(response, 403, "FORBIDDEN", "접근 권한이 없습니다.")
    }

    private fun write(
        response: HttpServletResponse,
        status: Int,
        code: String,
        message: String,
    ) {
        response.status = status
        response.characterEncoding = "UTF-8"
        response.contentType = "application/json"
        response.writer.write(
            objectMapper.writeValueAsString(
                ApiErrorResponse(
                    code = code,
                    message = message,
                )
            )
        )
    }
}