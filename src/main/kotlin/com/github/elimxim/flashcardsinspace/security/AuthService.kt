package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.ConfirmationPurpose
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import com.github.elimxim.flashcardsinspace.service.LanguageService
import com.github.elimxim.flashcardsinspace.service.mail.EmailService
import com.github.elimxim.flashcardsinspace.service.mail.Recipient
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.LoginRequest
import com.github.elimxim.flashcardsinspace.web.dto.SignUpRequest
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpConflictException
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
import com.github.elimxim.flashcardsinspace.web.exception.HttpUnauthorizedException
import jakarta.servlet.http.HttpServletResponse
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
    private val emailService: EmailService,
    private val verificationCodeService: VerificationCodeService,
) {
    @Transactional
    fun signUp(request: SignUpRequest, response: HttpServletResponse): User {
        log.info("Sign up attempt ${maskSecret(request.email?.escapeJava())}")
        val (email, name, secret, languageId, timezone) = requestValidator.validate(request)
        if (userRepository.findByEmail(email).isPresent) {
            log.info("Email ${maskSecret(email)} is already taken")
            throw HttpConflictException(
                ApiErrorCode.EAT409,
                "Email ${maskSecret(email)} is already taken"
            )
        }

        val language = languageService.getEntity(languageId)

        val user = User(
            email = email,
            emailVerified = false,
            name = name,
            secret = passwordEncoder.encode(secret.unmasked()),
            language = language,
            roles = "ASTRONAUT",
            registeredAt = ZonedDateTime.now(),
            timezone = timezone,
        )

        val savedUser = userRepository.save(user)
        log.info("Signup successful ${user.email} => ${user.id}, timezone: ${user.timezone}")

        emailService.sendWelcomeEmail(recipient = Recipient(user.email, user.name))
        jwtService.setCookies(user, response)
        verificationCodeService.send(savedUser, savedUser.email, ConfirmationPurpose.EMAIL_VERIFICATION, response)
        return savedUser
    }

    @Transactional
    fun login(request: LoginRequest, response: HttpServletResponse): User {
        log.info("Login attempt ${maskSecret(request.email?.escapeJava())}")
        val (email, secret, timezone) = requestValidator.validate(request)
        try {
            val token = UsernamePasswordAuthenticationToken(email, secret.unmasked())
            authenticationManager.authenticate(token)
        } catch (e: Exception) {
            throw HttpUnauthorizedException(
                ApiErrorCode.AUF401, "Failed to authenticate user: ${maskSecret(email.escapeJava())}", e
            )
        }

        val user = userRepository.findByEmail(email).orElseThrow {
            HttpNotFoundException(ApiErrorCode.USR404, "User not found: ${maskSecret(email.escapeJava())}")
        }

        user.lastLoginAt = ZonedDateTime.now()
        if (user.timezone != timezone) {
            user.timezone = timezone
            user.lastUpdatedAt = ZonedDateTime.now()
            log.info("Updated timezone for user ${user.id}: ${timezone}")
        }
        userRepository.save(user)

        log.info("Login successful ${maskSecret(user.email)} => ${user.id}, timezone: ${user.timezone}")
        jwtService.setCookies(user, response)
        return user
    }

    fun logout(response: HttpServletResponse) {
        log.info("Logout attempt")
        jwtService.clearCookies(response)
    }

    @Transactional
    fun refreshToken(refreshToken: String?, response: HttpServletResponse): User? {
        if (refreshToken == null) {
            log.error("Refresh token is null, can't refresh")
            return null
        }

        val email = jwtService.extractUsername(refreshToken)
        val user = userRepository.findByEmail(email).orElseThrow {
            UsernameNotFoundException("User not found ${maskSecret(email.escapeJava())}")
        }

        if (!jwtService.isTokenValid(refreshToken, user)) {
            log.info("Invalid/Expired refresh token for user {}", email)
            return null
        }

        jwtService.setCookies(user, response)
        return user
    }
}
