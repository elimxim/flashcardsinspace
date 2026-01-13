package com.github.elimxim.flashcardsinspace.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse

enum class Cookies(val cookieName: String) {
    REFRESH_TOKEN("refreshToken"),
    ACCESS_TOKEN("accessToken"),
    VERIFICATION_TOKEN("verificationToken"),
}

fun addRefreshTokenCookie(response: HttpServletResponse, token: String, maxAge: Int) =
    addSecureHttpOnlyCookie(response, Cookies.REFRESH_TOKEN.cookieName, value = token, path = "/auth/refresh", maxAge)

fun clearRefreshTokenCookie(response: HttpServletResponse) =
    clearSecureHttpOnlyCookie(response, Cookies.REFRESH_TOKEN.cookieName, path = "/auth/refresh")

fun addAccessTokenCookie(response: HttpServletResponse, token: String, maxAge: Int) =
    addSecureHttpOnlyCookie(response, Cookies.ACCESS_TOKEN.cookieName, value = token, path = "/", maxAge)

fun clearAccessTokenCookie(response: HttpServletResponse) =
    clearSecureHttpOnlyCookie(response, Cookies.ACCESS_TOKEN.cookieName, path = "/")

fun addVerificationTokenCookie(response: HttpServletResponse, token: String, maxAge: Int) =
    addSecureHttpOnlyCookie(response, Cookies.VERIFICATION_TOKEN.cookieName, value = token, path = "/", maxAge)

fun clearVerificationTokenCookie(response: HttpServletResponse) =
    clearSecureHttpOnlyCookie(response, Cookies.VERIFICATION_TOKEN.cookieName, path = "/")


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
