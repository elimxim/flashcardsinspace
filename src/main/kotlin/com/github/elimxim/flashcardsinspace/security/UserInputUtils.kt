package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.web.dto.LoginRequest
import com.github.elimxim.flashcardsinspace.web.dto.SignUpRequest
import org.apache.commons.text.StringEscapeUtils

fun String.escapeHtml(): String {
    return StringEscapeUtils.escapeHtml4(this)
}

fun String.escapeJava(): String {
    return StringEscapeUtils.escapeJava(this)
}

fun String.normalize(): String = this.trim()
fun Secret.normalize() = Secret(this.unmasked().normalize())
fun Secret.escapeJava() = Secret(this.unmasked().escapeJava())

fun SignUpRequest.normalize() = SignUpRequest(
    email = email.normalize().lowercase(),
    name = name.normalize(),
    secret = secret.normalize(),
    languageId = languageId
)

fun SignUpRequest.escapeJava() = SignUpRequest(
    email = email.escapeJava(),
    name = name.escapeJava(),
    secret = secret.escapeJava(),
    languageId = languageId,
)

fun LoginRequest.normalize() = LoginRequest(
    email = email?.normalize()?.lowercase(),
    secret = secret?.normalize()
)

fun LoginRequest.escapeJava() = LoginRequest(
    email = email?.escapeJava(),
    secret = secret?.escapeJava()
)
