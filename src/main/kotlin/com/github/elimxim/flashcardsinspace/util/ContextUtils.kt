package com.github.elimxim.flashcardsinspace.util

import com.github.elimxim.flashcardsinspace.entity.User
import org.springframework.security.core.context.SecurityContextHolder

fun getCurrentUser(): User? {
    return try {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.isAuthenticated && authentication.principal is User) {
            authentication.principal as User
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
