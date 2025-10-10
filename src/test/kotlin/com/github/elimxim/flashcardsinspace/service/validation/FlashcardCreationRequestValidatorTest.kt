package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardCreationRequest
import com.github.elimxim.flashcardsinspace.web.exception.InvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
class FlashcardCreationRequestValidatorTest {
    @Autowired
    lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        // given:
        val request = validRequest()

        // when:
        val validRequest = validator.validate(request)

        // then:
        assertThat(validRequest.frontSide).isEqualTo("What is the largest black hole?")
        assertThat(validRequest.backSide).isEqualTo("TON\n618")
        assertThat(validRequest.stage).isEqualTo(FlashcardStage.S1)
        assertThat(validRequest.creationDate).isEqualTo(LocalDate.of(2025, 1, 1))
    }

    @Test
    fun `should fail validation if frontSide is null`() {
        // given:
        val request = validRequest().apply {
            frontSide = null
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("frontSide")
    }

    @Test
    fun `should fail validation if frontSide is empty`() {
        // given:
        val request = validRequest().apply {
            frontSide = ""
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("frontSide")
    }

    @Test
    fun `should fail validation if frontSide is blank`() {
        // given:
        val request = validRequest().apply {
            frontSide = "   "
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("frontSide")
    }

    @Test
    fun `should fail validation if frontSide is too long`() {
        // given:
        val request = validRequest().apply {
            frontSide = "a".repeat(513)
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("frontSide")
    }

    @Test
    fun `should fail validation if backSide is null`() {
        // given:
        val request = validRequest().apply {
            backSide = null
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("backSide")
    }

    @Test
    fun `should fail validation if backSide is empty`() {
        // given:
        val request = validRequest().apply {
            backSide = ""
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("backSide")
    }

    @Test
    fun `should fail validation if backSide is blank`() {
        // given:
        val request = validRequest().apply {
            backSide = "   "
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("backSide")
    }

    @Test
    fun `should fail validation if backSide is too long`() {
        // given:
        val request = validRequest().apply {
            backSide = "a".repeat(513)
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("backSide")
    }

    @Test
    fun `should fail validation if stage is null`() {
        // given:
        val request = validRequest().apply {
            stage = null
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if stage is empty`() {
        // given:
        val request = validRequest().apply {
            stage = ""
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if stage is blank`() {
        // given:
        val request = validRequest().apply {
            stage = "   "
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if stage is invalid`() {
        // given:
        val request = validRequest().apply {
            stage = "INVALID_STAGE"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if createdAt is null`() {
        // given:
        val request = validRequest().apply {
            creationDate = null
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("creationDate")
    }

    @Test
    fun `should fail validation if createdAt is empty`() {
        // given:
        val request = validRequest().apply {
            creationDate = ""
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("creationDate")
    }

    @Test
    fun `should fail validation if createdAt is blank`() {
        // given:
        val request = validRequest().apply {
            creationDate = "   "
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("creationDate")
    }

    @Test
    fun `should fail validation if createdAt has invalid format`() {
        // given:
        val request = validRequest().apply {
            creationDate = "2025/01/01"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("creationDate")
    }

    @Test
    fun `should fail validation if all fields are invalid`() {
        // given:
        val request = FlashcardCreationRequest().apply {
            frontSide = null
            backSide = ""
            stage = " "
            creationDate = "invalid-date"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactlyInAnyOrder("frontSide", "backSide", "stage", "creationDate")
    }

    private fun validRequest() = FlashcardCreationRequest(
        frontSide = "What is the largest black hole?",
        backSide = "TON\n618",
        stage = "S1",
        creationDate = "2025-01-01"
    )
}
