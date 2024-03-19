package com.api.heys.domain.notification.repository

import com.api.heys.entity.Notification
import com.api.heys.entity.Users
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Long>, NotificationCustomRepository {

    fun findAllByReceiver(receiver : Users) : List<Notification>
}