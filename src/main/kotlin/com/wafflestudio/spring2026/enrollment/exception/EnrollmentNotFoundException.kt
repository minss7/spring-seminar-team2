package com.wafflestudio.spring2026.enrollment.exception

class EnrollmentNotFoundException(userId: Long, seminarId: Long):
    RuntimeException("Enrollment for user $userId in seminar $seminarId was not found.")
