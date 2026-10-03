package com.wafflestudio.spring2026.auth.exception

class EmailAlreadyExistsException :
    RuntimeException("해당 이메일은 이미 가입되어 있는 이메일입니다.")