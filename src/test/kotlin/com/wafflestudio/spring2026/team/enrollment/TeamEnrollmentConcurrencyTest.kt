package com.wafflestudio.spring2026.team.enrollment

import com.wafflestudio.spring2026.support.ApiIntegrationTest
import com.wafflestudio.spring2026.support.runConcurrently
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.Test
import kotlin.test.assertEquals

class TeamEnrollmentConcurrencyTest : ApiIntegrationTest() {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `정원 30명에 100명이 동시에 신청하면 30명만 저장된다`() {
        // 준비: 신청 기간 안인 세미나와 승인된 사용자 100명 생성
        val seminarId = createSeminar(capacity = 30)
        val rookies = List(100) { approvedRookie() }

        assertEquals(0L, enrollmentCount(seminarId))

        // 실행: 신청 요청만 동시에 실행
        val results = runConcurrently(rookies, parallelism = 32) { rookie ->
            rookie.id to enroll(seminarId, rookie.token).andReturn().response
        }

        // 검증 1: 성공 30건, 정원 마감 70건
        // 500 등 다른 상태 코드가 섞이면 이 검증이 실패한다.
        assertEquals(
            mapOf(201 to 30, 409 to 70),
            results.groupingBy { it.second.status }.eachCount(),
        )

        // 검증 2: 실제 DB에 저장된 신청도 정확히 30개
        assertEquals(30L, enrollmentCount(seminarId))

        // 검증 3: 성공 응답을 받은 사용자와 DB에 저장된 사용자가 일치
        val successfulUserIds = results
            .filter { it.second.status == 201 }
            .map { it.first }
            .toSet()

        val storedUserIds = jdbcTemplate.queryForList(
            """
            SELECT user_id
            FROM enrollments
            WHERE seminar_id = ?
            """.trimIndent(),
            Long::class.javaObjectType,
            seminarId,
        )

        assertEquals(30, storedUserIds.size)
        assertEquals(30, storedUserIds.toSet().size)
        assertEquals(successfulUserIds, storedUserIds.toSet())
    }

    @Test
    fun `한 사용자가 20회 동시에 신청하면 신청은 하나만 저장된다`() {
        // 정원을 20으로 설정하여 정원 마감과 중복 충돌을 구분한다.
        val seminarId = createSeminar(capacity = 20)
        val rookie = approvedRookie()
        val otherRookie = approvedRookie()

        assertEquals(0L, enrollmentCount(seminarId))

        // 동일한 사용자의 토큰으로 20회 동시 신청
        val responses = runConcurrently(
            inputs = List(20) { rookie },
            parallelism = 20,
        ) { actor ->
            enroll(seminarId, actor.token).andReturn().response
        }

        // 검증 1: 한 번만 성공하고 나머지는 중복 충돌
        assertEquals(
            mapOf(201 to 1, 409 to 19),
            responses.groupingBy { it.status }.eachCount(),
        )

        // 검증 2: 해당 사용자의 신청과 세미나 전체 신청 모두 1개
        val userEnrollmentCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM enrollments
            WHERE seminar_id = ? AND user_id = ?
            """.trimIndent(),
            Long::class.javaObjectType,
            seminarId,
            rookie.id,
        )

        assertEquals(1L, userEnrollmentCount)
        assertEquals(1L, enrollmentCount(seminarId))

        // 검증 3: 성공 응답의 신청 ID가 실제 저장된 신청 ID와 일치
        val successResponse = responses.single { it.status == 201 }
        val responseEnrollmentId = objectMapper
            .readTree(successResponse.contentAsString)
            .path("id")
            .asLong()

        val storedEnrollmentId = jdbcTemplate.queryForObject(
            """
            SELECT id
            FROM enrollments
            WHERE seminar_id = ? AND user_id = ?
            """.trimIndent(),
            Long::class.javaObjectType,
            seminarId,
            rookie.id,
        )

        assertEquals(responseEnrollmentId, storedEnrollmentId)

        // 검증 4: 중복 실패 후에도 다른 사용자가 남은 자리에 신청 가능
        enroll(seminarId, otherRookie.token)
            .andExpect { status { isCreated() } }

        assertEquals(2L, enrollmentCount(seminarId))
    }

    private fun enrollmentCount(seminarId: Long): Long =
        requireNotNull(
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM enrollments WHERE seminar_id = ?",
                Long::class.javaObjectType,
                seminarId,
            ),
        )

    @Test
    fun `한 사용자가 10회 동시에 취소하면 한 번만 취소된다`() {
        val seminarId = createSeminar(capacity = 20)
        val rookie1 = approvedRookie()
        val rookie2 = approvedRookie()

        enroll(seminarId, rookie1.token).andExpect {status {isCreated()}}
        enroll(seminarId, rookie2.token).andExpect {status {isCreated()}}

        assertEquals(2L, enrollmentCount(seminarId))

        val responses = runConcurrently(inputs = List(10) {rookie1}, parallelism = 10,
        ) {actor -> cancelEnrollment(seminarId, actor.token).andReturn().response.status}

        assertEquals(mapOf(204 to 1, 404 to 9), responses.groupingBy { it }.eachCount(),)

        val rookieEnrollmentCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM enrollments
            WHERE seminar_id = ? AND user_id = ?
            """.trimIndent(),
            Long::class.javaObjectType,
            seminarId,
            rookie1.id,
        )

        assertEquals(0L, rookieEnrollmentCount)
        assertEquals(1L, enrollmentCount(seminarId))
    }
}
