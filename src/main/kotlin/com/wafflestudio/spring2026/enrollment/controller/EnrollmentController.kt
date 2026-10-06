package com.wafflestudio.spring2026.enrollment.controller

import com.wafflestudio.spring2026.enrollment.dto.EnrollmentCreateRequest
import com.wafflestudio.spring2026.enrollment.dto.EnrollmentCreateResponse
import com.wafflestudio.spring2026.enrollment.service.EnrollmentService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
        @RequestBody request: EnrollmentCreateRequest,
    ): EnrollmentCreateResponse =
        enrollmentService.enroll(seminarId, request)

    @DeleteMapping("/{enrollmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable seminarId: Long,
        @PathVariable enrollmentId: Long
    ){
        enrollmentService.delete(seminarId, enrollmentId)
    }
}
