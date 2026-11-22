package com.wafflestudio.spring2026.team.session

import com.wafflestudio.spring2026.session.dto.SessionRecordsPatchRequest
import com.wafflestudio.spring2026.session.repository.SubmissionRepository
import com.wafflestudio.spring2026.session.service.SessionRecordService
import com.wafflestudio.spring2026.support.Week4ApiIntegrationTest
import com.wafflestudio.spring2026.user.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SessionRecordRollbackTest : Week4ApiIntegrationTest() {

    @Autowired
    private lateinit var sessionRecordService: SessionRecordService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    // 실제 Spring Data JDBC 저장소를 감싼 spy입니다.
    // 지정한 메서드를 제외한 나머지 동작은 실제 저장소가 수행합니다.
    @MockitoSpyBean
    private lateinit var submissionRepository: SubmissionRepository

    private class InjectedFailure : RuntimeException("테스트에서 주입한 실패")

    private data class StoredRecord(
        val enrollmentId: Long,
        val attendance: String?,
        val submission: String?,
    )

    @Test
    fun `실제 첫 기록 저장 이후 예외가 발생하면 일괄 수정 전체가 롤백된다`() {
        // 테스트 메서드에는 @Transactional을 붙이지 않습니다.
        // API 준비 요청은 각각 완료되며, 이후 서비스 호출과 별도 트랜잭션입니다.
        val adminToken = adminToken()
        val seminarId = createSeminar()
        val sessionId = createSession(seminarId)
        val first = enrolledRookie(seminarId)
        val second = enrolledRookie(seminarId)

        updateRecords(
            sessionId,
            adminToken,
            recordPatch(
                first.enrollmentId,
                "attendance" to "PRESENT",
                "assignmentStatus" to "PASSED",
            ),
            recordPatch(
                second.enrollmentId,
                "attendance" to "ABSENT",
                "assignmentStatus" to "NOT_SUBMITTED",
            ),
        ).andExpect { status { isNoContent() } }

        val admin = assertNotNull(userRepository.findByEmail(ADMIN_EMAIL))

        // 별도 트랜잭션에서 작업 전 상태를 읽습니다.
        val before = readRecords(sessionId)

        assertEquals(
            listOf(
                StoredRecord(first.enrollmentId, "PRESENT", "PASSED"),
                StoredRecord(second.enrollmentId, "ABSENT", "NOT_SUBMITTED"),
            ),
            before,
        )

        var observedFirstWrite = false

        // 두 번째 항목의 과제 기록을 조회하는 시점에 실패를 주입합니다.
        // 첫 번째 항목의 출석 변경이 이미 실행된 다음입니다.
        Mockito.doAnswer {
            assertTrue(
                TransactionSynchronizationManager.isActualTransactionActive(),
                "실패 주입이 서비스의 실제 트랜잭션 안에서 실행되어야 합니다.",
            )

            // 같은 트랜잭션의 JDBC 연결로 첫 변경을 직접 조회합니다.
            // Spring Data JDBC의 save는 이 시점 전에 SQL을 실행합니다.
            val statusInTransaction = jdbcTemplate.queryForObject(
                """
                SELECT status
                FROM attendances
                WHERE session_id = ? AND enrollment_id = ?
                """.trimIndent(),
                String::class.java,
                sessionId,
                first.enrollmentId,
            )

            assertEquals("ABSENT", statusInTransaction)
            observedFirstWrite = true

            throw InjectedFailure()
        }.`when`(submissionRepository)
            .findBySessionIdAndEnrollmentId(sessionId, second.enrollmentId)

        val request = SessionRecordsPatchRequest.fromMap(
            mapOf(
                "records" to listOf(
                    recordPatch(first.enrollmentId, "attendance" to "ABSENT"),
                    recordPatch(second.enrollmentId, "assignmentStatus" to "PASSED"),
                ),
            ),
        )

        // 스프링이 주입한 서비스 프록시를 호출해야 @Transactional이 적용됩니다.
        assertFailsWith<InjectedFailure> {
            sessionRecordService.updateRecords(sessionId, request, admin)
        }

        assertTrue(observedFirstWrite, "예외 발생 전에 첫 DB 쓰기가 확인되어야 합니다.")

        // 서비스 호출이 종료된 후 새로운 트랜잭션에서 실제 DB 상태를 비교합니다.
        assertEquals(before, readRecords(sessionId))
    }

    private fun readRecords(sessionId: Long): List<StoredRecord> {
        val transaction = TransactionTemplate(transactionManager)

        return requireNotNull(
            transaction.execute {
                jdbcTemplate.query(
                    """
                    SELECT e.id AS enrollment_id,
                           a.status AS attendance,
                           s.status AS submission
                    FROM enrollments e
                    LEFT JOIN attendances a
                        ON a.enrollment_id = e.id AND a.session_id = ?
                    LEFT JOIN submissions s
                        ON s.enrollment_id = e.id AND s.session_id = ?
                    WHERE e.seminar_id = (
                        SELECT seminar_id FROM sessions WHERE id = ?
                    )
                    ORDER BY e.id
                    """.trimIndent(),
                    { row, _ ->
                        StoredRecord(
                            enrollmentId = row.getLong("enrollment_id"),
                            attendance = row.getString("attendance"),
                            submission = row.getString("submission"),
                        )
                    },
                    sessionId,
                    sessionId,
                    sessionId,
                )
            },
        )
    }
}