package com.github.elimxim.flashcardsinspace.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

private val secureRandom = SecureRandom()
private val base64Encoder = Base64.getUrlEncoder().withoutPadding()

object TokenHelper {
    fun generateRawToken(byteLength: Int = 32): String {
        val bytes = ByteArray(byteLength)
        secureRandom.nextBytes(bytes)
        return base64Encoder.encodeToString(bytes)
    }

    fun hashToken(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(rawToken.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}

fun generateSecureCode(length: Int): String {
    return (0 until length)
        .map { secureRandom.nextInt(10) }
        .joinToString("")
}
