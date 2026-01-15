package com.github.elimxim.flashcardsinspace.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse

const val REFRESH_TOKEN_COOKIE = "refreshToken"
const val ACCESS_TOKEN_COOKIE = "accessToken"
const val VERIFICATION_TOKEN_COOKIE = "verificationToken"
const val PASSWORD_RESET_TOKEN_COOKIE = "passwordResetToken"

fun addRefreshTokenCookie(response: HttpServletResponse, token: String, maxAge: Int) =
    addSecureHttpOnlyCookie(response, REFRESH_TOKEN_COOKIE, value = token, path = "/auth/refresh", maxAge)

fun clearRefreshTokenCookie(response: HttpServletResponse) =
    clearSecureHttpOnlyCookie(response, REFRESH_TOKEN_COOKIE, path = "/auth/refresh")

fun addAccessTokenCookie(response: HttpServletResponse, token: String, maxAge: Int) =
    addSecureHttpOnlyCookie(response, ACCESS_TOKEN_COOKIE, value = token, path = "/", maxAge)

fun clearAccessTokenCookie(response: HttpServletResponse) =
    clearSecureHttpOnlyCookie(response, ACCESS_TOKEN_COOKIE, path = "/")

fun addVerificationTokenCookie(response: HttpServletResponse, token: String, maxAge: Int) =
    addSecureHttpOnlyCookie(response, VERIFICATION_TOKEN_COOKIE, value = token, path = "/", maxAge)

fun clearVerificationTokenCookie(response: HttpServletResponse) =
    clearSecureHttpOnlyCookie(response, VERIFICATION_TOKEN_COOKIE, path = "/")

fun addPasswordResetTokenCookie(response: HttpServletResponse, token: String, maxAge: Int) =
    addSecureHttpOnlyCookie(response, PASSWORD_RESET_TOKEN_COOKIE, value = token, path = "/auth/password-reset", maxAge)

fun clearPasswordResetTokenCookie(response: HttpServletResponse) =
    clearSecureHttpOnlyCookie(response, PASSWORD_RESET_TOKEN_COOKIE, path = "auth/password-reset")

fun addSecureHttpOnlyCookie(
    response: HttpServletResponse,
    name: String,
    value: String,
    path: String,
    maxAge: Int,
) {
    val cookie = Cookie(name, value).apply {
        this.isHttpOnly = true
        this.secure = true
        this.path = path
        this.maxAge = maxAge
    }

    response.addCookie(cookie)
}

fun clearSecureHttpOnlyCookie(
    response: HttpServletResponse,
    name: String,
    path: String,
) {
    val cookie = Cookie(name, null).apply {
        this.isHttpOnly = true
        this.secure = true
        this.path = path
        this.maxAge = 0
    }

    response.addCookie(cookie)
}
