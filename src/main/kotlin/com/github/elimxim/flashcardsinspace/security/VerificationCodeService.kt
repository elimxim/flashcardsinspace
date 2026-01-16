package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.VerificationIntent
import com.github.elimxim.flashcardsinspace.entity.VerificationType
import com.github.elimxim.flashcardsinspace.entity.repository.VerificationIntentRepository
import com.github.elimxim.flashcardsinspace.service.UserService
import com.github.elimxim.flashcardsinspace.service.mail.EmailService
import com.github.elimxim.flashcardsinspace.service.mail.Recipient
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.util.*
import com.github.elimxim.flashcardsinspace.web.dto.VerificationCodeRequest
import com.github.elimxim.flashcardsinspace.web.dto.VerificationIntentRequest
import com.github.elimxim.flashcardsinspace.web.dto.VerificationIntentResponse
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpInternalServerErrorException
import com.github.elimxim.flashcardsinspace.web.exception.HttpUnauthorizedException
import com.github.elimxim.flashcardsinspace.web.exception.TooManyRequestsException
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

private val log = LoggerFactory.getLogger(VerificationCodeService::class.java)

enum class VerificationResult {
    SUCCESS,
    FOUND,
    NOT_FOUND,
    SESSION_EXPIRED,
    EXPIRED,
    INVALID,
    USED,
    LOCKED,
    LIMITED,
}

private const val MAX_ATTEMPTS = 3
private const val MAX_INTENTS_PER_WINDOW = 5
private val INTENT_LIMIT_WINDOW: Duration = Duration.ofHours(4)

