package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.ConfirmationCode
import com.github.elimxim.flashcardsinspace.entity.ConfirmationPurpose
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.ConfirmationCodeRepository
import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import com.github.elimxim.flashcardsinspace.service.mail.EmailService
import com.github.elimxim.flashcardsinspace.service.mail.Recipient
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Duration
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(ConfirmationCodeService::class.java)

enum class VerificationResult {
    SUCCESS,
    TESTED,
    INVALID,
    NOT_FOUND,
    LOCKED,
    EXPIRED,
    LIMITED,
}

private const val CODE_LENGTH = 2
private const val MAX_ATTEMPTS = 3
private const val MAX_CODES_PER_WINDOW = 10
private val CODE_LIMIT_WINDOW: Duration = Duration.ofMinutes(60)
private val CODE_EXPIRATION: Duration = Duration.ofMinutes(60 * 24)

@Service
class ConfirmationCodeService(
    private val confirmationCodeRepository: ConfirmationCodeRepository,
    private val userRepository: UserRepository,
    private val requestValidator: RequestValidator,
    private val emailService: EmailService,
) {

    @Transactional
    fun generateAndSend(user: User?, request: ConfirmationCodeRequest) {
        log.info("Generating confirmation code, email: ${maskSecret(request.email?.escapeJava())}, purpose: ${request.purpose?.escapeJava()}")
        val validRequest = requestValidator.validate(request)
        generateAndSend(user, validRequest.email, validRequest.purpose)
    }

    @Transactional
    fun generateAndSend(user: User?, email: String, purpose: ConfirmationPurpose) {
        invalidateExistingCodes(email, purpose)

        val code = generateSecureCode()
        val now = ZonedDateTime.now()

        val confirmationCode = ConfirmationCode(
            email = email,
            code = code,
            purpose = purpose,
            createdAt = now,
            expiresAt = now.plus(CODE_EXPIRATION),
            user = user,
        )

        confirmationCodeRepository.save(confirmationCode)
        log.info("Confirmation code was generated, email: ${maskSecret(email)}, purpose: $purpose")

        emailService.sendConfirmationCodeEmail(
            recipient = Recipient(email, user?.name),
            code = code,
            purpose = purpose,
        )
    }

    @Transactional
    fun verify(user: User?, request: ConfirmationCodeVerificationRequest): ConfirmationCodeResponse {
        log.info("Verifying confirmation code, email: ${maskSecret(request.email?.escapeJava())}, purpose: ${request.purpose?.escapeJava()}")
        return verify(user, requestValidator.validate(request))
    }

    @Transactional
    fun verify(user: User?, request: ValidConfirmationCodeVerificationRequest): ConfirmationCodeResponse {
        val secretEmail = Secret(request.email)
        val lastConfirmationCode = when (val result = getLastValidConfirmationCode(request.email, request.purpose)) {
            is LastValidConfirmationCodeResult.Found -> {
                result.code
            }

            is LastValidConfirmationCodeResult.NotFound -> {
                return ConfirmationCodeResponse(result.verificationResult.name)
            }
        }

        if (lastConfirmationCode.code != request.code) {
            lastConfirmationCode.attempts++
            lastConfirmationCode.lastUpdatedAt = ZonedDateTime.now()
            confirmationCodeRepository.save(lastConfirmationCode)
            val attempts = lastConfirmationCode.attempts
            log.info("Invalid code, email: $secretEmail, purpose: ${request.purpose}, attempts remaining: $attempts")
            return if (attempts >= MAX_ATTEMPTS) {
                ConfirmationCodeResponse(VerificationResult.LOCKED.name)
            } else {
                ConfirmationCodeResponse(VerificationResult.INVALID.name, attempts)
            }
        }

        val now = ZonedDateTime.now()
        lastConfirmationCode.usedAt = now
        lastConfirmationCode.lastUpdatedAt = now
        confirmationCodeRepository.save(lastConfirmationCode)
        log.info("Confirmation code verified successfully, email: $secretEmail, purpose: ${request.purpose}")
        user?.let { userPostVerifyAction(it, secretEmail, request.purpose) }
        return ConfirmationCodeResponse(VerificationResult.SUCCESS.name)
    }

    @Transactional
    fun test(user: User?, request: ConfirmationCodeTestRequest): ConfirmationCodeResponse {
        log.info("Testing confirmation code, email: ${maskSecret(request.email?.escapeJava())}, purpose: ${request.purpose?.escapeJava()}")
        return test(user, requestValidator.validate(request))
    }

    @Transactional
    fun test(user: User?, request: ValidConfirmationCodeTestRequest): ConfirmationCodeResponse {
        if (request.purpose === ConfirmationPurpose.EMAIL_VERIFICATION && user != null && user.emailVerified) {
            return ConfirmationCodeResponse(VerificationResult.SUCCESS.name)
        }

        return when (val result = getLastValidConfirmationCode(request.email, request.purpose)) {
            is LastValidConfirmationCodeResult.Found -> {
                log.info("Confirmation code tested successfully, email: ${maskSecret(request.email)}, purpose: ${request.purpose}, attempts: ${result.code.attempts}")
                ConfirmationCodeResponse(VerificationResult.TESTED.name, result.code.attempts)
            }

            is LastValidConfirmationCodeResult.NotFound -> {
                ConfirmationCodeResponse(result.verificationResult.name)
            }
        }
    }

    sealed class LastValidConfirmationCodeResult {
        data class Found(val code: ConfirmationCode) : LastValidConfirmationCodeResult()
        data class NotFound(val verificationResult: VerificationResult) : LastValidConfirmationCodeResult()
    }

    private fun getLastValidConfirmationCode(
        email: String,
        purpose: ConfirmationPurpose
    ): LastValidConfirmationCodeResult {
        val secretEmail = Secret(email)
        val codeLimitWindow = ZonedDateTime.now().minus(CODE_LIMIT_WINDOW)

        val lastConfirmationCodes = confirmationCodeRepository
            .findByEmailAndPurpose(email, purpose)
            .filter { it.createdAt.isAfter(codeLimitWindow) }

        if (lastConfirmationCodes.size >= MAX_CODES_PER_WINDOW) {
            log.info("Too many confirmation codes generated, email: $secretEmail, purpose: $purpose")
            return LastValidConfirmationCodeResult.NotFound(VerificationResult.LIMITED)
        }

        val lastConfirmationCode = lastConfirmationCodes
            .filter { it.usedAt == null }
            .maxByOrNull { it.createdAt }

        if (lastConfirmationCode == null) {
            log.info("No valid confirmation code found, email: $secretEmail, purpose: $purpose")
            return LastValidConfirmationCodeResult.NotFound(VerificationResult.NOT_FOUND)
        }

        if (lastConfirmationCode.expiresAt.isBefore(ZonedDateTime.now())) {
            log.info("Confirmation code expired, email: $secretEmail, purpose: $purpose")
            return LastValidConfirmationCodeResult.NotFound(VerificationResult.EXPIRED)
        }

        if (lastConfirmationCode.attempts >= MAX_ATTEMPTS) {
            log.info("Confirmation code locked, email: $secretEmail, purpose: $purpose")
            return LastValidConfirmationCodeResult.NotFound(VerificationResult.LOCKED)
        }

        return LastValidConfirmationCodeResult.Found(lastConfirmationCode)
    }

    private fun userPostVerifyAction(user: User, secretEmail: Secret, purpose: ConfirmationPurpose) {
        if (purpose == ConfirmationPurpose.EMAIL_VERIFICATION) {
            user.emailVerified = true
            user.lastUpdatedAt = ZonedDateTime.now()
            log.info("Email $secretEmail verified")
            userRepository.save(user)
        }
    }

    private fun generateSecureCode(): String {
        val random = SecureRandom()
        return (0 until CODE_LENGTH)
            .map { random.nextInt(10) }
            .joinToString("")
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

