package com.api.heys.domain.common

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("common")
class CommonController {
    @GetMapping("/ping")
    fun ping(): String {
        return "pong!"
    }
}