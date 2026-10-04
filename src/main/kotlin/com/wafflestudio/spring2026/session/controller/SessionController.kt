package com.wafflestudio.spring2026.session.controller

import com.wafflestudio.spring2026.session.dto.SessionCreateRequest
import com.wafflestudio.spring2026.session.dto.SessionCreateResponse
import com.wafflestudio.spring2026.session.dto.SessionDetailResponse
import com.wafflestudio.spring2026.session.dto.SessionListResponse
import com.wafflestudio.spring2026.session.service.SessionService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI


@RestController

class SessionController (
    private val sessionService: SessionService,
){
    @PostMapping("/seminars/{seminarId}/sessions")
    fun createSession(
        @PathVariable seminarId : Long,
        @Valid @RequestBody request: SessionCreateRequest,
    ): ResponseEntity<SessionCreateResponse> {
        val response = sessionService.createSession(seminarId, request,)

        return ResponseEntity.created(URI.create("/sessions/${response.id}")).body(response)
    }


    @GetMapping("/seminars/{seminarId}/sessions")
    fun getSessions(
        @PathVariable seminarId: Long,
    ): ResponseEntity<List<SessionListResponse>> {
        val sessions = sessionService.getSessions(seminarId)
        return ResponseEntity.ok(sessions)
    }

    @GetMapping("/sessions/{sessionId}")
    fun getSession(
        @PathVariable sessionId: Long,
    ): ResponseEntity<SessionDetailResponse> {
        return ResponseEntity.ok(sessionService.getSession(sessionId))
    }

}