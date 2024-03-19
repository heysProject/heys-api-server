package com.api.heys.domain.notification.controller

import com.api.heys.domain.common.dto.CommonApiResponse
import com.api.heys.domain.notification.service.NotificationService
import com.api.heys.domain.notification.vo.NotificationResponseVo
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/app/notifications")
class NotificationController(
    private val notificationService: NotificationService
){

    @GetMapping
    @Operation(summary = "나의 알림 리스트 조회", description = "나의 알림 리스트 조회 API")
    fun getNotification(@Schema(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) bearer: String)
        : ResponseEntity<CommonApiResponse<List<NotificationResponseVo>>>?{

        val notifications = notificationService.getNotifications(bearer)
        return ResponseEntity.ok(CommonApiResponse(data = notifications))
    }

    @GetMapping("/new")
    @Operation(summary = "새로운 알림 여부 조회", description = "새로운 알림 여부 조회 API")
    fun isNewNotification(@Schema(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) bearer: String)
            : ResponseEntity<CommonApiResponse<Boolean>> {
        val hasNewNotification = notificationService.hasNewNotificaiton(bearer)
        return ResponseEntity.ok(CommonApiResponse(data = hasNewNotification))
    }

    @PutMapping
    @Operation(summary = "새로운 알림 읽음 처리", description = "새로운 알림 읽음 처리 API")
    fun readNewNotifications(@Schema(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) bearer: String)
            : ResponseEntity<CommonApiResponse<Any>> {
        notificationService.readNewNotification(bearer)
        return ResponseEntity.ok(CommonApiResponse())
    }
}