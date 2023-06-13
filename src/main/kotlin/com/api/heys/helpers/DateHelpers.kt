package com.api.heys.helpers

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component
object DateHelpers {
    fun diffDay(from: LocalDateTime?, to: LocalDateTime?): Long {
        if (from == null || to == null) return Long.MIN_VALUE
        val duration = Duration.between(from, to)
        return duration.toDays()
    }

    fun calculateDday(diff: LocalDateTime?): Long {
        if (diff == null) return Long.MIN_VALUE
        return this.diffDay(LocalDateTime.now(), diff)
    }

    fun calcTotalPage(count: Long, limit: Long): Long {
        val plusOne = count % limit > 0
        return count / limit + (if (plusOne) 1 else 0)
    }
}