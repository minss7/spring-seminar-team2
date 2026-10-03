package com.wafflestudio.spring2026.user.service

import com.wafflestudio.spring2026.auth.dto.SignupRequest
import com.wafflestudio.spring2026.auth.dto.SignupResponse
import com.wafflestudio.spring2026.auth.exception.EmailAlreadyExistsException
import com.wafflestudio.spring2026.seminar.exception.SeminarNotFoundException
import com.wafflestudio.spring2026.seminar.repository.SeminarRepository
import com.wafflestudio.spring2026.user.exception.UserNotFoundException
import com.wafflestudio.spring2026.user.model.User
import com.wafflestudio.spring2026.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class UserService(
    private val userRepository: UserRepository,
    private val seminarRepository: SeminarRepository,
) {
    fun signup( //POST /auth/signup
        request: SignupRequest,
    ): SignupResponse {
        val requestingUser = User(
            email = request.email,
            password = request.password,
            name = request.name,
            githubUsername = request.githubUsername,
            role = request.role,
            status = "PENDING",
            assignedSeminarId = request.seminarId, //null일때 있음
            createdAt = LocalDateTime.now(),
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
}