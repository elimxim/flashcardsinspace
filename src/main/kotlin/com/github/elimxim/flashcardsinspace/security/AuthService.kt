package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import com.github.elimxim.flashcardsinspace.service.LanguageService
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.LoginRequest
import com.github.elimxim.flashcardsinspace.web.dto.SignUpRequest
import com.github.elimxim.flashcardsinspace.web.dto.ValidLoginRequest
import com.github.elimxim.flashcardsinspace.web.dto.ValidSignUpRequest
import com.github.elimxim.flashcardsinspace.web.exception.AuthenticationFailedException
import com.github.elimxim.flashcardsinspace.web.exception.EmailIsAlreadyTakenException
import com.github.elimxim.flashcardsinspace.web.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(AuthService::class.java)

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val languageService: LanguageService,
    private val requestValidator: RequestValidator,
) {
    @Transactional
    fun signUp(request: SignUpRequest): User {
        log.info("Sign up attempt ${maskSecret(request.email?.escapeJava())}")
        return signUp(requestValidator.validate(request))
    }

    @Transactional
    fun signUp(request: ValidSignUpRequest): User {
        if (userRepository.findByEmail(request.email).isPresent) {
            log.info("Email ${maskSecret(request.email)} is already taken")
            throw EmailIsAlreadyTakenException(
                "Email ${maskSecret(request.email)} is already exists"
            )
        }

        val language = languageService.getEntity(request.languageId)

        val user = User(
            email = request.email,
            name = request.name,
            secret = passwordEncoder.encode(request.secret.unmasked()),
            language = language,
            roles = "ASTRONAUT",
            registeredAt = ZonedDateTime.now(),
            timezone = request.timezone,
        )

        val savedUser = userRepository.save(user)
        log.info("Signup successful ${user.email} => ${user.id}, timezone: ${user.timezone}")

        return savedUser
    }

    @Transactional
    fun login(request: LoginRequest): User {
        log.info("Login attempt ${maskSecret(request.email?.escapeJava())}")
        return login(requestValidator.validate(request))
    }

    @Transactional
    fun login(request: ValidLoginRequest): User {
        try {
            val token = UsernamePasswordAuthenticationToken(request.email, request.secret.unmasked())
            authenticationManager.authenticate(token)
        } catch (e: Exception) {
            throw AuthenticationFailedException(
                "Failed to authenticate user: ${maskSecret(request.email.escapeJava())}", e
            )
        }

        val user = userRepository.findByEmail(request.email).orElseThrow {
            UserNotFoundException("User not found: ${maskSecret(request.email.escapeJava())}")
        }

        user.lastLoginAt = ZonedDateTime.now()
        if (user.timezone != request.timezone) {
            user.timezone = request.timezone
            user.lastUpdatedAt = ZonedDateTime.now()
            log.info("Updated timezone for user ${user.id}: ${request.timezone}")
        }
        userRepository.save(user)

        log.info("Login successful ${maskSecret(user.email)} => ${user.id}, timezone: ${user.timezone}")
        return user
    }

    fun logout() {
        // todo log
    }

    @Transactional
    fun refreshToken(refreshToken: String?): User? {
        if (refreshToken == null) {
            log.error("Refresh token is null, can't refresh")
            return null
        }

        val email = jwtService.extractUsername(refreshToken)
        val user = userRepository.findByEmail(email).orElseThrow {
            UsernameNotFoundException("User not found ${maskSecret(email.escapeJava())}")
        }

        if (!jwtService.isTokenValid(refreshToken, user)) {
            log.error("Invalid refresh token for user {}", email)
            throw IllegalArgumentException("Invalid refresh token")
        }

        return user
    }
}
