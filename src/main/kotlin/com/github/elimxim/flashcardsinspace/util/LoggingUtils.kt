package com.github.elimxim.flashcardsinspace.util

import com.github.elimxim.flashcardsinspace.entity.User
import org.slf4j.MDC

const val USER_ID_MDC_KEY: String = "userId"

inline fun <T> withLoggingContext(user: User?, block: () -> T): T =
    withLoggingContext(user?.id, block)

inline fun <T> withLoggingContext(userId: Long?, block: () -> T): T {
    val previous = MDC.get(USER_ID_MDC_KEY)
    try {
        if (userId != null) {
            MDC.put(USER_ID_MDC_KEY, userId.toString())
        } else {
            MDC.remove(USER_ID_MDC_KEY)
        }
        return block()
    } finally {
        if (previous != null) {
            MDC.put(USER_ID_MDC_KEY, previous)
        }
    }
}
