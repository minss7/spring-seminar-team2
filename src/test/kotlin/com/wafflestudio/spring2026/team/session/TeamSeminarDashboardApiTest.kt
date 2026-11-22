package com.wafflestudio.spring2026.team.session

import com.wafflestudio.spring2026.support.Week4ApiIntegrationTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TeamSeminarDashboardApiTest : Week4ApiIntegrationTest() {
    @Test
    fun `루키 한명이 여러 세미나를 수강해도 데이터가 섞이지 않는다`() {
        val admin = adminToken()
        //세미나 A, B생성
        val seminarAId = createSeminar(totalGraceDays = 2)
        val seminarBId = createSeminar(totalGraceDays = 5)
        // session하나씩 생성
        val sessionAId = createSession(seminarAId)
        val sessionBId = createSession(seminarBId)
        //루키 한명이 두 세미나에 모두 신청
        val rookie = approvedRookie()
        val enrollmentAId = enrollSuccessfully(seminarAId, rookie)
        val enrollmentBId = enrollSuccessfully(seminarBId, rookie)
        //B에만 다른 수강생을 추가해서 세미나 인원 다르게
        val extraStudentB = enrolledRookie(seminarBId)
        //루키에게 세미나별로 서로 다른 기록 저장
        //A:PRESENT,PASSED
        updateRecords(
            sessionAId,
            admin,
            recordPatch(
                enrollmentAId,
                "attendance" to "PRESENT",
                "assignmentStatus" to "PASSED",
            ),
        ).andExpect { status { isNoContent() } }
        //B:ABSENT,NOT_SUBMITTED
        updateRecords(
            sessionBId,
            admin,
            recordPatch(
                enrollmentBId,
                "attendance" to "ABSENT",
                "assignmentStatus" to "NOT_SUBMITTED",
            ),
        ).andExpect { status { isNoContent() } }
        // 각 세미나 대시보드 조회
        val dbA = dashboard(seminarAId, admin)
        val dbB = dashboard(seminarBId, admin)
        //대시보드 검거 시작
        // 전체 인원이 세미나별로 집계되는지 확인
        assertEquals(
            1L,
            dbA.path("totalElements").asLong()
        )
        assertEquals(
            2L,
            dbB.path("totalElements").asLong()
        )
        // 해당 세미나의 수강 신청만 나오는지 확인
        val enrollmentIdsA = dbA.path("content").toList().map { it.path("id").asLong() }
        val enrollmentIdsB = dbB.path("content").toList().map { it.path("id").asLong() }
        assertEquals(listOf(enrollmentAId), enrollmentIdsA)
        assertEquals(listOf(enrollmentBId, extraStudentB.enrollmentId).sorted(), enrollmentIdsB,)
        // 해당 세미나의 회차만 나오는지 확인
        val sessionIdsA = dbA.path("sessions").toList().map { it.path("id").asLong() }
        val sessionIdsB = dbB.path("sessions").toList().map { it.path("id").asLong() }
        assertEquals(listOf(sessionAId), sessionIdsA)
        assertEquals(listOf(sessionBId), sessionIdsB)
        // 같은 루키의 수강 정보를 각각 확인
        val rowA = dashboardRow(dbA, enrollmentAId)
        val rowB = dashboardRow(dbB, enrollmentBId)
        assertEquals(rookie.id, rowA.path("user").path("id").asLong())
        assertEquals(rookie.id, rowB.path("user").path("id").asLong())
        assertEquals(2, rowA.path("graceDaysRemaining").asInt())
        assertEquals(5, rowB.path("graceDaysRemaining").asInt())
        // 세미나별로 저장했던 기록이 올바르게 나오는지 확인
        assertRecord(dbA, enrollmentAId, sessionAId, "PRESENT", "PASSED")
        assertRecord(dbB, enrollmentBId, sessionBId, "ABSENT", "NOT_SUBMITTED")
        // B의 추가 수강생에게는 기록을 저장하지 않았으므로 둘 다 null
        assertRecord(dbB, extraStudentB.enrollmentId, sessionBId, null, null)
    }

    @Test
    fun `조회자가 달라도 같은 대시보드 데이터를 반환한다`() {
        val admin = adminToken()
        val seminarId = createSeminar()
        val sessionId = createSession(seminarId)
        // 이 세미나를 담당하는 승인된 운영진
        val staff = approvedStaff(seminarId)
        // 이 세미나를 수강하는 루키 두 명
        val firstStudent = enrolledRookie(seminarId)
        val secondStudent = enrolledRookie(seminarId)
        // 두 수강생에게 서로 다른 기록을 저장
        updateRecords(
            sessionId,
            admin,
            recordPatch(
                firstStudent.enrollmentId,
                "attendance" to "PRESENT",
                "assignmentStatus" to "PASSED",
            ),
            recordPatch(
                secondStudent.enrollmentId,
                "attendance" to "ABSENT",
            ),
        ).andExpect { status { isNoContent() } }
        // 같은 대시보드를 서로 다른 권한의 사용자로 조회
        val adminDb = dashboard(seminarId, admin)
        val staffDb = dashboard(seminarId, staff.token)
        val rookieDb = dashboard(seminarId, firstStudent.actor.token)
        // 조회자가 달라도 응답 JSON 전체가 같아야 함
        assertEquals(adminDb, staffDb)
        assertEquals(adminDb, rookieDb)
        // 루키도 자신뿐 아니라 다른 수강생의 행까지 조회할 수 있어야 함
        assertEquals(2, rookieDb.path("content").size())
        val firstRow = dashboardRow(rookieDb, firstStudent.enrollmentId)
        val secondRow = dashboardRow(rookieDb, secondStudent.enrollmentId)
        // 각 행의 사용자 정보가 실제 수강생과 일치하는지 확인
        assertEquals(firstStudent.actor.id, firstRow.path("user").path("id").asLong())
        assertEquals(secondStudent.actor.id, secondRow.path("user").path("id").asLong())
        // 준비 단계에서 저장했던 기록도 확인
        assertRecord(
            rookieDb,
            firstStudent.enrollmentId,
            sessionId,
            "PRESENT",
            "PASSED",
        )
        assertRecord(
            rookieDb,
            secondStudent.enrollmentId,
            sessionId,
            "ABSENT",
            null,
        )
    }

    @Test
    fun `새 회차가 추가되면 기존 기록을 유지하고 미기록 칸을 추가한다`() {
        val admin = adminToken()
        val seminarId = createSeminar()
        // 기존 회차 하나 생성
        val firstStartsAt = now().plusDays(1)
        val firstSessionId = createSession(
            seminarId,
            startsAt = firstStartsAt,
            title = "기존 회차",
        )
        // 수강생 두 명 준비
        val firstStudent = enrolledRookie(seminarId)
        val secondStudent = enrolledRookie(seminarId)
        // 기존 회차에 서로 다른 기록 저장
        updateRecords(
            firstSessionId,
            admin,
            recordPatch(
                firstStudent.enrollmentId,
                "attendance" to "PRESENT",
                "assignmentStatus" to "PASSED",
            ),
            recordPatch(
                secondStudent.enrollmentId,
                "attendance" to "ABSENT",
                "assignmentStatus" to "NOT_SUBMITTED",
            ),
        ).andExpect { status { isNoContent() } }
        // 새 회차를 추가하기 전의 응답 저장
        val before = dashboard(seminarId, admin)
        // 기존 기록이 준비한 값과 일치하는지 확인
        assertRecord(
            before, firstStudent.enrollmentId, firstSessionId,
            "PRESENT", "PASSED",
        )
        assertRecord(
            before, secondStudent.enrollmentId, firstSessionId,
            "ABSENT", "NOT_SUBMITTED",
        )
        // 기존 회차보다 나중에 시작하는 새 회차 추가
        val newSessionId = createSession(
            seminarId,
            startsAt = firstStartsAt.plusDays(1),
            title = "추가 회차",
        )
        // 새 회차를 추가한 뒤 다시 조회
        val after = dashboard(seminarId, admin)
        // 기존 회차와 새 회차가 모두 나오는지 확인
        val sessionIds = after.path("sessions").toList().map { it.path("id").asLong() }
        assertEquals(listOf(firstSessionId, newSessionId), sessionIds)
        // 수강생 수와 수강 신청 목록은 그대로여야 함
        assertEquals(
            before.path("totalElements").asLong(),
            after.path("totalElements").asLong(),
        )
        val beforeIds = before.path("content").toList().map { it.path("id").asLong() }
        val afterIds = after.path("content").toList().map { it.path("id").asLong() }
        assertEquals(beforeIds, afterIds)
        // 두 수강생에게 같은 검사를 각각 수행
        for (student in listOf(firstStudent, secondStudent)) {
            val beforeRow = dashboardRow(before, student.enrollmentId)
            val afterRow = dashboardRow(after, student.enrollmentId)
            // 기존 회차의 기록은 변경 전후가 같아야 함
            val beforeRecord = dashboardRecord(beforeRow, firstSessionId)
            val afterRecord = dashboardRecord(afterRow, firstSessionId)
            assertEquals(beforeRecord, afterRecord)
            // 각 수강생의 기록에 회차 두 개가 있어야 함
            assertEquals(2, afterRow.path("records").size())
            // 새 회차의 출석과 과제는 모두 미기록이어야 함
            assertRecord(
                after,
                student.enrollmentId,
                newSessionId,
                null,
                null,
            )
        }
    }
}