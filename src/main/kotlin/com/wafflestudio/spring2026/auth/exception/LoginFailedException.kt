package com.wafflestudio.spring2026.auth.exception

class LoginFailedException :
    RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.")