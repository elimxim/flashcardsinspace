package com.github.elimxim.flashcardsinspace.security

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.userdetails.UserDetails
import java.util.Base64

class JwtServiceCookiesTest {
    private val userDetails = mockk<UserDetails> {
        every { username } returns "user@example.com"
    }

    @Test
    fun `should set secure http-only token cookies by default`() {
        val response = MockHttpServletResponse()

        jwtService(secureCookies = true).setCookies(userDetails, response)

        val accessToken = response.getCookie("accessToken")!!
        val refreshToken = response.getCookie("refreshToken")!!
        assertThat(accessToken.secure).isTrue()
        assertThat(accessToken.isHttpOnly).isTrue()
        assertThat(accessToken.path).isEqualTo("/")
        assertThat(accessToken.maxAge).isEqualTo(1)
        assertThat(refreshToken.secure).isTrue()
        assertThat(refreshToken.isHttpOnly).isTrue()
        assertThat(refreshToken.path).isEqualTo("/auth/refresh")
        assertThat(refreshToken.maxAge).isEqualTo(2)
    }

    @Test
    fun `should set token cookies without the secure flag when secure cookies are off`() {
        val response = MockHttpServletResponse()

        jwtService(secureCookies = false).setCookies(userDetails, response)

        assertThat(response.getCookie("accessToken")!!.secure).isFalse()
        assertThat(response.getCookie("refreshToken")!!.secure).isFalse()
    }

    @Test
    fun `should clear the token cookies with the same secure flag`() {
        val response = MockHttpServletResponse()

        jwtService(secureCookies = false).clearCookies(response)

        val cookies = response.cookies.associateBy { it.name }
        assertThat(cookies.keys).containsExactlyInAnyOrder("accessToken", "refreshToken")
        assertThat(cookies.values).allSatisfy {
            assertThat(it.maxAge).isZero()
            assertThat(it.secure).isFalse()
        }
    }

    private fun jwtService(secureCookies: Boolean) = JwtService(
        SecurityProperties(
            secureCookies = secureCookies,
            jwt = JwtProperties(
                secret = Base64.getEncoder().encodeToString(ByteArray(64) { it.toByte() }),
                accessTokenExpirationMs = 1000,
                refreshTokenExpirationMs = 2000,
            ),
            verificationTokens = VerificationTokenProperties(
                registrationRequest = SecurityTokenProperties(length = 4, maxAge = 60),
                passwordResetRequest = SecurityTokenProperties(length = 4, maxAge = 60),
                passwordResetAccess = SecurityTokenProperties(length = 4, maxAge = 60),
            ),
        )
    ).apply { init() }
}
