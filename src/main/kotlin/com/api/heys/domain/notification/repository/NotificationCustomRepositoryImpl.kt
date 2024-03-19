package com.api.heys.domain.notification.repository

import com.api.heys.entity.Notification
import com.api.heys.entity.QNotification
import com.api.heys.entity.Users
import com.querydsl.jpa.impl.JPAQueryFactory

class NotificationCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : NotificationCustomRepository {

    private val qNotification: QNotification = QNotification.notification

    override fun findNewNotification(receiver: Users) : List<Notification> {
        return jpaQueryFactory.selectFrom(qNotification)
            .where(qNotification.readAt.isNull)
            .fetch()
    }
}