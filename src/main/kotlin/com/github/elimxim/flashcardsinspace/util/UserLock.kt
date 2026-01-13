package com.github.elimxim.flashcardsinspace.util

import com.github.elimxim.flashcardsinspace.entity.User
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.absoluteValue

private const val BUCKETS = 1024

object UserLock {
    private val locks = Array(BUCKETS) { ReentrantLock() }

    fun <T> withLock(user: User?, action: () -> T): T {
        if (user == null) {
            return action()
        }

        val bucket = user.id.hashCode().absoluteValue % locks.size
        val lock = locks[bucket]

        lock.lock()
        try {
            return action()
        } finally {
            lock.unlock()
        }
    }
}
