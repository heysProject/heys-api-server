package com.api.heys.domain.channel.dto

import com.api.heys.domain.common.dto.BaseResponse
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "getChannelsResponse 요청 결과")
data class GetChannelsResponse(
        @field:ArraySchema(schema = Schema(implementation = ChannelListItemData::class))
        val data: List<ChannelListItemData>,

        @field:Schema(example = "success", type = "string")
        override var message: String = "success"
): BaseResponse
