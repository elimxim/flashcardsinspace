package com.github.elimxim.flashcardsinspace.config

import com.github.elimxim.flashcardsinspace.security.SecurityProperties
import org.springframework.core.env.Environment
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class SecurityPropertiesValidator(private val environment: Environment) : Validator {
    override fun supports(clazz: Class<*>): Boolean =
        SecurityProperties::class.java.isAssignableFrom(clazz)

    override fun validate(target: Any, errors: Errors) {
        val properties = target as SecurityProperties
        if (environment.matchesProfiles("dev")) return
        if (!properties.enabled) {
            errors.rejectValue(
                "enabled",
                "security.disabledOutsideDev",
                "can be false only with the dev profile",
            )
        }
        if (!properties.secureCookies) {
            errors.rejectValue(
                "secureCookies",
                "security.insecureCookiesOutsideDev",
                "can be false only with the dev profile",
            )
        }
    }
}
