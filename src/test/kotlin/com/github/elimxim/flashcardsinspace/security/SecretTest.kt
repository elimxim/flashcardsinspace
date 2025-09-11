package com.github.elimxim.flashcardsinspace.security

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class SecretTest {

    @Test
    fun `Secret should not change the original string`() {
        assertThat(Secret("1234").unmasked()).isEqualTo("1234")
    }

    @Test
    fun `Password should not change the original string`() {
        assertThat(Password("1234").unmasked()).isEqualTo("1234")
    }

    @Test
    fun `Secret should mask 60 percent of the string and should not expose more than 3 characters at the start and end`() {
        for (i in 0..64) {
            val secret = "0".repeat(i)
            val masked = Secret(secret).masked()
            
            println("iteration: $i, secret: $secret, masked: $masked")
            
            if (i == 0) {
                assertThat(masked).isEmpty()
            } else {
                assertThat(masked.count { it == '*' }).isEqualTo(3)
            }

            if (i < 5) {
                assertThat(masked.count { it != '*' }).isEqualTo(0)
            } else if (i < 10) {
                assertThat(masked.count { it != '*' }).isEqualTo(2)
                assertThat(masked.take(1)).isNotEqualTo("*")
                assertThat(masked.takeLast(1)).isNotEqualTo("*")
            } else if (i < 15) {
                assertThat(masked.count { it != '*' }).isEqualTo(4)
                assertThat(masked.take(2)).isNotEqualTo("*")
                assertThat(masked.takeLast(2)).isNotEqualTo("*")
            } else {
                assertThat(masked.count { it != '*' }).isEqualTo(6)
                assertThat(masked.take(3)).isNotEqualTo("*")
                assertThat(masked.takeLast(3)).isNotEqualTo("*")
            }
        }
    }

    @Test
    fun `Password should mask 100 percent of the string and should not expose any characters`() {
        for (i in 0..64) {
            val secret = "0".repeat(i)
            val masked = Password(secret).masked()

            println("iteration: $i, secret: $secret, masked: $masked")

            if (i == 0) {
                assertThat(masked).isEmpty()
            } else {
                assertThat(masked.count { it == '*' }).isEqualTo(3)
                assertThat(masked.length).isEqualTo(3)
            }
        }
    }
}
