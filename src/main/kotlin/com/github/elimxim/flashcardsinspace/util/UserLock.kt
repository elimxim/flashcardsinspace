package com.github.elimxim.flashcardsinspace.util

import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs

private const val BUCKETS = 1024

object UserLock {
    private val locks = Array(BUCKETS) { ReentrantLock() }

    fun <T> withLock(key: Any, action: () -> T): T {
        val bucket = abs(key.hashCode() % locks.size)
        val lock = locks[bucket]

        lock.lock()
        try {
            return action()
        } finally {
            lock.unlock()
        }
    }
}
