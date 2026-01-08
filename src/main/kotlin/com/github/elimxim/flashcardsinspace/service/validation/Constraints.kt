package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.entity.*
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.zone.ZoneRulesException
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [LocalDateFormatValidator::class])
annotation class IsoLocalDate(
    val message: String = "{jakarta.validation.constraints.localDate.invalid.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class LocalDateFormatValidator : ConstraintValidator<IsoLocalDate, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true
        try {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
            return true
        } catch (_: DateTimeParseException) {
            return false
        }
    }
}

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ZonedDateTimeFormatValidator::class])
annotation class IsoZonedDateTime(
    val message: String = "{jakarta.validation.constraints.zonedDateTime.invalid.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class ZonedDateTimeFormatValidator : ConstraintValidator<IsoZonedDateTime, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true
        try {
            ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME)
            return true
        } catch (_: DateTimeParseException) {
            return false
        }
    }
}

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [FlashcardStageValidator::class])
annotation class ValidFlashcardStage(
    val message: String = "{jakarta.validation.constraints.flashcard.stage.invalid.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class FlashcardStageValidator : ConstraintValidator<ValidFlashcardStage, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true
        try {
            FlashcardStage.valueOf(value)
            return true
        } catch (_: IllegalArgumentException) {
            return false
        }
    }
}

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [FlashcardSetStatusValidator::class])
annotation class ValidFlashcardSetStatus(
    val message: String = "{jakarta.validation.constraints.flashcardSet.status.invalid.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class FlashcardSetStatusValidator : ConstraintValidator<ValidFlashcardSetStatus, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true
        try {
            FlashcardSetStatus.valueOf(value)
            return true
        } catch (_: IllegalArgumentException) {
            return false
        }
    }
}

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ChronodayStatusValidator::class])
annotation class ValidChronodayStatus(
    val message: String = "{jakarta.validation.constraints.chronoday.status.invalid.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class ChronodayStatusValidator : ConstraintValidator<ValidChronodayStatus, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true
        try {
            ChronodayStatus.valueOf(value)
            return true
        } catch (_: IllegalArgumentException) {
            return false
        }
    }
}

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [TimezoneValidator::class])
annotation class ValidTimezone(
    val message: String = "{jakarta.validation.constraints.timezone.invalid.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class TimezoneValidator : ConstraintValidator<ValidTimezone, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) return true
        try {
            ZoneId.of(value)
            return true
        } catch (_: ZoneRulesException) {
            return false
        } catch (_: Exception) {
            // Catch all exceptions including DateTimeException, IllegalArgumentException, etc.
            return false
        }
    }
}

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ReviewSessionTypeValidator::class])
annotation class ValidReviewSessionType(
    val message: String = "{jakarta.validation.constraints.reviewSession.type.invalid.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class ReviewSessionTypeValidator : ConstraintValidator<ValidReviewSessionType, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true
        try {
            ReviewSessionType.valueOf(value)
            return true
        } catch (_: IllegalArgumentException) {
            return false
        }
    }
}

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [MetadataValidator::class])
annotation class ValidMetadata(
    val message: String = "{jakarta.validation.constraints.metadata.invalid.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class MetadataValidator : ConstraintValidator<ValidMetadata, Map<String, Any>> {
    private val keyPattern = Regex("^[A-Za-z0-9]+$")
    private val stringPattern = Regex("^[A-Za-z0-9 _\\[,\\]-]+$")

    override fun isValid(value: Map<String, Any>?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true
        return value.all { (key, v) -> keyPattern.matches(key) && isValidValue(v) }
    }

    private fun isValidValue(value: Any?): Boolean {
        return when (value) {
            null -> true
            is String -> stringPattern.matches(value)
            is Number -> true
            is Boolean -> true
            is List<*> -> value.all { isValidValue(it) }
            is Map<*, *> -> value.all { (k, v) ->
                k is String && keyPattern.matches(k) && isValidValue(v)
            }
            else -> false
        }
    }
}

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ConfirmationPurposeValidator::class])
annotation class ValidConfirmationPurpose(
    val message: String = "{jakarta.validation.constraints.confirmationPurpose.invalid.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class ConfirmationPurposeValidator : ConstraintValidator<ValidConfirmationPurpose, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true
        try {
            ConfirmationPurpose.valueOf(value)
            return true
        } catch (_: IllegalArgumentException) {
            return false
        }
    }
}
