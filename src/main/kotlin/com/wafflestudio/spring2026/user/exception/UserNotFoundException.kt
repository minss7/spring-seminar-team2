package com.wafflestudio.spring2026.user.exception

class UserNotFoundException :
    RuntimeException("해당 ID를 가진 사용자는 존재하지 않습니다.")