package com.wafflestudio.spring2026.user.controller

import com.wafflestudio.spring2026.user.dto.GetUserResponse
import com.wafflestudio.spring2026.user.dto.UserApprovalRequest
import com.wafflestudio.spring2026.user.model.User
import com.wafflestudio.spring2026.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/{userId}")
    fun getUserById(
        @PathVariable("userId") userId: Long,
    ): ResponseEntity<GetUserResponse> {
        val response: User = userService.getUserById(userId)

        return ResponseEntity
            .ok(GetUserResponse.from(response))
    }

    @PatchMapping("/{userId}/approval")
    fun patchUserStatus(
        @PathVariable("userId") userId: Long,
        @Valid @RequestBody request: UserApprovalRequest,
    ): ResponseEntity<GetUserResponse> {
        userService.patchUserStatus(userId, request.status)
        val response: User = userService.getUserById(userId)

        return ResponseEntity
            .ok(GetUserResponse.from(response))
    }
}