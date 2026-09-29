package com.wafflestudio.spring2026.seminar.controller

import com.wafflestudio.spring2026.seminar.dto.SeminarCreateRequest
import com.wafflestudio.spring2026.seminar.dto.SeminarCreateResponse
import com.wafflestudio.spring2026.seminar.service.SeminarService
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/seminars")
class SeminarController(
        private val seminarService: SeminarService,
) {
    @PostMapping
    fun createSeminar(
            @Valid @RequestBody request: SeminarCreateRequest,
    ): ResponseEntity<SeminarCreateResponse> {
        val seminar = seminarService.createSeminar(request)
        val response = SeminarCreateResponse.from(seminar)

        return ResponseEntity
                .created(URI.create("/seminars/${seminar.id}"))
                .body(response)
    }
}