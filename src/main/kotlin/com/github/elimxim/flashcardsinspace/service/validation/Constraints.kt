package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.entity.ChronodayStatus
import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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
