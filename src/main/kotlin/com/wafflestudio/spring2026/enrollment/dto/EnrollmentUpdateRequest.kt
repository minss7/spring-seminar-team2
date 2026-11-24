package com.wafflestudio.spring2026.enrollment.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import tools.jackson.databind.JsonNode

//유효성 검증 구현을 위한, 정수값 여부 측정을 위해 Json 사용
@JsonIgnoreProperties(ignoreUnknown = true)
class EnrollmentUpdateRequest {
    var graceDaysRemaining: JsonNode? = null
        private set
    var hasGraceDaysRemaining = false
        private set

    var dropped: JsonNode? = null
        private set
    var hasDropped = false
        private set

    @JsonSetter(value = "graceDaysRemaining", nulls = Nulls.SET)
    fun receiveGraceDaysRemaining(value: JsonNode?) {
        hasGraceDaysRemaining = true
        graceDaysRemaining = value
    }

    @JsonSetter(value = "dropped", nulls = Nulls.SET)
    fun receiveDropped(value: JsonNode?) {
        hasDropped = true
        dropped = value
    }
}