@Service
class VerificationCodeService(
    private val verificationIntentRepository: VerificationIntentRepository,
    private val requestValidator: RequestValidator,
    private val emailService: EmailService,
    private val securityProperties: SecurityProperties,
    private val userService: UserService,
) {
    @Transactional
    fun send(
        user: User?,
        verificationToken: String?,
        request: VerificationIntentRequest?,
        response: HttpServletResponse
    ) {
        log.info(
            """
            Sending verification code, 
            user: ${user?.id},
            email: ${maskSecret(request?.email?.escapeJava())},
            type: ${request?.type?.escapeJava()},
            verification token: ${maskSecret(verificationToken?.escapeJava())}
            """.trimOneLine()
        )

        if (request != null) {
            val (email, type) = requestValidator.validate(request)
            val user = user ?: userService.getEntity(email)
            send(user, email, type, response)
        } else if (verificationToken == null) {
            if (user != null && !user.emailVerified) {
                send(user, user.email, VerificationType.REGISTRATION_REQUEST, response)
            } else {
                throw HttpUnauthorizedException(ApiErrorCode.AUF401, "Verification token is expired")
            }
        } else {
            val tokenHash = TokenHelper.hashToken(verificationToken)
            val intent = getEntity(tokenHash)
            send(intent.user, intent.email, intent.type, response)
        }
    }

    @Transactional
    fun send(user: User, email: String, type: VerificationType, response: HttpServletResponse) =
        withLoggingContext(user) {
            UserLock.withLock(user) {
                if (intentsPerWindowExceeded(email, type)) {
                    throw TooManyRequestsException(ApiErrorCode.MIW429, "Intents per window exceeded")
                }

                invalidateExistingIntents(email, type)

                val tokenProperties = securityProperties.getSecurityTokenProperties(type)
                val code = generateSecureCode(tokenProperties.length)
                val token = TokenHelper.generateRawToken()
                val tokenHash = TokenHelper.hashToken(token)
                val now = ZonedDateTime.now()
                val expiresAt = now.plus(tokenProperties.maxAge.toLong(), ChronoUnit.SECONDS)

                val verificationIntent = VerificationIntent(
                    email = email,
                    tokenHash = tokenHash,
                    code = code,
                    type = type,
                    createdAt = now,
                    expiresAt = expiresAt,
                    user = user,
                )

                verificationIntentRepository.save(verificationIntent)
                log.info("Verification intent created, email: ${maskSecret(email)}, type: $type")
                addVerificationTokenCookie(response, token, tokenProperties.maxAge)

                try {
                    emailService.sendVerificationIntentEmail(
                        recipient = Recipient(email, user.name),
                        code = code,
                        type = type,
                        maxAgeSeconds = tokenProperties.maxAge,
                    )
                } catch (e: Exception) {
                    log.error("Failed to email verification code to ${maskSecret(email)}", e)
                    throw HttpInternalServerErrorException(ApiErrorCode.ESF500, "Failed to email verification code")
                }

                verificationIntent
            }
        }

    @Transactional
    fun verify(
        verificationToken: String?,
        request: VerificationCodeRequest,
        response: HttpServletResponse
    ): VerificationIntentResponse {
        log.info("Verifying code, token: ${maskSecret(verificationToken?.escapeJava())}")

        val (code) = requestValidator.validate(request)

        val intent = when (val result = lookupIntent(verificationToken)) {
            is LookupResult.Success -> result.verificationIntent
            is LookupResult.Failure -> {
                if (result.verificationResult == VerificationResult.EXPIRED) {
                    clearVerificationTokenCookie(response)
                }

                return VerificationIntentResponse(result.verificationResult.name, result.type?.name)
            }
        }

        withLoggingContext(intent.user) {
            val secretEmail = Secret(intent.email)
            if (intent.code != code) {
                intent.attempts++
                intent.lastUpdatedAt = ZonedDateTime.now()
                verificationIntentRepository.save(intent)
                val attempts = intent.attempts
                log.info("Invalid code, email: $secretEmail, type: ${intent.type}, attempts remaining: $attempts")
                val reason = if (attempts >= MAX_ATTEMPTS) {
                    VerificationResult.LOCKED.name
                } else {
                    VerificationResult.INVALID.name
                }

                return VerificationIntentResponse(
                    result = reason,
                    type = intent.type.name,
                    attempts = intent.attempts
                )
            }

            markAsUsed(intent)
            log.info("Verification code is correct, email: $secretEmail, type: ${intent.type}")
            clearVerificationTokenCookie(response)
            onSuccessfulVerification(intent, response)
            return VerificationIntentResponse(VerificationResult.SUCCESS.name)
        }
    }

    @Transactional(readOnly = true)
    fun context(verificationToken: String?, response: HttpServletResponse): VerificationIntentResponse {
        log.info("Retrieving verification intent context, token: ${maskSecret(verificationToken?.escapeJava())}")
        return when (val result = lookupIntent(verificationToken)) {
            is LookupResult.Success -> {
                VerificationIntentResponse(
                    result = VerificationResult.FOUND.name,
                    type = result.verificationIntent.type.name,
                    attempts = result.verificationIntent.attempts
                )
            }

            is LookupResult.Failure -> {
                if (result.verificationResult == VerificationResult.EXPIRED) {
                    clearVerificationTokenCookie(response)
                }

                VerificationIntentResponse(result.verificationResult.name, result.type?.name)
            }
        }
    }

    @Transactional(readOnly = true)
    fun getEntity(tokenHash: String): VerificationIntent =
        verificationIntentRepository.findByTokenHash(tokenHash)
            ?: throw HttpUnauthorizedException(ApiErrorCode.VIN401, "Verification intent not found")

    @Transactional
    fun markAsUsed(intent: VerificationIntent) {
        val now = ZonedDateTime.now()
        intent.usedAt = now
        intent.lastUpdatedAt = now
        verificationIntentRepository.save(intent)
    }

    private fun intentsPerWindowExceeded(email: String, type: VerificationType): Boolean {
        val intentLimitWindow = ZonedDateTime.now().minus(INTENT_LIMIT_WINDOW)
        val intentCount = verificationIntentRepository.countByEmailAndTypeAndCreatedAtAfter(
            email, type, intentLimitWindow
        )
        return intentCount >= MAX_INTENTS_PER_WINDOW
    }

    sealed class LookupResult {
        data class Success(val verificationIntent: VerificationIntent) : LookupResult()
        data class Failure(
            val verificationResult: VerificationResult,
            val type: VerificationType? = null
        ) : LookupResult()
    }

    private fun lookupIntent(verificationToken: String?): LookupResult {
        if (verificationToken == null) {
            log.info("$VERIFICATION_TOKEN_COOKIE is not in cookies")
            return LookupResult.Failure(VerificationResult.SESSION_EXPIRED)
        }

        val tokenHash = TokenHelper.hashToken(verificationToken)
        val intent = verificationIntentRepository.findByTokenHash(tokenHash)
        if (intent == null) {
            log.info("Verification intent not found, token hash: $tokenHash")
            return LookupResult.Failure(VerificationResult.NOT_FOUND)
        }

        val secretEmail = Secret(intent.email)

        if (intent.usedAt != null) {
            log.info("Verification intent already used, token hash: $tokenHash")
            return LookupResult.Failure(VerificationResult.USED, intent.type)
        }

        if (intentsPerWindowExceeded(intent.email, intent.type)) {
            return LookupResult.Failure(VerificationResult.LIMITED, intent.type)
        }

        if (intent.expiresAt.isBefore(ZonedDateTime.now())) {
            log.info("Verification intent expired, email: $secretEmail, type: ${intent.type}")
            return LookupResult.Failure(VerificationResult.EXPIRED, intent.type)
        }

        if (intent.attempts >= MAX_ATTEMPTS) {
            log.info("Verification intent locked, email: $secretEmail, type: ${intent.type}")
            return LookupResult.Failure(VerificationResult.LOCKED, intent.type)
        }

        return LookupResult.Success(intent)
    }

    private fun onSuccessfulVerification(intent: VerificationIntent, response: HttpServletResponse) {
        if (intent.type == VerificationType.REGISTRATION_REQUEST) {
            log.info("Email ${intent.email} verified")
            userService.verify(intent.user)
        }

        if (intent.type == VerificationType.PASSWORD_RESET_REQUEST) {
            log.info("Password reset request verified")

            val tokenProperties = securityProperties.getSecurityTokenProperties(VerificationType.PASSWORD_RESET_ACCESS)
            val token = TokenHelper.generateRawToken()
            val tokenHash = TokenHelper.hashToken(token)
            val now = ZonedDateTime.now()
            val expiresAt = now.plus(tokenProperties.maxAge.toLong(), ChronoUnit.SECONDS)

            val accessIntent = VerificationIntent(
                email = intent.email,
                tokenHash = tokenHash,
                code = null,
                type = VerificationType.PASSWORD_RESET_ACCESS,
                createdAt = now,
                expiresAt = expiresAt,
                user = intent.user,
            )

            verificationIntentRepository.save(accessIntent)
            addPasswordResetTokenCookie(response, token, tokenProperties.maxAge)
            log.info("Password reset token generated")
        }
    }

    private fun invalidateExistingIntents(email: String, type: VerificationType) {
        val now = ZonedDateTime.now()
        val invalidatedCodes = verificationIntentRepository.findByEmailAndType(email, type)
            .filter { it.usedAt == null }
            .onEach { it.usedAt = now }

        if (invalidatedCodes.isNotEmpty()) {
            verificationIntentRepository.saveAll(invalidatedCodes)
            log.info("Invalidated ${invalidatedCodes.size} existing codes, type: $type")
        }
    }

}

