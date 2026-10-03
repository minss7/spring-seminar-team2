package com.wafflestudio.spring2026

import com.wafflestudio.spring2026.auth.exception.EmailAlreadyExistsException
import com.wafflestudio.spring2026.meeting.MeetingNotFoundException
import com.wafflestudio.spring2026.seminar.exception.SeminarNotFoundException
import com.wafflestudio.spring2026.user.exception.UserNotFoundException
import com.wafflestudio.spring2026.user.exception.UserStatusNotPendingException
import jakarta.validation.constraints.Email
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.Collections.emptyList

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        exception: MethodArgumentNotValidException,
    ): ResponseEntity<ApiErrorResponse> {
        val fieldErrors = exception.bindingResult.fieldErrors.map { error ->
            FieldErrorResponse(
                field = error.field,
                message = error.defaultMessage ?: "잘못된 값입니다.",
            )
        }

        return ResponseEntity.badRequest().body(
            ApiErrorResponse(
                code = "INVALID_REQUEST",
                message = "요청값이 올바르지 않습니다.",
                fieldErrors = fieldErrors,
            ),
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
            exception: IllegalArgumentException,
    ): ResponseEntity<ApiErrorResponse> =
            ResponseEntity.badRequest().body(
                    ApiErrorResponse(
                            code = "INVALID_REQUEST",
                            message = exception.message ?: "잘못된 요청입니다.",
                            fieldErrors = emptyList(),
                    ),
            )

    @ExceptionHandler(MeetingNotFoundException::class)
    fun handleMeetingNotFound(
        exception: MeetingNotFoundException,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiErrorResponse(
                code = "MEETING_NOT_FOUND",
                message = exception.message ?: "모임을 찾을 수 없습니다.",
                fieldErrors = emptyList(),
            ),
        )

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExists(
        exception: EmailAlreadyExistsException,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiErrorResponse(
                code = "EMAIL_ALREADY_EXISTS",
                message = exception.message ?: "해당 이메일은 이미 가입되어 있는 이메일입니다.",
            ),
        )

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(
        exception: UserNotFoundException,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiErrorResponse(
                code = "USER_NOT_FOUND",
                message = exception.message ?: "해당 ID를 가진 사용자는 존재하지 않습니다.",
            ),
        )

    @ExceptionHandler(UserStatusNotPendingException::class)
    fun handleUserStatusNotPending(
        exception: UserStatusNotPendingException,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiErrorResponse(
                code = "USER_STATUS_NOT_PENDING",
                message = exception.message ?: "해당 사용자는 이미 ${exception.status} 상태입니다.",
            ),
        )

    @ExceptionHandler(SeminarNotFoundException::class)
    fun handleSeminarNotFound(
        exception: SeminarNotFoundException,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiErrorResponse(
                code = "SEMINAR_NOT_FOUND",
                message = exception.message ?: "세미나를 찾을 수 없습니다.",
                fieldErrors = emptyList(),
            ),
        )
}
