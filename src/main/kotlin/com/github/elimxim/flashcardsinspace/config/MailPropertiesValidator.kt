package com.github.elimxim.flashcardsinspace.config

import com.github.elimxim.flashcardsinspace.service.mail.MailProperties
import org.springframework.core.env.Environment
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class MailPropertiesValidator(private val environment: Environment) : Validator {
    override fun supports(clazz: Class<*>): Boolean =
        MailProperties::class.java.isAssignableFrom(clazz)

    override fun validate(target: Any, errors: Errors) {
        val properties = target as MailProperties
        if (environment.matchesProfiles("dev", "test")) return
        if (!properties.enabled) {
            errors.rejectValue(
                "enabled",
                "mail.disabledOutsideDev",
                "can be false only with the dev profile"
            )
        }
    }
}
