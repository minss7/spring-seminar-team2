package com.wafflestudio.spring2026.seminar.controller

import com.wafflestudio.spring2026.seminar.dto.SeminarCreateRequest
import com.wafflestudio.spring2026.seminar.dto.SeminarCreateResponse
import com.wafflestudio.spring2026.seminar.service.SeminarService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/seminars")
class SeminarController(
        private val seminarService: SeminarService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createSeminar(
            @Valid @RequestBody request: SeminarCreateRequest,
    ): SeminarCreateResponse {
        val seminar = seminarService.createSeminar(request)
        return SeminarCreateResponse.from(seminar)
    }
}