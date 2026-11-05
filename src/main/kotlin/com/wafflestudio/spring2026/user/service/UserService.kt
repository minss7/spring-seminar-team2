package com.wafflestudio.spring2026.user.service

import com.wafflestudio.spring2026.auth.dto.SignupRequest
import com.wafflestudio.spring2026.auth.dto.SignupResponse
import com.wafflestudio.spring2026.auth.exception.EmailAlreadyExistsException
import com.wafflestudio.spring2026.seminar.exception.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import com.wafflestudio.spring2026.user.exception.UserNotFoundException
import com.wafflestudio.spring2026.user.exception.UserStatusNotPendingException
import com.wafflestudio.spring2026.user.model.User
import com.wafflestudio.spring2026.user.repository.UserRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class UserService(
    private val userRepository: UserRepository,
    private val seminarRepository: SeminarRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun signup( //POST /auth/signup
        request: SignupRequest,
    ): SignupResponse {
        val requestingUser = User(
            email = request.email,
            password = requireNotNull(passwordEncoder.encode(request.password)),
            name = request.name,
            githubUsername = request.githubUsername,
            role = request.role,
            status = "PENDING",
            assignedSeminarId = request.seminarId, //null일때 있음
            createdAt = LocalDateTime.now(ZoneOffset.ofHours(9)),
        )

        //이메일 중복이면 409
        if(userRepository.findByEmail(requestingUser.email) != null){
            throw EmailAlreadyExistsException()
        }

        if(requestingUser.role == "ROOKIE"){
            //루키 seminarId 있으면 400
            require(requestingUser.assignedSeminarId == null)
        } else if(requestingUser.role == "STAFF"){
            //staff seminarId 없으면 400
            requireNotNull(requestingUser.assignedSeminarId)
            //seminarId 존재안하는거면 404 the new era era
            if(!seminarRepository.existsById(requestingUser.assignedSeminarId)){
                throw SeminarNotFoundException(requestingUser.assignedSeminarId)
            }
        }

        //신규유저 등록
        val savedUser = userRepository.save(requestingUser)

        return SignupResponse(
            id = requireNotNull(savedUser.id),
            status = savedUser.status,
            createdAt = savedUser.createdAt
                .atOffset(ZoneOffset.ofHours(9))
                .toString(),
        )
    }

    fun getUserById(userId: Long): User { //GET /users/{userId}
        return userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
    }

    fun patchUserStatus(userId: Long, newStatus: String) { //PATCH /users/{userId}/approval
        //없는 사용자면 404
        val user: User = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        //status가 PENDING아니면 409
        if(user.status != "PENDING"){
            throw UserStatusNotPendingException(user.status)
        }
        //status 수정
        userRepository.save(user.copy(status = newStatus))
    }

    fun checkIfUserCanRequestUserId(user: User){
        //GET /users/{userId}
        //가입이 승인된 사용자인지 확인(admin도 approved상태)
        //안 맞으면 403
        var canRequest = true
        if(user.role !in setOf("ADMIN", "STAFF", "ROOKIE")) canRequest = false
        if(user.status != "APPROVED") canRequest = false
        if(!canRequest){
            throw AccessDeniedException("사용자 조회 권한이 없습니다.");
        }
    }

    fun checkIfCanApprove(principal: User, userId: Long){
        //PATCH /users/{userId}/approval
        //principal이 userId를 심사할수 있는 사람인지 확인
        //운영진 가입신청 - 와장만 가능. 루키 가입신청 - 와장,운영진 가능
        //안맞으면 403
        fun throw403() {
            throw AccessDeniedException("심사 권한이 없습니다.")
        }

        if(principal.status != "APPROVED") throw403()
        val principalRole = principal.role
        val userRole = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() } //없는 userId면 404
            .role

        if (principalRole !in setOf("ADMIN", "STAFF")) throw403()
        if (userRole !in setOf("STAFF", "ROOKIE")) throw403()
        if (userRole == "STAFF") {
            if (principalRole != "ADMIN") throw403()
        }
    }
}