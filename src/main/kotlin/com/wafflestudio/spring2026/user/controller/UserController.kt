package com.wafflestudio.spring2026.user.controller

import com.wafflestudio.spring2026.enrollment.dto.MyEnrollmentListResponse
import com.wafflestudio.spring2026.enrollment.service.EnrollmentService
import com.wafflestudio.spring2026.user.dto.AuthRequestListResponse
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val enrollmentService: EnrollmentService,
) {
    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal principal: User
    ): ResponseEntity<GetUserResponse> {
        return ResponseEntity.ok(GetUserResponse.from(principal))
    }

    @GetMapping("/me/enrollments")
    fun getMyEnrollments(
        @AuthenticationPrincipal principal: User,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<MyEnrollmentListResponse>{
        return ResponseEntity.ok(
            enrollmentService.getMyEnrollments(principal, page, size)
        )
    }

    @GetMapping
    fun getAuthRequestPage( //가입 신청 목록 조회(와장, 운영진이 사용)
        @AuthenticationPrincipal principal: User,
        @RequestParam(name = "status", required = false) status: String?,
        @RequestParam(name = "role", required = false) role: String?,
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        @RequestParam(name = "size", defaultValue = "20") size: Int,
    ): ResponseEntity<AuthRequestListResponse> {
        //먼저 제대로된 요청인지 검증
        userService.checkIfGetAuthRequestListValid(principal, status, role, page, size)
        //response 받아오기
        val response: AuthRequestListResponse =
            userService.getAuthRequestPage(principal, status, role, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{userId}")
    fun getUserById( //가입이 승인된 사용자가 누구의 정보든 조회
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
