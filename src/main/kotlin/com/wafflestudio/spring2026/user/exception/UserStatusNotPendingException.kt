package com.wafflestudio.spring2026.user.exception

class UserStatusNotPendingException(val status: String) :
    RuntimeException("해당 사용자는 이미 ${status} 상태입니다.")