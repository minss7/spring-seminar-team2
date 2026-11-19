package com.wafflestudio.spring2026.session.dto

data class SessionRecordsPatchRequest(
    val records: List<RecordPatch>,
) {
    data class RecordPatch(
        val enrollmentId: Long,

        val attendancePresent: Boolean,
        val attendance: String?,

        val assignmentStatusPresent: Boolean,
        val assignmentStatus: String?,
    )

    companion object {
        private val attendanceValues = setOf("PRESENT", "ABSENT")

        private val assignmentValues = setOf(
            "PASSED",
            "SUBMITTED_BUT_FAILED",
            "NOT_SUBMITTED",
        )

        fun fromMap(body: Map<String, Any?>): SessionRecordsPatchRequest {
            val rawRecords = body["records"] as? List<*>
                ?: throw IllegalArgumentException("records는 배열이어야 합니다.")

            require(rawRecords.isNotEmpty()) {
                "records에는 항목이 하나 이상 필요합니다."
            }

            val records = rawRecords.map { rawRecord ->
                val record = rawRecord as? Map<*, *>
                    ?: throw IllegalArgumentException("각 기록은 객체여야 합니다.")

                // JSON 정수는 크기에 따라 Int 또는 Long으로 역직렬화될 수 있다.
                val enrollmentId = when (val value = record["enrollmentId"]) {
                    is Int -> value.toLong()
                    is Long -> value
                    else -> throw IllegalArgumentException(
                        "enrollmentId는 양의 정수여야 합니다.",
                    )
                }

                require(enrollmentId > 0) {
                    "enrollmentId는 양의 정수여야 합니다."
                }

                val attendancePresent = record.containsKey("attendance")
                val assignmentStatusPresent = record.containsKey("assignmentStatus")

                require(attendancePresent || assignmentStatusPresent) {
                    "attendance 또는 assignmentStatus가 필요합니다."
                }

                val attendance = readStatus(
                    record = record,
                    key = "attendance",
                    allowedValues = attendanceValues,
                )

                val assignmentStatus = readStatus(
                    record = record,
                    key = "assignmentStatus",
                    allowedValues = assignmentValues,
                )

                RecordPatch(
                    enrollmentId = enrollmentId,
                    attendancePresent = attendancePresent,
                    attendance = attendance,
                    assignmentStatusPresent = assignmentStatusPresent,
                    assignmentStatus = assignmentStatus,
                )
            }

            require(records.map { it.enrollmentId }.distinct().size == records.size) {
                "같은 enrollmentId를 중복해서 보낼 수 없습니다."
            }

            return SessionRecordsPatchRequest(records)
        }

        private fun readStatus(
            record: Map<*, *>,
            key: String,
            allowedValues: Set<String>,
        ): String? {
            val value = record[key] ?: return null

            require(value is String && value in allowedValues) {
                "$key 값이 올바르지 않습니다."
            }

            return value
        }
    }
}