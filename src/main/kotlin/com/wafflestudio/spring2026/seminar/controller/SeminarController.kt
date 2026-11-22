package com.wafflestudio.spring2026.seminar.controller

import com.fasterxml.jackson.databind.JsonNode
import com.wafflestudio.spring2026.seminar.dto.SeminarCreateRequest
import com.wafflestudio.spring2026.seminar.dto.SeminarCreateResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarDetailResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarPageResponse
import com.wafflestudio.spring2026.seminar.dto.SeminarUpdateRequest
import com.wafflestudio.spring2026.seminar.dto.SeminarDashboardResponse
import com.wafflestudio.spring2026.seminar.service.SeminarService
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.wafflestudio.spring2026.user.model.User
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestParam

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
    @GetMapping
    fun getSeminars(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<SeminarPageResponse>{
        val response = seminarService.getSeminars(keyword, status, page, size)
        return ResponseEntity.ok(response)
    }
    @GetMapping("/{seminarId}")
    fun getSeminar(@PathVariable seminarId: Long,
                        @AuthenticationPrincipal principal: User,
                   ): ResponseEntity<SeminarDetailResponse> {
            val response = seminarService.getSeminar(seminarId, principal)
            return ResponseEntity.ok(response)
    }

    @GetMapping("/{seminarId}/dashboard")
    fun getDashboard(//세미나의 회차와 수강생별 운영 현황
        @AuthenticationPrincipal principal: User,
        @PathVariable seminarId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<SeminarDashboardResponse>{
        require(page >= 0) { "page는 0 이상이어야 합니다." }
        require(size in 1..100) { "size는 1 이상 100 이하여야 합니다." }
        //권한 검사하고 response 받아오기
        val response: SeminarDashboardResponse =
            seminarService.getDashboard(principal, seminarId, page, size)
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