package com.github.elimxim.flashcardsinspace.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse

const val REFRESH_TOKEN_COOKIE = "refreshToken"
const val ACCESS_TOKEN_COOKIE = "accessToken"
const val VERIFICATION_TOKEN_COOKIE = "verificationToken"
const val PASSWORD_RESET_TOKEN_COOKIE = "passwordResetToken"

fun addRefreshTokenCookie(response: HttpServletResponse, token: String, maxAge: Int, secure: Boolean) =
    addHttpOnlyCookie(
        response,
        REFRESH_TOKEN_COOKIE,
        value = token,
        path = "/auth/refresh",
        maxAge,
        secure
    )

fun clearRefreshTokenCookie(response: HttpServletResponse, secure: Boolean) =
    clearHttpOnlyCookie(
        response,
        REFRESH_TOKEN_COOKIE,
        path = "/auth/refresh",
        secure
    )

fun addAccessTokenCookie(response: HttpServletResponse, token: String, maxAge: Int, secure: Boolean) =
    addHttpOnlyCookie(
        response,
        ACCESS_TOKEN_COOKIE,
        value = token,
        path = "/",
        maxAge,
        secure
    )

fun clearAccessTokenCookie(response: HttpServletResponse, secure: Boolean) =
    clearHttpOnlyCookie(
        response,
        ACCESS_TOKEN_COOKIE,
        path = "/",
        secure
    )

fun addVerificationTokenCookie(response: HttpServletResponse, token: String, maxAge: Int, secure: Boolean) =
    addHttpOnlyCookie(
        response,
        VERIFICATION_TOKEN_COOKIE,
        value = token,
        path = "/",
        maxAge,
        secure
    )

fun clearVerificationTokenCookie(response: HttpServletResponse, secure: Boolean) =
    clearHttpOnlyCookie(
        response,
        VERIFICATION_TOKEN_COOKIE,
        path = "/",
        secure
    )

fun addPasswordResetTokenCookie(response: HttpServletResponse, token: String, maxAge: Int, secure: Boolean) =
    addHttpOnlyCookie(
        response,
        PASSWORD_RESET_TOKEN_COOKIE,
        value = token,
        path = "/auth/password-reset",
        maxAge,
        secure
    )

fun clearPasswordResetTokenCookie(response: HttpServletResponse, secure: Boolean) =
    clearHttpOnlyCookie(
        response,
        PASSWORD_RESET_TOKEN_COOKIE,
        path = "/auth/password-reset",
        secure
    )

/** [secure] comes from app.security.secure-cookies, which is false only in development */
fun addHttpOnlyCookie(
    response: HttpServletResponse,
    name: String,
    value: String,
    path: String,
    maxAge: Int,
    secure: Boolean,
) {
    val cookie = Cookie(name, value).apply {
        this.isHttpOnly = true
        this.secure = secure
        this.path = path
        this.maxAge = maxAge
    }

    response.addCookie(cookie)
}

fun clearHttpOnlyCookie(
    response: HttpServletResponse,
    name: String,
    path: String,
    secure: Boolean,
) {
    val cookie = Cookie(name, null).apply {
        this.isHttpOnly = true
        this.secure = secure
        this.path = path
        this.maxAge = 0
    }

    response.addCookie(cookie)
}
