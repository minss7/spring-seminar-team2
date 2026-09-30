package com.wafflestudio.spring2026.seminar.exception

class SeminarNotFoundException(seminarId: Long) :
    RuntimeException("존재하지 않는 세미나입니다. (id: $seminarId)")