package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.ConfirmationCode
import com.github.elimxim.flashcardsinspace.entity.ConfirmationPurpose
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.ConfirmationCodeRepository
import com.github.elimxim.flashcardsinspace.service.UserService
import com.github.elimxim.flashcardsinspace.service.mail.EmailService
import com.github.elimxim.flashcardsinspace.service.mail.Recipient
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.util.*
import com.github.elimxim.flashcardsinspace.web.dto.ConfirmationCodeRequest
import com.github.elimxim.flashcardsinspace.web.dto.VerificationCodeRequest
import com.github.elimxim.flashcardsinspace.web.dto.VerificationCodeResponse
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpInternalServerErrorException
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
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
private const val MAX_CODES_PER_WINDOW = 5
private val CODE_LIMIT_WINDOW: Duration = Duration.ofHours(4)

@Service
class VerificationCodeService(
    private val confirmationCodeRepository: ConfirmationCodeRepository,
    private val requestValidator: RequestValidator,
    private val emailService: EmailService,
    private val securityProperties: SecurityProperties,
    private val userService: UserService,
) {
    @Transactional
    fun send(
        user: User?,
        verificationToken: String?,
        request: ConfirmationCodeRequest?,
        response: HttpServletResponse
    ) {
        log.info(
            """
            Sending confirmation code, user: ${user?.id}
            , email: ${maskSecret(request?.email?.escapeJava())}
            , purpose: ${request?.purpose?.escapeJava()}
            , verification token: ${maskSecret(verificationToken?.escapeJava())}
            """.trimOneLine()
        )

        if (request != null) {
            val (email, purpose) = requestValidator.validate(request)
            val user = user ?: userService.getEntity(email)
            send(user, email, purpose, response)
        } else if (verificationToken == null) {
            if (user != null && !user.emailVerified) {
                send(user, user.email, ConfirmationPurpose.EMAIL_VERIFICATION, response)
            } else {
                throw HttpNotFoundException(ApiErrorCode.VTE404, "Verification token is expired")
            }
        } else {
            val tokenHash = TokenHelper.hashToken(verificationToken)
            val confirmationCode = confirmationCodeRepository.findByTokenHash(tokenHash)
                ?: throw HttpNotFoundException(ApiErrorCode.COC404, "Confirmation code not found")

            send(confirmationCode.user, confirmationCode.email, confirmationCode.purpose, response)
        }
    }

    @Transactional
    fun send(user: User, email: String, purpose: ConfirmationPurpose, response: HttpServletResponse) =
        UserLock.withLock(user) {
            invalidateExistingCodes(email, purpose)

            val codeLents = securityProperties.verificationCodes.email.length
            val maxAgeSeconds = securityProperties.verificationCodes.email.maxAge

            val code = generateSecureCode(codeLents)
            val token = TokenHelper.generateRawToken()
            val tokenHash = TokenHelper.hashToken(token)
            val now = ZonedDateTime.now()
            val expiresAt = now.plus(maxAgeSeconds.toLong(), ChronoUnit.SECONDS)

            val confirmationCode = ConfirmationCode(
                email = email,
                tokenHash = tokenHash,
                code = code,
                purpose = purpose,
                createdAt = now,
                expiresAt = expiresAt,
                user = user,
            )

            confirmationCodeRepository.save(confirmationCode)
            log.info("Confirmation code was generated, email: ${maskSecret(email)}, purpose: $purpose")
            addVerificationTokenCookie(response, token, securityProperties.verificationCodes.email.maxAge)

            try {
                emailService.sendConfirmationCodeEmail(
                    recipient = Recipient(email, user.name),
                    code = code,
                    purpose = purpose,
                    maxAgeSeconds = maxAgeSeconds,
                )
            } catch (e: Exception) {
                log.error("Failed to send confirmation code email to ${maskSecret(email)}", e)
                throw HttpInternalServerErrorException(ApiErrorCode.ESF500, "Failed to send confirmation code email")
                // fixme this exception is not handled well on UI
            }

            confirmationCode
        }

    @Transactional
    fun verify(
        verificationToken: String?,
        request: VerificationCodeRequest,
        response: HttpServletResponse
    ): VerificationCodeResponse {
        log.info("Verifying confirmation code, token: ${maskSecret(verificationToken?.escapeJava())}")

        val (code) = requestValidator.validate(request)

        val confirmationCode = when (val result = lookupConfirmationCode(verificationToken)) {
            is LookupResult.Success -> result.confirmationCode
            is LookupResult.Failure -> {
                if (result.verificationResult == VerificationResult.EXPIRED) {
                    clearVerificationTokenCookie(response)
                }

                return VerificationCodeResponse(result.verificationResult.name, result.purpose?.name)
            }
        }

        val secretEmail = Secret(confirmationCode.email)

        if (confirmationCode.code != code) {
            confirmationCode.attempts++
            confirmationCode.lastUpdatedAt = ZonedDateTime.now()
            confirmationCodeRepository.save(confirmationCode)
            val attempts = confirmationCode.attempts
            log.info("Invalid code, email: $secretEmail, purpose: ${confirmationCode.purpose}, attempts remaining: $attempts")
            val reason = if (attempts >= MAX_ATTEMPTS) {
                VerificationResult.LOCKED.name
            } else {
                VerificationResult.INVALID.name
            }

            return VerificationCodeResponse(
                result = reason,
                purpose = confirmationCode.purpose.name,
                attempts = confirmationCode.attempts
            )
        }

        val now = ZonedDateTime.now()
        confirmationCode.usedAt = now
        confirmationCode.lastUpdatedAt = now
        confirmationCodeRepository.save(confirmationCode)
        log.info("Confirmation code verified successfully, email: $secretEmail, purpose: ${confirmationCode.purpose}")
        clearVerificationTokenCookie(response)
        onSuccessfulVerification(confirmationCode.user, secretEmail, confirmationCode.purpose)
        return VerificationCodeResponse(VerificationResult.SUCCESS.name)
    }

    @Transactional(readOnly = true)
    fun context(verificationToken: String?, response: HttpServletResponse): VerificationCodeResponse {
        log.info("Retrieving confirmation code context, token: ${maskSecret(verificationToken?.escapeJava())}")
        return when (val result = lookupConfirmationCode(verificationToken)) {
            is LookupResult.Success -> {
                VerificationCodeResponse(
                    result = VerificationResult.FOUND.name,
                    purpose = result.confirmationCode.purpose.name,
                    attempts = result.confirmationCode.attempts
                )
            }

            is LookupResult.Failure -> {
                if (result.verificationResult == VerificationResult.EXPIRED) {
                    clearVerificationTokenCookie(response)
                }

                VerificationCodeResponse(result.verificationResult.name, result.purpose?.name)
            }
        }
    }

    private fun isMaxCodesPerWindowExceeded(confirmationCode: ConfirmationCode): Boolean {
        val codeLimitWindow = ZonedDateTime.now().minus(CODE_LIMIT_WINDOW)
        val codeCount = confirmationCodeRepository.countByEmailAndPurposeAndCreatedAtAfter(
            confirmationCode.email, confirmationCode.purpose, codeLimitWindow
        )
        return codeCount > MAX_CODES_PER_WINDOW
    }

    sealed class LookupResult {
        data class Success(val confirmationCode: ConfirmationCode) : LookupResult()
        data class Failure(
            val verificationResult: VerificationResult,
            val purpose: ConfirmationPurpose? = null
        ) : LookupResult()
    }

    private fun lookupConfirmationCode(verificationToken: String?): LookupResult {
        if (verificationToken == null) {
            log.info("$VERIFICATION_TOKEN_COOKIE is not in cookies")
            return LookupResult.Failure(VerificationResult.SESSION_EXPIRED)
        }

        val tokenHash = TokenHelper.hashToken(verificationToken)
        val confirmationCode = confirmationCodeRepository.findByTokenHash(tokenHash)
        if (confirmationCode == null) {
            log.info("Confirmation code not found, token hash: $tokenHash")
            return LookupResult.Failure(VerificationResult.NOT_FOUND)
        }

        val secretEmail = Secret(confirmationCode.email)

        if (confirmationCode.usedAt != null) {
            log.info("Confirmation code already used, token hash: $tokenHash")
            return LookupResult.Failure(VerificationResult.USED, confirmationCode.purpose)
        }

        if (isMaxCodesPerWindowExceeded(confirmationCode)) {
            return LookupResult.Failure(VerificationResult.LIMITED, confirmationCode.purpose)
        }

        if (confirmationCode.expiresAt.isBefore(ZonedDateTime.now())) {
            log.info("Confirmation code expired, email: $secretEmail, purpose: ${confirmationCode.purpose}")
            return LookupResult.Failure(VerificationResult.EXPIRED, confirmationCode.purpose)
        }

        if (confirmationCode.attempts >= MAX_ATTEMPTS) {
            log.info("Confirmation code locked, email: $secretEmail, purpose: ${confirmationCode.purpose}")
            return LookupResult.Failure(VerificationResult.LOCKED, confirmationCode.purpose)
        }

        return LookupResult.Success(confirmationCode)
    }

    private fun onSuccessfulVerification(user: User, email: Secret, purpose: ConfirmationPurpose) {
        if (purpose == ConfirmationPurpose.EMAIL_VERIFICATION) {
            log.info("Email $email verified")
            userService.verify(user)
        }
    }

    private fun invalidateExistingCodes(email: String, purpose: ConfirmationPurpose) {
        val now = ZonedDateTime.now()
        val invalidatedCodes = confirmationCodeRepository.findByEmailAndPurpose(email, purpose)
            .filter { it.usedAt == null }
            .onEach { it.usedAt = now }

        if (invalidatedCodes.isNotEmpty()) {
            confirmationCodeRepository.saveAll(invalidatedCodes)
            log.info("Invalidated ${invalidatedCodes.size} existing codes, purpose: $purpose")
        }
    }

}

