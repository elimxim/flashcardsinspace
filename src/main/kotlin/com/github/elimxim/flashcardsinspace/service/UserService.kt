package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.UserDto
import com.github.elimxim.flashcardsinspace.web.dto.UserUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.ValidUserUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(UserService::class.java)

@Service
class UserService(
    private val userRepository: UserRepository,
    private val requestValidator: RequestValidator,
    private val languageService: LanguageService,
) {
    @Transactional
    fun findByEmail(username: String): UserDto {
        return userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }
            .toDto()
    }

    @Transactional
    fun update(user: User, request: UserUpdateRequest): UserDto {
        log.info("Updating user info")
        return update(user, requestValidator.validate(request)).toDto()
    }

    @Transactional
    fun update(user: User, request: ValidUserUpdateRequest): User {
        return if (mergeUser(user, request)) {
            userRepository.save(user)
        } else user
    }

    fun mergeUser(user: User, request: ValidUserUpdateRequest): Boolean {
        var changed = false
        if (request.email != user.email) {
            user.email = request.email
            changed = true
        }
        if (request.name != user.name) {
            user.name = request.name
            changed = true
        }
        if (request.languageId != user.language.id) {
            user.language = languageService.getEntity(request.languageId)
            changed = true
        }

        if (changed) {
            user.lastUpdatedAt = ZonedDateTime.now()
        }

        return changed
    }
}
