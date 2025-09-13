package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.security.escapeJava
import com.github.elimxim.flashcardsinspace.web.dto.LoginRequest
import com.github.elimxim.flashcardsinspace.web.dto.SignUpRequest
import com.github.elimxim.flashcardsinspace.web.dto.ValidLoginRequest
import com.github.elimxim.flashcardsinspace.web.dto.ValidSignUpRequest
import com.github.elimxim.flashcardsinspace.web.exception.InvalidRequestFieldsException
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator
import org.springframework.stereotype.Service

@Service
class RequestValidator(private val validator: Validator) {
    fun validate(request: LoginRequest): ValidLoginRequest {
        validateRequest<LoginRequest>(request) { violations ->
            throw InvalidRequestFieldsException(
                "Request is invalid ${request.escapeJava()}, violations: $violations",
                formatted(violations)
            )
        }

        return ValidLoginRequest(
            email = request.email!!,
            secret = request.secret!!
        )
    }

    fun validate(request: SignUpRequest): ValidSignUpRequest {
        validateRequest<SignUpRequest>(request) { violations ->
            throw InvalidRequestFieldsException(
                "Request is invalid ${request.escapeJava()}, violations: $violations",
                formatted(violations)
            )
        }

        return ValidSignUpRequest(
            email = request.email!!,
            name = request.name!!,
            secret = request.secret!!,
            languageId = request.languageId!!,
        )
    }

    private inline fun <reified T> validateRequest(request: T, ifInvalid: (List<Violation>) -> Unit) {
        val constraintViolations = validator.validate(request)
        if (constraintViolations.isNotEmpty()) {
            val violations = constraintViolations
                .map { it.toViolation() }
                .toSet()
                .sortedBy { it.field }

            ifInvalid(violations)
        }
    }
}

private data class Violation(
    val field: String?,
    val message: String?,
    val invalidValue: Any?,
)

private fun <T> ConstraintViolation<T>.toViolation(): Violation {
    return Violation(
        field = propertyPath.last().name,
        message = message,
        invalidValue = invalidValue
    )
}

private fun formatted(violations: List<Violation>) = violations
    .filter { !it.field.isNullOrBlank() }
    .joinToString(separator = "', '", prefix = "'", postfix = "'") {
        it.field.toString()
    }
