package com.wafflestudio.spring2026.seminar.controller

import com.fasterxml.jackson.databind.JsonNode
import com.wafflestudio.spring2026.seminar.dto.SeminarCreateRequest
import com.wafflestudio.spring2026.seminar.dto.SeminarCreateResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarDetailResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarUpdateRequest
import com.wafflestudio.spring2026.seminar.service.SeminarService
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.wafflestudio.spring2026.user.model.User
import org.springframework.security.core.annotation.AuthenticationPrincipal

@RestController
@RequestMapping("/seminars")
class SeminarController(
        private val seminarService: SeminarService,
) {
    @PostMapping
    fun createSeminar(
            @Valid @RequestBody request: SeminarCreateRequest,
            @AuthenticationPrincipal principal: User,
    ): ResponseEntity<SeminarCreateResponse> {
        val seminar = seminarService.createSeminar(request, principal)
        val response = SeminarCreateResponse.from(seminar)

        return ResponseEntity
                .created(URI.create("/seminars/${seminar.id}"))
                .body(response)
    }

    @GetMapping("/{seminarId}")
    fun getSeminar(@PathVariable seminarId: Long,
                        @AuthenticationPrincipal principal: User,
                   ): ResponseEntity<SeminarDetailResponse> {
            val response = seminarService.getSeminar(seminarId, principal)
            return ResponseEntity.ok(response)
    }

    @PatchMapping("/{seminarId}")
    fun updateSeminar(
        @PathVariable seminarId: Long,
        @RequestBody body: Map<String, Any?>,
        @AuthenticationPrincipal principal: User,
    ): SeminarDetailResponse {
        val request = SeminarUpdateRequest.fromMap(body)
        return seminarService.updateSeminar(seminarId, request, principal)
    }


}