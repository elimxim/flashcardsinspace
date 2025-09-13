package com.github.elimxim.flashcardsinspace.security

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.properties.Delegates
import kotlin.reflect.KClass

interface Confidential {
    fun masked(): String
    fun unmasked(): String
}

@JsonSerialize(using = PasswordSerializer::class)
@JsonDeserialize(using = PasswordDeserializer::class)
class Password(private val secret: String) : Confidential by Secret(secret) {
    override fun masked(): String {
        return if (secret.isEmpty()) "" else "***"
    }
}

@JsonSerialize(using = SecretSerializer::class)
@JsonDeserialize(using = SecretDeserializer::class)
class Secret(private val secret: String) : Confidential {
    val maskPercentage: Int = 80

    override fun unmasked(): String {
        return secret
    }

    override fun masked(): String {
        return maskSecret(secret, percentage = 60)
    }

    override fun toString(): String {
        return masked()
    }

    override fun equals(other: Any?): Boolean {
        return secret == other
    }

    override fun hashCode(): Int {
        return secret.hashCode()
    }
}

fun maskSecret(
    secret: String,
    percentage: Int,
    dynamicMask: Boolean = false,
    staticMaskLength: Int = 3,
    maxVisibleSideLength: Int = 3,
): String {
    if (percentage !in 0..100) {
        throw IllegalArgumentException("Percentage must be between 0 and 100")
    } else if (secret.isEmpty()) {
        return secret
    }

    val visiblePercentage = (100 - percentage) / 100.0
    val visibleSideLength: Int = (secret.length * (visiblePercentage / 2.0)).toInt()
    val maskedLength = if (dynamicMask) {
        secret.length - (visibleSideLength * 2)
    } else {
        staticMaskLength
    }

    return buildString {
        append(secret.take(visibleSideLength.coerceAtMost(maxVisibleSideLength)))
        append("*".repeat(maskedLength))
        append(secret.takeLast(visibleSideLength.coerceAtMost(maxVisibleSideLength)))
    }
}

class SecretSerializer : JsonSerializer<Secret>() {
    override fun serialize(value: Secret?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(value?.unmasked())
    }
}

class SecretDeserializer : JsonDeserializer<Secret>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Secret? {
        return p?.text?.let { Secret(it) }
    }
}

class PasswordSerializer : JsonSerializer<Password>() {
    override fun serialize(value: Password?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(value?.unmasked())
    }
}

class PasswordDeserializer : JsonDeserializer<Password>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Password? {
        return p?.text?.let { Password(it) }
    }
}

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ConfidentialRequiredValidator::class])
annotation class RequiredConfidential(
    val message: String = "{jakarta.validation.constraints.confidential.isBlank.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class ConfidentialRequiredValidator : ConstraintValidator<RequiredConfidential, Confidential> {
    override fun isValid(value: Confidential?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true
        return value.unmasked().isNotBlank()
    }
}

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ConfidentialLengthValidator::class])
annotation class ConfidentialLength(
    val min: Int = 0,
    val max: Int = Int.MAX_VALUE,
    val message: String = "{jakarta.validation.constraints.confidential.lengthLimitExceeded}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class ConfidentialLengthValidator : ConstraintValidator<ConfidentialLength, Confidential> {
    var min by Delegates.notNull<Int>()
    var max by Delegates.notNull<Int>()

    override fun initialize(parameters: ConfidentialLength) {
        min = parameters.min
        max = parameters.max
    }

    override fun isValid(value: Confidential?, context: ConstraintValidatorContext?): Boolean {
        if (value == null || value.unmasked().isBlank()) return true
        return value.unmasked().length in min..max
    }
}

