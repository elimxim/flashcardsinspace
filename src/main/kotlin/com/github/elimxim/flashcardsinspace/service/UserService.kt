package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import com.github.elimxim.flashcardsinspace.security.maskSecret
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.UserDto
import com.github.elimxim.flashcardsinspace.web.dto.UserUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.ValidUserUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
import org.slf4j.LoggerFactory
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
    @Transactional(readOnly = true)
    fun getEntity(email: String): User {
        return userRepository.findByEmail(email).orElseThrow {
            HttpNotFoundException(
                ApiErrorCode.USR404,
                "User not found: ${maskSecret(email)}"
            )
        }
    }

    @Transactional(readOnly = true)
    fun findByEmail(email: String): UserDto {
        return getEntity(email).toDto()
    }

    @Transactional
    fun update(user: User, request: UserUpdateRequest): UserDto {
        log.info("Updating user info")
        val validRequest = requestValidator.validate(request)
        val updatedUser = if (mergeUser(user, validRequest)) {
            userRepository.save(user)
        } else user
        return updatedUser.toDto()
    }

    private fun mergeUser(user: User, request: ValidUserUpdateRequest): Boolean {
        var changed = false
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

    @Transactional
    fun verify(user: User) {
        user.emailVerified = true
        user.lastUpdatedAt = ZonedDateTime.now()
        userRepository.save(user)
        log.info("User ${user.id} verified")
    }
}
