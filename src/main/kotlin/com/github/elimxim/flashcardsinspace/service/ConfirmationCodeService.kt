package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.ConfirmationCode
import com.github.elimxim.flashcardsinspace.entity.ConfirmationPurpose
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.ConfirmationCodeRepository
import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import com.github.elimxim.flashcardsinspace.security.escapeJava
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
    INVALID,
    NOT_FOUND,
    LOCKED,
    EXPIRED,
    LIMITED,
}

private const val CODE_LENGTH = 2
private const val MAX_ATTEMPTS = 3
private const val MAX_CODES_PER_WINDOW = 4
private val CODE_LIMIT_WINDOW: Duration = Duration.ofMinutes(60)
private val CODE_EXPIRATION: Duration = Duration.ofMinutes(1) // fixme set 4 days

@Service
class ConfirmationCodeService(
    private val confirmationCodeRepository: ConfirmationCodeRepository,
    private val userRepository: UserRepository,
    private val requestValidator: RequestValidator,
) {

    @Transactional
    fun generateAndSend(user: User, request: SendConfirmationCodeRequest) {
        log.info("Generating confirmation code, purpose: ${request.purpose?.escapeJava()}")
        generateAndSend(user, requestValidator.validate(request))
    }

    @Transactional
    fun generateAndSend(user: User, request: ValidSendConfirmationCodeRequest) {
        invalidateExistingCodes(user, request.purpose)

        val code = generateSecureCode()
        val now = ZonedDateTime.now()

        val confirmationCode = ConfirmationCode(
            email = user.email,
            code = code,
            purpose = request.purpose,
            createdAt = now,
            expiresAt = now.plus(CODE_EXPIRATION),
            user = user,
        )

        confirmationCodeRepository.save(confirmationCode)
        log.info("Confirmation code was generated, purpose: ${request.purpose}")

        // todo send confirmation code email
    }

    @Transactional
    fun verify(user: User, request: VerifyConfirmationCodeRequest): VerifyConfirmationCodeResponse {
        log.info("Verifying confirmation code, purpose: ${request.purpose?.escapeJava()}")
        return verify(user, requestValidator.validate(request))
    }

    @Transactional
    fun verify(user: User, request: ValidVerifyConfirmationCodeRequest): VerifyConfirmationCodeResponse {
        val codeLimitWindow = ZonedDateTime.now().minus(CODE_LIMIT_WINDOW)

        val lastConfirmationCodes = confirmationCodeRepository
            .findByUserAndPurpose(user, request.purpose)
            .filter { it.createdAt.isAfter(codeLimitWindow) }

        if (lastConfirmationCodes.size >= MAX_CODES_PER_WINDOW) {
            log.info("Too many confirmation codes generated, purpose: ${request.purpose}")
            return VerifyConfirmationCodeResponse(VerificationResult.LIMITED.name)
        }

        val lastConfirmationCode = lastConfirmationCodes
            .filter { it.usedAt == null }
            .maxByOrNull { it.createdAt }

        if (lastConfirmationCode == null) {
            log.info("No valid confirmation code found, purpose: ${request.purpose}")
            return VerifyConfirmationCodeResponse(VerificationResult.NOT_FOUND.name)
        }

        if (lastConfirmationCode.expiresAt.isBefore(ZonedDateTime.now())) {
            log.info("Confirmation code expired, purpose: ${request.purpose}")
            return VerifyConfirmationCodeResponse(VerificationResult.EXPIRED.name)
        }

        if (lastConfirmationCode.attempts >= MAX_ATTEMPTS) {
            log.info("Confirmation code locked, purpose: ${request.purpose}")
            return VerifyConfirmationCodeResponse(VerificationResult.LOCKED.name, 0)
        }

        if (lastConfirmationCode.code != request.code) {
            lastConfirmationCode.attempts++
            lastConfirmationCode.lastUpdatedAt = ZonedDateTime.now()
            confirmationCodeRepository.save(lastConfirmationCode)
            val attemptsRemaining = MAX_ATTEMPTS - lastConfirmationCode.attempts
            log.info("Invalid code, purpose: ${request.purpose}, attempts remaining: $attemptsRemaining")
            return if (attemptsRemaining <= 0) {
                VerifyConfirmationCodeResponse(VerificationResult.LOCKED.name, 0)
            } else {
                VerifyConfirmationCodeResponse(VerificationResult.INVALID.name, attemptsRemaining)
            }
        }

        val now = ZonedDateTime.now()
        lastConfirmationCode.usedAt = now
        lastConfirmationCode.lastUpdatedAt = now
        confirmationCodeRepository.save(lastConfirmationCode)
        log.info("Confirmation code verified successfully, purpose: ${request.purpose}")
        verifyPostAction(user, request.purpose)
        return VerifyConfirmationCodeResponse(VerificationResult.SUCCESS.name)
    }

    private fun verifyPostAction(user: User, purpose: ConfirmationPurpose) {
        if (purpose == ConfirmationPurpose.EMAIL_VERIFICATION) {
            user.emailVerified = true
            log.info("Email verified")
            userRepository.save(user)
        }
    }

    private fun generateSecureCode(): String {
        val random = SecureRandom()
        return (0 until CODE_LENGTH)
            .map { random.nextInt(10) }
            .joinToString("")
    }

    private fun invalidateExistingCodes(user: User, purpose: ConfirmationPurpose) {
        val now = ZonedDateTime.now()
        val invalidatedCodes = confirmationCodeRepository.findByUserAndPurpose(user, purpose)
            .filter { it.usedAt == null }
            .onEach { it.usedAt = now }

        if (invalidatedCodes.isNotEmpty()) {
            confirmationCodeRepository.saveAll(invalidatedCodes)
            log.info("Invalidated ${invalidatedCodes.size} existing codes, purpose: $purpose")
        }
    }

}

