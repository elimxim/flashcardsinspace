package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun findByEmail(username: String): User {
        return userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }
    }
}
