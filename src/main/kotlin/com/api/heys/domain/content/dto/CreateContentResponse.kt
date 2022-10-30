package com.api.heys.domain.content.dto

import com.api.heys.domain.common.dto.BaseResponse
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "createContent 요청 결과")
data class CreateContentResponse(
        @field:Schema(example = "success", type = "string")
        override val message: String = "success"
): BaseResponse
