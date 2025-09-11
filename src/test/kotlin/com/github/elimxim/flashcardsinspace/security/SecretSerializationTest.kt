package com.github.elimxim.flashcardsinspace.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private data class Container(
    val secret: Secret? = null,
    val password: Password? = null
)

class SecretSerializationTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `should serialize Secret`() {
        // given:
        val secret = Secret("1234")

        // when:
        val json = objectMapper.writeValueAsString(secret)

        // then:
        assertThat(json).isEqualTo("\"1234\"")
    }

    @Test
    fun `should deserialize Secret`() {
        // given:
        val json = "\"1234\""

        // when:
        val secret = objectMapper.readValue(json, Secret::class.java)

        // then:
        assertThat(secret.unmasked()).isEqualTo("1234")
    }

    @Test
    fun `should serialize empty Secret`() {
        // given:
        val secret = Secret("")

        // when:
        val json = objectMapper.writeValueAsString(secret)

        // then:
        assertThat(json).isEqualTo("\"\"")
    }

    @Test
    fun `should deserialize empty Secret`() {
        // given:
        val json = "\"\""

        // when:
        val secret = objectMapper.readValue(json, Secret::class.java)

        // then:
        assertThat(secret.unmasked()).isEmpty()
    }

    @Test
    fun `should serialize Password`() {
        // given:
        val password = Password("1234")

        // when:
        val json = objectMapper.writeValueAsString(password)

        // then:
        assertThat(json).isEqualTo("\"1234\"")
    }

    @Test
    fun `should deserialize Password`() {
        // given:
        val json = "\"1234\""

        // when:
        val password = objectMapper.readValue(json, Password::class.java)

        // then:
        assertThat(password.unmasked()).isEqualTo("1234")
    }

    @Test
    fun `should serialize empty Password`() {
        // given:
        val password = Password("")

        // when:
        val json = objectMapper.writeValueAsString(password)

        // then:
        assertThat(json).isEqualTo("\"\"")
    }

    @Test
    fun `should deserialize empty Password`() {
        // given:
        val json = "\"\""

        // when:
        val password = objectMapper.readValue(json, Password::class.java)

        // then:
        assertThat(password.unmasked()).isEmpty()
    }

    @Test
    fun `should serialize container with values`() {
        // given:
        val container = Container(
            secret = Secret("1234"),
            password = Password("1234")
        )

        // when:
        val json = objectMapper.writeValueAsString(container)

        // then:
        assertThat(json).isEqualTo("{\"secret\":\"1234\",\"password\":\"1234\"}")
    }

    @Test
    fun `should deserialize container with values`() {
        // given:
        val json = "{\"secret\":\"1234\",\"password\":\"1234\"}"

        // when:
        val container = objectMapper.readValue(json, Container::class.java)

        // then:
        assertThat(container.secret?.unmasked()).isEqualTo("1234")
        assertThat(container.password?.unmasked()).isEqualTo("1234")
    }

    @Test
    fun `should serialize container with null values`() {
        // given:
        val container = Container(secret = null, password = null)

        // when:
        val json = objectMapper.writeValueAsString(container)

        // then:
        assertThat(json).isEqualTo("{\"secret\":null,\"password\":null}")
    }

    @Test
    fun `should deserialize container with null values`() {
        // given:
        val json = "{\"secret\":null,\"password\":null}"

        // when:
        val container = objectMapper.readValue(json, Container::class.java)

        // then:
        assertThat(container.secret).isNull()
        assertThat(container.password).isNull()
    }

    @Test
    fun `should deserialize container with absent fields`() {
        // given:
        val json = "{}"

        // when:
        val container = objectMapper.readValue(json, Container::class.java)

        // then:
        assertThat(container.secret).isNull()
        assertThat(container.password).isNull()
    }
}
