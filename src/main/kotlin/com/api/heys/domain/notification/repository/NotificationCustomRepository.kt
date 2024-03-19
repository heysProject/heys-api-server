package com.api.heys.domain.notification.repository

import com.api.heys.entity.Notification
import com.api.heys.entity.Users

interface NotificationCustomRepository {

    fun findNewNotification(receiver : Users) : List<Notification>
}