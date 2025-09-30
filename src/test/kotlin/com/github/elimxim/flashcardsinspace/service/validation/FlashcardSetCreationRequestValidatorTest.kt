package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetCreationRequest
import com.github.elimxim.flashcardsinspace.web.exception.InvalidRequestFieldsException
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
        // given:
        val request = validRequest()

        // when:
        val validRequest = validator.validate(request)

        // then:
        assertThat(validRequest.name).isEqualTo("My German Flashcards")
        assertThat(validRequest.languageId).isEqualTo(2L)
    }

    @Test
    fun `should fail validation if name is null`() {
        // given:
        val request = validRequest().apply {
            name = null
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name is empty`() {
        // given:
        val request = validRequest().apply {
            name = ""
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name is blank`() {
        // given:
        val request = validRequest().apply {
            name = "  "
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name is too long`() {
        // given:
        val request = validRequest().apply {
            name = "a".repeat(65)
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if name contains invalid characters`() {
        // given:
        val request = validRequest().apply {
            name = "My Flashcards!"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("name")
    }

    @Test
    fun `should fail validation if languageId is null`() {
        // given:
        val request = validRequest().apply {
            languageId = null
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if languageId is empty`() {
        // given:
        val request = validRequest().apply {
            languageId = ""
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if languageId is blank`() {
        // given:
        val request = validRequest().apply {
            languageId = "  "
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if languageId is not a number`() {
        // given:
        val request = validRequest().apply {
            languageId = "abc"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("languageId")
    }

    @Test
    fun `should fail validation if all fields are invalid`() {
        // given:
        val request = FlashcardSetCreationRequest().apply {
            name = "a".repeat(65)
            languageId = "abc"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactlyInAnyOrder("name", "languageId")
    }

    private fun validRequest() = FlashcardSetCreationRequest(
        name = "My German Flashcards",
        languageId = "2"
    )
}
