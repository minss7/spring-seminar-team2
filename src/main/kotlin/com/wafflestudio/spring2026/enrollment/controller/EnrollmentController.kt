package com.wafflestudio.spring2026.enrollment.controller

import com.wafflestudio.spring2026.enrollment.dto.EnrollmentCreateResponse
import com.wafflestudio.spring2026.enrollment.service.EnrollmentService
import com.wafflestudio.spring2026.user.model.User
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/seminars/{seminarId}/enrollments")
class EnrollmentController(
    private val enrollmentService: EnrollmentService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun enroll(
        @PathVariable seminarId: Long,
        @AuthenticationPrincipal principal: User,
    ): EnrollmentCreateResponse =
        enrollmentService.enroll(seminarId, principal)

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable seminarId: Long,
        @AuthenticationPrincipal principal: User,
    ){
        enrollmentService.delete(seminarId, principal)
    }
}
