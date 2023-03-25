package com.api.heys.domain.content.dto

import com.api.heys.constants.MessageString
import com.api.heys.domain.common.dto.BaseResponse
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "getContents 요청 결과")
data class GetExtraContentsResponse(
        @field:ArraySchema(schema = Schema(implementation = ExtraContentListItemData::class))
        val data: List<ExtraContentListItemData>,

        @field:Schema(example = MessageString.SUCCESS_EN, type = "string")
        override var message: String = MessageString.SUCCESS_EN
): BaseResponse