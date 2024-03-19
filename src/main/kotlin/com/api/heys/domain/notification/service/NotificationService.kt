package com.api.heys.domain.notification.service

import com.api.heys.domain.aws.push.service.AwsPushService
import com.api.heys.domain.aws.push.vo.TargetPushMessageVo
import com.api.heys.domain.devicetoken.service.DeviceTokenService
import com.api.heys.domain.notification.repository.NotificationRepository
import com.api.heys.domain.notification.vo.NotificationRequestVo
import com.api.heys.domain.notification.vo.NotificationResponseVo
import com.api.heys.domain.user.repository.UserRepository
import com.api.heys.entity.Notification
import com.api.heys.exception.CustomException
import com.api.heys.exception.type.UserExceptionType.*
import com.api.heys.helpers.DateHelpers
import com.api.heys.utils.JwtUtil
import com.api.heys.utils.MessageUtils
import com.api.heys.utils.UserUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class NotificationService(
    private val jwtUtil: JwtUtil,
    private val userUtil: UserUtil,
    private val userRepository: UserRepository,
    private val awsPushService: AwsPushService,
    private val deviceTokenService: DeviceTokenService,
    private val notificationRepository: NotificationRepository
){

    @Transactional
    fun saveNotification(notificationRequestVo: NotificationRequestVo) {

        val messageType = notificationRequestVo.messageType

        val notification = Notification(messageType = messageType,
            sender = notificationRequestVo.sender,
            receiver = notificationRequestVo.receiver,
            channels = notificationRequestVo.channel)
        notificationRepository.save(notification)

        val dispatcher = Dispatchers.Default

        val senderName = notificationRequestVo.sender?.detail?.username
        val channelName = notificationRequestVo.channel?.detail?.name
        val messageParam = MessageUtils.getMessageParam(user = senderName, channel = channelName)
        val content = MessageUtils.getContent(messageType, messageParam)

        val deviceTokens = deviceTokenService.getAvailableDeviceTokens(notificationRequestVo.receiver.id)
        val endPoints = deviceTokens.mapNotNull { it.arn }.toSet()

        runBlocking {
            withContext(dispatcher) {
                endPoints.forEach {
                    val targetPushMessageVo = TargetPushMessageVo(
                        title = messageType.title(),
                        content = content,
                        endPoint = it
                    )
                    awsPushService.sendTargetArnPush(targetPushMessageVo)
                }
            }
        }
    }

    fun getNotifications(token : String) : List<NotificationResponseVo> {
        val users = userUtil.findUserByToken(token, jwtUtil, userRepository)
        val notifications = notificationRepository.findAllByReceiver(users!!)

        return notifications.map {
            val senderName = it.sender?.detail?.username
            val channelName = it.channels?.detail?.name
            val messageParam = MessageUtils.getMessageParam(user = senderName, channel = channelName)
            val content = MessageUtils.getContent(it.messageType, messageParam)

            NotificationResponseVo(
                content = content,
                channelId = it.channels?.id,
                createdAt = DateHelpers.formatDate(it.createdAt),
                isRead = it.readAt != null
            )
        }.toList()
    }

    fun hasNewNotificaiton(token : String) : Boolean {
        val users = userUtil.findUserByToken(token, jwtUtil, userRepository) ?: throw CustomException(NOT_EXIST_USER)
        return notificationRepository.findNewNotification(users).isNotEmpty()
    }

    @Transactional
    fun readNewNotification(token : String) {
        val users = userUtil.findUserByToken(token, jwtUtil, userRepository) ?: throw CustomException(NOT_EXIST_USER)
        val newNotifications = notificationRepository.findNewNotification(users)
        for (newNotification in newNotifications) {
            newNotification.readAt = LocalDateTime.now()
        }
    }

}