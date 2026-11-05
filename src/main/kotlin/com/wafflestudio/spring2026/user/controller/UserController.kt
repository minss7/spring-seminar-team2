package com.wafflestudio.spring2026.user.controller

import com.wafflestudio.spring2026.user.dto.GetUserResponse
import com.wafflestudio.spring2026.user.dto.UserApprovalRequest
import com.wafflestudio.spring2026.user.model.User
import com.wafflestudio.spring2026.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
        @AuthenticationPrincipal principal: User,
    ): ResponseEntity<GetUserResponse> {
        //user가 approve돼있는지 확인, 아니면 service에서 403
        userService.checkIfUserCanRequestUserId(principal)

        val response: User = userService.getUserById(userId)

        return ResponseEntity
            .ok(GetUserResponse.from(response))
    }

    @PatchMapping("/{userId}/approval")
    fun patchUserStatus(
        @PathVariable("userId") userId: Long,
        @Valid @RequestBody request: UserApprovalRequest,
        @AuthenticationPrincipal principal: User,
    ): ResponseEntity<GetUserResponse> {
        //요청자가 찐빠 안내는지 확인
        //운영진 가입신청 - 와장만 가능. 루키 가입신청 - 와장,운영진 가능. 찐빠=403
        userService.checkIfCanApprove(principal, userId)

        userService.patchUserStatus(userId, request.status)
        val response: User = userService.getUserById(userId)

        return ResponseEntity
            .ok(GetUserResponse.from(response))
    }
}