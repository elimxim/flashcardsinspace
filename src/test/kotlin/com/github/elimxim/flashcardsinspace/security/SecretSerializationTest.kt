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
        val secret = Secret("1234")

        val json = objectMapper.writeValueAsString(secret)

        assertThat(json).isEqualTo("\"1234\"")
    }

    @Test
    fun `should deserialize Secret`() {
        val json = "\"1234\""

        val secret = objectMapper.readValue(json, Secret::class.java)

        assertThat(secret.unmasked()).isEqualTo("1234")
    }

    @Test
    fun `should serialize empty Secret`() {
        val secret = Secret("")

        val json = objectMapper.writeValueAsString(secret)

        assertThat(json).isEqualTo("\"\"")
    }

    @Test
    fun `should deserialize empty Secret`() {
        val json = "\"\""

        val secret = objectMapper.readValue(json, Secret::class.java)

        assertThat(secret.unmasked()).isEmpty()
    }

    @Test
    fun `should serialize Password`() {
        val password = Password("1234")

        val json = objectMapper.writeValueAsString(password)

        assertThat(json).isEqualTo("\"1234\"")
    }

    @Test
    fun `should deserialize Password`() {
        val json = "\"1234\""

        val password = objectMapper.readValue(json, Password::class.java)

        assertThat(password.unmasked()).isEqualTo("1234")
    }

    @Test
    fun `should serialize empty Password`() {
        val password = Password("")

        val json = objectMapper.writeValueAsString(password)

        assertThat(json).isEqualTo("\"\"")
    }

    @Test
    fun `should deserialize empty Password`() {
        val json = "\"\""

        val password = objectMapper.readValue(json, Password::class.java)

        assertThat(password.unmasked()).isEmpty()
    }

    @Test
    fun `should serialize container with values`() {
        val container = Container(
            secret = Secret("1234"),
            password = Password("1234")
        )

        val json = objectMapper.writeValueAsString(container)

        assertThat(json).isEqualTo("{\"secret\":\"1234\",\"password\":\"1234\"}")
    }

    @Test
    fun `should deserialize container with values`() {
        val json = "{\"secret\":\"1234\",\"password\":\"1234\"}"

        val container = objectMapper.readValue(json, Container::class.java)

        assertThat(container.secret?.unmasked()).isEqualTo("1234")
        assertThat(container.password?.unmasked()).isEqualTo("1234")
    }

    @Test
    fun `should serialize container with null values`() {
        val container = Container(secret = null, password = null)

        val json = objectMapper.writeValueAsString(container)

        assertThat(json).isEqualTo("{\"secret\":null,\"password\":null}")
    }

    @Test
    fun `should deserialize container with null values`() {
        val json = "{\"secret\":null,\"password\":null}"

        val container = objectMapper.readValue(json, Container::class.java)

        assertThat(container.secret).isNull()
        assertThat(container.password).isNull()
    }

    @Test
    fun `should deserialize container with absent fields`() {
        val json = "{}"

        val container = objectMapper.readValue(json, Container::class.java)

        assertThat(container.secret).isNull()
        assertThat(container.password).isNull()
    }
}
