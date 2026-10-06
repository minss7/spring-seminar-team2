package com.wafflestudio.spring2026.enrollment.exception

class EnrollmentNotFoundException(enrollmentId: Long):
    RuntimeException("등록하지 않은 세미나입니다. (id: $enrollmentId)")
