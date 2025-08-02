package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun findByEmail(username: String): User {
        return userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }
    }
}
