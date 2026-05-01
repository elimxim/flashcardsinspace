package com.github.elimxim.flashcardsinspace.util

import java.util.concurrent.locks.ReentrantLock
import kotlin.math.absoluteValue

class BucketReentrantLock private constructor(size: Int) {
    private val buckets = Array(size) { ReentrantLock() }

    fun get(value: Long): ReentrantLock {
        val bucket = value.hashCode().absoluteValue % buckets.size
        return buckets[bucket]
    }

    companion object {
        fun lock(size: Int): BucketReentrantLock {
            if (size <= 0) {
                throw IllegalArgumentException("Size must be greater than 0")
            }

            return BucketReentrantLock(size)
        }
    }
}
