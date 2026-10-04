package com.wafflestudio.spring2026.session.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class SessionNotFoundException(sessionId: Long) :
    RuntimeException("회차를 찾을 수 없습니다. id=$sessionId")