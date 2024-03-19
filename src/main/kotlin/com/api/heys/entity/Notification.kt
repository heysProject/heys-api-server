package com.api.heys.entity

import com.api.heys.constants.enums.MessageType
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*
import javax.persistence.FetchType.LAZY

/**
 * 알림 메시지 테이블
 */

@Entity
@Table(name = "notification")
class Notification(

        @Column(name = "message_type", length = 30)
        val messageType: MessageType,

        @ManyToOne(fetch = LAZY)
        @JoinColumn(name = "send_user_id")
        val sender : Users? = null,

        @ManyToOne(fetch = LAZY)
        @JoinColumn(name = "receiver_user_id", nullable = false)
        val receiver: Users,

        @OneToOne
        @JoinColumn(name = "channel_id", nullable = true)
        val channels : Channels? = null,

        @Column(name = "read_at")
        var readAt : LocalDateTime? = null

): BaseIdentityDate(), Serializable