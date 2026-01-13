package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.ConfirmationCode
import com.github.elimxim.flashcardsinspace.entity.ConfirmationPurpose
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.ConfirmationCodeRepository
import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import com.github.elimxim.flashcardsinspace.service.mail.EmailService
import com.github.elimxim.flashcardsinspace.service.mail.Recipient
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.util.*
import com.github.elimxim.flashcardsinspace.web.dto.ConfirmationCodeRequest
import com.github.elimxim.flashcardsinspace.web.dto.VerificationCodeRequest
import com.github.elimxim.flashcardsinspace.web.dto.VerificationCodeResponse
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
    CONTEXT,
    INVALID,
    NOT_FOUND,
    LOCKED,
    EXPIRED,
    LIMITED,
    USED,
}

private const val MAX_ATTEMPTS = 3
private const val MAX_CODES_PER_WINDOW = 10
private val CODE_LIMIT_WINDOW: Duration = Duration.ofHours(4)

@Service
class VerificationCodeService(
    private val confirmationCodeRepository: ConfirmationCodeRepository,
    private val userRepository: UserRepository,
    private val requestValidator: RequestValidator,
    private val emailService: EmailService,
    private val securityProperties: SecurityProperties,
) {

    @Transactional
    fun generateAndSend(user: User?, request: ConfirmationCodeRequest, response: HttpServletResponse) {
        log.info("Generating confirmation code, email: ${maskSecret(request.email?.escapeJava())}, purpose: ${request.purpose?.escapeJava()}")
        UserLock.withLock(user) {
            val validRequest = requestValidator.validate(request)
            val token = generateAndSend(user, validRequest.email, validRequest.purpose)
            addVerificationTokenCookie(response, token, securityProperties.verificationCodes.email.maxAge)
        }
    }

    @Transactional
    fun generateAndSend(user: User?, email: String, purpose: ConfirmationPurpose): String {
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

        emailService.sendConfirmationCodeEmail(
            recipient = Recipient(email, user?.name),
            code = code,
            purpose = purpose,
            maxAgeSeconds = maxAgeSeconds,
        )

        return token
    }

    @Transactional
    fun verify(
        user: User?,
        verificationToken: String?,
        request: VerificationCodeRequest,
        response: HttpServletResponse
    ): VerificationCodeResponse {
        log.info("Verifying confirmation code, token: ${maskSecret(verificationToken?.escapeJava())}")

        val (code) = requestValidator.validate(request)

        val confirmationCode = when (val result = lookupConfirmationCode(verificationToken)) {
            is LookupResult.Success -> result.confirmationCode
            is LookupResult.Failure -> {
                if (verificationToken != null && result.verificationResult == VerificationResult.EXPIRED) {
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

            VerificationCodeResponse(
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
        user?.let { userPostVerifyAction(it, secretEmail, confirmationCode.purpose) }
        return VerificationCodeResponse(VerificationResult.SUCCESS.name)
    }

    @Transactional
    fun context(verificationToken: String?, response: HttpServletResponse): VerificationCodeResponse {
        log.info("Retrieving confirmation code context, token: ${maskSecret(verificationToken?.escapeJava())}")
        return when (val result = lookupConfirmationCode(verificationToken)) {
            is LookupResult.Success -> {
                VerificationCodeResponse(
                    result = VerificationResult.CONTEXT.name,
                    purpose = result.confirmationCode.purpose.name,
                    attempts = result.confirmationCode.attempts
                )
            }
            is LookupResult.Failure -> {
                if (verificationToken != null && result.verificationResult == VerificationResult.EXPIRED) {
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
            return LookupResult.Failure(VerificationResult.EXPIRED)
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

    private fun userPostVerifyAction(user: User, secretEmail: Secret, purpose: ConfirmationPurpose) {
        if (purpose == ConfirmationPurpose.EMAIL_VERIFICATION) {
            user.emailVerified = true
            user.lastUpdatedAt = ZonedDateTime.now()
            log.info("Email $secretEmail verified")
            userRepository.save(user)
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

