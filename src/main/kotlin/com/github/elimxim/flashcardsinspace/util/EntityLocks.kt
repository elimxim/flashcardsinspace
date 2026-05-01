package com.github.elimxim.flashcardsinspace.util

import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.User

private val userLock = BucketReentrantLock.lock(256)
private val flashcardSetLock = BucketReentrantLock.lock(1024)

fun <T> withLock(user: User?, action: () -> T): T {
    if (user == null) {
        return action()
    }

    val lock = userLock.get(user.id)

    lock.lock()
    try {
        return action()
    } finally {
        lock.unlock()
    }
}

fun <T> withLock(set: FlashcardSet, action: () -> T): T {
    val lock = flashcardSetLock.get(set.id)

    lock.lock()
    try {
        return action()
    } finally {
        lock.unlock()
    }
}
