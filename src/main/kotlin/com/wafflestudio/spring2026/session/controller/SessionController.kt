package com.wafflestudio.spring2026.session.controller

import com.wafflestudio.spring2026.session.dto.SessionCreateRequest
import com.wafflestudio.spring2026.session.dto.SessionCreateResponse
import com.wafflestudio.spring2026.session.dto.SessionDetailResponse
import com.wafflestudio.spring2026.session.dto.SessionListResponse
import com.wafflestudio.spring2026.session.service.SessionService
import com.wafflestudio.spring2026.user.model.User
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import com.wafflestudio.spring2026.session.dto.SessionUpdateRequest

@RestController

class SessionController (
    private val sessionService: SessionService,
){
    @PostMapping("/seminars/{seminarId}/sessions")
    fun createSession(
        @PathVariable seminarId : Long,
        @Valid @RequestBody request: SessionCreateRequest,
        @AuthenticationPrincipal principal: User,
    ): ResponseEntity<SessionCreateResponse> {
        val response = sessionService.createSession(seminarId, request, principal)

        return ResponseEntity.created(URI.create("/sessions/${response.id}")).body(response)
    }


    @GetMapping("/seminars/{seminarId}/sessions")
    fun getSessions(
        @PathVariable seminarId: Long,
        @AuthenticationPrincipal principal: User,
    ): ResponseEntity<List<SessionListResponse>> {
        val sessions = sessionService.getSessions(seminarId, principal)
        return ResponseEntity.ok(sessions)
    }

    @GetMapping("/sessions/{sessionId}")
    fun getSession(
        @PathVariable sessionId: Long,
        @AuthenticationPrincipal principal: User,
    ): ResponseEntity<SessionDetailResponse> {
        return ResponseEntity.ok(sessionService.getSession(sessionId, principal))
    }

    @PatchMapping("/sessions/{sessionId}")
    fun updateSession(
        @PathVariable sessionId: Long,
        @RequestBody body: Map<String, Any?>,
        @AuthenticationPrincipal principal: User,
    ): ResponseEntity<SessionDetailResponse> {
        val request = SessionUpdateRequest.fromMap(body)
        return ResponseEntity.ok(
            sessionService.updateSession(sessionId, request, principal)
        )
    }

}