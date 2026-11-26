package com.github.elimxim.flashcardsinspace.config

import org.springframework.validation.Errors
import org.springframework.validation.Validator

class CompositeValidator(private vararg val validators: Validator) : Validator {
    override fun supports(clazz: Class<*>): Boolean =
        validators.any { it.supports(clazz) }

    override fun validate(target: Any, errors: Errors) =
        validators.filter { it.supports(target.javaClass) }.forEach { it.validate(target, errors) }
}
