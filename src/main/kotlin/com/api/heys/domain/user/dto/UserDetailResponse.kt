package com.api.heys.domain.user.dto

import com.api.heys.constants.enums.Gender
import io.swagger.v3.oas.annotations.media.Schema

data class UserDetailResponse(
    @field:Schema(example = "Joenna", type = "string")
    val userName: String? = null,

    @field:Schema(example = "01012341234", type = "string")
    val phone: String? = null,

    @field:Schema(example = "NonBinary", type = "string")
    val gender: Gender? = null,

    @field:Schema(example = "29", type = "int")
    val age: Int? = 10,

    @field:Schema(example = "개발자", type = "string")
    val job: String? = null,

    @field:Schema(example = "https://res.cloudinary.com/dyfuiigbw/image/upload/v1670047057/heys-dev/test1_jnkego.jpg", type = "string")
    val profileUrl: String? = null,

    @field:Schema(example = "안녕하세요!", type = "string")
    val introduce: String? = null,

    @field:Schema(example = "java, Spring", type = "string")
    val capability: String? = null,

    @field:Schema(example = "[\"스터디\", \"자기개발\"]", type = "array<string>")
    val interests: Set<String?>? = mutableSetOf(),

    @field:Schema(example = "2", type = "int")
    val joinChannelCount: Long? = 0,

    @field:Schema(example = "1", type = "int")
    val waitingChannelCount: Long? = 0,
)