package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetCreationRequest
import com.github.elimxim.flashcardsinspace.web.exception.HttpInvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class FlashcardSetCreationRequestValidatorTest {
    @Autowired
    private lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        val request = validRequest()

        val validRequest = validator.validate(request)

        assertThat(validRequest.name).isEqualTo("My German Flashcards")
        assertThat(validRequest.languageId).isEqualTo(2L)
    }

    @Test
    fun `should fail validation if name is null`() {
        val request = validRequest().apply {
            name = null
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name is empty`() {
        val request = validRequest().apply {
            name = ""
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name is blank`() {
        val request = validRequest().apply {
            name = "  "
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name is too long`() {
        val request = validRequest().apply {
            name = "a".repeat(65)
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name contains invalid characters`() {
        val request = validRequest().apply {
            name = "My Flashcards!"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if languageId is null`() {
        val request = validRequest().apply {
            languageId = null
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if languageId is empty`() {
        val request = validRequest().apply {
            languageId = ""
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if languageId is blank`() {
        val request = validRequest().apply {
            languageId = "  "
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if languageId is not a number`() {
        val request = validRequest().apply {
            languageId = "abc"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if all fields are invalid`() {
        val request = FlashcardSetCreationRequest().apply {
            name = "a".repeat(65)
            languageId = "abc"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactlyInAnyOrder("name", "languageId")
    }

    private fun validRequest() = FlashcardSetCreationRequest(
        name = "My German Flashcards",
        languageId = "2"
    )
}
